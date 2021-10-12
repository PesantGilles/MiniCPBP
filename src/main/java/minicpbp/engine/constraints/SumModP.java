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
import minicpbp.state.StateManager;
import minicpbp.state.StateInt;
import minicpbp.state.StateBool;
import minicpbp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Sum modulo p Constraint
 */
public class SumModP extends AbstractConstraint {
    private int[] unBounds;
    private StateInt nUnBounds;
    private StateInt sumBounds;
    private IntVar[] x;
    private int p; // the modulus
    private int n;
    private double[][] ip; // ip[i][k]=(nb of incoming paths reaching k) for partial sums x[0]+x[1]+...+x[i-1] mod p; layer i (i.e. before x[i])
    private double[][] op; // op[i][k]=(nb of outgoing paths reaching k) for partial sums x[i+1]+...+x[n-1] mod p; layer i+1 (i.e. after x[i])
    private double[][] ibelief; // ibelief[i][k]=(cumulative belief of incoming paths reaching k) for partial sums x[0]+x[1]+...+x[i-1] mod p; layer i (i.e. before x[i])
    private double[][] obelief; // obelief[i][k]=(cumulative belief of outgoing paths reaching k) for partial sums x[i+1]+...+x[n-1] mod p; layer i+1 (i.e. after x[i])
    private static final int domainConsistencyThreshold = 8;
    private StateBool domainEvents; // true iff we have switched to domain events once the threshold has been reached

    /**
     * Creates a sum modulo p constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == 0 (mod p)}.
     *
     * @param x the non empty set of variables that should sum to zero
     * @param p the modulus
     */
    public SumModP(IntVar[] x, int p) {
        super(x[0].getSolver(), x);
        setName("SumModP");
        n = x.length;
        this.x = x;
        this.p = p;
        assert (p > 1);
        StateManager sm = getSolver().getStateManager();
        nUnBounds = sm.makeStateInt(n);
        sumBounds = sm.makeStateInt(0);
        unBounds = IntStream.range(0, n).toArray();
        domainEvents = sm.makeStateBool(false);
        op = new double[domainConsistencyThreshold][p];
        ip = new double[domainConsistencyThreshold][p];
        obelief = new double[n][p];
        ibelief = new double[n][p];

        setExactWCounting(true);
    }

    @Override
    public void post() {
        switch (getSolver().getMode()) {
            case BP:
                break;
            case SP:
            case SBP:
                for (IntVar var : x)
                    var.propagateOnBind(this);
        }
        propagate();
    }

    @Override
    public void propagate() {
        // Collect potential newly bound vars and update the partial sum
        int nU = nUnBounds.value();
        for (int i = nU - 1; i >= 0; i--) {
            int idx = unBounds[i];
            if (x[idx].isBound()) {
                sumBounds.setValue(Math.floorMod(sumBounds.value() + x[idx].min(), p));
                unBounds[i] = unBounds[nU - 1]; // Swap the variables
                unBounds[nU - 1] = idx;
                nU--;
            }
        }
        nUnBounds.setValue(nU);
        if (nU == 0) {
            if (sumBounds.value() != 0)
                throw new InconsistencyException();
            else
                return;
        }
        if (nU > domainConsistencyThreshold) // do nothing further until the number of unbound variables is small enough for some filtering being likely
            return;

	/*
	if (!domainEvents.value()) {// switch to finer-grained "domain" events for the unbound variables
	    domainEvents.setValue(true);
	    for (int i = 0; i < nU; i++){
		x[unBounds[i]].propagateOnDomainChange(this);
	    }
	}
	*/

        // Filter the unbound vars (domain consistency)
        for (int i = 0; i < nUnBounds.value(); i++) {
            Arrays.fill(ip[i], 0);
        }
        // Reach forward
        ip[0][sumBounds.value()] = 1;
        for (int i = 0; i < nUnBounds.value() - 1; i++) {
            int idx = unBounds[i];
            int s = x[idx].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < p; k++) {
                    if (ip[i][k] > 0)
                        ip[i + 1][Math.floorMod(k + v, p)] += ip[i][k];
                }
            }
        }
        for (int i = 0; i < nUnBounds.value(); i++) {
            Arrays.fill(op[i], 0);
        }
        // Reach backward and remove unsupported var/val pairs
        op[nUnBounds.value() - 1][0] = 1;
        for (int i = nUnBounds.value() - 1; i > 0; i--) {
            int idx = unBounds[i];
            int s = x[idx].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                boolean supported = false;
                for (int k = 0; k < p; k++) {
                    if (op[i][k] > 0) {
                        int tmp = Math.floorMod(k - v, p);
                        op[i - 1][tmp] += op[i][k];
                        if (ip[i][tmp] > 0)
                            supported = true;
                    }
                }
                if (!supported) {
                    x[idx].remove(v);
                }
            }
        }
        int idx = unBounds[0];
        int s = x[idx].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            if (op[0][Math.floorMod(sumBounds.value() + v, p)] == 0) {
                x[idx].remove(v);
            }
        }
    }

    /**
     * Returns the exact number of solutions to this constraint
     * WARNING: assumes op[] is current
     *
     * @return number of solutions
     */
    public double getNbSolns() {
        double nbSolns = 0;
        int idx = unBounds[0];
        int s = x[idx].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            nbSolns += op[0][Math.floorMod(sumBounds.value() + domainValues[j], p)];
        }
        return nbSolns;
    }


    @Override
    public void updateBelief() {
        // NOTE: we do not explicitly set the local belief of bound variables: handled by normalizeMarginals()
        if (nUnBounds.value() == 0)
            return;
        for (int i = 0; i < nUnBounds.value(); i++) {
            Arrays.fill(ibelief[i], beliefRep.zero());
        }
        // Reach forward
        ibelief[0][sumBounds.value()] = beliefRep.one();
        for (int i = 0; i < nUnBounds.value() - 1; i++) {
            int idx = unBounds[i];
            int s = x[idx].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < p; k++) {
                    if (!beliefRep.isZero(ibelief[i][k])) {
                        int tmp = Math.floorMod(k + v, p);
                        // add the combination of ibelief[i][k] and outsideBelief(idx,v) to ibelief[i+1][Math.floorMod(k+v,p)]
                        ibelief[i + 1][tmp] = beliefRep.add(ibelief[i + 1][tmp], beliefRep.multiply(ibelief[i][k], outsideBelief(idx, v)));
                    }
                }
            }
        }
        for (int i = 0; i < nUnBounds.value(); i++) {
            Arrays.fill(obelief[i], beliefRep.zero());
        }
        // Reach backward and set local beliefs
        obelief[nUnBounds.value() - 1][0] = beliefRep.one();
        for (int i = nUnBounds.value() - 1; i > 0; i--) {
            int idx = unBounds[i];
            int s = x[idx].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                double belief = beliefRep.zero();
                for (int k = 0; k < p; k++) {
                    if (!beliefRep.isZero(obelief[i][k])) {
                        int tmp = Math.floorMod(k - v, p);
                        // add the combination of obelief[i][k] and outsideBelief(idx,v) to obelief[i-1][Math.floorMod(k-v,p)]
                        obelief[i - 1][tmp] = beliefRep.add(obelief[i - 1][tmp], beliefRep.multiply(obelief[i][k], outsideBelief(idx, v)));
                        // add the combination of ibelief[i][Math.floorMod(k-v,p)] and obelief[i][k] to belief
                        belief = beliefRep.add(belief, beliefRep.multiply(ibelief[i][tmp], obelief[i][k]));
                    }
                }
                setLocalBelief(idx, v, belief);
            }
        }
        int idx = unBounds[0];
        int s = x[idx].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            setLocalBelief(idx, v, obelief[0][Math.floorMod(sumBounds.value() + v, p)]);
        }
    }
}
