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

package minicp.cp;

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.LimitedDiscrepancyBranching;
import minicp.search.Sequencer;
import minicp.util.Procedure;
import minicp.util.Belief;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.Random;

import static minicp.cp.Factory.equal;
import static minicp.cp.Factory.notEqual;
import static minicp.cp.Factory.branchEqual;
import static minicp.cp.Factory.branchNotEqual;

/**
 * Factory for search procedures.
 *
 * <p>A typical custom search on an array of variable {@code q} is illustrated next</p>
 *  <pre>
 * {@code
 * DFSearch search = Factory.makeDfs(cp, () -> {
 *   int idx = -1; // index of the first variable that is not bound
 *   for (int k = 0; k < q.length; k++)
 *      if(q[k].size() > 1) {
 *        idx=k;
 *        break;
 *      }
 *   if(idx == -1)
 *      return new Procedure[0];
 *   else {
 *      IntVar qi = q[idx];
 *      int v = qi.min();
 *      Procedure left = () -> Factory.equal(qi, v);
 *      Procedure right = () -> Factory.notEqual(qi, v);
 *      return branch(left,right);
 *   }
 * });
 * }
 * </pre>
 * @see Factory#makeDfs(Solver, Supplier)
 */
public final class BranchingScheme {

    static Random rand = new Random();

    private BranchingScheme() {
        throw new UnsupportedOperationException();
    }

    /**
     * Constant that should be returned
     * to notify the solver that there are no branches
     * to create any more and that the current state should
     * be considered as a solution.
     * @see Factory#makeDfs(Solver, Supplier)
     */
    public static final Procedure[] EMPTY = new Procedure[0];

    /**
     *
     * @param branches the ordered closures for the child branches
     *                 ordered from left to right in the depth first search.
     *
     * @return an array with those branches
     * @see minicp.search.DFSearch
     */
    public static Procedure[] branch(Procedure... branches) {
        return branches;
    }

    /**
     * Minimum selector.
     * <p>Example of usage.
     * <pre>
     * {@code
     * IntVar xs = selectMin(x,xi -> xi.size() > 1,xi -> xi.size());
     * }
     * </pre>
     *
     * @param x the array on which the minimum value is searched
     * @param p the predicate that filters the element eligible for selection
     * @param f the evaluation function that returns a comparable when applied on an element of x
     * @param <T> the type of the elements in x, for instance {@link IntVar}
     * @param <N> the type on which the minimum is computed, for instance {@link Integer}
     * @return the minimum element in x that satisfies the predicate p
     *         or null if no element satisfies the predicate.
     */
    public static <T, N extends Comparable<N>> T selectMin(T[] x, Predicate<T> p, Function<T, N> f) {
        T sel = null;
        for (T xi : x) {
            if (p.test(xi)) {
                sel = sel == null || f.apply(xi).compareTo(f.apply(sel)) < 0 ? xi : sel;
            }
        }
        return sel;
    }

    /**
     * Minimum selector with randomized tie-breaking.
     * <p>Example of usage.
     * <pre>
     * {@code
     * IntVar xs = selectMinRandomTieBreak(x,xi -> xi.size() > 1,xi -> xi.size());
     * }
     * </pre>
     *
     * @param x the array on which the minimum value is searched
     * @param p the predicate that filters the element eligible for selection
     * @param f the evaluation function that returns a comparable when applied on an element of x
     * @param <T> the type of the elements in x, for instance {@link IntVar}
     * @param <N> the type on which the minimum is computed, for instance {@link Integer}
     * @return a minimum element in x that satisfies the predicate p, chosen uniformly at random,
     *         or null if no element satisfies the predicate.
     */
    public static <T, N extends Comparable<N>> T selectMinRandomTieBreak(T[] x, Predicate<T> p, Function<T, N> f) {
	int nbTied = 0;
        T sel = null;
        for (T xi : x) {
            if (p.test(xi)) {
		if (sel == null) {
		    sel = xi;
		    nbTied = 1;
		} 
		else {
		    int comparison = f.apply(xi).compareTo(f.apply(sel));
		    if (comparison < 0) {
			sel = xi;
			nbTied = 1;
		    } 
		    else if (comparison == 0) {
			nbTied++;
			if (rand.nextInt(nbTied) == 0) // with probability 1/nbTied
			    sel = xi;
		    }
		}
            }
        }
        return sel;
    }
    
    /**
     * Lexicographic strategy.
     * It selects the first variable with a domain larger than one.
     * Then it creates two branches:
     * the left branch assigning the variable to its minimum value;
     * the right branch removing this minimum value from the domain.
     * @param x the variable on which the lexicographic strategy is applied.
     * @return a lexicographic branching strategy
     * @see Factory#makeDfs(Solver, Supplier)
     */
    public static Supplier<Procedure[]> lexico(IntVar... x) {
	boolean tracing = x[0].getSolver().tracingSearch();
        return () -> {
            IntVar xs = selectMin(x,
                    xi -> xi.size() > 1,
		    xi -> 1); // any constant value
            if (xs == null)
                return EMPTY;
            else {
                int v = xs.min();
                return branch(
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"="+v);
				  branchEqual(xs, v);
			      },
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"!="+v);
				  branchNotEqual(xs, v);
			      } );
            }
        };
    }

    /**
     * First-Fail strategy.
     * It selects the first variable with a domain larger than one.
     * Then it creates two branches:
     * the left branch assigning the variable to its minimum value;
     * the right branch removing this minimum value from the domain.
     * @param x the variable on which the first fail strategy is applied.
     * @return a first-fail branching strategy
     * @see Factory#makeDfs(Solver, Supplier)
     */
    public static Supplier<Procedure[]> firstFail(IntVar... x) {
	boolean tracing = x[0].getSolver().tracingSearch();
        return () -> {
            IntVar xs = selectMin(x,
                    xi -> xi.size() > 1,
                    xi -> xi.size());
            if (xs == null)
                return EMPTY;
            else {
                int v = xs.min();
                return branch(
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"="+v);
				  branchEqual(xs, v);
			      },
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"!="+v);
				  branchNotEqual(xs, v);
			      } );
            }
        };
    }

    /**
     * First-Fail strategy + random value selection.
     * It selects the first variable with a domain larger than one.
     * Then it creates two branches:
     * the left branch assigning the variable to a value in its domain, chosen uniformly at random;
     * the right branch removing this value from the domain.
     * @param x the variable on which the first fail strategy is applied.
     * @return a first-fail/random-value branching strategy
     * @see Factory#makeDfs(Solver, Supplier)
     */
    public static Supplier<Procedure[]> firstFailRandomVal(IntVar... x) {
	boolean tracing = x[0].getSolver().tracingSearch();
        return () -> {
            IntVar xs = selectMin(x,
                    xi -> xi.size() > 1,
                    xi -> xi.size());
            if (xs == null)
                return EMPTY;
            else {
                int v = xs.randomValue();
                return branch(
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"="+v);
				  branchEqual(xs, v);
			      },
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"!="+v);
				  branchNotEqual(xs, v);
			      } );
	    }
	};
    }

    /**
     * Maximum Marginal Strength strategy.
     * It selects an unbound variable with the largest marginal strength 
     * on one of the values in its domain.
     * Then it creates two branches:
     * the left branch assigning the variable to that value;
     * the right branch removing this value from the domain.
     * @param x the variable on which the max marginal strength strategy is applied.
     * @return maxMarginalStrength branching strategy
     * @see Factory#makeDfs(Solver, Supplier)
     */
    public static Supplier<Procedure[]> maxMarginalStrength(IntVar[] x) {
	boolean tracing = x[0].getSolver().tracingSearch();
	Belief beliefRep = x[0].getSolver().getBeliefRep();
        return () -> {
	    IntVar xs = selectMin(x,
                    xi -> xi.size() > 1,
		    xi -> 1.0 / xi.size() - beliefRep.rep2std(xi.maxMarginal()));
            if (xs == null)
                return EMPTY;
            else {
		int v = xs.valueWithMaxMarginal(); 
                return branch(
			      () -> { 
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"="+v+" marginal="+beliefRep.rep2std(xs.maxMarginal()));
				  branchEqual(xs, v); 
			      },
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"!="+v+" marginal="+(1-beliefRep.rep2std(xs.maxMarginal())));
				  branchNotEqual(xs, v);
			      } );
            }
        };
    }

    /**
     * Maximum Marginal Strength strategy with random tie breaking.
     * It selects an unbound variable with the largest marginal strength 
     * on one of the values in its domain.
     * Then it creates two branches:
     * the left branch assigning the variable to that value;
     * the right branch removing this value from the domain.
     * @param x the variable on which the max marginal strength strategy is applied.
     * @return maxMarginalStrengthRandomTieBreak branching strategy
     * @see Factory#makeDfs(Solver, Supplier)
     */
    public static Supplier<Procedure[]> maxMarginalStrengthRandomTieBreak(IntVar[] x) {
	boolean tracing = x[0].getSolver().tracingSearch();
	Belief beliefRep = x[0].getSolver().getBeliefRep();
        return () -> {
	    IntVar xs = selectMinRandomTieBreak(x,
                    xi -> xi.size() > 1,
		    xi -> 1.0 / xi.size() - beliefRep.rep2std(xi.maxMarginal()));
            if (xs == null)
                return EMPTY;
            else {
		int v = xs.valueWithMaxMarginal(); 
                return branch(
			      () -> { 
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"="+v+" marginal="+beliefRep.rep2std(xs.maxMarginal()));
				  branchEqual(xs, v); 
			      },
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"!="+v+" marginal="+(1-beliefRep.rep2std(xs.maxMarginal())));
				  branchNotEqual(xs, v);
			      } );
            }
        };
    }

    /**
     * Minimum Marginal Strength strategy.
     * It selects an unbound variable with the smallest marginal strength 
     * on one of the values in its domain.
     * Then it creates two branches: 
     * the left branch _removing_ this value from the domain;
     * the right branch _assigning_ the variable to that value.
     * @param x the variable on which the min marginal strength strategy is applied.
     * @return minMarginalStrength branching strategy
     * @see Factory#makeDfs(Solver, Supplier)
     */
    public static Supplier<Procedure[]> minMarginalStrength(IntVar... x) {
	boolean tracing = x[0].getSolver().tracingSearch();
	Belief beliefRep = x[0].getSolver().getBeliefRep();
        return () -> {
            IntVar xs = selectMin(x,
                    xi -> xi.size() > 1,
		    xi -> beliefRep.rep2std(xi.minMarginal()) - 1.0 / xi.size());
            if (xs == null)
                return EMPTY;
            else {
		int v = xs.valueWithMinMarginal(); 
                return branch(
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"!="+v+" marginal="+(1-beliefRep.rep2std(xs.minMarginal())));
				  branchNotEqual(xs, v);
			      },
			      () -> { 
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"="+v+" marginal="+beliefRep.rep2std(xs.minMarginal()));
				  branchEqual(xs, v); 
			      } );
            }
        };
    }

    /**
     * Maximum Marginal strategy.
     * It selects an unbound variable with the largest marginal
     * on one of the values in its domain.
     * Then it creates two branches:
     * the left branch assigning the variable to that value;
     * the right branch removing this value from the domain.
     * @param x the variable on which the max marginal strategy is applied.
     * @return maxMarginal branching strategy
     * @see Factory#makeDfs(Solver, Supplier)
     */
    public static Supplier<Procedure[]> maxMarginal(IntVar... x) {
	boolean tracing = x[0].getSolver().tracingSearch();
	Belief beliefRep = x[0].getSolver().getBeliefRep();
        return () -> {
            IntVar xs = selectMin(x,
                    xi -> xi.size() > 1,
		    xi -> - beliefRep.rep2std(xi.maxMarginal()));
            if (xs == null)
                return EMPTY;
            else {
		int v = xs.valueWithMaxMarginal(); 
                return branch(
			      () -> { 
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"="+v+" marginal="+beliefRep.rep2std(xs.maxMarginal()));
				  branchEqual(xs, v); 
			      },
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"!="+v+" marginal="+(1-beliefRep.rep2std(xs.maxMarginal())));
				  branchNotEqual(xs, v);
			      } );
            }
        };
    }

    /**
     * Minimum Marginal strategy.
     * It selects an unbound variable with the smallest marginal
     * on one of the values in its domain.
     * Then it creates two branches: 
     * the left branch _removing_ this value from the domain;
     * the right branch _assigning_ the variable to that value.
     * @param x the variable on which the min marginal strategy is applied.
     * @return minMarginal branching strategy
     * @see Factory#makeDfs(Solver, Supplier)
     */
    public static Supplier<Procedure[]> minMarginal(IntVar... x) {
	boolean tracing = x[0].getSolver().tracingSearch();
	Belief beliefRep = x[0].getSolver().getBeliefRep();
        return () -> {
            IntVar xs = selectMin(x,
                    xi -> xi.size() > 1,
		    xi -> beliefRep.rep2std(xi.minMarginal()));
            if (xs == null)
                return EMPTY;
            else {
		int v = xs.valueWithMinMarginal(); 
                return branch(
			      () -> {
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"!="+v+" marginal="+(1-beliefRep.rep2std(xs.minMarginal())));
				  branchNotEqual(xs, v);
			      },
			      () -> { 
				  if (tracing)
				      System.out.println("### branching on "+xs.getName()+"="+v+" marginal="+beliefRep.rep2std(xs.minMarginal()));
				  branchEqual(xs, v); 
			      } );
            }
        };
    }

    /**
     * Sequential Search combinator that linearly
     * considers a list of branching generator.
     * One branching of this list is executed
     * when all the previous ones are exhausted (they return an empty array).
     * @param choices the branching schemes considered sequentially in the sequential by
     *                path in the search tree
     * @return a branching scheme implementing the sequential search
     * @see Sequencer
     */
    public static Supplier<Procedure[]> and(Supplier<Procedure[]>... choices) {
        return new Sequencer(choices);
    }

    /**
     * Limited Discrepancy Search combinator
     * that limits the number of right decisions
     * @param branching a branching scheme
     * @param maxDiscrepancy a discrepancy limit (non negative number)
     * @return a branching scheme that cuts off any path accumulating
     *         a discrepancy beyond the limit maxDiscrepancy
     * @see LimitedDiscrepancyBranching
     */
    public static Supplier<Procedure[]> limitedDiscrepancy(Supplier<Procedure[]> branching, int maxDiscrepancy) {
        return new LimitedDiscrepancyBranching(branching, maxDiscrepancy);
    }
}
