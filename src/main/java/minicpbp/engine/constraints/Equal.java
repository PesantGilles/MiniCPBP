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

package minicpbp.engine.constraints;

import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;

public class Equal extends AbstractConstraint {
    private final IntVar x, y;


    /**
     * Creates a constraint such
     * that {@code x = y}
     *
     * @param x the left member
     * @param y the right memer
     * @see minicpbp.cp.Factory#equal(IntVar, IntVar)
     */
    public Equal(IntVar x, IntVar y) { // x == y
        super(x.getSolver(), new IntVar[]{x,y});
        setName("Equal");
        this.x = x;
        this.y = y;
        setExactWCounting(true);
    }

    @Override
    public void post() {
        if (y.isBound()) {
            x.assign(y.min());
	}
	else if (x.isBound()) {
            y.assign(x.min());
	}
        else {
            boundsIntersect();
            pruneEquals(y, x);
            pruneEquals(x, y);
	    switch (getSolver().getMode()) {
	    case BP:
		break;
	    case SP:
	    case SBP:
		x.whenDomainChange(() -> {
			boundsIntersect();
			pruneEquals(x, y);
		    });
		y.whenDomainChange(() -> {
			boundsIntersect();
			pruneEquals(y, x);
		    });
	    }
	}
    }
            
    // dom consistent filtering in the direction from -> to
    // every value of to has a support in from
    private void pruneEquals(IntVar from, IntVar to) {
        int nVal = to.fillArray(domainValues);
        for (int k = 0; k < nVal; k++)
            if (!from.contains(domainValues[k]))
                to.remove(domainValues[k]);
    }

    // make sure bound of variables are the same
    private void boundsIntersect() {
        int newMin = Math.max(x.min(), y.min());
        int newMax = Math.min(x.max(), y.max());
        x.removeBelow(newMin);
        x.removeAbove(newMax);
        y.removeBelow(newMin);
        y.removeAbove(newMax);
    }

    @Override
    public void updateBelief() {
        // Treatment of x
        int nVal = x.fillArray(domainValues);
        for (int k = 0; k < nVal; k++) {
	    int vx = domainValues[k];
            if (y.contains(vx))
		setLocalBelief(0, vx, outsideBelief(1, vx));
	    else
		setLocalBelief(0, vx, beliefRep.zero());
	}
        // Treatment of y
        nVal = y.fillArray(domainValues);
        for (int k = 0; k < nVal; k++) {
	    int vy = domainValues[k];
            if (x.contains(vy))
		setLocalBelief(1, vy, outsideBelief(0, vy));
	    else
		setLocalBelief(1, vy, beliefRep.zero());
	}
    }

}
