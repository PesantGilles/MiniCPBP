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

    //the intermediate model build by the parser
    private ASTModel m;

    //contains all function declaration
    private final Map<String, ASTFuncDecl> funcDict = new HashMap<>();

    //contains all variables declarations
	private final Map<String, ASTDecl> declDict = new HashMap<>();

    //contains all the variables of the problem
	private final Map<String, IntVar> varDict = new HashMap<>();

    //contains all the variables of the problem
    private final ArrayList<IntVar> decisionsVar = new ArrayList<>();

    private int type;
    private IntVar objective;

    private final Map<String,ASTLit> outputVars = new HashMap<>(); 

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
        //load each parameter
        for(ASTParamDecl p : m.getParamDecls())
            addParam(p);
        //load each variables
        for(ASTVarDecl v : m.getVarDecls())
            addVar(v);
        //build and post each constraint
        for(ASTConstraint c : m.getConstraints())
            addConstraint(c);
        //load each function declared in the model, these functions are ignored by MiniCP-BP
        for(ASTFuncDecl f : m.getFuncDecls())
            addFunc(f);
        
        setObjective(m.getSolve());
    }

    private void setObjective(ASTSolve solve) {
        this.type = solve.getType();

        //load the cost funtion if the problem is a COP
        if(type != ASTSolve.SAT) {
            objective = getIntVar(solve.getExpr());
        }
        //annotations are not used, because search parameters such as heuristic or search-type
        //are given by the user with the SolveXCSPFZN interface
        //ArrayList<ASTLit> anns =  solve.getAnns();
    }

    /**
     * 
     * @return an integer indicating if the problem is a COP or a CSP
     */
    public int getGoal() {
        return this.type;
    }

    public IntVar getObjective() {
        return this.objective;
    }
    
    private void addParam(ASTParamDecl decl) {
        //List<ASTLit> anns = decl.getAnns();
        declDict.put(decl.getId().getValue(), decl);
    }

    /**
     * build a variable from its declaration
     * @param decl the declaration of the variable
     */
    private void addVar(ASTVarDecl decl) {
        List<ASTLit> anns = decl.getAnns();
        if(decl.hasExpr() && decl.getExpr() instanceof ASTId) {
            ASTId alias = (ASTId) decl.getExpr();
            ASTDecl aliasDecl = declDict.get(alias.getValue());
            declDict.put(decl.getId().getValue(), aliasDecl);
        }
        else
            declDict.put(decl.getId().getValue(), decl);

        for(ASTLit ann: anns) {
            if(ann instanceof ASTId) {
                if(((ASTId) ann).getValue().contains("output_var")) {
                    outputVars.put(decl.getId().getValue(), ann);
                }
            }
            else if(ann instanceof ASTAnnotation) {
                if(((ASTAnnotation) ann).getId().getValue().contains("output_array"))
                    outputVars.put(decl.getId().getValue(), ann);
            }
        }

        
        if(!(decl.getType() instanceof ASTArrayType)) {
            IntVar varToAdd;
            //if the variable is an integer 
            if(decl.getType().getDataType() == ASTConstants.INT) {
                varToAdd = getIntVar(decl.getId());
                varToAdd.setName(decl.getId().getValue());
                decisionsVar.add(varToAdd);
            }
            //if the variable is a boolean
            else if(decl.getType().getDataType() == ASTConstants.BOOL) {
                varToAdd = getBoolVar(decl.getId());
                varToAdd.setName(decl.getId().getValue());
                decisionsVar.add(varToAdd);
            }
            else {
                throw new NotImplementedException();
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

    /**
     * build and return an integer from a literal
     * @param lit the literal of the integer
     * @return the integer
     */
    private int getInt(ASTLit lit) {
        //if the literal is a value, this value is returned as an integer
        if(lit instanceof ASTInt) {
            int valeur = ((ASTInt) lit).getValue();
            return valeur;
        }
        //if the literal is an id linked to a declaration, the value in the declaration is returned
        else if (lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            if(declDict.get(id.getValue()).getId() != id) {
                return getInt(declDict.get(id.getValue()).getId());
            }
            return ((ASTInt) declDict.get(id.getValue()).getExpr()).getValue();
        }
        else throw new NotImplementedException(lit.toString());
    }

    /**
     * build an return an integer variable from a literal
     * @param lit the literal of the variable
     * @return the variable
     */
    private IntVar getIntVar(ASTLit lit) {
        //case where the literal is the variable's declaration
        if (lit instanceof ASTInt) {
            int valeur = ((ASTInt) lit).getValue();
            return makeIntVar(solver, valeur, valeur);
        }
        //case where the literal is an Id linked to a declaration
        else if (lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            if(declDict.get(id.getValue()).getId() != id) {
                return getIntVar(declDict.get(id.getValue()).getId());
            }
            if (varDict.containsKey(id.getValue())) {
                return varDict.get(id.getValue());
            }
            return createSingleVarInt(declDict.get(id.getValue()));
        }
        else throw new NotImplementedException(lit.toString());
    }

    /**
     * build and return an array of integer from a literal
     * @param lit the literal
     * @return the array of integer
     */
    private int[] getIntArray(ASTLit lit) {
        //case where the literal is an Id linked to a declaration
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return getIntArray(declDict.get(id.getValue()).getExpr());
        }
        //case where the literal is the array's declaration
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

    private Set<Integer> getSetInt(ASTLit lit) {
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return getSetInt(declDict.get(id.getValue()).getExpr()); 
        }
        Set<Integer> set;
        if(lit instanceof ASTRange) {
            ASTRange range = (ASTRange) lit;
            set = new HashSet<>();
            for(int i = range.getLb().getValue(); i <= range.getUb().getValue(); i++)
                set.add(i);
        }
        else if(lit instanceof ASTSet)
            set = ((ASTSet)lit).getSet().stream().map(e -> e.getValue()).collect(Collectors.toSet());
        else 
            throw new NotImplementedException(lit.toString());
        return set;
    }

    /**
     * build and return a 2D array of integer from a literal
     * @param lit the literal
     * @return the array of integer
     */
    /*private int[][] get2DIntArray(ASTLit lit) {
        //case where the literal is an Id linked to a declaration
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return get2DIntArray(declDict.get(id.getValue()).getExpr());
        }
        //case where the literal is the array's declaration
        else if(lit instanceof ASTArray) {
            System.out.println((ASTArray) lit);
            ArrayList<ASTLit> astarray = ((ASTArray) lit).getElems();
            int array[] = new int[astarray.size()];
            for(int i = 0; i < astarray.size(); i++) {
                array[i] = getInt(astarray.get(i));
            }
            return null;
            //return (Integer[]) array.toArray();
        }
        throw new NotImplementedException(lit.toString());
    }*/

    /**
     * build and return an array of integer variable from a literal
     * @param lit the literal
     * @return the array of variable
     */
    private IntVar[] getIntVarArray(ASTLit lit) {
        //case where the literal is an Id linked to a declaration
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return getIntVarArray(declDict.get(id.getValue()).getExpr());
        }
        //case where the literal is the array's declaration
        else if(lit instanceof ASTArray) {
            ArrayList<ASTLit> astarray = ((ASTArray) lit).getElems();
            IntVar array[] = new IntVar[astarray.size()];
            for(int i = 0; i < astarray.size(); i++)
                array[i] = getIntVar(astarray.get(i));
            
            return array;
        }
        throw new NotImplementedException(lit.toString());
    }

    /**
     * build an return a boolean from a literal
     * @param lit the literal of the boolean
     * @return the boolean
     */
    private boolean getBool(ASTLit lit) {
        // case where the literal is the value of the boolean
        if(lit instanceof ASTInt) {
            boolean valeur = ((ASTBool) lit).getValue();
            return valeur;
        }
        //case where the literal is an Id linked to a declaration
        else if (lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            if(declDict.get(id.getValue()).getId() != id) {
                return getBool(declDict.get(id.getValue()).getId());
            }
            return ((ASTBool) declDict.get(id.getValue()).getExpr()).getValue();
        }
        else throw new NotImplementedException(lit.toString());
    }

    /**
     * build an return a boolean variable from a literal
     * @param lit the literal of the variable
     * @return the variable
     */
    private BoolVar getBoolVar(ASTLit lit) {
        //case where the literal is the variable's declaration
        if(lit instanceof ASTBool) {
            ASTBool constant = (ASTBool) lit;
            boolean b = constant.getValue();
            BoolVar newVar =  makeBoolVar(solver);
            newVar.assign(b);
            return newVar;
        }
        //case where the literal is an Id linked to a declaration
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

    /**
     * build and return an array of boolean variable from a literal
     * @param lit the literal
     * @return the array of variable
     */
    private BoolVar[] getBoolVarArray(ASTLit lit) {
        //case where the literal is an Id linked to a declaration
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return getBoolVarArray(declDict.get(id.getValue()).getExpr());
        }
        //case where the literal is the array's declaration
        else if(lit instanceof ASTArray) {
            ArrayList<ASTLit> astarray = ((ASTArray) lit).getElems();
            BoolVar array[] = new BoolVar[astarray.size()];
            for(int i = 0; i < astarray.size(); i++)
                array[i] = getBoolVar(astarray.get(i));
            
            return array;
        }
        throw new NotImplementedException(lit.toString());  
    }

    /**
     * build and return an array of integer from a literal
     * @param lit the literal
     * @return the array of integer
     */
    private boolean[] getBoolArray(ASTLit lit) {
        //case where the literal is an Id linked to a declaration
        if(lit instanceof ASTId) {
            ASTId id = (ASTId) lit;
            return getBoolArray(declDict.get(id.getValue()).getExpr());
        }
        //case where the literal is the array's declaration
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

    /**
     * Creates an IntVar from a declaration
     * @param v the declaration
     * @return the variable
     */
    private IntVar createSingleVarInt(ASTDecl v) {
        IntVar newVar;
        ASTVarType type = (ASTVarType) v.getType();
        //case where the domain of the variable is given as an interval
		if(type.getDom() instanceof ASTRange) {
			newVar =  Factory.makeIntVar(solver,
				((ASTRange) type.getDom()).getLb().getValue(),
				((ASTRange) type.getDom()).getUb().getValue());
		}
        //case where the domain of the variable is given as a set
		else if(type.getDom() instanceof ASTSet){
			newVar = Factory.makeIntVar(solver,
				((ASTSet) type.getDom()).getSet().stream().map(e -> e.getValue()).collect(Collectors.toSet()));
		}
        //case where there is no given domain
        else if(type.getDom() == null) {
            //Integer.MIN_VALUE and Integer.MAX_VALUE is divided by 3,
            //otherwise the solver tries to create an array of size : 2*Integer*MAX_VALUE wich is impossible
            newVar = Factory.makeIntVar(solver, Integer.MIN_VALUE/3, Integer.MAX_VALUE/3);
            System.out.println(newVar);
        }
		else {
			System.out.println("Domaine type : " + type.getDom().getClass());
			throw new NotImplementedException();
		}
        newVar.setName(v.getName());
        varDict.put(v.getName(), newVar);
        return newVar;
	}

    /**
     * build and post a given constraint
     * @param name the id of the constraint
     * @param args the variables subjects to the constraint
     */
    private void constructConstraint(String name, ArrayList<ASTLit> args) {

        constraintBuilder builder = new constraintBuilder(solver);
        switch(name){
            case "fzn_among":
                builder.makeAmong(getIntVarArray(args.get(1)), getIntArray(args.get(2)), getIntVar(args.get(0)));
                break;
            case "fzn_all_different_int":
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
                builder.makeIntEqReif(getIntVar(args.get(0)), getIntVar(args.get(1)), getBoolVar(args.get(2)));
                break;
            case "int_ne":
                builder.makeIntNe(getIntVar(args.get(0)), getIntVar(args.get(1)));
                break;
            case "int_ne_reif":
                builder.makeIntNeReif(getIntVar(args.get(0)), getIntVar(args.get(1)), getBoolVar(args.get(2)));
                break;  
            case "int_le":
                builder.makeIntLe(getIntVar(args.get(0)), getIntVar(args.get(1)));
                break;
            case "int_le_reif":
                builder.makeIntLeReif(getIntVar(args.get(0)), getIntVar(args.get(1)), getBoolVar(args.get(2)));
                break;  
            case "int_lt":
                builder.makeIntLt(getIntVar(args.get(0)),getIntVar(args.get(1)));
                break;
            case "int_lt_reif":
                builder.makeIntLtReif(getIntVar(args.get(0)), getIntVar(args.get(1)), getBoolVar(args.get(2)));
                break; 
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
                builder.makeBoolLt(getBoolVar(args.get(0)),getBoolVar(args.get(1)));
                break;
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
                if(args.size() == 2)
                    builder.makeArrayBoolXor(getBoolVar(args.get(0)), getBoolVar(args.get(1)));
                else 
                    builder.makeArrayBoolXorReif(getBoolVar(args.get(2)), getBoolVar(args.get(0)), getBoolVar(args.get(1)));
                break; 
            case "array_bool_or":  
                builder.makeArrayBoolOr(getBoolVarArray(args.get(0)), getBoolVar(args.get(1)));
                break;
            case "array_bool_xor":  
                builder.makeArrayBoolXor(getBoolVarArray(args.get(0)));
                break;
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
            case "fzn_exactly_int":  
                builder.makeExactlyInt(getInt(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "fzn_at_least_int":  
                builder.makeAtLeastInt(getInt(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "fzn_at_most_int":  
                builder.makeAtMostInt(getInt(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "int_max":  
                builder.makeIntMax(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2)));
                break;
            case "int_min":  
                builder.makeIntMin(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2)));
                break;
            case "int_pow": 
                builder.makeIntPow(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2))); 
                break;
            case "int_times":  
                builder.makeIntTimes(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2)));
                break;
            case "int_plus":  
                builder.makeIntPlus(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2)));
                break;
            case "int_div":  
                builder.makeIntDiv(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2)));
                break;
            case "int_mod":  
                builder.makeIntMod(getIntVar(args.get(0)), getIntVar(args.get(1)), getIntVar(args.get(2)));
                break;
            case "int_abs":  
                builder.makeIntAbs(getIntVar(args.get(0)), getIntVar(args.get(1)));
                break;
            case "maximum_int":  
                builder.makeMaximumInt(getIntVar(args.get(0)), getIntVarArray(args.get(1)));
                break;
            case "minimum_int":  
                builder.makeMinimumInt(getIntVar(args.get(0)), getIntVarArray(args.get(1)));
                break;
            case "fzn_circuit_with_offset":
                builder.makeCircuit(getIntVarArray(args.get(0)), getInt(args.get(1)));
                break;
            case "fzn_global_cardinality":  
                builder.makeGlobalCardinality(getIntVarArray(args.get(0)), getIntArray(args.get(1)), getIntVarArray(args.get(2)));
                break;
            case "fzn_global_cardinality_low_up":  
                builder.makeGlobalCardinalityLowUp(getIntVarArray(args.get(0)), getIntArray(args.get(1)), getIntArray(args.get(2)), getIntArray(args.get(3)));
                break;
            case "set_in":
                builder.makeSetIn(getIntVar(args.get(0)), getSetInt(args.get(1)));
                break;
            case "fzn_table_int":
                builder.makeTable(getIntVarArray(args.get(0)), getIntArray(args.get(1)));
                break;
            case "fzn_inverse_with_offset":
                builder.makeInverse(getIntVarArray(args.get(0)), getIntVarArray(args.get(1)), getInt(args.get(2)));
                break;
            case "fzn_lex_less_int":
                builder.makeLexLess(getIntVarArray(args.get(0)), getIntVarArray(args.get(1)));
                break;
            case "fzn_lex_lesseq_int":
                builder.makeLexLesseq(getIntVarArray(args.get(0)), getIntVarArray(args.get(1)));
                break;
            default:
                throw new NotImplementedException("Constraint : " + name);
        }
    }

    /**
     * 
     * @return the variables of the problem
     */
    public IntVar[] getDecisionsVar() {
        IntVar vars[] = new IntVar[decisionsVar.size()];
        decisionsVar.forEach(x -> x.setForBranching(true));
        decisionsVar.toArray(vars);
        return vars;
    }

    public String getSolutionOutput() {
        StringBuilder sol = new StringBuilder("");
        for(Map.Entry<String, ASTLit> entry : outputVars.entrySet()) {
            String var = entry.getKey();
            ASTLit ann = entry.getValue();
            if(ann instanceof ASTId) {
                IntVar x = varDict.get(var);
                sol.append(x.getName() + " = " + x.min()+";\n");
            }
            else {
                ASTAnnotation annot = (ASTAnnotation) ann;
                ASTArray dimensions = (ASTArray) annot.getArgs().get(0);
                sol.append(var);
                sol.append(" = array");
                sol.append(dimensions.getElems().size());
                sol.append("d(");
                for(ASTLit elem : dimensions.getElems())
                    sol.append(elem.toString() + ", ");
                sol.append("[");
                IntVar array[] = getIntVarArray(declDict.get(var).getExpr());
                for(int i = 0; i < array.length-1; i++) 
                    sol.append(array[i].min() + ", ");
                    //System.out.println(array[i].getName());
                sol.append(array[array.length-1].min() + "]);\n");
            }
        }
        sol.append("----------\n");
        return sol.toString();
    }
}
