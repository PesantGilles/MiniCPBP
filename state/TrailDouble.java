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
 * Implementation of {@link StateDouble} with trail strategy
 * @see Trailer
 * @see StateManager#makeStateDouble(double)
 */
public class TrailDouble implements StateDouble {
    class StateEntryDouble implements StateEntry {
        private final double v;

        StateEntryDouble(double v) {
            this.v = v;
        }

        @Override
        public void restore() {
            TrailDouble.this.v = v;
        }
    }

    private Trailer trail;
    private double v;
    private long lastMagic = -1L;

    protected TrailDouble(Trailer trail, double initial) {
        this.trail = trail;
        v = initial;
        lastMagic = trail.getMagic() - 1;
    }

    private void trail() {
        long trailMagic = trail.getMagic();
        if (lastMagic != trailMagic) {
            lastMagic = trailMagic;
            trail.pushState(new StateEntryDouble(v));
        }
    }

    @Override
    public double setValue(double v) {
        if (v != this.v) {
            trail();
            this.v = v;
        }
        return this.v;
    }

    @Override
    public double value() {
        return this.v;
    }

    @Override
    public String toString() {
        return "" + v;
    }
}
