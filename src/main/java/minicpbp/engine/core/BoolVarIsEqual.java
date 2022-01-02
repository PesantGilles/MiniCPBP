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

package minicpbp.engine.core;

import minicpbp.util.exception.InconsistencyException;

public class BoolVarIsEqual extends IntVarImpl implements BoolVar {

    public BoolVarIsEqual(IntVar x, int v) {
        super(x.getSolver(), 0, 1);

        if (!x.contains(v)) {
            assign(false);
        } else if (x.isBound() && x.min() == v) {
            assign(true);
        } else {

            this.whenBind(() -> {
                if (isTrue()) x.assign(v);
                else x.remove(v);
            });

            x.whenDomainChange(() -> {
                if (!x.contains(v)) {
                    this.assign(false);
                }
            });

            x.whenBind(() -> {
                if (x.min() == v) {
                    assign(true);
                } else {
                    assign(false);
                }
            });

        }

    }

    @Override
    public boolean isTrue() {
        return min() == 1;
    }

    @Override
    public boolean isFalse() {
        return max() == 0;
    }

    @Override
    public void assign(boolean b) throws InconsistencyException {
        assign(b ? 1 : 0);
    }
}
