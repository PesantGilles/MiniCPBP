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
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.exception.NotImplementedException;
import minicpbp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import static minicpbp.cp.BranchingScheme.firstFail;
import static minicpbp.cp.Factory.makeDfs;
import static minicpbp.cp.Factory.makeIntVar;
import static org.junit.Assert.*;

public class Element1DTest extends SolverTest {

    @Test
    public void element1dTest1() {

        try {

            Solver cp = solverFactory.get();
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, 2, 40);

            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z),true);

            assertEquals(0, y.min());
            assertEquals(4, y.max());


            assertEquals(5, z.min());
            assertEquals(9, z.max());

            z.removeAbove(7);
            cp.fixPoint();

            assertEquals(2, y.min());


            y.remove(3);
            cp.fixPoint();

            assertEquals(7, z.max());
            assertEquals(6, z.min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void element1dTest2() {

        try {

            Solver cp = solverFactory.get();
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -20, 40);

            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z),true);

            DFSearch dfs = makeDfs(cp, firstFail(y, z));
            dfs.onSolution(() ->
                    assertEquals(T[y.min()], z.min())
            );
            SearchStatistics stats = dfs.solve();

            assertEquals(5, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void element1dTest3() {
        try {

            Solver cp = solverFactory.get();
            IntVar y = makeIntVar(cp, 0, 4);
            IntVar z = makeIntVar(cp, 5, 9);


            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z),true);

            y.remove(3); //T[4]=5
            y.remove(0); //T[0]=9

            cp.fixPoint();

            assertEquals(6, z.min());
            assertEquals(8, z.max());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void element1dTest4() {

        try {

            Solver cp = solverFactory.get();
            IntVar y = makeIntVar(cp, 0, 4);
            IntVar z = makeIntVar(cp, 5, 9);


            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z),true);

            z.remove(9); //new max is 8
            z.remove(5); //new min is 6
            cp.fixPoint();

            assertFalse(y.contains(0));
            assertFalse(y.contains(3));
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
