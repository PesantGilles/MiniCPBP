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

public class IsLessOrEqualVarTest extends SolverTest {

    @Test
    public void test1() {
        try {


            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, 0, 5);
            IntVar y = makeIntVar(cp, 0, 5);

            BoolVar b = makeBoolVar(cp);

            cp.post(new IsLessOrEqualVar(b, x, y),true);

            DFSearch search = makeDfs(cp, firstFail(x, y));

            SearchStatistics stats = search.solve();

            search.onSolution(() ->
                    assertTrue(x.min() <= y.min() && b.isTrue() || x.min() > y.min() && b.isFalse())
            );

            assertEquals(36, stats.numberOfSolutions());

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
            IntVar x = makeIntVar(cp, -8, 7);
            IntVar y = makeIntVar(cp, -4, 3);

            BoolVar b = makeBoolVar(cp);

            cp.post(new IsLessOrEqualVar(b, x, y),true);

            cp.getStateManager().saveState();
            b.assign(1);
            cp.fixPoint();
            assertEquals(3, x.max());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            b.assign(0);
            cp.fixPoint();
            assertEquals(-3, x.min());
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
            IntVar y = makeIntVar(cp, 0, 7);
            x.assign(-2);

            BoolVar b = makeBoolVar(cp);
            cp.post(new IsLessOrEqualVar(b, x, y),true);
            assertTrue(b.isTrue());


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
            cp.post(new IsLessOrEqual(b, x, -2),true);
            assertEquals(-2, x.max());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            b.assign(0);
            cp.post(new IsLessOrEqual(b, x, -2),true);
            assertEquals(-1, x.min());
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
