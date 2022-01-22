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
import minicpbp.util.exception.InconsistencyException;
import org.junit.Test;

import static minicpbp.cp.Factory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class NotEqualTest extends SolverTest {

    @Test
    public void notEqualTest() {
        Solver cp = solverFactory.get();

        IntVar x = makeIntVar(cp, 10);
        IntVar y = makeIntVar(cp, 10);

        try {
            cp.post(notEqual(x, y),true);

            x.assign(6);
            cp.fixPoint();

            assertFalse(y.contains(6));
            assertEquals(9, y.size());

        } catch (InconsistencyException e) {
            assert (false);
        }
        assertFalse(y.contains(6));
    }

}
