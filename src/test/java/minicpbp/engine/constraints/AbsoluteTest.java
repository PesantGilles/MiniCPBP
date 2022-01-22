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

import static minicpbp.cp.Factory.*;
import static org.junit.Assert.*;

public class AbsoluteTest extends SolverTest {
    @Test
    public void simpleTest0() {

        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 5);
            IntVar y = makeIntVar(cp, -10, 10);

            cp.post(new Absolute(x, y),true);

            assertEquals(0, y.min());
            assertEquals(5, y.max());
            assertEquals(11, x.size());

            x.removeAbove(-2);
            cp.fixPoint();

            assertEquals(2, y.min());

            x.removeBelow(-4);
            cp.fixPoint();

            assertEquals(4, y.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void simpleTest1() {
        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 5);
            IntVar y = makeIntVar(cp, -10, 10);
	    
	    x.remove(0);
	    x.remove(5);
	    x.remove(-5);


            cp.post(new Absolute(x, y),true);


            assertEquals(1, y.min());
            assertEquals(4, y.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void simpleTest2() {
        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 0);
            IntVar y = makeIntVar(cp, 4, 4);

            cp.post(new Absolute(x, y),true);

            assertTrue(x.isBound());
            assertTrue(y.isBound());
            assertEquals(-4, x.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void simpleTest3() {
        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, 7, 7);
            IntVar y = makeIntVar(cp, -1000, 12);

            cp.post(new Absolute(x, y),true);

            assertTrue(x.isBound());
            assertTrue(y.isBound());
            assertEquals(7, y.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void simpleTest4() {
        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 10);
            IntVar y = makeIntVar(cp, -6, 7);

            cp.post(new Absolute(x, y) ,true);

            assertEquals(7, x.max());
            assertEquals(-5, x.min());

	        y.remove(0);

            x.removeAbove(4);
            cp.fixPoint();

            assertEquals(5, y.max());

            x.removeAbove(-2);
            cp.fixPoint();

            assertEquals(2, y.min());

            y.removeBelow(5);
            cp.fixPoint();

            assertTrue(x.isBound());
            assertTrue(y.isBound());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
