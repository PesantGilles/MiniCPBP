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
import minicp.util.exception.NotImplementedException;

/**
 * Absolute value constraint
 */
public class Absolute extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;

    /**
     * Creates the absolute value constraint {@code y = |x|}.
     *
     * @param x the input variable such that its absolute value is equal to y
     * @param y the variable that represents the absolute value of x
     */
    public Absolute(IntVar x, IntVar y, IntVar[] vars) {
        super(vars);
        this.x = x;
        this.y = y;
    }

    public void post() {
        y.removeBelow(0);
        x.propagateOnBoundChange(this);
        y.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        // y = |x|
        if (x.isBound()) {
            y.assign(Math.abs(x.min()));
            setActive(false);
        } else if (y.isBound()) { // y is bound
            // y = |x|
            if (!x.contains(-y.min())) {
                x.assign(y.min());
            } else if (!x.contains(y.min())) {
                x.assign(-y.min());
            } else {
                // x can be (y or -y)
                // remove everything except y and -y from x
                for (int v = x.min(); v <= x.max(); v++) {
                    if (v != y.min() && v != -y.min()) {
                        x.remove(v);
                    }
                }
            }
            setActive(false);
        } else if (x.min() >= 0) {
            y.removeBelow(x.min());
            y.removeAbove(x.max());
            x.removeBelow(y.min());
            x.removeAbove(y.max());
        } else if (x.max() <= 0) {
            y.removeBelow(-x.max());
            y.removeAbove(-x.min());
            x.removeBelow(-y.max());
            x.removeAbove(-y.min());
        } else {
            int maxAbs = Math.max(x.max(), -x.min());
            y.removeAbove(maxAbs);
            x.removeAbove(y.max());
            x.removeBelow(-y.max());
            while (!x.contains(y.min()) && !x.contains(-y.min())) {
                y.remove(y.min());
            }
        }
    }

}
