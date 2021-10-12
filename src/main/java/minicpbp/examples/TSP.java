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

import minicpbp.engine.constraints.Circuit;
import minicpbp.engine.constraints.Element1D;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.Objective;
import minicpbp.util.io.InputReader;

import static minicpbp.cp.BranchingScheme.firstFail;
import static minicpbp.cp.Factory.*;

/**
 * Traveling salesman problem.
 * <a href="https://en.wikipedia.org/wiki/Travelling_salesman_problem">Wikipedia</a>.
 */
public class TSP {
    public static void main(String[] args) {


        // instance gr17 https://people.sc.fsu.edu/~jburkardt/datasets/tsp/gr17_d.txt
        InputReader reader = new InputReader("data/tsp.txt");

        int n = reader.getInt();

        int[][] distanceMatrix = reader.getMatrix(n, n);

        Solver cp = makeSolver(false);
        IntVar[] succ = makeIntVarArray(cp, n, n);
        IntVar[] distSucc = makeIntVarArray(cp, n, 1000);

        cp.post(new Circuit(succ));

        for (int i = 0; i < n; i++) {
            cp.post(new Element1D(distanceMatrix[i], succ[i], distSucc[i]));
        }

        IntVar totalDist = sum(distSucc);

        Objective obj = cp.minimize(totalDist);

        //DFSearch dfs = makeDfs(cp, firstFail(succ));

        DFSearch dfs = makeDfs(cp, firstFail(succ));

        /*
        dfs.onSolution(() ->
                System.out.println(totalDist)
        );

        // take a while (optimum = 291)
        SearchStatistics stats = dfs.solve();

        System.out.println(stats);
        */

        // --- Large Neighborhood Search ---

        // Current best solution
        int[] succBest = new int[n];
        for (int i = 0; i < n; i++) {
            succBest[i] = i;
        }

        dfs.onSolution(() -> {
            // Update the current best solution
            for (int i = 0; i < n; i++) {
                succBest[i] = succ[i].min();
            }
            System.out.println("objective:" + totalDist.min());
        });

        dfs.optimize(obj);
        /*
        int nRestarts = 1000;
        int failureLimit = 100;
        Random rand = new java.util.Random(0);

        for (int i = 0; i < nRestarts; i++) {
            if (i%10==0)
                System.out.println("restart number #"+i);
            // Record the state such that the fragment constraints can be cancelled
            dfs.optimizeSubjectTo(obj,statistics -> false, //statistics.nFailures >= failureLimit,
                    () -> {
                        // Assign the fragment 5% of the variables randomly chosen
                        for (int j = 0; j < n; j++) {
                            if (rand.nextInt(100) < 10) {
                                equal(succ[j],succBest[j]);
                            }
                        }
                    });
        }
        */
    }
}
