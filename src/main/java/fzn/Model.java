package fzn;

import minicpbp.cp.Factory;
import minicpbp.engine.core.Constraint;
import minicpbp.engine.core.BoolVar;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.util.exception.NotImplementedException;


import java.util.*;
import java.util.stream.Collectors;

import fzn.parser.intermediatemodel.ASTConstraint;
import fzn.parser.intermediatemodel.ASTSolve;
import fzn.parser.intermediatemodel.ASTDecls.ASTFuncDecl;
import fzn.parser.intermediatemodel.ASTDecls.ASTParamDecl;
import fzn.parser.intermediatemodel.ASTDecls.ASTVarDecl;
import fzn.parser.intermediatemodel.ASTLiterals.ASTId;
import fzn.parser.intermediatemodel.ASTModel;
import fzn.parser.intermediatemodel.ASTTypes.ASTArrayType;
import fzn.parser.intermediatemodel.ASTTypes.ASTConstants;
import fzn.parser.intermediatemodel.ASTTypes.ASTVarType;
import fzn.parser.intermediatemodel.ASTDecls.ASTDecl;
import fzn.parser.intermediatemodel.ASTLiterals.ASTLit;
import fzn.parser.intermediatemodel.ASTLiterals.ASTRange;
import fzn.parser.intermediatemodel.ASTLiterals.*;

import static minicpbp.cp.Factory.*;


public class Model {

    private Solver solver;
    private ASTModel m;

    private final Map<String, ASTFuncDecl> funcDict = new HashMap<>();
	private final Map<String, ASTDecl> declDict = new HashMap<>();
	private final Map<String, IntVar> varDict = new HashMap<>();
	private final List<Constraint> listeConstraint = new LinkedList<>();
    private final ArrayList<IntVar> decisionsVar = new ArrayList<>();

    private int type;
    private IntVar objective;

    private boolean acceptAnyCstr;

    public Model(boolean acceptAnyCstr) {
        this.acceptAnyCstr = acceptAnyCstr;
    }

    public void addSolver(Solver solver) {
        this.solver = solver;
    }

    public void addASTModel(ASTModel m) {
        this.m = m;
    }

    public void buildModel() {
        for(ASTParamDecl p : m.getParamDecls())
            addParam(p);
        for(ASTVarDecl v : m.getVarDecls())
            addVar(v);
        for(ASTConstraint c : m.getConstraints())
            addConstraint(c);
        for(ASTFuncDecl f : m.getFuncDecls())
            addFunc(f);
            
        setObjective(m.getSolve());
    }

    private void setObjective(ASTSolve solve) {
        this.type = solve.getType();
        if(type != ASTSolve.SAT) {
            objective = getIntVar(solve.getExpr());
        }
        //annotations are not used, because search parameters such as heuristic or search-type
        //are given by the user with the SolveXCSPFZN interface
        //ArrayList<ASTLit> anns =  solve.getAnns();
    }

    public int getGoal() {
        return this.type;
    }

    public IntVar getObjective() {
        return this.objective;
    }

    private void addDecl(ASTDecl decl) {
        if(decl instanceof ASTParamDecl)
            addParam((ASTParamDecl) decl);
        else if(decl instanceof ASTVarDecl)
            addVar((ASTVarDecl) decl);
    }
    
    private void addParam(ASTParamDecl decl) {
        //List<ASTLit> anns = decl.getAnns();
        declDict.put(decl.getId().getValue(), decl);
    }
    private void addVar(ASTVarDecl decl) {
        //List<ASTLit> anns = decl.getAnns();
        if(decl.hasExpr() && decl.getExpr() instanceof ASTId) {
            ASTId alias = (ASTId) decl.getExpr();
            ASTDecl aliasDecl = declDict.get(alias.getValue());
            declDict.put(decl.getId().getValue(), aliasDecl);
            if(decl.getAnns() != null) {
                //TODO
            }
        }
        else {
            declDict.put(decl.getId().getValue(), decl);
            if(decl.getAnns() != null) {
                //TODO
            }
        }
    
        if(!(decl.getType() instanceof ASTArrayType)) {
            if(decl.getType().getDataType() == ASTConstants.INT) {
                IntVar varToAdd = getIntVar(decl.getId());
                varToAdd.setName(decl.getId().getValue());
                decisionsVar.add(varToAdd);
            }
            else if(decl.getType().getDataType() == ASTConstants.BOOL) {
                BoolVar varToAdd = getBoolVar(decl.getId());
                varToAdd.setName(decl.getId().getValue());
                decisionsVar.add(varToAdd);
            }
        }
    }
    
    private void addFunc(ASTFuncDecl decl) {
        funcDict.put(decl.getId().getValue(), decl);
    }
    
    private void addConstraint(ASTConstraint c) {
        //List<ASTLit> anns = c.getAnns();
        String name = c.getId().getValue();
        ArrayList<ASTLit> args = c.getArgs();
        //TODO handle annotations
        constructConstraint(name, args);
    } 

    private int getInt(ASTLit lit) {
        if(lit instanceof ASTInt) {
            int valeur = ((ASTInt) lit).getValue();
            return valeur;
        }
        else if (lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            if(declDict.get(id.getValue()).getId() != id) {
                return getInt(declDict.get(id.getValue()).getId());
            }
            return ((ASTInt) declDict.get(id.getValue()).getExpr()).getValue();
        }
        else throw new NotImplementedException(lit.toString());
    }

    private IntVar getIntVar(ASTLit lit) {
        if (lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            if(declDict.get(id.getValue()).getId() != id) {
                return getIntVar(declDict.get(id.getValue()).getId());
            }
            if (varDict.containsKey(id.getValue())) {
                return varDict.get(id.getValue());
            }
            return createSingleVarInt(declDict.get(id.getValue()));
        }
        else if(lit instanceof ASTInt) {
            int valeur = ((ASTInt) lit).getValue();
            return makeIntVar(solver, valeur, valeur);
        }
        else throw new NotImplementedException(lit.toString());
    }

    private int[] getIntArray(ASTLit lit) {
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return getIntArray(declDict.get(id.getValue()).getExpr());
        }
        else if(lit instanceof ASTArray) {
            ArrayList<ASTLit> astarray = ((ASTArray) lit).getElems();
            int array[] = new int[astarray.size()];
            for(int i = 0; i < astarray.size(); i++) {
                array[i] = getInt(astarray.get(i));
            }
            return array;
            //return (Integer[]) array.toArray();
        }
        throw new NotImplementedException(lit.toString());
    }

    private IntVar[] getIntVarArray(ASTLit lit) {
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return getIntVarArray(declDict.get(id.getValue()).getExpr());
        }
        else if(lit instanceof ASTArray) {
            ArrayList<ASTLit> astarray = ((ASTArray) lit).getElems();
            IntVar array[] = new IntVar[astarray.size()];
            for(int i = 0; i < astarray.size(); i++)
                array[i] = getIntVar(astarray.get(i));
            
            return array;
        }
        throw new NotImplementedException(lit.toString());
    }

    private boolean getBool(ASTLit lit) {
        if(lit instanceof ASTInt) {
            boolean valeur = ((ASTBool) lit).getValue();
            return valeur;
        }
        else if (lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            if(declDict.get(id.getValue()).getId() != id) {
                return getBool(declDict.get(id.getValue()).getId());
            }
            return ((ASTBool) declDict.get(id.getValue()).getExpr()).getValue();
        }
        else throw new NotImplementedException(lit.toString());
    }

    private BoolVar getBoolVar(ASTLit lit) {
        if(lit instanceof ASTBool) {
            ASTBool constant = (ASTBool) lit;
            boolean b = constant.getValue();
            BoolVar newVar =  makeBoolVar(solver);
            newVar.assign(b);
            return newVar;
        }
        else if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            if(declDict.get(id.getValue()).getId() != id) {
                return getBoolVar(declDict.get(id.getValue()).getId());
            }
            if (varDict.containsKey(id.getValue())) {
                return (BoolVar) varDict.get(id.getValue());
            }
            BoolVar newVar = makeBoolVar(solver);
            varDict.put(declDict.get(id.getValue()).getName(), newVar);
            return newVar;
        }
        throw new NotImplementedException(lit.toString());
    }

    private BoolVar[] getBoolVarArray(ASTLit lit) {
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return getBoolVarArray(declDict.get(id.getValue()).getExpr());
        }
        else if(lit instanceof ASTArray) {
            ArrayList<ASTLit> astarray = ((ASTArray) lit).getElems();
            BoolVar array[] = new BoolVar[astarray.size()];
            for(int i = 0; i < astarray.size(); i++)
                array[i] = getBoolVar(astarray.get(i));
            
            return array;
        }
        throw new NotImplementedException(lit.toString());  
    }

    private boolean[] getBoolArray(ASTLit lit) {
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return getBoolArray(declDict.get(id.getValue()).getExpr());
        }
        else if(lit instanceof ASTArray) {
            ArrayList<ASTLit> astarray = ((ASTArray) lit).getElems();
            boolean array[] = new boolean[astarray.size()];
            for(int i = 0; i < astarray.size(); i++) {
                array[i] = getBool(astarray.get(i));
            }
            return array;
        }
        throw new NotImplementedException(lit.toString());
    }

    private IntVar createSingleVarInt(ASTDecl v) {
        IntVar newVar;
        ASTVarType type = (ASTVarType) v.getType();
		if(type.getDom() instanceof ASTRange) {
			newVar =  Factory.makeIntVar(solver,
				((ASTRange) type.getDom()).getLb().getValue(),
				((ASTRange) type.getDom()).getUb().getValue());
		}
		else if(type.getDom() instanceof ASTSet){
			newVar = Factory.makeIntVar(solver,
				((ASTSet) type.getDom()).getSet().stream().map(e -> e.getValue()).collect(Collectors.toSet()));
		}
        else if(type.getDom() == null) {
            newVar = Factory.makeIntVar(solver, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
		else {
			System.out.println("Domaine type : " + type.getDom().getClass());
			throw new NotImplementedException();
		}
        newVar.setName(v.getName());
        varDict.put(v.getName(), newVar);
        return newVar;
	}

    private void constructConstraint(String name, ArrayList<ASTLit> args) {

        ConstraintBuilder builder = new ConstraintBuilder(solver);
        switch(name){
            case "all_different_int":
                builder.makeAllDifferentInt(getIntVarArray(args.get(0)));
                break;
            case "int_lin_ne":
                builder.makeIntLinNe(getIntArray(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "int_lin_ne_reif":
                builder.makeIntLinNeReif(getIntArray(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)), getBoolVar(args.get(3)));
                break;
            case "int_lin_eq" :
                builder.makeIntLinEq(getIntArray(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "int_lin_eq_reif":
                builder.makeIntLinEqReif(getIntArray(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)), getBoolVar(args.get(3)));
                break;
            case "int_lin_le":
                builder.makeIntLinNe(getIntArray(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "int_lin_le_reif":
                builder.makeIntLinLeReif(getIntArray(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)), getBoolVar(args.get(3)));
                break;
            case "int_eq":
                builder.makeIntEq(getIntVar(args.get(0)),getIntVar(args.get(1)));
                break;
            case "int_eq_reif":
                throw new NotImplementedException("Constraint : " +name);
            case "int_ne":
                builder.makeIntNe(getIntVar(args.get(0)), getIntVar(args.get(1)));
                break;
            case "int_ne_reif":
                throw new NotImplementedException("Constraint : " +name);   
            case "int_le":
                builder.makeIntLe(getIntVar(args.get(0)), getIntVar(args.get(1)));
                break;
            case "int_le_reif":
                throw new NotImplementedException("Constraint : " +name);
            case "int_lt":
                builder.makeIntLt(getIntVar(args.get(0)),getIntVar(args.get(1)));
                break;
            case "int_lt_reif":
                throw new NotImplementedException("Constraint : " +name);
            case "array_int_element":
                builder.makeArrayIntElement(getIntVar(args.get(0)), getIntArray(args.get(1)), getIntVar(args.get(2)));
                break;
            case "array_var_int_element":
                builder.makeArrayVarIntElement(getIntVar(args.get(0)), getIntVarArray(args.get(1)), getIntVar(args.get(2)));
                break;
            case "bool_eq":  
                builder.makeBoolEq(getBoolVar(args.get(0)),getBoolVar(args.get(1)));
                break;
            case "bool_lt":  
                throw new NotImplementedException("Constraint : " +name); 
            case "bool_le":  
                builder.makeBoolLe(getBoolVar(args.get(0)),getBoolVar(args.get(1)));
                break; 
            case "bool_not":  
                builder.makeBoolNot(getBoolVar(args.get(0)),getBoolVar(args.get(1)));
                break;
            case "bool_and":
                builder.makeBoolAnd(getBoolVar(args.get(0)),getBoolVar(args.get(1)), getBoolVar(args.get(2)));
                break;
            case "bool_or":
                builder.makeBoolOr(getBoolVar(args.get(0)),getBoolVar(args.get(1)), getBoolVar(args.get(2)));
                break;
            case "bool_xor":  
                throw new NotImplementedException("Constraint : " +name); 
            case "array_bool_or":  
                builder.makeArrayBoolOr(getBoolVarArray(args.get(0)), getBoolVar(args.get(1)));
                break;
            case "array_bool_xor":  
                throw new NotImplementedException("Constraint : " +name);
            case "array_bool_and":  
                builder.makeArrayBoolAnd(getBoolVarArray(args.get(0)), getBoolVar(args.get(1)));
                break;
            case "bool_clause":  
                builder.makeBoolClause(getBoolVarArray(args.get(0)), getBoolVarArray(args.get(1)));
                break;
            case "bool2int":  
                builder.makeBool2Int(getBoolVar(args.get(0)), getIntVar(args.get(1)));
                break;
            case "array_bool_element":  
                builder.makeArrayBoolElement(getIntVar(args.get(0)), getBoolArray(args.get(1)), getBoolVar(args.get(2)));
                break;
            case "array_var_bool_element":  
                builder.makeArrayVarBoolElement(getIntVar(args.get(0)), getBoolVarArray(args.get(1)), getBoolVar(args.get(2)));
                break;
            case "count":  
                throw new NotImplementedException("Constraint : " +name);
            case "exactly_int":  
                builder.makeExactlyInt(getInt(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "at_least_int":  
                builder.makeAtLeastInt(getInt(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "at_most_int":  
                builder.makeAtMostInt(getInt(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "count_eq":  
                throw new NotImplementedException("Constraint : " +name);
            case "int_max":  
                builder.makeIntMax(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2)));
                break;
            case "int_min":  
                builder.makeIntMin(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2)));
                break;
            case "int_pow":  
                throw new NotImplementedException("Constraint : " +name);
            case "int_times":  
                throw new NotImplementedException("Constraint : " +name);
            case "int_plus":  
                builder.makeIntPlus(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2)));
                break;
            case "int_div":  
                throw new NotImplementedException("Constraint : " +name);
            case "int_mod":  
                throw new NotImplementedException("Constraint : " +name);
            case "int_abs":  
                builder.makeIntAbs(getIntVar(args.get(0)), getIntVar(args.get(1)));
                break;
            case "set_in":  
                throw new NotImplementedException("Constraint : " +name);
            case "member_int":  
                throw new NotImplementedException("Constraint : " +name);
            case "maximum_int":  
                builder.makeMaximumInt(getIntVar(args.get(0)), getIntVarArray(args.get(1)));
                break;
            case "minimum_int":  
                builder.makeMinimumInt(getIntVar(args.get(0)), getIntVarArray(args.get(1)));
                break;
            case "inverse_no_offset":  
                throw new NotImplementedException("Constraint : " +name);
            case "subcircuit_no_offset":  
                throw new NotImplementedException("Constraint : " +name);
            case "circuit_no_offset":  
                builder.makeCircuit(getIntVarArray(args.get(0)));
                break;
            case "global_cardinality":  
                builder.makeGlobalCardinality(getIntVarArray(args.get(0)), getIntArray(args.get(1)), getIntVarArray(args.get(2)));
                break;
            case "global_cardinality_closed":  
                throw new NotImplementedException("Constraint : " +name);
            case "global_cardinality_low_up":  
                builder.makeGlobalCardinalityLowUp(getIntVarArray(args.get(0)), getIntArray(args.get(1)), getIntArray(args.get(2)), getIntArray(args.get(3)));
                break;
            case "global_cardinality_low_up_closed":  
                throw new NotImplementedException("Constraint : " +name);
            case "cumulative":  
                throw new NotImplementedException("Constraint : " +name);
            case "nvalue_int":  
                throw new NotImplementedException("Constraint : " +name);
            case "bin_packing_load":  
                throw new NotImplementedException("Constraint : " +name);
            case "table_int":  
                throw new NotImplementedException("Constraint : " +name);
            case "table_bool":  
                throw new NotImplementedException("Constraint : " +name);
            default:
                throw new NotImplementedException("Constraint : " + name);
        }
    }

    public IntVar[] getDecisionsVar() {
        IntVar vars[] = new IntVar[decisionsVar.size()];
        decisionsVar.forEach(x -> x.setForBranching(true));
        decisionsVar.toArray(vars);
        return vars;
    }

    public List<Constraint> getListeConstraint() {
        return listeConstraint;
    }
}
