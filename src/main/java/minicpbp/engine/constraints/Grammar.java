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

package minicpbp.engine.constraints;

import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;
import minicpbp.util.CFG;
import minicpbp.util.Production;
import minicpbp.util.exception.InconsistencyException;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * Grammar Constraint
 */
public class Grammar extends AbstractConstraint {
    private IntVar[] x;
    private CFG g;
    private int n;
    private Set<Integer>[] V;
    private Set<Integer>[] flags;
    private Set<Integer> supportedVals;
    private double[][] belief; // probabilistic weight of each nonterminal symbol at each idx

    /**
     * Creates a grammar constraint.
     * <p> This constraint holds iff
     * {@code x is a word recognized by the context-free grammar}.
     *
     * @param x an array of variables
     * @param g a context-free grammar
     *
     * (code adapted from that of Claude-Guy Quimper)
     * NOTE: The grammar must be in its Chomsky form
     */
    public Grammar(IntVar[] x, CFG g) {
        super(x[0].getSolver(), x);
        setName("Grammar");
        this.x = x;
        n = x.length;
        this.g = g;
        V = new Set[n*(n+1)/2];
        flags = new Set[n*(n+1)/2];
        belief = new double[n*(n+1)/2][g.nonTerminalCount()];
        for (int i = 0; i < V.length; i++) {
            V[i] = new HashSet<Integer>();
            flags[i] = new HashSet<Integer>();
        }
        supportedVals = new HashSet<Integer>();

        setExactWCounting(false); // our weighted counting algorithm is exact for unambiguous grammars (where there is one-to-one correspondence between parse trees and words belonging to the language) but determining ambiguity is undecidable in general
    }

    @Override
    public void post() {
        switch (getSolver().getMode()) {
            case BP: // won't work without fixpoint() because we assume cleaned up flags table
                throw new InvalidParameterException("Grammar constraint will not work properly in Solver.PropaMode.BP");
            case SBP:
            case SP:
                for (IntVar var : x)
                    var.propagateOnDomainChange(this);
        }
        propagate();
    }

    int idx(int i, int j) {
        return (j - 1) * (2 * n - j + 2) / 2 + i;
    }

    @Override
    public void propagate() {
        // based on CYK algorithm
        // Clear tables V and flags
        for (int i = 0; i < n; i++) {
            for (int j = 1; j + i <= n; j++) {
                V[idx(i, j)].clear();
                flags[idx(i, j)].clear();
            }
        }
        // Initialize bottom row of table V
        for (int pIt = 0; pIt < g.length1productionCount(); pIt++) {
            Production p = g.length1productions()[pIt];
            for (int i = 0; i < n; i++) {
                if (x[i].contains(p.right()[0])) {
                    V[idx(i, 1)].add(p.left());
                }
            }
        }
        // Compute possible productions
        for (int j = 2; j <= n; j++) {
            for (int i = 0; i < n - j + 1; i++) {
                for (int k = 1; k < j; k++) {
                    Iterator<Integer> itr = V[idx(i, k)].iterator();
                    while (itr.hasNext()) { // for each nonterminal in table V at that index
                        Iterator<Production> itr2 = g.rhs2prod()[itr.next() - g.terminalCount()].iterator();
                        while (itr2.hasNext()) { // for each length-2 production with that nonterminal as 1st one on rhs
                            Production p = itr2.next();
                            if (V[idx(i + k, j - k)].contains(p.right()[1])) {
                                V[idx(i, j)].add(p.left()); // Production p can be applied from i over j characters
                                //                        System.out.println("pos "+i+"; length "+j+"; nonterminal "+p.left());
                            }
                        }
                    }
                }
            }
        }
        // Check if the constraint is satisfiable
        if (V[idx(0, n)].contains(g.terminalCount())) { // Can S be produced from 0 over n characters?
            flags[idx(0, n)].add(g.terminalCount());
        } else {
            throw new InconsistencyException();
        }
        // Backtrack in the table and flag admissible productions
        for (int j = n; j > 1; j--) {
            for (int i = 0; i <= n - j; i++) {
                Iterator<Integer> itr = flags[idx(i, j)].iterator();
                while (itr.hasNext()) { // for each flagged nonterminal at that index
                    Iterator<Production> itr2 = g.lhs2prod()[itr.next() - g.terminalCount()].iterator();
                    while (itr2.hasNext()) { // for each length-2 production with that nonterminal as lhs
                        Production p = itr2.next();
                        for (int k = 1; k < j; k++) {
                            if (V[idx(i, k)].contains(p.right()[0]) && V[idx(i + k, j - k)].contains(p.right()[1])) {
                                flags[idx(i, k)].add(p.right()[0]);
                                flags[idx(i + k, j - k)].add(p.right()[1]);
                            }
                        }
                    }
                }
            }
        }
        // Filter out unsupported domain values
        for (int i = 0; i < n; i++) {
            supportedVals.clear();
            Iterator<Integer> itr = flags[idx(i, 1)].iterator();
            while (itr.hasNext()) { // for each flagged nonterminal in position i
                Iterator<Production> itr2 = g.lhs1prod()[itr.next() - g.terminalCount()].iterator();
                while (itr2.hasNext()) { // for each length-1 production with that nonterminal as lhs
                    supportedVals.add(itr2.next().right()[0]);
                }
            }
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                if (!supportedVals.contains(v)) {
                    x[i].remove(v);
                }
            }
        }
    }

    @Override
    public void updateBelief() {
        // after fixpoint() has been called, flags contains the nonterminals involved in some derivation tree
        // Clear table of beliefs
        for (int i = 0; i < n; i++) {
            for (int j = 1; j + i <= n; j++) {
                Arrays.fill(belief[idx(i, j)], beliefRep.zero());
            }
        }
        // Initialize bottom row
        for (int pIt = 0; pIt < g.length1productionCount(); pIt++) {
            Production p = g.length1productions()[pIt];
            for (int i = 0; i < n; i++) {
                if (flags[idx(i, 1)].contains(p.left())) {
                    belief[idx(i, 1)][p.left() - g.terminalCount()] = beliefRep.add(belief[idx(i, 1)][p.left() - g.terminalCount()], outsideBelief(i, p.right()[0]));
                }
            }
        }
        // Move up the rows, accumulating beliefs
        for (int j = 2; j <= n; j++) {
            for (int i = 0; i < n - j + 1; i++) {
                for (int k = 1; k < j; k++) {
                    Iterator<Integer> itr = flags[idx(i, k)].iterator();
                    while (itr.hasNext()) { // for each flagged nonterminal at that index
                        Iterator<Production> itr2 = g.rhs2prod()[itr.next() - g.terminalCount()].iterator();
                        while (itr2.hasNext()) { // for each length-2 production with that nonterminal as 1st one on rhs
                            Production p = itr2.next();
                            if (flags[idx(i + k, j - k)].contains(p.right()[1])) {
                                belief[idx(i, j)][p.left() - g.terminalCount()] = beliefRep.add(belief[idx(i, j)][p.left() - g.terminalCount()], beliefRep.multiply(belief[idx(i, k)][p.right()[0] - g.terminalCount()], belief[idx(i + k, j - k)][p.right()[1] - g.terminalCount()]));
                                //   System.out.println("pos " + i + "; length " + j + "; nonterminal " + p.left() + " rewritten as " + p.right()[0] + " at " + i + ";" + k + " and " + p.right()[1] + " at " + (i + k) + ";" + (j - k));
                            }
                        }
                    }
                }
            }
        }
 //       System.out.println("belief at top is " + belief[idx(0, n)][0]);
        // Clear local beliefs
        for (int i = 0; i < n; i++) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                setLocalBelief(i, domainValues[j], beliefRep.zero());
            }
        }
        // For each derivation tree, go down each path from root to leaf, taking the product of beliefs that branch off that path, and add the result to the local belief of the corresponding var-val pair
        dive(g.terminalCount(), n, 0, beliefRep.one());
    }

    private void dive(int nonTerminal, int len, int pos, double product) {
        if (len == 1) { // base case: bottom row
 //           System.out.println("reached bottom for pos "+pos+" and nonterminal "+nonTerminal+" with product "+product);
            Iterator<Production> itr2 = g.lhs1prod()[nonTerminal - g.terminalCount()].iterator();
            while (itr2.hasNext()) { // for each length-1 production with nonTerminal as lhs
                Production p = itr2.next();
                setLocalBelief(pos, p.right()[0], beliefRep.add(localBelief(pos, p.right()[0]), product));
            }
            return;
        }
        Iterator<Production> itr2 = g.lhs2prod()[nonTerminal - g.terminalCount()].iterator();
        while (itr2.hasNext()) { // for each length-2 production with nonTerminal as lhs
            Production p = itr2.next();
            for (int k = 1; k < len; k++) {
                if (flags[idx(pos, k)].contains(p.right()[0]) && flags[idx(pos + k, len - k)].contains(p.right()[1])) {
                    dive(p.right()[0], k, pos, beliefRep.multiply(product,belief[idx(pos + k, len - k)][p.right()[1] - g.terminalCount()]));
                    dive(p.right()[1], len - k, pos + k, beliefRep.multiply(product,belief[idx(pos, k)][p.right()[0] - g.terminalCount()]));
                }
            }
        }
    }

}
