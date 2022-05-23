/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 *
 * mini-cpbp, replacing classic propagation by belief propagation
 * Copyright (c)  2019. by Gilles Pesant
 */

package minicpbp.cp;

import minicpbp.engine.constraints.*;
import minicpbp.engine.core.*;
import minicpbp.search.DFSearch;
import minicpbp.search.LDSearch;
import minicpbp.search.Objective;
import minicpbp.state.Copier;
import minicpbp.state.StateStack;
import minicpbp.state.Trailer;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.Procedure;

import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Factory to create {@link Solver}, {@link IntVar}, {@link Constraint}
 * and some modeling utility methods.
 * Example for the n-queens problem:
 * <pre>
 * {@code
 *  Solver cp = Factory.makeSolver(false);
 *  IntVar[] q = Factory.makeIntVarArray(cp, n, n);
 *  for (int i = 0; i < n; i++)
 *    for (int j = i + 1; j < n; j++) {
 *      cp.post(Factory.notEqual(q[i], q[j]));
 *      cp.post(Factory.notEqual(q[i], q[j], j - i));
 *      cp.post(Factory.notEqual(q[i], q[j], i - j));
 *    }
 *  search.onSolution(() ->
 *    System.out.println("solution:" + Arrays.toString(q))
 *  );
 *  DFSearch search = Factory.makeDfs(cp,firstFail(q));
 *  SearchStatistics stats = search.solve();
 * }
 * </pre>
 */
public final class Factory {

    private Factory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a constraint programming solver
     *
     * @return a constraint programming solver with trail-based memory management
     */
    public static Solver makeSolver() {
        return new MiniCP(new Trailer());
    }

    /**
     * Creates a constraint programming solver
     *
     * @param seed the random number generator seed
     * @return a constraint programming solver with trail-based memory management
     */
    public static Solver makeSolver(long seed) {
        return new MiniCP(new Trailer(), seed);
    }

    /**
     * Creates a constraint programming solver
     *
     * @param byCopy a value that should be true to specify
     *               copy-based state management
     *               or false for a trail-based memory management
     * @return a constraint programming solver
     */
    public static Solver makeSolver(boolean byCopy) {
        return new MiniCP(byCopy ? new Copier() : new Trailer());
    }

    /**
     * Creates a constraint programming solver
     *
     * @param byCopy a value that should be true to specify
     *               copy-based state management
     *               or false for a trail-based memory management
     * @param seed the random number generator seed
     * @return a constraint programming solver
     */
    public static Solver makeSolver(boolean byCopy, long seed) {
        return new MiniCP(byCopy ? new Copier() : new Trailer(), seed);
    }

    /**
     * Creates a variable with a domain of specified arity.
     *
     * @param cp the solver in which the variable is created
     * @param sz a positive value that is the size of the domain
     * @return a variable with domain equal to the set {0,...,sz-1}
     */
    public static IntVar makeIntVar(Solver cp, int sz) {
        return new IntVarImpl(cp, sz);
    }

    /**
     * Creates a variable with a domain equal to the specified range.
     *
     * @param cp  the solver in which the variable is created
     * @param min the lower bound of the domain (included)
     * @param max the upper bound of the domain (included) {@code max >= min}
     * @return a variable with domain equal to the set {min,...,max}
     */
    public static IntVar makeIntVar(Solver cp, int min, int max) {
        return new IntVarImpl(cp, min, max);
    }

    /**
     * Creates a variable with a domain equal to the specified set of values.
     *
     * @param cp     the solver in which the variable is created
     * @param values a set of values
     * @return a variable with domain equal to the set of values
     */
    public static IntVar makeIntVar(Solver cp, Set<Integer> values) {
        return new IntVarImpl(cp, values);
    }

    /**
     * Creates a boolean variable.
     *
     * @param cp the solver in which the variable is created
     * @return an uninstantiated boolean variable
     */
    public static BoolVar makeBoolVar(Solver cp) {
        return new BoolVarImpl(cp);
    }

    /**
     * Creates an array of variables with specified domain size.
     *
     * @param cp the solver in which the variables are created
     * @param n  the number of variables to create
     * @param sz a positive value that is the size of the domain
     * @return an array of n variables, each with domain equal to the set {0,...,sz-1}
     */
    public static IntVar[] makeIntVarArray(Solver cp, int n, int sz) {
        return makeIntVarArray(n, i -> makeIntVar(cp, sz));
    }

    /**
     * Creates an array of variables with specified domain bounds.
     *
     * @param cp  the solver in which the variables are created
     * @param n   the number of variables to create
     * @param min the lower bound of the domain (included)
     * @param max the upper bound of the domain (included) {@code max > min}
     * @return an array of n variables each with a domain equal to the set {min,...,max}
     */
    public static IntVar[] makeIntVarArray(Solver cp, int n, int min, int max) {
        return makeIntVarArray(n, i -> makeIntVar(cp, min, max));
    }

    /**
     * Creates an array of variables with specified lambda function
     *
     * @param n    the number of variables to create
     * @param body the function that given the index i in the array creates/map the corresponding {@link IntVar}
     * @return an array of n variables
     * with variable at index <i>i</i> generated as {@code body.get(i)}
     */
    public static IntVar[] makeIntVarArray(int n, Function<Integer, IntVar> body) {
        IntVar[] t = new IntVar[n];
        for (int i = 0; i < n; i++)
            t[i] = body.apply(i);
        return t;
    }

    /**
     * Creates a Depth First Search with custom branching heuristic
     * <pre>
     * // Example of binary search: At each node it selects
     * // the first free variable qi from the array q,
     * // and creates two branches qi=v, qi!=v where v is the min value domain
     * {@code
     * DFSearch search = Factory.makeDfs(cp, () -> {
     *     IntVar qi = Arrays.stream(q).reduce(null, (a, b) -> b.size() > 1 && a == null ? b : a);
     *     if (qi == null) {
     *        return return EMPTY;
     *     } else {
     *        int v = qi.min();
     *        Procedure left = () -> equal(qi, v); // left branch
     *        Procedure right = () -> notEqual(qi, v); // right branch
     *        return branch(left, right);
     *     }
     * });
     * }
     * </pre>
     *
     * @param cp        the solver that will be used for the search
     * @param branching a generator that is called at each node of the depth first search
     *                  tree to generate an array of {@link Procedure} objects
     *                  that will be used to commit to child nodes.
     *                  It should return {@link BranchingScheme#EMPTY} whenever the current state
     *                  is a solution.
     * @return the depth first search object ready to execute with
     * {@link DFSearch#solve()} or
     * {@link DFSearch#optimize(Objective)}
     * using the given branching scheme
     * @see BranchingScheme#firstFail(IntVar...)
     * @see BranchingScheme#branch(Procedure...)
     */
    public static DFSearch makeDfs(Solver cp, Supplier<Procedure[]> branching) {
        cp.propagateSolver(); // initial propagation at root node
        return new DFSearch(cp.getStateManager(), branching);
    }

    public static DFSearch makeDfs(Solver cp, Supplier<Procedure[]> branching, Supplier<Procedure[]> branchingSecond) {
        cp.propagateSolver();
        return new DFSearch(cp.getStateManager(), branching, branchingSecond);
    }


    /**
     * Creates a Limited Discrepancy Search with custom branching heuristic
     *
     * @param cp        the solver that will be used for the search
     * @param branching a generator that is called at each node of the search
     *                  tree to generate an array of {@link Procedure} objects
     *                  that will be used to commit to child nodes.
     *                  It should return {@link BranchingScheme#EMPTY} whenever the current state
     *                  is a solution.
     * @param geometric to indicate whether the progression of maxDiscrepancy is geometric
     * @return the limited discrepancy search object ready to execute with
     * {@link LDSearch#solve()} or
     * {@link LDSearch#optimize(Objective)}
     * using the given branching scheme
     * @see BranchingScheme#firstFail(IntVar...)
     * @see BranchingScheme#branch(Procedure...)
     */
    public static LDSearch makeLds(Solver cp, Supplier<Procedure[]> branching, boolean geometric) {
        cp.propagateSolver(); // initial propagation at root node
        // compute an upper bound on the number of discrepancies in the rightmost branch of a complete search tree
        int discrepancyUB = 0;
        for (int i = 0; i < cp.getVariables().size(); i++) {
            discrepancyUB += cp.getVariables().get(i).size() - 1;
        }
        return new LDSearch(cp.getStateManager(), branching, geometric, discrepancyUB);
    }

    public static LDSearch makeLds(Solver cp, Supplier<Procedure[]> branching) {
        return makeLds(cp, branching, true);
    }

    // -------------- views -----------------------

    /**
     * A variable that is a view of {@code x*a}.
     *
     * @param x a variable
     * @param a a constant to multiply x with
     * @return a variable that is a view of {@code x*a}
     */
    public static IntVar mul(IntVar x, int a) {
        if (a == 0) return makeIntVar(x.getSolver(), 0, 0);
        else if (a == 1) return x;
        else if (a < 0) {
            return minus(new IntVarViewMul(x, -a));
        } else {
            return new IntVarViewMul(x, a);
        }
    }

    /**
     * A variable that is a view of {@code -x}.
     *
     * @param x a variable
     * @return a variable that is a view of {@code -x}
     */
    public static IntVar minus(IntVar x) {
        return new IntVarViewOpposite(x);
    }

    /**
     * A variable that is a view of {@code x+v}.
     *
     * @param x a variable
     * @param v a value
     * @return a variable that is a view of {@code x+v}
     */
    public static IntVar plus(IntVar x, int v) {
        return new IntVarViewOffset(x, v);
    }

    /**
     * A variable that is a view of {@code x-v}.
     *
     * @param x a variable
     * @param v a value
     * @return a variable that is a view of {@code x-v}
     */
    public static IntVar minus(IntVar x, int v) {
        return new IntVarViewOffset(x, -v);
    }

    /**
     * A variable that is a view of not x.
     *
     * @param x a variable
     * @return a variable that is a view of not x
     */
    public static BoolVar not(BoolVar x) {
        return new BoolVarViewNot(x);
    }

    // -------------- branches -----------------------

    /**
     * Branches on x=v  
     * and performs propagation according to the mode.
     *
     * @param x the variable to be assigned to v
     * @param v the value that must be assigned to x
     */
    public static void branchEqual(IntVar x, int v) {
        x.assign(v);
        x.getSolver().propagateSolver();
    }

    /**
     * Branches on x=v,  
     * performs propagation according to the mode
     * and compute impact on the entropy of the model
     *
     * @param x the variable to be assigned to v
     * @param v the value that must be assigned to x
     */
    public static void branchEqualRegisterImpact(IntVar x, int v) {
        double oldEntropy = 0.0;
        double newEntropy = 0.0;
        StateStack<IntVar> listeVariables =  x.getSolver().getVariables();
        for(int i = 0; i < listeVariables.size(); i++) 
            if(listeVariables.get(i).isForBranching())
                oldEntropy += listeVariables.get(i).entropy()/Math.log(listeVariables.get(i).size());
        
        x.assign(v);
        try {
            x.getSolver().propagateSolver();
        }
        catch (InconsistencyException e) {
            x.registerImpact(v, 1.0);
            throw e;
        }
        for(int i = 0; i < listeVariables.size(); i++) 
            if(listeVariables.get(i).isForBranching())
                newEntropy += listeVariables.get(i).entropy()/Math.log(listeVariables.get(i).size());

        x.registerImpact(v, (1.0 - (newEntropy/oldEntropy)));
    }

    public static class IntHolder {
        private int val;
        private IntVar var;
        public IntHolder() {}
        public int getVal() {
            return val;
        }
        public IntVar getVar() {
            return var;
        }
        public void setVal(int value) {
            val = value;
        }
        public void setVar(IntVar a) {
            var = a;
        }
    }

    /**
     * Branches on x=v,  
     * performs propagation according to the mode
     * and compute impact on the entropy of the model
     *
     * @param x the variable to be assigned to v
     * @param v the value that must be assigned to x
     */
    public static void branchEqualRegisterImpact(IntHolder a) {
        IntVar x = a.getVar();
        int v = a.getVal();
        double oldEntropy = 0.0;
        double newEntropy = 0.0;
        StateStack<IntVar> listeVariables =  x.getSolver().getVariables();

        for(int i = 0; i < listeVariables.size(); i++) {
            if(listeVariables.get(i).isForBranching())
                oldEntropy += listeVariables.get(i).entropy();
        }
        x.assign(v);
        try {
            x.getSolver().propagateSolver();
        }
        catch (InconsistencyException e) {
            x.registerImpact(v, 1.0);
            throw e;
        }
        for(int i = 0; i < listeVariables.size(); i++) 
            if(listeVariables.get(i).isForBranching())
                newEntropy += listeVariables.get(i).entropy();

        x.registerImpact(v, (1.0 - (newEntropy/oldEntropy)));
    }

    /**
     * Branches on x!=v 
     * and performs propagation according to the mode.
     *
     * @param x the variable that is constrained to be different from v
     * @param v the value that must be different from x
     */
    public static void branchNotEqual(IntVar x, int v) {
        x.remove(v);
        x.getSolver().propagateSolver();
    }

    /**
     * Branches on x<=v  
     * and performs propagation according to the mode.
     *
     * @param x the variable that is constrained to be less or equal to v
     * @param v the value that must be the upper bound on x
     */
    public static void branchLessOrEqual(IntVar x, int v) {
        x.removeAbove(v);
        x.getSolver().propagateSolver();
    }

    /**
     * Branches on x>v  
     * and performs propagation according to the mode.
     *
     * @param x the variable that is constrained to be greater than v
     * @param v 
     */
    public static void branchGreater(IntVar x, int v) {
        x.removeBelow(v+1);
        x.getSolver().propagateSolver();
    }

    // -------------- constraints -----------------------

    /**
     * Computes a variable that is the absolute value of the given variable.
     * This relation is enforced by the {@link Absolute} constraint
     * posted by calling this method.
     *
     * @param x a variable
     * @return a variable that represents the absolute value of x
     */
    public static IntVar abs(IntVar x) {
        IntVar r = makeIntVar(x.getSolver(), 0, Math.max(x.max(), -x.min()));
        x.getSolver().post(new Absolute(x, r));
        return r;
    }

    public static BoolVar isOr(BoolVar[] x) {
        BoolVar r = makeBoolVar(x[0].getSolver());
        r.getSolver().post(new IsOr(r,x));
        return r;
    }

    public static Constraint isOr(BoolVar b, BoolVar[] x) {
        return new IsOr(b, x);
    }

    public static Constraint or(BoolVar[] a) {
        return new Or(a);
    }

    /**
     * Computes a variable that is the maximum of a set of variables.
     * This relation is enforced by the {@link Maximum} constraint
     * posted by calling this method.
     *
     * @param x the variables on which to compute the maximum
     * @return a variable that represents the maximum on x
     * @see Factory#minimum(IntVar...)
     */
    public static IntVar maximum(IntVar... x) {
        Solver cp = x[0].getSolver();
        int min = Arrays.stream(x).mapToInt(IntVar::min).min().getAsInt();
        int max = Arrays.stream(x).mapToInt(IntVar::max).max().getAsInt();
        IntVar y = makeIntVar(cp, min, max);
        cp.post(new Maximum(x, y));
        return y;
    }

    /**
     * Returns a maximum constraint.
     *
     * @param x an array of variables
     * @param y a variable
     * @return a constraint so that {@code y = max{x[0],x[1],...,x[n-1]}}
     */
    public static Constraint maximum(IntVar[] x, IntVar y) {
        return new Maximum(x, y);
    }

    /**
     * Computes a variable that is the minimum of a set of variables.
     * This relation is enforced by the {@link Maximum} constraint
     * posted by calling this method.
     *
     * @param x the variables on which to compute the minimum
     * @return a variable that represents the minimum on x
     * @see Factory#maximum(IntVar...) (IntVar...)
     */
    public static IntVar minimum(IntVar... x) {
        IntVar[] minusX = Arrays.stream(x).map(Factory::minus).toArray(IntVar[]::new);
        return minus(maximum(minusX));
    }

    /**
     * Returns a constraint imposing that the
     * the first variable differs from the second
     * one minus a constant value.
     *
     * @param x a variable
     * @param y a variable
     * @param c a constant
     * @return a constraint so that {@code x != y+c}
     */
    public static Constraint notEqual(IntVar x, IntVar y, int c) {
        return new NotEqual(x, y, c);
    }

    /**
     * Returns a constraint imposing that the two different variables
     * must take different values.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x != y}
     */
    public static Constraint notEqual(IntVar x, IntVar y) {
        return new NotEqual(x, y, 0);
    }

    /**
     * Returns a constraint imposing that the two different variables
     * must take the same value.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x = y}
     */
    public static Constraint equal(IntVar x, IntVar y) {
        return new Equal(x, y);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is equal to the given constant.
     * This relation is enforced by the {@link IsEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if x takes the value c
     * @see IsEqual
     */
    public static BoolVar isEqual(IntVar x, final int c) {
        BoolVar b = makeBoolVar(x.getSolver());
        Solver cp = x.getSolver();
        try {
            cp.post(new IsEqual(b, x, c));
        } catch (InconsistencyException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is not equal to the given constant.
     * This relation is enforced by the {@link IsEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if x does not take the value c
     * @see IsEqual
     */
    public static BoolVar isNotEqual(IntVar x, final int c) {
        return not(isEqual(x, c));
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is equal to another.
     * This relation is enforced by the {@link IsEqualVar} constraint
     * posted by calling this method.
     *
     * @param x the first variable
     * @param y the second variable
     * @return a boolean variable that is true if and only if x is equal to y
     * @see IsEqualVar
     */
    public static BoolVar isEqual(IntVar x, IntVar y) {
        BoolVar b = makeBoolVar(x.getSolver());
        Solver cp = x.getSolver();
        try {
            cp.post(new IsEqualVar(b, x, y));
        } catch (InconsistencyException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is not equal to another.
     * This relation is enforced by the {@link IsEqualVar} constraint
     * posted by calling this method.
     *
     * @param x the first variable
     * @param y the second variable
     * @return a boolean variable that is true if and only if x is not equal to y
     * @see IsEqualVar
     */
    public static BoolVar isNotEqual(IntVar x, IntVar y) {
        return not(isEqual(x, y));
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is less or equal to the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     * x takes a value less or equal to c
     */
    public static BoolVar isLessOrEqual(IntVar x, final int c) {
        BoolVar b = makeBoolVar(x.getSolver());
        Solver cp = x.getSolver();
        cp.post(new IsLessOrEqual(b, x, c));
        return b;
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is less or equal to another variable.
     * This relation is enforced by the {@link IsLessOrEqualVar} constraint
     * posted by calling this method.
     *
     * @param x the lhs variable
     * @param y the rhs variable
     * @return a boolean variable that is true if and only if
     * x takes a value less or equal to that of y
     */
    public static BoolVar isLessOrEqual(IntVar x, IntVar y) {
        BoolVar b = makeBoolVar(x.getSolver());
        Solver cp = x.getSolver();
        cp.post(new IsLessOrEqualVar(b, x, y));
        return b;
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is less than the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     * x takes a value less than c
     */
    public static BoolVar isLess(IntVar x, final int c) {
        return isLessOrEqual(x, c - 1);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is less than another variable.
     * This relation is enforced by the {@link IsLessOrEqualVar} constraint
     * posted by calling this method.
     *
     * @param x the lhs variable
     * @param y the rhs variable
     * @return a boolean variable that is true if and only if
     * x takes a value less than that of y
     */
    public static BoolVar isLess(IntVar x, IntVar y) {
        return isLessOrEqual(x, minus(y,1));
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is larger or equal to the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     * x takes a value larger or equal to c
     */
    public static BoolVar isLargerOrEqual(IntVar x, final int c) {
        return isLessOrEqual(minus(x), -c);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is larger or equal to another variable.
     * This relation is enforced by the {@link IsLessOrEqualVar} constraint
     * posted by calling this method.
     *
     * @param x the lhs variable
     * @param y the rhs variable
     * @return a boolean variable that is true if and only if
     * x takes a value larger or equal to that of y
     */
    public static BoolVar isLargerOrEqual(IntVar x, IntVar y) {
        return isLessOrEqual(y, x);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is larger than the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     * x takes a value larger than c
     */
    public static BoolVar isLarger(IntVar x, final int c) {
        return isLargerOrEqual(x, c + 1);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is larger than another variable.
     * This relation is enforced by the {@link IsLessOrEqualVar} constraint
     * posted by calling this method.
     *
     * @param x the lhs variable
     * @param y the rhs variable
     * @return a boolean variable that is true if and only if
     * x takes a value larger than that of y
     */
    public static BoolVar isLarger(IntVar x, IntVar y) {
        return isLessOrEqual(y, minus(x,1));
    }

    /**
     * Returns a constraint imposing that the
     * a first variable is less or equal to a second one.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x <= y}
     */
    public static Constraint lessOrEqual(IntVar x, IntVar y) {
        return new LessOrEqual(x, y);
    }

    /**
     * Returns a constraint imposing that the
     * a first variable is less than a second one.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x < y}
     */
    public static Constraint less(IntVar x, IntVar y) {
        return new LessOrEqual(x, minus(y,1));
    }

    /**
     * Returns a constraint imposing that the
     * a first variable is larger or equal to a second one.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x >= y}
     */
    public static Constraint largerOrEqual(IntVar x, IntVar y) {
        return new LessOrEqual(y, x);
    }

    /*
     * Returns a constraint imposing that the
     * a first variable is larger than a second one.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x > y}
     */
    public static Constraint larger(IntVar x, IntVar y) {
        return new LessOrEqual(y, minus(x,1));
    }

    /**
     * Returns a constraint imposing that the product of two variables
     * is equal to the third one.
     *
     * @param x a variable
     * @param y a variable
     * @param z a variable
     * @return a constraint so that {@code x * y = z}
     */
    public static Constraint product(IntVar x, IntVar y, IntVar z) {
        return new Product(x, y, z);
    }

    /**
     * Returns a variable representing the product of two variables
     *
     * @param x a variable
     * @param y a variable
     * @return a variable equal to {@code x * y}
     */
    public static IntVar product(IntVar x, IntVar y) {
        Solver cp = x.getSolver();
	IntVar z = makeIntVar(cp, Math.min(Math.min(Math.min(x.min()*y.min(),x.min()*y.max()),x.max()*y.min()),x.max()*y.max()), Math.max(Math.max(Math.max(x.min()*y.min(),x.min()*y.max()),x.max()*y.min()),x.max()*y.max()));
        cp.post(new Product(x, y, z));
	return z;
    }

    /**
     * Returns a constraint imposing that array[y] = z
     * @param array an array of int
     * @param y a variable
     * @param z a variable
     * @return a constraint so that {@code array[y] = z}
     */
    public static Constraint element(int[] array, IntVar y, IntVar z) {
        return new Element1DDomainConsistent(array, y, z);
    }

    public static Constraint element(IntVar[] array, IntVar y, IntVar z) {
        return new Element1DVar(array, y, z);
    }
  
    /**
     * Returns a constraint imposing that array[x][y] = z
     * @param array an array of int
     * @param x a variable
     * @param y a variable
     * @param z a variable
     * @return a constraint so that {@code array[x][y] = z}
     */
    public static Constraint element(int[][] array, IntVar x, IntVar y, IntVar z) {
        return new Element2DDomainConsistent(array, x, y, z);
    }

    /**
     * Returns a variable representing
     * the value in an array at the position
     * specified by the given index variable
     * This relation is enforced by the {@link Element1DDomainConsistent} constraint
     * posted by calling this method.
     *
     * @param array the array of values
     * @param y     the variable
     * @return a variable equal to {@code array[y]}
     */
    public static IntVar element(int[] array, IntVar y) {
        Solver cp = y.getSolver();
        IntVar z = makeIntVar(cp, IntStream.of(array).min().getAsInt(), IntStream.of(array).max().getAsInt());
        cp.post(new Element1DDomainConsistent(array, y, z));
        return z;
    }

    /**
     * Returns a variable representing
     * the value in a matrix at the position
     * specified by the two given row and column index variables
     * This relation is enforced by the {@link Element2D} constraint
     * posted by calling this method.
     *
     * @param matrix the n x m 2D array of values
     * @param x      the row variable with domain included in 0..n-1
     * @param y      the column variable with domain included in 0..m-1
     * @return a variable equal to {@code matrix[x][y]}
     */
    public static IntVar element(int[][] matrix, IntVar x, IntVar y) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                min = Math.min(min, matrix[i][j]);
                max = Math.max(max, matrix[i][j]);
            }
        }
        IntVar z = makeIntVar(x.getSolver(), min, max);
        x.getSolver().post(new Element2DDomainConsistent(matrix, x, y, z));
        return z;
    }

    /**
     * Returns a variable representing
     * the sum of a given set of variables.
     * This relation is enforced by the {@link Sum} constraint
     * posted by calling this method.
     *
     * @param x the n variables to sum
     * @return a variable equal to {@code x[0]+x[1]+...+x[n-1]}
     */
    public static IntVar sum(IntVar... x) {
        int sumMin = 0;
        int sumMax = 0;
        for (int i = 0; i < x.length; i++) {
            sumMin += x[i].min();
            sumMax += x[i].max();
        }
        Solver cp = x[0].getSolver();
        // merge repeated variables among x (so that beliefs are computed correctly)
        int nbUnique = 0;
        int[] nbOcc = new int[x.length];
        for (int i = 0; i < x.length; i++)
            nbOcc[i] = 1;
        for (int i = 0; i < x.length; i++)
            if (nbOcc[i] == 1) {
                nbUnique++;
                for (int j = i + 1; j < x.length; j++)
                    if (x[i] == x[j]) { // repeated var
                        nbOcc[i]++;
                        nbOcc[j] = 0;
                    }
            }
        IntVar[] vars = new IntVar[nbUnique + 1];
        for (int i = 0; i < x.length; i++)
            switch (nbOcc[i]) {
                case 0:
                    break;
                case 1:
                    vars[nbUnique] = x[i];
                    nbUnique--;
                    break;
                default:
                    vars[nbUnique] = mul(x[i], nbOcc[i]);
                    nbUnique--;
            }
        vars[0] = makeIntVar(cp, -sumMax, -sumMin);
        cp.post(new SumDC(vars));
        return minus(vars[0]);
    }

    /**
     * Returns a variable representing
     * the weighted sum of a given set of variables.
     * This relation is enforced by the {@link Sum} constraint
     * posted by calling this method.
     *
     * @param c an array of integer coefficients
     * @param x an array of variables
     * @return a variable equal to {@code c[0]*x[0]+c[1]*x[1]+...+c[n-1]*x[n-1]}
     */
    public static IntVar sum(int[] c, IntVar... x) {
        assert (c.length == x.length);
        int sumMin = 0;
        int sumMax = 0;
        for (int i = 0; i < x.length; i++) {
            sumMin += c[i] * (c[i] >= 0 ? x[i].min() : x[i].max());
            sumMax += c[i] * (c[i] >= 0 ? x[i].max() : x[i].min());
        }
        Solver cp = x[0].getSolver();
        IntVar[] vars = new IntVar[x.length + 1];
        for (int i = 0; i < x.length; i++) {
            vars[i] = mul(x[i], c[i]);
        }
        vars[x.length] = makeIntVar(cp, -sumMax, -sumMin);
        cp.post(new SumDC(vars));
        return minus(vars[x.length]);
    }

    /**
     * Returns a sum constraint.
     *
     * @param x an array of variables
     * @param y a variable
     * @return a constraint so that {@code y = x[0]+x[1]+...+x[n-1]}
     */
    public static Constraint sum(IntVar[] x, IntVar y) {
        IntVar[] vars = Arrays.copyOf(x, x.length + 1);
        vars[x.length] = minus(y);
        return new SumDC(vars);
    }

    /**
     * Returns a weighted sum constraint.
     *
     * @param c an array of integer coefficients
     * @param x an array of variables
     * @param y a variable
     * @return a constraint so that {@code y = c[0]*x[0]+c[1]*x[1]+...+c[n-1]*x[n-1]}
     */
    public static Constraint sum(int[] c, IntVar[] x, IntVar y) {
        IntVar[] vars = new IntVar[x.length + 1];
        for (int i = 0; i < x.length; i++) {
            vars[i] = mul(x[i], c[i]);
        }
        vars[x.length] = minus(y);
        return new SumDC(vars);
    }

    /**
     * Returns a sum constraint.
     *
     * @param x an array of variables
     * @param y a constant
     * @return a constraint so that {@code y = x[0]+x[1]+...+x[n-1]}
     */
    public static Constraint sum(IntVar[] x, int y) {
        Solver cp = x[0].getSolver();
        // merge repeated variables among x
        int nbUnique = 0;
        int[] nbOcc = new int[x.length];
        for (int i = 0; i < x.length; i++)
            nbOcc[i] = 1;
        for (int i = 0; i < x.length; i++)
            if (nbOcc[i] == 1) {
                nbUnique++;
                for (int j = i + 1; j < x.length; j++)
                    if (x[i] == x[j]) { // repeated var
                        nbOcc[i]++;
                        nbOcc[j] = 0;
                    }
            }
        IntVar[] vars = new IntVar[nbUnique + 1];
        for (int i = 0; i < x.length; i++)
            switch (nbOcc[i]) {
                case 0:
                    break;
                case 1:
                    vars[nbUnique] = x[i];
                    nbUnique--;
                    break;
                default:
                    vars[nbUnique] = mul(x[i], nbOcc[i]);
                    nbUnique--;
            }
        vars[0] = makeIntVar(cp, -y, -y);
        return new SumDC(vars);
    }

    /**
     * Returns a weighted sum constraint.
     *
     * @param c an array of integer coefficients
     * @param x an array of variables
     * @param y a constant
     * @return a constraint so that {@code y = c[0]*x[0]+c[1]*x[1]+...+c[n-1]*x[n-1]}
     */
    public static Constraint sum(int[] c, IntVar[] x, int y) {
        Solver cp = x[0].getSolver();
        // merge repeated variables among x
        int nbUnique = 0;
        int[] coef = new int[x.length];
        for (int i = 0; i < x.length; i++)
            coef[i] = c[i];
        for (int i = 0; i < x.length; i++)
            if (coef[i] != 0) {
                nbUnique++;
                for (int j = i + 1; j < x.length; j++)
                    if (x[i] == x[j]) { // repeated var
                        coef[i] += coef[j];
                        coef[j] = 0;
                    }
            }
        IntVar[] vars = new IntVar[nbUnique + 1];
        for (int i = 0; i < x.length; i++)
            switch (coef[i]) {
                case 0:
                    break;
                case 1:
                    vars[nbUnique] = x[i];
                    nbUnique--;
                    break;
                default:
                    vars[nbUnique] = mul(x[i], coef[i]);
                    nbUnique--;
            }
        vars[0] = makeIntVar(cp, -y, -y);
        return new SumDC(vars);
    }

    /**
     * Returns a binary decomposition of the allDifferent constraint.
     *
     * @param x an array of variables
     * @return a constraint so that {@code x[i] != x[j] for all i < j}
     */
    public static Constraint allDifferentBinary(IntVar[] x) {
        return new AllDifferentBinary(x);
    }

    /**
     * Returns an allDifferent constraint that enforces
     * domain consistency.
     *
     * @param x an array of variables
     * @return a constraint so that {@code x[i] != x[j] for all i < j}
     */
    public static Constraint allDifferent(IntVar[] x) {
        return new AllDifferentDC(x);
    }

    /**
     * Returns a table constraint.
     * This relation is enforced by the {@link TableCT} constraint
     * posted by calling this method.
     *
     * <p>The table constraint ensures that
     * {@code x} is a row from the given table.
     * More exactly, there exist some row <i>i</i>
     * such that
     * {@code x[0]==table[i][0], x[1]==table[i][1], etc}.
     *
     * <p>This constraint is sometimes called <i>in extension</i> constraint
     * as the user enumerates the set of solutions that can be taken
     * by the variables.
     *
     * @param x     the non empty set of variables to constraint
     * @param table the possible set of solutions for x.
     *              The second dimension must be of the same size as the array x.
     * @return a table constraint
     */
    public static Constraint table(IntVar[] x, int[][] table) {
        return new TableCT(x, table);
    }

    /**
     * special case using only the first "tableLength" tuples
     */
    public static Constraint table(IntVar[] x, int[][] table, int tableLength) {
        return new TableCT(x, table);
    }

    /**
     * Returns a regular constraint.
     * This relation is enforced by the {@link Regular} constraint
     * posted by calling this method.
     *
     * @param x an array of variables
     * @param A a 2D array giving the transition function of the automaton: {states} x {domain values} -> {states} (domain values are nonnegative and start at 0)
     * @param s is the initial state
     * @param f a list of accepting states
     * @return a constraint so that {@code x is a word recognized by automaton A}
     */
    public static Constraint regular(IntVar[] x, int[][] A, int s, List<Integer> f) {
        return new Regular(x, A, s, f);
    }

    /**
     * special case with 0 being the initial state
     */
    public static Constraint regular(IntVar[] x, int[][] A, List<Integer> f) {
        return new Regular(x, A, 0, f);
    }

    /**
     * special case with 0 being the initial state and all states being accepting
     */
    public static Constraint regular(IntVar[] x, int[][] A) {
        List<Integer> f = new ArrayList<Integer>();
        for (int i = 0; i < A.length; i++) {
            f.add(i);
        }
        return new Regular(x, A, 0, f);
    }

    /**
     * Returns a costRegular constraint.
     * This relation is enforced by the {@link CostRegular} constraint
     * posted by calling this method.
     *
     * @param x  an array of variables
     * @param A  a 2D array giving the transition function of the automaton: {states} x {domain values} -> {states} (domain values are nonnegative and start at 0)
     * @param s  is the initial state
     * @param f  a list of accepting states
     * @param c  a 3D array giving integer costs for each combination of variable, state, and domain value (in that order)
     * @param tc the total cost of word x computed as the sum of the corresponding integer costs from array c
     * @return a constraint so that {@code x is a word recognized by automaton A and of total cost tc}
     */
    public static Constraint costRegular(IntVar[] x, int[][] A, int s, List<Integer> f, int[][][] c, IntVar tc) {
        return new CostRegular(x, A, s, f, c, tc);
    }

    /**
     * special case with 0 being the initial state
     */
    public static Constraint costRegular(IntVar[] x, int[][] A, List<Integer> f, int[][][] c, IntVar tc) {
        return new CostRegular(x, A, 0, f, c, tc);
    }

    /**
     * special case with 0 being the initial state and all states being accepting
     */
    public static Constraint costRegular(IntVar[] x, int[][] A, int[][][] c, IntVar tc) {
        List<Integer> f = new ArrayList<Integer>();
        for (int i = 0; i < A.length; i++) {
            f.add(i);
        }
        return new CostRegular(x, A, 0, f, c, tc);
    }

    /**
     * special case with 2D cost matrix: state x domain value
     */
    public static Constraint costRegular(IntVar[] x, int[][] A, int s, List<Integer> f, int[][] c, IntVar tc) {
        return new CostRegular(x, A, s, f, c, tc);
    }

    /**
     * special case with 0 being the initial state
     */
    public static Constraint costRegular(IntVar[] x, int[][] A, List<Integer> f, int[][] c, IntVar tc) {
        return new CostRegular(x, A, 0, f, c, tc);
    }

    /**
     * special case with 0 being the initial state and all states being accepting
     */
    public static Constraint costRegular(IntVar[] x, int[][] A, int[][] c, IntVar tc) {
        List<Integer> f = new ArrayList<Integer>();
        for (int i = 0; i < A.length; i++) {
            f.add(i);
        }
        return new CostRegular(x, A, 0, f, c, tc);
    }

    /**
     * special case with 1D cost matrix: domain value
     */
    public static Constraint costRegular(IntVar[] x, int[][] A, int s, List<Integer> f, int[] c, IntVar tc) {
        return new CostRegular(x, A, s, f, c, tc);
    }

    /**
     * special case with 0 being the initial state
     */
    public static Constraint costRegular(IntVar[] x, int[][] A, List<Integer> f, int[] c, IntVar tc) {
        return new CostRegular(x, A, 0, f, c, tc);
    }

    /**
     * special case with 0 being the initial state and all states being accepting
     */
    public static Constraint costRegular(IntVar[] x, int[][] A, int[] c, IntVar tc) {
        List<Integer> f = new ArrayList<Integer>();
        for (int i = 0; i < A.length; i++) {
            f.add(i);
        }
        return new CostRegular(x, A, 0, f, c, tc);
    }

    /**
     * Returns an among constraint.
     * This relation is enforced by the {@link Among} constraint
     * posted by calling this method.
     *
     * @param x an array of variables whose instantiations belonging to V we count
     * @param V an array of values whose occurrences in x we count
     * @param o the variable corresponding to the number of occurrences of values from V in x
     * @return a constraint so that {@code (x[0] \in V) + (x[1] \in V) + ... + (x[x.length-1] \in V) == o}
     */
    public static Constraint among(IntVar[] x, int[] V, IntVar o) {
        Solver cp = x[0].getSolver();
        IntVar[] vars = Arrays.copyOf(x, 2 * x.length);
        IntVar[] y = new IntVar[x.length]; // indicator variables: (y[i] == 1) iff (x[i] \in V)
        for (int i = 0; i < y.length; i++) {
            y[i] = makeIntVar(cp, 0, 1);
            y[i].setName("y" + "[" + i + "]");
            vars[x.length + i] = y[i];
        }
        return new Among(x, V, o, y, vars);
    }

    /**
     * special cases with fixed (limit on) nb of occurrences
     */
    public static Constraint atleast(IntVar[] x, int[] V, int lb) {
        return among(x, V, makeIntVar(x[0].getSolver(), lb, Math.max(lb, x.length)));
    }

    public static Constraint atmost(IntVar[] x, int[] V, int ub) {
        return among(x, V, makeIntVar(x[0].getSolver(), Math.min(0, ub), ub));
    }

    public static Constraint exactly(IntVar[] x, int[] V, int o) {
        return among(x, V, makeIntVar(x[0].getSolver(), o, o));
    }

    /**
     * special cases with a single value in V
     */
    public static Constraint among(IntVar[] x, int v, IntVar o) {
        return among(x, new int[]{v}, o);
    }

    public static Constraint atleast(IntVar[] x, int v, int lb) {
        return among(x, new int[]{v}, makeIntVar(x[0].getSolver(), lb, Math.max(lb, x.length)));
    }

    public static Constraint atmost(IntVar[] x, int v, int ub) {
        return among(x, new int[]{v}, makeIntVar(x[0].getSolver(), Math.min(0, ub), ub));
    }

    public static Constraint exactly(IntVar[] x, int v, int o) {
        return among(x, new int[]{v}, makeIntVar(x[0].getSolver(), o, o));
    }

    /**
     * Returns a cardinality constraint.
     * This relation is currently enforced by decomposing it into {@link Among} constraints; hence it is not domain consistent
     *
     * @param x    an array of variables
     * @param vals an array of values whose occurrences in x we count
     * @param o    an array of variables corresponding to the number of occurrences of vals in x
     * @return a cardinality constraint
     */
    public static Constraint cardinality(IntVar[] x, int[] vals, IntVar[] o) {
        assert (vals.length == o.length);
        int maxDomainSize = 0;
        for (int i = 0; i < x.length; i++) {
            maxDomainSize = Math.max(maxDomainSize, x[i].size());
        }
        return new Cardinality(x, vals, o, makeIntVar(x[0].getSolver(), 1, maxDomainSize));
    }

    /**
     * special case with fixed nb of occurrences
     */
    public static Constraint cardinality(IntVar[] x, int[] vals, int[] o) {
        assert (vals.length == o.length);
        int maxDomainSize = 0;
        for (int i = 0; i < x.length; i++) {
            maxDomainSize = Math.max(maxDomainSize, x[i].size());
        }
        IntVar[] oVar = new IntVar[o.length];
        Solver cp = x[0].getSolver();
        for (int i = 0; i < o.length; i++) {
            oVar[i] = makeIntVar(cp, o[i], o[i]);
        }
        return new Cardinality(x, vals, oVar, makeIntVar(cp, 1, maxDomainSize));
    }

    /**
 	 * A special case of cardinality constraint when the bounds on number of
 	 * occurrences are given
 	 * 
 	 * @param x    an array of variables
 	 * @param vals an array of values whose occurrences in x we count
 	 * @param oMin an array of constants indicating the minimum number of
 	 *             occurrences of each entry of vals in x
 	 * @param oMax an array of constants indicating the maximum number of
 	 *             occurrences of each entry of vals in x
 	 * @return
 	 */
	  public static Constraint cardinality(IntVar[] x, int[] vals, int[] oMin, int[] oMax) {
		int n = vals.length;
		assert (oMin.length == n);
		assert (oMax.length == n);
		int maxDomainSize = 0;
			for (int i = 0; i < x.length; i++) {
			maxDomainSize = Math.max(maxDomainSize, x[i].size());
		}
		IntVar[] oVar = new IntVar[n];
		Solver cp = x[0].getSolver();
		for (int i = 0; i < n; i++)
			oVar[i] = makeIntVar(cp, oMin[i], oMax[i]);

		return new Cardinality(x, vals, oVar, makeIntVar(cp,1,maxDomainSize));
	}

    /**
     * Returns a Circuit Constraint
     * @param x an array of variables
     * @return
     */
    public static Constraint circuit(IntVar[] x) {
        return new Circuit(x);
    }

    /**
     * Returns a sum modulo p constraint.
     *
     * @param x an array of variables
     * @param y a constant
     * @param p the modulus
     * @return a constraint so that {@code y = x[0]+x[1]+...+x[n-1] (mod p)}
     */
    public static Constraint sumModP(IntVar[] x, int y, int p) {
        Solver cp = x[0].getSolver();
        // merge repeated variables among x
        int nbUnique = 0;
        int[] nbOcc = new int[x.length];
        for (int i = 0; i < x.length; i++)
            nbOcc[i] = 1;
        for (int i = 0; i < x.length; i++)
            if (nbOcc[i] == 1) {
                nbUnique++;
                for (int j = i + 1; j < x.length; j++)
                    if (x[i] == x[j]) { // repeated var
                        nbOcc[i]++;
                        nbOcc[j] = 0;
                    }
            }
        IntVar[] vars = new IntVar[nbUnique + 1];
        for (int i = 0; i < x.length; i++)
            switch (nbOcc[i]) {
                case 0:
                    break;
                case 1:
                    vars[nbUnique] = x[i];
                    nbUnique--;
                    break;
                default:
                    vars[nbUnique] = mul(x[i], nbOcc[i]);
                    nbUnique--;
            }
        vars[0] = makeIntVar(cp, -y, -y);
        return new SumModP(vars, p);
    }

    /**
     * Returns a weighted sum modulo p constraint.
     *
     * @param c an array of integer coefficients
     * @param x an array of variables
     * @param y a constant
     * @param p the modulus
     * @return a constraint so that {@code y = c[0]*x[0]+c[1]*x[1]+...+c[n-1]*x[n-1] (mod p)}
     */

    public static Constraint sumModP(int[] c, IntVar[] x, int y, int p) {
        assert (c.length == x.length);
        Solver cp = x[0].getSolver();
        // merge repeated variables among x
        int nbUnique = 0;
        int[] coef = new int[x.length];
        for (int i = 0; i < x.length; i++)
            coef[i] = c[i];
        for (int i = 0; i < x.length; i++)
            if (coef[i] != 0) {
                nbUnique++;
                for (int j = i + 1; j < x.length; j++)
                    if (x[i] == x[j]) { // repeated var
                        coef[i] += coef[j];
                        coef[j] = 0;
                    }
            }
        IntVar[] vars = new IntVar[nbUnique + 1];
        for (int i = 0; i < x.length; i++)
            switch (coef[i]) {
                case 0:
                    break;
                case 1:
                    vars[nbUnique] = x[i];
                    nbUnique--;
                    break;
                default:
                    vars[nbUnique] = mul(x[i], coef[i]);
                    nbUnique--;
            }
        vars[0] = makeIntVar(cp, -y, -y);
        return new SumModP(vars, p);
    }

    /**
     * Returns a LinEqSystemModP constraint.
     *
     * @param A the mxn matrix of coefficients
     * @param x the column vector of n variables
     * @param b the column vector of m rhs values
     * @param p the prime modulus
     * @return a constraint so that {@code Ax == b (mod p)}.
     */
    public static Constraint linEqSystemModP(int[][] A, IntVar[] x, int[] b, int p) {
        return new LinEqSystemModP(A, x, b, p);
    }

    /**
     * Returns a LinIneqSystemModP constraint.
     *
     * @param A the mxn matrix of coefficients
     * @param x the column vector of n variables
     * @param b the column vector of m rhs values
     * @param p the prime modulus
     * @return a constraint so that {@code Ax <= b (mod p)}.
     */
    public static Constraint linIneqSystemModP(int[][] A, IntVar[] x, int[] b, int p) {
        return new LinIneqSystemModP(A, x, b, p);
    }

    /**
     * Returns a LinSystemModP constraint.
     *
     * @param Ae the mexn matrix of coefficients
     * @param Ai the mixn matrix of coefficients
     * @param x the column vector of n variables
     * @param be the column vector of me rhs values
     * @param bi the column vector of mi rhs values
     * @param p the prime modulus
     * @return a constraint so that {@code Aex = be and Aix <= bi (mod p)}.
     */
    public static Constraint linSystemModP(int[][] Ae, int[][] Ai, IntVar[] x, int[] be, int[] bi, int p) {
        return new LinSystemModP(Ae, Ai, x, be, bi, p);
    }

    /**
     * Oracle unary constraint providing fixed marginals (possibly through ML)
     * Does not perform any filtering
     *
     * @param x the variable
     * @param v the values
     * @param m the marginals for v
     *          Note: any domain value not appearing in v will be assigned a zero marginal
     */
    public static Constraint oracle(IntVar x, int[] v, double[] m) {
        return new Oracle(x, v, m);
    }

}
