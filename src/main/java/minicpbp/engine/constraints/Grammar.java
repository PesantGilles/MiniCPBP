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
        for (int pIt = 0; pIt < g.productionCount(); pIt++) {
            Production p = g.productions()[pIt];
            if (p.length() == 1) {
                for (int i = 0; i < n; i++) {
                    if (x[i].contains(p.right()[0])) {
                        V[idx(i, 1)].add(p.left());
                    }
                }
            }
        }
        // Compute possible productions
        for (int j = 2; j <= n; j++) {
            for (int pIt = 0; pIt < g.productionCount(); pIt++) {
                Production p = g.productions()[pIt];
                assert (1 <= p.length() && p.length() <= 2); // The grammar must be in its Chomsky form
                if (p.length() == 2) {
                    for (int i = 0; i < n - j + 1; i++) {
                        for (int k = 1; k < j; k++) {
                            if (V[idx(i, k)].contains(p.right()[0]) && V[idx(i + k, j - k)].contains(p.right()[1])) {
                                V[idx(i, j)].add(p.left()); // Production p can be applied from i over j characters
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
                for (int pIt = 0; pIt < g.productionCount(); pIt++) {
                    Production p = g.productions()[pIt];
                    if ((p.length() == 2) && flags[idx(i, j)].contains(p.left())) {
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
            for (int pIt = 0; pIt < g.productionCount(); pIt++) {
                Production p = g.productions()[pIt];
                if ((p.length() == 1) && flags[idx(i, 1)].contains(p.left())) {
                    supportedVals.add(p.right()[0]);
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
}
