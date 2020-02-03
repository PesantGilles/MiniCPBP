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

/**
 * Less or equal constraint between two variables
 */
public class LessOrEqual extends AbstractConstraint { // x <= y

    private final IntVar x;
    private final IntVar y;

    public LessOrEqual(IntVar x, IntVar y, IntVar[] vars) {
        super(vars);
	setName("LessOrEqual");
        this.x = x;
        this.y = y;
   	setExactWCounting(true);
    }

    @Override
    public void post() {
	switch(getSolver().getMode()) {
	case BP:
	    break; 
	case SP:
	case SBP:
	    x.propagateOnBoundChange(this);
	    y.propagateOnBoundChange(this);
	}
        propagate();
    }

    @Override
    public void propagate() {
        x.removeAbove(y.max());
        y.removeBelow(x.min());
        if (x.max() <= y.min())
            setActive(false);
    }

    @Override
    public void updateBelief() {
	double belief;
	int vx, vy;
	// Treatment of x
	belief = beliefRep.zero();
	vy = y.max();
	for (vx = x.max(); vx >= x.min(); vx--) {
	    if (x.contains(vx)) {
		while ((vx <= vy) && (vy >= y.min())) {
		    belief = beliefRep.add(belief,outsideBelief(1,vy));
		    do vy--; while ( !y.contains(vy) && (vy >= y.min()));
		}
		setLocalBelief(0,vx,belief);
	    }
	}
	// Treatment of y
	belief = beliefRep.zero();
	vx = x.min();
	for (vy = y.min(); vy <= y.max(); vy++) {
	    if (y.contains(vy)) {
		while ((vx <= vy) && (vx <= x.max())) {
		    belief = beliefRep.add(belief,outsideBelief(0,vx));
		    do vx++; while ( !x.contains(vx) && (vx <= x.max()));
		}
		setLocalBelief(1,vy,belief);
	    }
	}
    }
}
