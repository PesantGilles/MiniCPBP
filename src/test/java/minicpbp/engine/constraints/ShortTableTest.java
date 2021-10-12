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

package minicpbp.engine.constraints;

import minicpbp.engine.SolverTest;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.SearchStatistics;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.exception.NotImplementedException;
import minicpbp.util.NotImplementedExceptionAssume;
import org.junit.Assume;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static minicpbp.cp.BranchingScheme.firstFail;
import static minicpbp.cp.Factory.*;
import static org.junit.Assert.*;

public class ShortTableTest extends SolverTest {


    private int[][] randomTuples(Random rand, int arity, int nTuples, int minvalue, int maxvalue) {
        int[][] r = new int[nTuples][arity];
        for (int i = 0; i < nTuples; i++)
            for (int j = 0; j < arity; j++)
                r[i][j] = rand.nextInt(maxvalue - minvalue) + minvalue;
        return r;
    }

    @Test
    public void simpleTest0() {
        try {

            Solver cp = solverFactory.get();
            IntVar[] x = makeIntVarArray(cp, 2, 1);
            int[][] table = new int[][]{{0, 0}};
            cp.post(new ShortTableCT(x, table, -1),true);

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            Assume.assumeNoException(e);
        }
    }


    @Test
    public void simpleTest3() {

        try {
            Solver cp = solverFactory.get();
            IntVar[] x = makeIntVarArray(cp, 3, 12);
            int[][] table = new int[][]{{0, 0, 2},
                    {3, 5, 7},
                    {6, 9, 10},
                    {1, 2, 3}};
            cp.post(new ShortTableCT(x, table, 0),true);

            assertEquals(12, x[0].size());
            assertEquals(12, x[1].size());
            assertEquals(4, x[2].size());

            assertEquals(0, x[0].min());
            assertEquals(11, x[0].max());
            assertEquals(0, x[1].min());
            assertEquals(11, x[1].max());
            assertEquals(2, x[2].min());
            assertEquals(10, x[2].max());


        } catch (InconsistencyException e) {
            fail("should not fail");

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void randomTest() {
        Random rand = new Random(67292);

        for (int i = 0; i < 50; i++) {
            int[][] tuples1 = randomTuples(rand, 3, 50, 2, 8);
            int[][] tuples2 = randomTuples(rand, 3, 50, 1, 7);
            int[][] tuples3 = randomTuples(rand, 3, 50, 0, 6);
            int star = 3;
            try {
                testTable(tuples1, tuples2, tuples3, star);
            } catch (NotImplementedException e) {
                Assume.assumeNoException(e);
            }
        }
    }


    public void testTable(int[][] t1, int[][] t2, int[][] t3, int star) {

        SearchStatistics statsDecomp;
        SearchStatistics statsAlgo;

        try {
            Solver cp = solverFactory.get();
            IntVar[] x = makeIntVarArray(cp, 5, 9);
            cp.post(allDifferent(x),true);
            cp.post(new ShortTableDecomp(new IntVar[]{x[0], x[1], x[2]}, t1, star),true);
            cp.post(new ShortTableDecomp(new IntVar[]{x[2], x[3], x[4]}, t2, star),true);
            cp.post(new ShortTableDecomp(new IntVar[]{x[0], x[2], x[4]}, t3, star),true);
            statsDecomp = makeDfs(cp, firstFail(x)).solve();
        } catch (InconsistencyException e) {
            statsDecomp = null;
        }

        try {
            Solver cp = solverFactory.get();
            IntVar[] x = makeIntVarArray(cp, 5, 9);
            cp.post(allDifferent(x),true);
            cp.post(new ShortTableCT(new IntVar[]{x[0], x[1], x[2]}, t1, star),true);
            cp.post(new ShortTableCT(new IntVar[]{x[2], x[3], x[4]}, t2, star),true);
            cp.post(new ShortTableCT(new IntVar[]{x[0], x[2], x[4]}, t3, star),true);
            statsAlgo = makeDfs(cp, firstFail(x)).solve();
        } catch (InconsistencyException e) {
            statsAlgo = null;
        }

        assertTrue((statsDecomp == null && statsAlgo == null) || (statsDecomp != null && statsAlgo != null));
        if (statsDecomp != null) {
            assertEquals(statsDecomp.numberOfSolutions(), statsAlgo.numberOfSolutions());
            assertEquals(statsDecomp.numberOfFailures(), statsAlgo.numberOfFailures());
            assertEquals(statsDecomp.numberOfNodes(), statsAlgo.numberOfNodes());
        }
    }

    /**
     * The table should accept all values of x0 and x1. However, it prunes off
     * some values of x1.
     */
    @Test
    public void minicpReplayShortTableCtIsStrongerThanAc() {

        try {
            final int star = 2147483647;

            // This table should accept all values.
            final int[][] table = {
                    {2147483647, 2147483647}
            };

            Solver cp = solverFactory.get();

            final IntVar x0 = makeIntVar(cp, new HashSet<>(Arrays.asList(0)));
            final IntVar x1 = makeIntVar(cp, new HashSet<>(Arrays.asList(-1, 2)));


            cp.post(new ShortTableCT(new IntVar[]{x0, x1}, table, star),true);

            assertEquals(1, x0.size());
            assertEquals(2, x1.size());

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test(expected = InconsistencyException.class)
    public void issue13() {

        try {
            final int star = -2147483648;

            // This table should accept all values.
            final int[][] table = {{0, 0}};

            Solver cp = solverFactory.get();
            final IntVar x0 = makeIntVar(cp, new HashSet<>(Arrays.asList(-5)));
            final IntVar x1 = makeIntVar(cp, new HashSet<>(Arrays.asList(-5)));

            cp.post(new ShortTableCT(new IntVar[]{x0, x1}, table, star),true);

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void issue14() {

        try {

            final int arity = 2;
            final int star = 2147483647;
            final int[][] table = {
                    {2147483647, 2147483647} // means *, *
            };

            Solver cp = solverFactory.get();
            IntVar x0 = makeIntVar(cp, new HashSet<>(Arrays.asList(0)));
            IntVar x1 = makeIntVar(cp, new HashSet<>(Arrays.asList(-1, 2)));

            IntVar y = makeIntVar(cp, new HashSet<>(Arrays.asList(0, 1)));
            IntVar z = makeIntVar(cp, new HashSet<>(Arrays.asList(3)));

            IntVar[] data = new IntVar[]{x0, x1};

            cp.post(new ShortTableCT(data, table, star),true);
            assertEquals(-1, data[1].min());

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
