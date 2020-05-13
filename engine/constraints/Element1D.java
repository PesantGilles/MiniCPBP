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


package minicp.engine.constraints;

import minicp.cp.Factory;
import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.util.exception.NotImplementedException;

/**
 *
 * Element Constraint modeling {@code array[y] = z}
 *
 */
public class Element1D extends AbstractConstraint {

    private final int[] t;
    private final IntVar y;
    private final IntVar z;
    
    /**
     * Creates an element constraint {@code array[y] = z}
     *
     * @param array the array to index
     * @param y the index variable
     * @param z the result variable
     */
    public Element1D(int[] array, IntVar y, IntVar z, IntVar[] vars) {
        super(vars);
	setName("Element1D");
        this.t = array;
        this.y = y;
        this.z = z;
    }

    @Override
    public void post() {
        int[][] t2 = new int[1][t.length];
        System.arraycopy(t, 0, t2[0], 0, t.length);
        Constraint c = new Element2D(t2, Factory.makeIntVar(getSolver(), 0, 0), y, z, new IntVar[]{y,z});
        getSolver().post(c, false);
    }

}
