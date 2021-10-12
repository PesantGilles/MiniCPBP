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

package minicpbp.state;

/**
 * Implementation of {@link StateBool} with trail strategy
 * @see Trailer
 * @see StateManager#makeStateBool(boolean)
 */
public class TrailBool implements StateBool {

    private final StateEntry restoreTrue = new StateEntry() {
        @Override
        public void restore() {
            v = true;
        }
    };

    private final StateEntry restoreFalse = new StateEntry() {
        @Override
        public void restore() {
            v = false;
        }
    };

    private boolean v;
    private Trailer trail;
    private long lastMagic;

    protected TrailBool(Trailer context, boolean initial) {
        this.trail = context;
        v = initial;
        lastMagic = context.getMagic() - 1;
    }

    private void trail() {
        long contextMagic = trail.getMagic();
        if (lastMagic != contextMagic) {
            lastMagic = contextMagic;
            if (v) trail.pushState(restoreTrue);
            else trail.pushState(restoreFalse);
        }
    }

    @Override
    public void setValue(boolean v) {
        if (v != this.v) {
            trail();
            this.v = v;
        }
    }

    @Override
    public boolean value() {
        return this.v;
    }


}
