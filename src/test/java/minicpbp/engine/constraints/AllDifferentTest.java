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
import minicpbp.util.NotImplementedExceptionAssume;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.exception.NotImplementedException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static minicpbp.cp.BranchingScheme.firstFail;
import static minicpbp.cp.Factory.*;
import static org.junit.Assert.*;


public class AllDifferentTest extends SolverTest {

    @Test
    public void allDifferentTest1() {

        Solver cp = solverFactory.get();

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            cp.post(allDifferent(x),true);
            cp.post(equal(x[0], 0),true);
            for (int i = 1; i < x.length; i++) {
                assertEquals(4, x[i].size());
                assertEquals(1, x[i].min());
            }

        } catch (InconsistencyException e) {
            assert (false);
        }
    }


    @Test
    public void allDifferentTest2() {

        Solver cp = solverFactory.get();

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            cp.post(allDifferent(x),true);

            SearchStatistics stats = makeDfs(cp, firstFail(x)).solve();
            assertEquals(120, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            assert (false);
        }
    }


}
