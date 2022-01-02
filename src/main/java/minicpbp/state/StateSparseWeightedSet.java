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

import minicpbp.engine.core.Solver;

/**
 * Weighted Set implemented using a sparse-set data structure
 * that can be saved and restored through
 * the {@link StateManager#saveState()} / {@link StateManager#restoreState()}
 * methods.
 */
public class StateSparseWeightedSet extends StateSparseSet {

    private StateDouble[] weights;

    /**
     * Creates a set containing the elements {@code {ofs,ofs+1,...,ofs+n-1}},
     * each with a reversible modifiable weight.
     *
     * @param cp  the solver
     * @param n   the number of elements in the set
     * @param ofs the minimum value in the set containing {@code {ofs,ofs+1,...,ofs+n-1}}
     */
    public StateSparseWeightedSet(Solver cp, int n, int ofs) {
        super(cp.getStateManager(), n, ofs);
        weights = new StateDouble[n];
        for (int i = 0; i < n; i++) {
            weights[i] = cp.getStateManager().makeStateDouble(cp.getBeliefRep().one()); // not normalized
        }
    }


    protected void exchangePositions(int val1, int val2) {
        int i1 = indexes[val1];
        int i2 = indexes[val2];
        StateDouble w1 = weights[i1];
        StateDouble w2 = weights[i2];
        super.exchangePositions(val1, val2);
        weights[i1] = w2;
        weights[i2] = w1;
    }

    /**
     * Removes all the element from the set except the given value.
     *
     * @param v is an element in the set
     */
    public void removeAllBut(int v) {
        assert (contains(v));
        int index = indexes[v - ofs];
        super.removeAllBut(v);
        double w = weights[0].value();
        weights[0].setValue(weights[index].value());
        weights[index].setValue(w);
    }


    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        for (int i = 0; i < size() - 1; i++) {
            b.append(values[i] + ofs);
            b.append("  <");
            b.append(weights[i].value());
            b.append(">, ");
        }
        if (size() > 0) {
            b.append(values[size() - 1] + ofs);
            b.append("  <");
            b.append(weights[size() - 1].value());
            b.append('>');
        }
        b.append("}");
        return b.toString();
    }

    /**
     * Returns the weight of an element from the set.
     *
     * @param v is an element in the set
     */
    public double weight(int v) {
        assert (contains(v));
        return weights[indexes[v - ofs]].value();
    }

    /**
     * Sets the weight of an element from the set.
     *
     * @param v is an element in the set, w is the weight
     */
    public void setWeight(int v, double w) {
        assert (contains(v));
        weights[indexes[v - ofs]].setValue(w);
    }
}
