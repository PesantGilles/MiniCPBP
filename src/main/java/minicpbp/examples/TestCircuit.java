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

package minicpbp.examples;

import minicpbp.engine.constraints.Circuit;
import minicpbp.engine.constraints.Element1D;
import minicpbp.engine.constraints.LessOrEqual;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.LDSearch;
import minicpbp.search.Objective;
import minicpbp.util.io.InputReader;
import minicpbp.search.SearchStatistics;

import java.util.Random;

import static minicpbp.cp.BranchingScheme.*;
import static minicpbp.cp.Factory.*;

/**
 * Traveling salesman problem.
 * <a href="https://en.wikipedia.org/wiki/Travelling_salesman_problem">Wikipedia</a>.
 */
public class TestCircuit {
    public static void main(String[] args) {

        int n = 30;

        Solver cp = makeSolver(false);
        IntVar[] succ = makeIntVarArray(cp, n, n);

        Random rand = new Random();

        int count = 0;
        for (int i = 0; i < n; i++){
            succ[i].setName("succ["+i+"]");
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    if (rand.nextDouble() > 0.7) {
                        succ[i].remove(j);
                        count++;
                    }
                }
            }
        }


        System.out.println("Il y a " + ((n*(n-1) - count)/n) + " arêtes en moyenne par noeuds dans le graphe.");

        //Constraints
        cp.post(new Circuit(succ));
        // Add LessOrEqual Constraints

        count = 0;
        for (int i = 1; i < n-1; i++){
            for (int j = i+1; j < n-1; j++){
                if (rand.nextDouble() > 0.9){
                    count++;
                    if (rand.nextDouble() > 0.5)
                        cp.post(new LessOrEqual(succ[i], succ[j]));
                    else
                        cp.post(new LessOrEqual(succ[j], succ[i]));
                }
            }
        }
        for (int i = 0; i < n; i++){
            System.out.println(succ[i].getName()+succ[i].toString());
        }

        System.out.println("Il y a " + count + " contraintes LessOrEqual.");

//        cp.setTraceBPFlag(true);
        cp.setTraceSearchFlag(true);

        DFSearch dfs = makeDfs(cp, minEntropy(succ));
//        LDSearch dfs = makeLds(cp, minEntropy(succ));
//        cp.setMode(Solver.PropaMode.SP);
//        DFSearch dfs = makeDfs(cp, firstFailRandomVal(succ));

        dfs.onSolution(() -> {
            for (int i = 0; i < n; i++) {
                System.out.print(succ[i].min()+" ");
            }
            System.out.println();
        });

        //############# search ################
        SearchStatistics stats = dfs.solve(stat -> stat.numberOfSolutions() >= 1); // stop on 1st solution
//       SearchStatistics stats = dfs.solve();
        System.out.println(stats);

    }
}
