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

import java.util.Arrays;
import java.util.HashSet;

import static minicpbp.cp.BranchingScheme.firstFail;
import static minicpbp.cp.Factory.makeDfs;
import static minicpbp.cp.Factory.makeIntVar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Element1DVarTest extends SolverTest {

    private static IntVar makeIVar(Solver cp, Integer... values) {
        return makeIntVar(cp, new HashSet<>(Arrays.asList(values)));
    }

    @Test
    public void element1dVarTest1() {

        try {

            Solver cp = solverFactory.get();
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, 2, 40);

            IntVar[] T = new IntVar[]{makeIntVar(cp, 9, 9), makeIntVar(cp, 8, 8), makeIntVar(cp, 7, 7), makeIntVar(cp, 5, 5), makeIntVar(cp, 6, 6)};

            cp.post(new Element1DVar(T, y, z),true);

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
    public void element1dVarTest2() {

        try {

            Solver cp = solverFactory.get();
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -4, 40);

            IntVar[] T = new IntVar[]{makeIntVar(cp, 1, 2),
                    makeIntVar(cp, 3, 4),
                    makeIntVar(cp, 5, 6),
                    makeIntVar(cp, 7, 8),
                    makeIntVar(cp, 9, 10)};

            cp.post(new Element1DVar(T, y, z),true);

            assertEquals(0, y.min());
            assertEquals(4, y.max());

            assertEquals(1, z.min());
            assertEquals(10, z.max());

            y.removeAbove(2);
            cp.fixPoint();

            assertEquals(6, z.max());

            y.assign(2);
            cp.fixPoint();

            assertEquals(5, z.min());
            assertEquals(6, z.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void element1dVarTest3() {

        try {

            Solver cp = solverFactory.get();
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -20, 40);

            IntVar[] T = new IntVar[]{makeIntVar(cp, 9, 9), makeIntVar(cp, 8, 8), makeIntVar(cp, 7, 7), makeIntVar(cp, 5, 5), makeIntVar(cp, 6, 6)};

            cp.post(new Element1DVar(T, y, z),true);

            DFSearch dfs = makeDfs(cp, firstFail(y, z));
            dfs.onSolution(() ->
                    assertEquals(T[y.min()].min(), z.min())
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
    public void element1dVarTest4() {

        try {

            Solver cp = solverFactory.get();
            IntVar x0 = makeIVar(cp, 0, 1, 5);
            IntVar x1 = makeIVar(cp, -5, -4, -3, -2, 0, 1, 5);
            IntVar x2 = makeIVar(cp, -2, 0);


            cp.post(new Element1DVar(new IntVar[]{x0}, x1, x2),true);

            assertEquals(0, x0.min());
            assertEquals(0, x1.min());
            assertEquals(0, x2.min());
            assertEquals(0, x0.max());
            assertEquals(0, x1.max());
            assertEquals(0, x2.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
