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
import minicpbp.engine.core.IntVar;
import minicpbp.state.StateInt;
import minicpbp.util.exception.NotImplementedException;

import static minicpbp.cp.Factory.allDifferent;

/**
 * Hamiltonian Circuit Constraint with a successor model
 */
public class Circuit extends AbstractConstraint {

    private final IntVar[] x;
    private final StateInt[] dest;
    private final StateInt[] orig;
    private final StateInt[] lengthToDest;

    /**
     * Creates an Hamiltonian Circuit Constraint
     * with a successor model.
     *
     * @param x the variables representing the successor array that is
     *          {@code x[i]} is the city visited after city i
     */
    public Circuit(IntVar... x) {
        super(x[0].getSolver(), x);
        setName("Circuit");
        assert (x.length > 0);
        this.x = x;
        dest = new StateInt[x.length];
        orig = new StateInt[x.length];
        lengthToDest = new StateInt[x.length];
        for (int i = 0; i < x.length; i++) {
            dest[i] = getSolver().getStateManager().makeStateInt(i);
            orig[i] = getSolver().getStateManager().makeStateInt(i);
            lengthToDest[i] = getSolver().getStateManager().makeStateInt(0);
        }
    }


    @Override
    public void post() {
        getSolver().post(allDifferent(x));
        if (x.length == 1) {
            x[0].assign(0);
            return;
        }
        for (int i = 0; i < x.length; i++) {
            x[i].remove(i);
        }
        for (int i = 0; i < x.length; i++) {
            if (x[i].isBound()) bind(i);
            else {
                final int fi = i;
                x[i].whenBind(() -> bind(fi));
            }
        }
    }


    private void bind(int i) {
        int j = x[i].min();
        int origi = orig[i].value();
        int destj = dest[j].value();
        // orig[i] *-> i -> j *-> dest[j]
        dest[origi].setValue(destj);
        orig[destj].setValue(origi);
        int length = lengthToDest[origi].value()
                + lengthToDest[j].value() + 1;
        lengthToDest[origi].setValue(length);

        if (length < x.length - 1) {
            // avoid inner loops
            x[destj].remove(origi); // avoid inner loops
        }
    }

}
