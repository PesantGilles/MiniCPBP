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
import minicpbp.engine.core.BoolVar;
import minicpbp.engine.core.IntVar;


/**
 * Reified equality constraint
 *
 * @see minicpbp.cp.Factory#isEqual(IntVar, int)
 */
public class IsEqual extends AbstractConstraint { // b <=> x == c

    private final BoolVar b;
    private final IntVar x;
    private final int c;

    /**
     * Returns a boolean variable representing
     * whether one variable is equal to the given constant.
     *
     * @param x the variable
     * @param c the constant
     * @param b the boolean variable that is set to true
     *          if and only if x takes the value c
     * @see minicpbp.cp.Factory#isEqual(IntVar, int)
     */
    public IsEqual(BoolVar b, IntVar x, int c) {
        super(b.getSolver(), new IntVar[]{b, x});
        setName("IsEqual");
        this.b = b;
        this.x = x;
        this.c = c;
    }

    @Override
    public void post() {
        propagate();
	switch (getSolver().getMode()) {
	case BP:
	    break;
	case SP:
	case SBP:
	    if (isActive()) {
		x.propagateOnDomainChange(this);
		b.propagateOnBind(this);
	    }
	}
    }

    @Override
    public void propagate() {
        if (b.isTrue()) {
            x.assign(c);
            setActive(false);
        } else if (b.isFalse()) {
            x.remove(c);
            setActive(false);
        } else if (!x.contains(c)) {
            b.assign(false);
            setActive(false);
        } else if (x.isBound()) {
            b.assign(true);
            setActive(false);
        }
    }

    @Override
    public void updateBelief() {
        // Treatment of b
	    setLocalBelief(0, 1, outsideBelief(1, c));
	    setLocalBelief(0, 0, beliefRep.complement(outsideBelief(1, c)));
        // Treatment of x
        int nVal = x.fillArray(domainValues);
	    for (int k = 0; k < nVal; k++) {
	        setLocalBelief(1, domainValues[k], outsideBelief(0, 0));
	    }
	    if (x.contains(c)) { // set correctly for c
	        setLocalBelief(1, c, outsideBelief(0, 1));
	    }
    }

}
