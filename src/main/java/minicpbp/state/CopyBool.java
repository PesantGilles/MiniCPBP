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
 * Implementation of {@link StateBool} with copy strategy
 * @see Copier
 * @see StateManager#makeStateBool(boolean)
 */
public class CopyBool implements Storage, StateBool {

    class CopyBoolStateEntry implements StateEntry {
        private final boolean v;

        CopyBoolStateEntry(boolean v) {
            this.v = v;
        }

        public void restore() {
            CopyBool.this.v = v;
        }
    }

    private boolean v;

    CopyBool(boolean initial) {
        v = initial;
    }

    @Override
    public void setValue(boolean v) {
        this.v = v;
    }

    @Override
    public boolean value() {
        return v;
    }

    @Override
    public StateEntry save() {
        return new CopyBoolStateEntry(v);
    }
}
