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
import minicpbp.engine.core.Constraint;
import minicpbp.state.StateInt;
import minicpbp.cp.Factory;

/**
 * Reified equality constraint
 *
 * @see minicpbp.cp.Factory#isEqual(IntVar, IntVar)
 */
public class IsEqualVar extends AbstractConstraint { // b <=> x == y

    private final BoolVar b;
    private final IntVar x;
    private final IntVar y;

    private final Constraint eqC;
    private final Constraint neqC;

    private StateInt witness; // value common to both domains of x and y

    /**
     * Returns a boolean variable representing
     * whether one variable is equal to another.
     *
     * @param x the lhs variable
     * @param y the rhs variable
     * @param b the boolean variable that is set to true
     *          if and only if x is equal to y
     * @see minicpbp.cp.Factory#isEqualVar(IntVar, IntVar)
     */
    public IsEqualVar(BoolVar b, IntVar x, IntVar y) {
        super(b.getSolver(), new IntVar[]{b, x, y});
        setName("IsEqualVar");
        this.b = b;
        this.x = x;
        this.y = y;
        eqC = Factory.equal(x, y);
        neqC = Factory.notEqual(x, y);
        witness = getSolver().getStateManager().makeStateInt(Math.max(x.min(), y.min())); // smallest potential witness
	    setExactWCounting(true);
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
		        y.propagateOnDomainChange(this);
		        b.propagateOnBind(this);
	        }
	    }
    }

    @Override
    public void propagate() {
        if (b.isTrue()) {
            getSolver().post(eqC, false);
            setActive(false);
        } else if (b.isFalse()) {
            getSolver().post(neqC, false);
            setActive(false);
        } else if (!commonValue()) { // domains don't intersect
            b.assign(false);
            setActive(false);
        } else if (x.isBound() && y.isBound()) { // domains intersect and both singleton
            b.assign(true);
            setActive(false);
        }
    }

    private boolean commonValue() {
	    if (x.contains(witness.value()) && y.contains(witness.value()))
	        return true;
        for (int v = witness.value()+1; v <= Math.min(x.max(), y.max()); v++) {
	        if (x.contains(v) && y.contains(v)) {
		        witness.setValue(v);
		        return true;
	        }
	    }
	    return false;
    }

    @Override
    public void updateBelief() {
	    double beliefSAT;
	    int v;
        beliefSAT = beliefRep.zero(); // that x=y is satisfied
        // Treatment of x
        int nVal = x.fillArray(domainValues);
	    for (int k = 0; k < nVal; k++) {
	        v = domainValues[k];
	        if (y.contains(v)) {
                setLocalBelief(1, v, beliefRep.add( beliefRep.multiply(outsideBelief(2,v), outsideBelief(0,1)), beliefRep.multiply(beliefRep.complement(outsideBelief(2,v)), outsideBelief(0,0)) ));
		        beliefSAT = beliefRep.add(beliefSAT,beliefRep.multiply(outsideBelief(1,v),outsideBelief(2,v)));
	        } else
                setLocalBelief(1, v, outsideBelief(0,0));
	    }
        // Treatment of y
        nVal = y.fillArray(domainValues);
	    for (int k = 0; k < nVal; k++) {
	        v = domainValues[k];
	        if (x.contains(v))
                setLocalBelief(2, v, beliefRep.add( beliefRep.multiply(outsideBelief(1,v), outsideBelief(0,1)), beliefRep.multiply(beliefRep.complement(outsideBelief(1,v)), outsideBelief(0,0)) ));
	        else
                setLocalBelief(2, v, outsideBelief(0,0));
	    }
        // Treatment of b
	    setLocalBelief(0, 1, beliefSAT);
	    setLocalBelief(0, 0, beliefRep.complement(beliefSAT));
    }

}
