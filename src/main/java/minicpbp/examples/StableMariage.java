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

import minicpbp.engine.core.BoolVar;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;
import minicpbp.util.io.InputReader;

import java.util.Arrays;

import static minicpbp.cp.BranchingScheme.and;
import static minicpbp.cp.BranchingScheme.firstFail;
import static minicpbp.cp.Factory.*;

/**
 * Stable Marriage problem:
 * Given n men and n women, where each person has ranked makeIntVarArray members
 * of the opposite sex with a unique number between 1 and n in order of preference,
 * marry the men and women together such that there are no two people of opposite sex
 * who would both rather have each other than their current partners.
 * If there are no such people, makeIntVarArray the marriages are "stable".
 * <a href="http://en.wikipedia.org/wiki/Stable_marriage_problem">Wikipedia</a>.
 */
public class StableMariage {


    public static void main(String[] args) {


        // http://mathworld.wolfram.com/StableMarriageProblem.html
        // for each man, what is his ranking for the women (lower is better)
        InputReader reader = new InputReader("data/stable_mariage.txt");
        int n = reader.getInt();
        int[][] rankWomen = reader.getMatrix(n, n);
        int[][] rankMen = reader.getMatrix(n, n);

        // you should get six solutions:
        /*
        wife   :5,3,8,7,2,6,0,4,1
        husband:6,8,4,1,7,0,5,3,2

        wife   :5,4,8,7,2,6,0,3,1
        husband:6,8,4,7,1,0,5,3,2

        wife   :5,0,3,7,4,8,2,1,6
        husband:1,7,6,2,4,0,8,3,5

        wife   :5,0,3,7,4,6,2,1,8
        husband:1,7,6,2,4,0,5,3,8

        wife   :5,3,0,7,4,6,2,1,8
        husband:2,7,6,1,4,0,5,3,8

        wife   :6,4,8,7,2,5,0,3,1
        husband:6,8,4,7,1,5,0,3,2
        */


        Solver cp = makeSolver();

        // wife[m] is the woman chosen for man m
        IntVar[] wife = makeIntVarArray(cp, n, n);
        // husband[w] is the man chosen for woman w
        IntVar[] husband = makeIntVarArray(cp, n, n);

        // wifePref[m] is the preference for the woman chosen for man m
        IntVar[] wifePref = makeIntVarArray(cp, n, n + 1);
        // husbandPref[w] is the preference for the man chosen for woman w
        IntVar[] husbandPref = makeIntVarArray(cp, n, n + 1);


        for (int m = 0; m < n; m++) {
            // the husband of the wife of man m is m
            // TODO: model this with Element1DVar
            

            // TODO: model this with Element1D: rankWomen[m][wife[m]] == wifeFref[m]
            

        }

        for (int w = 0; w < n; w++) {
            // the wife of the husband of woman i is i
            // TODO: model this with Element1DVar
            

            // TODO: model this with Element1D: rankMen[w][husband[w]] == husbandPref[w]
            
        }

        for (int m = 0; m < n; m++) {
            for (int w = 0; w < n; w++) {
                // if m prefers w than his wife, the opposite is not true i.e. w prefers her own husband than m
                // (wifePref[m] > rankWomen[m][w]) => (husbandPref[w] < rankMen[w][m])

                BoolVar mPrefersW = isLarger(wifePref[m], rankWomen[m][w]);
                BoolVar wDont = isLess(husbandPref[w], rankMen[w][m]);
                cp.post(implies(mPrefersW, wDont));

                // if w prefers m than her husband, the opposite is not true i.e. m prefers his own woman than w
                // (husbandPref[w] > rankMen[w][m]) => (wifePref[m] < rankWomen[m][w])
                // TODO: model this constraint
                

            }
        }


        DFSearch dfs = makeDfs(cp, and(firstFail(wife), firstFail(husband)));

        dfs.onSolution(() -> {
                    System.out.println(Arrays.toString(wife));
                    System.out.println(Arrays.toString(husband));
                }
        );


        SearchStatistics stats = dfs.solve();
        System.out.println(stats);

    }

    /**
     * Model the reified logical implication constraint
     * @param b1 left hand side of the implication
     * @param b2 right hand side of the implication
     * @return a boolean variable that is true if and only if
     *         the relation "b1 implies b2" is true, false otehrwise.
     */
    private static BoolVar implies(BoolVar b1, BoolVar b2) {
        IntVar notB1 = plus(minus(b1), 1);
        return isLargerOrEqual(sum(notB1, b2), 1);
    }
}

