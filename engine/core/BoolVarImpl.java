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

package minicp.engine.core;

public class BoolVarImpl extends IntVarImpl implements BoolVar {

    public BoolVarImpl(Solver cp) {
        super(cp, 0, 1);
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
    public void assign(boolean b) {
        assign(b ? 1 : 0);
    }
}
