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

public class IsLessOrEqualTest extends SolverTest {

    @Test
    public void test1() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);

            BoolVar b = makeBoolVar(cp);

            cp.post(new IsLessOrEqual(b, x, 3),true);

            DFSearch search = makeDfs(cp, firstFail(x));

            search.onSolution(() ->
                    assertTrue(x.min() <= 3 && b.isTrue() || x.min() > 3 && b.isFalse())
            );

            SearchStatistics stats = search.solve();


            assertEquals(12, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test2() {

        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);

            BoolVar b = makeBoolVar(cp);

            cp.post(new IsLessOrEqual(b, x, -2),true);

            cp.getStateManager().saveState();
            cp.post(equal(b, 1),true);
            assertEquals(-2, x.max());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(b, 0),true);
            assertEquals(-1, x.min());
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
            cp.post(equal(x, -2),true);
            {
                BoolVar b = makeBoolVar(cp);
                cp.post(new IsLessOrEqual(b, x, -2),true);
                assertTrue(b.isTrue());
            }
            {
                BoolVar b = makeBoolVar(cp);
                cp.post(new IsLessOrEqual(b, x, -3),true);
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
            cp.post(equal(b, 1),true);
            cp.post(new IsLessOrEqual(b, x, -2),true);
            assertEquals(-2, x.max());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(b, 0),true);
            cp.post(new IsLessOrEqual(b, x, -2),true);
            assertEquals(-1, x.min());
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test5() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 10);
            BoolVar b = makeBoolVar(cp);

            cp.getStateManager().saveState();
            cp.post(new IsLessOrEqual(b, x, -6),true);
            assertTrue(b.isBound());
            assertTrue(b.isFalse());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(new IsLessOrEqual(b, x, 11),true);
            assertTrue(b.isBound());
            assertTrue(b.isTrue());
            cp.getStateManager().restoreState();

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test6() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, -3);
            BoolVar b = makeBoolVar(cp);

            cp.getStateManager().saveState();
            cp.post(new IsLessOrEqual(b, x, -3),true);
            assertTrue(b.isTrue());
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }



}
