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
 */

package minicpbp.examples;

import minicpbp.cp.Factory;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;

import java.util.Arrays;

import static minicpbp.cp.BranchingScheme.*;
import static minicpbp.cp.Factory.*;

/**
 * The Magic Series problem.
 * <a href="http://csplib.org/Problems/prob019/">CSPLib</a>.
 */
public class MagicSerie {
    public static void main(String[] args) {

        int n = 50;
        Solver cp = makeSolver(true);

        IntVar[] s = makeIntVarArray(cp, n, n);

        for (int i = 0; i < n; i++) {
            final int fi = i;
            cp.post(sum(Factory.makeIntVarArray(n, j -> isEqual(s[j], fi)), s[i]));
        }
        cp.post(sum(s, n));
        cp.post(sum(Factory.makeIntVarArray(n, i -> mul(s[i], i)), n));
        //cp.post(sum(makeIntVarArray(0,n-1,i -> mul(s[i],i-1)),0));

        long t0 = System.currentTimeMillis();
        DFSearch dfs = makeDfs(cp, () -> {
            IntVar sv = selectMin(s,
                    si -> si.size() > 1,
                    si -> -si.size());
            if (sv == null) return EMPTY;
            else {
                int v = sv.min();
                return branch(() -> equal(sv, v),
                        () -> notEqual(sv, v));
            }
        });

        dfs.onSolution(() ->
                System.out.println("solution:" + Arrays.toString(s))
        );

        SearchStatistics stats = dfs.solve();

        long t1 = System.currentTimeMillis();

        System.out.println(t1 - t0);

        System.out.format("#Solutions: %s\n", stats.numberOfSolutions());
        System.out.format("Statistics: %s\n", stats);

    }
}
