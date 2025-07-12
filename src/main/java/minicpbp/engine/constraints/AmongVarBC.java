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
 *
 * mini-cpbp, replacing classic propagation by belief propagation
 * Copyright (c)  2019. by Gilles Pesant
 */

package minicpbp.engine.constraints;

import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.IntVarImpl;
import minicpbp.state.StateInt;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.Set;
import java.util.HashSet;


public class AmongVarBC extends AbstractConstraint {
    private IntVar[] x;
    private Set<Integer> V;
    private IntVar occurrence;
    private int n; // nb of vars
    private int[] undecided; // indices of vars from x whose domain contains values both inside and outside of V
    private StateInt nUndecided; // current size of undecided
    private StateInt[] witnessInsideV; // for each x[i], an element from its domain that belongs to V
    private StateInt[] witnessOutsideV; // for each x[i], an element from its domain that does not belong to V
    private StateInt nInside; // number of vars from x decided to be inside V
    private StateInt nOutside; // number of vars from x decided to be outside V

    /**
     * Creates an among constraint enforcing bounds consistency on the occurrence variable (contribution of Damien Van Meerbeeck)
     * <p> This constraint holds iff
     * {@code occurrence.min() <= (x[0] \in V) + (x[1] \in V) + ... + (x[x.length-1] \in V) <= occurrence.max()}.
     * <p>
     *
     * @param x          an array of variables whose instantiations belonging to V we count
     * @param V          an array of values whose occurrences in x we count
     * @param occurrence the variable corresponding to the number of occurrences of values from V in x
     */
    public AmongVarBC(IntVar[] x, int[] V, IntVar occurrence) {
        super(x[0].getSolver(), x);
        setName("AmongVarBC");
        this.x = x;
        this.n = x.length;
        this.occurrence = occurrence;
        this.occurrence.removeBelow(0);
        this.occurrence.removeAbove(x.length);
        this.V = new HashSet<Integer>();
        for (int i = 0; i < V.length; i++) {
            this.V.add(V[i]);
        }
        nUndecided = getSolver().getStateManager().makeStateInt(n);
        undecided = IntStream.range(0, n).toArray();
        witnessInsideV = new StateInt[n];
        witnessOutsideV = new StateInt[n];
        for (int i = 0; i < n; i++) {
            assert !x[i].contains(Integer.MIN_VALUE) : "Among constraint: variable x[" + i + "] has Integer.MIN_VALUE in its domain!";
            witnessInsideV[i] = getSolver().getStateManager().makeStateInt(Integer.MIN_VALUE);
            witnessOutsideV[i] = getSolver().getStateManager().makeStateInt(Integer.MIN_VALUE);
        }
        nInside = getSolver().getStateManager().makeStateInt(0);
        nOutside = getSolver().getStateManager().makeStateInt(0);
        setExactWCounting(false);
    }

    @Override
    public void post() {
        switch (getSolver().getMode()) {
            case BP:
                break;
            case SP:
            case SBP:
                for (IntVar var : x)
                    var.propagateOnDomainChange(this);
                occurrence.propagateOnBoundChange(this);
        }
        propagate();
    }

    @Override
    public void propagate() {
        int nU = nUndecided.value();
        for (int i = nU - 1; i >= 0; i--) {
            int idx = undecided[i];
            switch (isDecided(x[idx], idx)) {
                case 0:
                    continue; // still undecided --- skip the update of undecided[]
                case 1:
                    nInside.setValue(nInside.value()+1); // decided as taking a value inside V
                    break;
                case -1:
                    nOutside.setValue(nOutside.value()+1); // decided as not taking a value inside V
                    break;
            }
            undecided[i] = undecided[nU - 1]; // Swap the variables
            undecided[nU - 1] = idx;
            nU--;
        }
        nUndecided.setValue(nU);

        int nI = nInside.value();
        int nO = nOutside.value();

        occurrence.removeBelow(nI);
        occurrence.removeAbove(nI+nU);

        if (nI+nU == occurrence.min()) { // force all undecided vars to be inside V
            for (int i = nU - 1; i >= 0; i--) {
                int idx = undecided[i];
                int s = x[idx].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    int v = domainValues[j];
                    if (!V.contains(v)) {
                        x[idx].remove(v);
                    }
                }
            }
        }
        if (nI == occurrence.max()) { // force all undecided vars to be outside V
            for (int i = nU - 1; i >= 0; i--) {
                int idx = undecided[i];
                int s = x[idx].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    int v = domainValues[j];
                    if (V.contains(v)) {
                        x[idx].remove(v);
                    }
                }
            }
        }
    }

    // returns 0 if var can still take a value inside and outside V
    //         1 if var can only take a value inside V
    //        -1 if var cannot take a value inside V
    private int isDecided(IntVar var, int i) {
        int j;
        if (!var.contains(witnessInsideV[i].value())) {
            // find new witness
            // iterate over smallest between var's domain and V
            if (var.size() < V.size()) {
                int s = var.fillArray(domainValues);
                for (j = 0; j < s; j++) {
                    int v = domainValues[j];
                    if (V.contains(v)) {
                        witnessInsideV[i].setValue(v);
                        break;
                    }
                }
                if (j == s) // no inside witness
                    return -1;
            }
            else { // V is smaller
                Iterator<Integer> itr = V.iterator();
                while (itr.hasNext()) {
                    int val = itr.next();
                    if (var.contains(val)) {
                        witnessInsideV[i].setValue(val);
                        break;
                    }
                }
                if (!var.contains(witnessInsideV[i].value())) // no inside witness
                    return -1;
            }
        }
        if (!var.contains(witnessOutsideV[i].value())) {
            // find new witness
            int s = var.fillArray(domainValues);
            for (j = 0; j < s; j++) {
                int v = domainValues[j];
                if (!V.contains(v)) {
                    witnessOutsideV[i].setValue(v);
                    break;
                }
            }
            if (j == s) // no outside witness
                return 1;
        }
        return 0;
    }

}
