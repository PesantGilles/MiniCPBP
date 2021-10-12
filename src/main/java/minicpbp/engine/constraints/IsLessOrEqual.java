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
import minicpbp.engine.core.IntVar;
import minicpbp.util.exception.NotImplementedException;

/**
 * Reified less or equal constraint.
 */
public class IsLessOrEqual extends AbstractConstraint { // b <=> x <= c

    private final BoolVar b;
    private final IntVar x;
    private final int v;

    /**
     * Creates a constraint that
     * link a boolean variable representing
     * whether one variable is less or equal to the given constant.
     *
     * @param b a boolean variable that is true if and only if
     *          x takes a value less or equal to c
     * @param x the variable
     * @param c the constant
     * @see minicpbp.cp.Factory#isLessOrEqual(IntVar, int)
     */
    public IsLessOrEqual(BoolVar b, IntVar x, int c) {
        super(b.getSolver(), new IntVar[]{b, x});
        setName("IsLessOrEqual");
        this.b = b;
        this.x = x;
        this.v = c;
    }

    @Override
    public void post() {
        if (b.isTrue()) {
            x.removeAbove(v);
        } else if (b.isFalse()) {
            x.removeBelow(v + 1);
        } else if (x.max() <= v) {
            b.assign(1);
        } else if (x.min() > v) {
            b.assign(0);
        } else {
            b.whenBind(() -> {
                // should deactivate the constraint as it is entailed
                if (b.isTrue()) {
                    x.removeAbove(v);

                } else {
                    x.removeBelow(v + 1);
                }
            });
            x.whenBoundsChange(() -> {
                if (x.max() <= v) {
                    // should deactivate the constraint as it is entailed
                    b.assign(1);
                } else if (x.min() > v) {
                    // should deactivate the constraint as it is entailed
                    b.assign(0);
                }
            });
        }
    }

}
