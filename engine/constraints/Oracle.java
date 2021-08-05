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
 *
 * mini-cpbp, replacing classic propagation by belief propagation 
 * Copyright (c)  2019. by Gilles Pesant
 */

package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.util.*;

/**
 * Oracle unary constraint providing fixed marginals (possibly through ML)
 * Does not perform any filtering
 */
public class Oracle extends AbstractConstraint {
    private IntVar x;
    private double[] marginal;
    private int ofs;

    /**
     * @param x the variable
     * @param v the values
     * @param m the marginals for v
     * Note: any domain value not appearing in v will be assigned a zero marginal
     */
    public Oracle(IntVar x, int[] v, double[] m, IntVar[] vars) {
        super(vars);
	setName("Oracle");
	assert( v.length == m.length );
        this.x = x;
	ofs = x.min();
	marginal = new double[x.max() - ofs + 1];
	for(int i=0; i<marginal.length; i++){
	    marginal[i] = 0;
	}
	for(int i=0; i<v.length; i++){
	    if (x.contains(v[i])) {
		marginal[v[i]-ofs] = m[i];
	    }
	}
   	setExactWCounting(true);
    }

    @Override
    public void post() {
    }

    @Override
    public void propagate() {
    }

    @Override
    public void updateBelief() { 
	for (int val = x.min(); val <= x.max(); val++) {
	    if (x.contains(val)) {
		setLocalBelief(0,val,marginal[val-ofs]);
	    }
	}
    }
}
