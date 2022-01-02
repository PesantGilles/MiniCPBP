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
 * Implementation of {@link StateDouble} with copy strategy
 * @see Copier
 * @see StateManager#makeStateDouble(double)
 */
public class CopyDouble implements Storage, StateDouble {

    class CopyDoubleStateEntry implements StateEntry {
        private final double v;

        CopyDoubleStateEntry(double v) {
            this.v = v;
        }

        @Override
        public void restore() {
            CopyDouble.this.v = v;
        }
    }

    private double v;

    protected CopyDouble(double initial) {
        v = initial;
    }

    @Override
    public double setValue(double v) {
        this.v = v;
        return v;
    }

    @Override
    public double value() {
        return v;
    }

    @Override
    public String toString() {
        return String.valueOf(v);
    }

    @Override
    public StateEntry save() {
        return new CopyDoubleStateEntry(v);
    }
}
