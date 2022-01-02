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

import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.BoolVar;
import minicpbp.engine.core.Constraint;
import minicpbp.engine.core.IntVar;
import minicpbp.util.exception.NotImplementedException;

import static minicpbp.cp.Factory.lessOrEqual;
import static minicpbp.cp.Factory.plus;

/**
 * Reified is less or equal constraint {@code b <=> x <= y}.
 */
public class IsLessOrEqualVar extends AbstractConstraint {

    private final BoolVar b;
    private final IntVar x;
    private final IntVar y;

    private final Constraint lEqC;
    private final Constraint grC;

    /**
     * Creates a reified is less or equal constraint {@code b <=> x <= y}.
     *
     * @param b the truth value that will be set to true if {@code x <= y}, false otherwise
     * @param x left hand side of less or equal operator
     * @param y right hand side of less or equal operator
     */
    public IsLessOrEqualVar(BoolVar b, IntVar x, IntVar y) {
        super(b.getSolver(), new IntVar[]{b, x, y});
        setName("IsLessOrEqualVar");
        this.b = b;
        this.x = x;
        this.y = y;
        lEqC = lessOrEqual(x, y);
        grC = lessOrEqual(plus(y, 1), x);
    }

    @Override
    public void post() {
        x.propagateOnBoundChange(this);
        y.propagateOnBoundChange(this);
        b.propagateOnBind(this);
        propagate();
    }

    @Override
    public void propagate() {
        if (b.isTrue()) {
            getSolver().post(lEqC, false);
            setActive(false);
        } else if (b.isFalse()) {
            getSolver().post(grC, false);
            setActive(false);
        } else {
            if (x.max() <= y.min()) {
                b.assign(1);
                setActive(false);
            } else if (x.min() > y.max()) {
                b.assign(0);
                setActive(false);
            }
        }
    }

}
