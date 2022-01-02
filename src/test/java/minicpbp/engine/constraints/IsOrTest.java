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

public class IsOrTest extends SolverTest {

    @Test
    public void isOr1() {
        try {

            Solver cp = solverFactory.get();
            BoolVar[] x = new BoolVar[]{makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp)};
            BoolVar b = makeBoolVar(cp);
            cp.post(new IsOr(b, x),true);

            for (BoolVar xi : x) {
                assertTrue(!xi.isBound());
            }

            cp.getStateManager().saveState();
            cp.post(equal(x[1], 0),true);
            cp.post(equal(x[2], 0),true);
            cp.post(equal(x[3], 0),true);
            assertTrue(!b.isBound());
            cp.post(equal(x[0], 0),true);
            assertTrue(b.isFalse());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(x[1], 0),true);
            cp.post(equal(x[2], 1),true);
            assertTrue(b.isTrue());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(b, 1),true);
            cp.post(equal(x[1], 0),true);
            cp.post(equal(x[2], 0),true);
            assertTrue(!x[0].isBound());
            cp.post(equal(x[3], 0),true);
            assertTrue(x[0].isTrue());
            cp.getStateManager().restoreState();


            cp.getStateManager().saveState();
            cp.post(equal(b, 0),true);
            assertTrue(x[0].isFalse());
            assertTrue(x[1].isFalse());
            assertTrue(x[2].isFalse());
            assertTrue(x[3].isFalse());
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @Test
    public void isOr2() {
        try {
            Solver cp = solverFactory.get();
            BoolVar[] x = new BoolVar[]{makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp)};
            BoolVar b = makeBoolVar(cp);
            cp.post(new IsOr(b, x),true);

            DFSearch dfs = makeDfs(cp, firstFail(x));

            dfs.onSolution(() -> {
                        int nTrue = 0;
                        for (BoolVar xi : x) {
                            if (xi.isTrue()) nTrue++;
                        }
                        assertTrue((nTrue > 0 && b.isTrue()) || (nTrue == 0 && b.isFalse()));
                    }
            );

            SearchStatistics stats = dfs.solve();
            assertEquals(16, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
