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

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import static minicp.cp.Factory.*;

    /**
     * Creates a cardinality constraint.
     * This relation is currently enforced by decomposing it into {@link Among} constraints; hence it is not domain consistent
     *
     * @param x an array of variables
     * @param vals an array of values whose occurrences in x we count
     * @param o an array of variables corresponding to the number of occurrences of vals in x
     * @return a cardinality constraint
     */
public class Cardinality extends AbstractConstraint {

    private IntVar[] x;
    private int[] vals;
    private IntVar[] o;

    public Cardinality(IntVar[] x, int[] vals, IntVar[] o) {
        super(new IntVar[]{makeIntVar(x[0].getSolver(),0,0)}); // not a real constraint so we want a minimal footprint in the superclass' constructor
        this.x = x;
	this.vals = vals;
	this.o = o;
    }

    @Override
    public void post() {
        Solver cp = x[0].getSolver();
        for (int i = 0; i < vals.length; i++) {
                cp.post(among(x,vals[i],o[i]));
	}
    }

}
