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
import minicpbp.util.exception.NotImplementedException;
import minicpbp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import static minicpbp.cp.Factory.makeIntVar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class LessOrEqualTest extends SolverTest {


    @Test
    public void simpleTest0() {
        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 5);
            IntVar y = makeIntVar(cp, -10, 10);

            cp.post(new LessOrEqual(x, y),true);

            assertEquals(-5, y.min());

            y.removeAbove(3);
            cp.fixPoint();

            assertEquals(9, x.size());
            assertEquals(3, x.max());

            x.removeBelow(-4);
            cp.fixPoint();

            assertEquals(-4, y.min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
