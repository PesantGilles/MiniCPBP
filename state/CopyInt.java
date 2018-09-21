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

package minicp.state;

/**
 * Implementation of {@link StateInt} with copy strategy
 * @see Copier
 * @see StateManager#makeStateInt(int)
 */
public class CopyInt implements Storage, StateInt {

    class CopyIntStateEntry implements StateEntry {
        private final int v;

        CopyIntStateEntry(int v) {
            this.v = v;
        }

        @Override
        public void restore() {
            CopyInt.this.v = v;
        }
    }

    private int v;

    protected CopyInt(int initial) {
        v = initial;
    }

    @Override
    public int setValue(int v) {
        this.v = v;
        return v;
    }

    @Override
    public int value() {
        return v;
    }

    @Override
    public int increment() {
        v += 1;
        return v;
    }

    @Override
    public int decrement() {
        v -= 1;
        return v;
    }

    @Override
    public String toString() {
        return String.valueOf(v);
    }

    @Override
    public StateEntry save() {
        return new CopyIntStateEntry(v);
    }
}
