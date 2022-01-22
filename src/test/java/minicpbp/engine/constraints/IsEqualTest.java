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
import minicpbp.engine.core.BoolVar;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.exception.NotImplementedException;
import minicpbp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import static minicpbp.cp.BranchingScheme.firstFail;
import static minicpbp.cp.Factory.*;
import static org.junit.Assert.*;


public class IsEqualTest extends SolverTest {

    @Test
    public void test1() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);

            BoolVar b = isEqual(x, -2);

            DFSearch search = makeDfs(cp, firstFail(x));

            SearchStatistics stats = search.solve();

            search.onSolution(() ->
                    assertEquals(-2 == x.min(), b.isTrue())
            );

            assertEquals(12, stats.numberOfSolutions());


        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test2() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);

            BoolVar b = isEqual(x, -2);

            cp.getStateManager().saveState();
            b.assign(1);
            cp.fixPoint();
            assertEquals(-2, x.min());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            b.assign(0);
            cp.fixPoint();
            assertFalse(x.contains(-2));
            cp.getStateManager().restoreState();

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test3() {

        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);
            x.assign(-2);

            {
                BoolVar b = makeBoolVar(cp);
                cp.post(new IsEqual(b, x, -2),true);
                assertTrue(b.isTrue());
            }
            {
                BoolVar b = makeBoolVar(cp);
                cp.post(new IsEqual(b, x, -3),true);
                assertTrue(b.isFalse());
            }

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test4() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);
            BoolVar b = makeBoolVar(cp);

            cp.getStateManager().saveState();
            b.assign(1);
            cp.post(new IsEqual(b, x, -2),true);
            assertEquals(-2, x.min());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            b.assign(0);
            cp.post(new IsEqual(b, x, -2),true);
            assertFalse(x.contains(-2));
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
