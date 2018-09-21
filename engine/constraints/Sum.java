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
import minicp.engine.core.IntVar;
import minicp.engine.core.IntVarImpl;
import minicp.state.StateInt;
import minicp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Sum Constraint
 */
public class Sum extends AbstractConstraint {
    private int[] unBounds;
    private StateInt nUnBounds;
    private StateInt sumBounds;
    private IntVar[] x;
    private int n;

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == y}.
     *
     * @param x the non empty left hand side of the sum
     * @param y the right hand side of the sum
     */
    public Sum(IntVar[] x, IntVar y) {
        this(Arrays.copyOf(x, x.length + 1));
        this.x[x.length] = Factory.minus(y);
    }

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == y}.
     *
     * @param x the non empty left hand side of the sum
     * @param y the right hand side of the sum
     */
    public Sum(IntVar[] x, int y) {
        this(Arrays.copyOf(x, x.length + 1));
        this.x[x.length] = Factory.makeIntVar(getSolver(), -y, -y);
    }

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == 0}.
     *
     * @param x the non empty set of variables that should sum to zero
     */
    public Sum(IntVar[] x) {
        super(x[0].getSolver());
        this.x = x;
        this.n = x.length;
        nUnBounds = getSolver().getStateManager().makeStateInt(n);
        sumBounds = getSolver().getStateManager().makeStateInt(0);
        unBounds = IntStream.range(0, n).toArray();
    }

    @Override
    public void post() {
        for (IntVar var : x)
            var.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        // Filter the unbound vars and update the partial sum
        int nU = nUnBounds.value();
        int sumMin = sumBounds.value(), sumMax = sumBounds.value();
        for (int i = nU - 1; i >= 0; i--) {
            int idx = unBounds[i];
            sumMin += x[idx].min(); // Update partial sum
            sumMax += x[idx].max();
            if (x[idx].isBound()) {
                sumBounds.setValue(sumBounds.value() + x[idx].min());
                unBounds[i] = unBounds[nU - 1]; // Swap the variables
                unBounds[nU - 1] = idx;
                nU--;
            }
        }
        nUnBounds.setValue(nU);
        if (sumMin > 0 || sumMax < 0)
            throw new InconsistencyException();
        for (int i = nU - 1; i >= 0; i--) {
            int idx = unBounds[i];
            x[idx].removeAbove(-(sumMin - x[idx].min()));
            x[idx].removeBelow(-(sumMax - x[idx].max()));
        }
    }
}
