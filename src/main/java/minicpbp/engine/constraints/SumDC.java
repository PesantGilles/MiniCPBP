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

import minicpbp.cp.Factory;
import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;
import minicpbp.state.StateInt;
import minicpbp.util.ArrayUtil;
import minicpbp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Sum Constraint -- enforcing domain consistency
 */
public class SumDC extends AbstractConstraint {
    private Integer[] unBounds;
    private StateInt nUnBounds;
    private StateInt sumBounds;
    private IntVar[] x;
    private int n;
    private double[][] ip; // ip[i][]>0 for partial sums x[0]+x[1]+...+x[i-1]; layer i (i.e. before x[i])
    private double[][] op; // op[i][]>0 for partial sums x[i+1]+...+x[n-1]; layer i+1 (i.e. after x[i])
    private int[] minState; // minState[i] = lowest-numbered feasible state in layer i
    private int[] maxState; // maxState[i] = highest-numbered feasible state in layer i
    private int offset;
    private int mini;
    private int maxi;
    private boolean incrementalUpdateBelief;
    private DomRangeComparator domRangeComparator;

    public class DomRangeComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer i, Integer j) {
            return (x[i].max() - x[i].min()) - (x[j].max() - x[j].min());
        }
    }

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == y}.
     *
     * @param x the non empty left hand side of the sum
     * @param y the right hand side of the sum
     */
    public SumDC(IntVar[] x, IntVar y) {
        this(ArrayUtil.append(x,Factory.minus(y)));
    }

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == y}.
     *
     * @param x the non empty left hand side of the sum
     * @param y the right hand side of the sum
     */
    public SumDC(IntVar[] x, int y) {
        this(ArrayUtil.append(x,Factory.makeIntVar(x[0].getSolver(), -y, -y)));
    }

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == 0}.
     *
     * @param x the non empty set of variables that should sum to zero
     */
    public SumDC(IntVar[] x) {
        super(x[0].getSolver(), x);
        setName("SumDC");
        this.x = x;
        n = x.length;
        nUnBounds = getSolver().getStateManager().makeStateInt(n);
        sumBounds = getSolver().getStateManager().makeStateInt(0);
        unBounds = IntStream.range(0, n).boxed().toArray(Integer[]::new);
        setExactWCounting(true);
        switch (getSolver().getMode()) {
            case BP:
                incrementalUpdateBelief = false; // will not be able to use SP's updated unBounds[]
                break;
            case SP:
            case SBP:
                incrementalUpdateBelief = true;
        }
        // compute the extent of the dynamic programming tables ip and op
        if (incrementalUpdateBelief) {
            maxi = 0;
            for (int i = 0; i < n; i++) {
                if (x[i].max() > 0)
                    maxi += x[i].max();
            }
            offset = maxi;
            op = new double[n][2 * maxi + 1]; // TODO : doesn't handle big integers
            ip = new double[n][2 * maxi + 1];
            minState = new int[n + 1];
            maxState = new int[n + 1];
            domRangeComparator = new DomRangeComparator();
        } else { // exploits the fact that variables appear in order from x[0] to x[n-1] to reduce the extent
            int fwd = 0;
            int bwd = 0;
            for (int i = 0; i < n; i++) {
                bwd -= x[i].min();
            }
            maxi = 0;
            for (int i = 0; i < n; i++) {
                maxi = Math.max(maxi, Math.min(fwd, bwd));
                fwd += x[i].max();
                bwd += x[i].min();
            }
            fwd = 0;
            bwd = 0;
            for (int i = 0; i < n; i++) {
                bwd -= x[i].max();
            }
            mini = 0;
            for (int i = 0; i < n; i++) {
                mini = Math.min(mini, Math.max(fwd, bwd));
                fwd += x[i].min();
                bwd += x[i].max();
            }
            offset = -mini;
            op = new double[n][maxi - mini + 1];
            ip = new double[n][maxi - mini + 1];
        }
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
        }
        propagate();
    }

    @Override
    public void propagate() {
        // Update the unbound vars and the partial sum
        int nU = nUnBounds.value();
         for (int i = nU - 1; i >= 0; i--) {
            int idx = unBounds[i];
            if (x[idx].isBound()) {
                sumBounds.setValue(sumBounds.value() + x[idx].min());
                unBounds[i] = unBounds[nU - 1]; // Swap the variables
                unBounds[nU - 1] = idx;
                nU--;
            }
        }
        nUnBounds.setValue(nU);
        int idx, s, v;
        if (incrementalUpdateBelief) { // incremental version using unBounds[]
            // In an effort to keep the range of DP states small, we order vars by increasing domain range.
            Arrays.sort(unBounds, 0, nU, domRangeComparator);
            // compute the range of feasible states for each layer
            int fwd_hi = offset + sumBounds.value();
            int fwd_lo = fwd_hi;
            int bwd_hi = offset;
            int bwd_lo = bwd_hi;
            for (int i = 0; i < nUnBounds.value(); i++) {
                idx = unBounds[i];
                bwd_hi -= x[idx].min();
                bwd_lo -= x[idx].max();
            }
            if (fwd_lo > bwd_hi || fwd_hi < bwd_lo)
                throw new InconsistencyException(); // sum constraint cannot be satisfied
            if (nUnBounds.value() == 0) {// sum constraint is satisfied and all variables are bound
                setActive(false);
                return;
            }
            for (int i = 0; i < nUnBounds.value(); i++) {
                minState[i] = Math.max(fwd_lo, bwd_lo);
                maxState[i] = Math.min(fwd_hi, bwd_hi);
                idx = unBounds[i];
                fwd_hi += x[idx].max();
                fwd_lo += x[idx].min();
                bwd_hi += x[idx].min();
                bwd_lo += x[idx].max();
            }
            minState[nUnBounds.value()] = Math.max(fwd_lo, bwd_lo);
            maxState[nUnBounds.value()] = Math.min(fwd_hi, bwd_hi);
            // Reach forward
            ip[0][minState[0]] = 1.0;
            for (int i = 0; i < nUnBounds.value() - 1; i++) {
                idx = unBounds[i];
                Arrays.fill(ip[i + 1], minState[i + 1], maxState[i + 1] + 1, 0);
                s = x[idx].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    for (int k = Math.max(minState[i], minState[i + 1] - v); k <= Math.min(maxState[i], maxState[i + 1] - v); k++) {
                        if (ip[i][k] == 1.0) {
                            ip[i + 1][k + v] = 1.0;
                        }
                    }
                }
            }
            // Reach backward and remove unsupported var/val pairs
            op[nUnBounds.value() - 1][minState[nUnBounds.value()]] = 1.0;
            for (int i = nUnBounds.value() - 1; i > 0; i--) {
                idx = unBounds[i];
                Arrays.fill(op[i - 1], minState[i], maxState[i] + 1, 0);
                s = x[idx].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    boolean supported = false;
                    for (int k = Math.max(minState[i + 1], minState[i] + v); k <= Math.min(maxState[i + 1], maxState[i] + v); k++) {
                        if (op[i][k] == 1.0) {
                            op[i - 1][k - v] = 1.0;
                            if (ip[i][k - v] == 1.0) {
				                supported = true;
			                 }
                        }
                    }
                    if (!supported) {
			            x[idx].remove(v);
		            }
                }
            }
            idx = unBounds[0];
            s = x[idx].fillArray(domainValues);
            if (nUnBounds.value() == 1) { // special case
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    if (minState[0] + v != minState[1]) {
                        x[idx].remove(v);
                    }
                }
            } else {
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    if ((minState[0] + v < minState[1]) || (minState[0] + v > maxState[1]) || (op[0][minState[0] + v] == 0)) {
                            x[idx].remove(v);
                    }
                }
            }
        } else { // non-incremental version
            for (int i = 0; i < n; i++) {
                Arrays.fill(ip[i], 0);
            }
            // Reach forward
            ip[0][offset] = 1.0;
            for (int i = 0; i < n - 1; i++) {
                s = x[i].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    for (int k = mini - (v < 0 ? v : 0); k <= maxi - (v > 0 ? v : 0); k++) {
                        if (ip[i][k + offset] == 1.0) {
                            ip[i + 1][k + offset + v] = 1.0;
                        }
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                Arrays.fill(op[i], 0);
            }
            // Reach backward and remove unsupported var/val pairs
            op[n - 1][offset] = 1.0;
            for (int i = n - 1; i > 0; i--) {
                s = x[i].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    boolean supported = false;
                    for (int k = mini - (v < 0 ? v : 0); k <= maxi - (v > 0 ? v : 0); k++) {
                        if (op[i][k + offset + v] == 1.0) {
                            op[i - 1][k + offset] = 1.0;
			                if (ip[i][k + offset] == 1.0) {
				                supported = true;
			                }
                        }
                    }
                    if (!supported) {
			            x[i].remove(v);
		            }
                }
            }
            s = x[0].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                v = domainValues[j];
                if (op[0][offset + v] == 0) {
		            x[0].remove(v);
		        }
            }
        }
    }

    @Override
    public void updateBelief() {
        int idx, s, v;
        if (incrementalUpdateBelief) { // incremental version using unBounds[]
            // NOTE: we do not explicitly set the local belief of bound variables: handled by normalizeMarginals()
            if (nUnBounds.value() == 0)
                return;
            // compute the range of feasible states for each layer
            int fwd_hi = offset + sumBounds.value();
            int fwd_lo = fwd_hi;
            int bwd_hi = offset;
            int bwd_lo = bwd_hi;
            for (int i = 0; i < nUnBounds.value(); i++) {
                idx = unBounds[i];
                bwd_hi -= x[idx].min();
                bwd_lo -= x[idx].max();
            }
            for (int i = 0; i < nUnBounds.value(); i++) {
                minState[i] = Math.max(fwd_lo, bwd_lo);
                maxState[i] = Math.min(fwd_hi, bwd_hi);
                idx = unBounds[i];
                fwd_hi += x[idx].max();
                fwd_lo += x[idx].min();
                bwd_hi += x[idx].min();
                bwd_lo += x[idx].max();
            }
            minState[nUnBounds.value()] = Math.max(fwd_lo, bwd_lo);
            maxState[nUnBounds.value()] = Math.min(fwd_hi, bwd_hi);
            // Reach forward
            ip[0][minState[0]] = beliefRep.one();
            for (int i = 0; i < nUnBounds.value() - 1; i++) {
                idx = unBounds[i];
                Arrays.fill(ip[i + 1], minState[i + 1], maxState[i + 1] + 1, beliefRep.zero());
                s = x[idx].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    for (int k = Math.max(minState[i], minState[i + 1] - v); k <= Math.min(maxState[i], maxState[i + 1] - v); k++) {
                        if (!beliefRep.isZero(ip[i][k])) {
                            // add the combination of ip[i][k] and outsideBelief(idx,v) to ip[i+1][k+v]
                            ip[i + 1][k + v] = beliefRep.add(ip[i + 1][k + v], beliefRep.multiply(ip[i][k], outsideBelief(idx, v)));
                        }
                    }
                }
            }
            // Reach backward and set local beliefs
            op[nUnBounds.value() - 1][minState[nUnBounds.value()]] = beliefRep.one();
            for (int i = nUnBounds.value() - 1; i > 0; i--) {
                idx = unBounds[i];
                Arrays.fill(op[i - 1], minState[i], maxState[i] + 1, beliefRep.zero());
                s = x[idx].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    double belief = beliefRep.zero();
                    for (int k = Math.max(minState[i + 1], minState[i] + v); k <= Math.min(maxState[i + 1], maxState[i] + v); k++) {
                        if (!beliefRep.isZero(op[i][k])) {
                            // add the combination of op[i][k] and outsideBelief(idx,v) to op[i-1][k-v]
                            double b = beliefRep.multiply(op[i][k], outsideBelief(idx, v));
                            op[i - 1][k - v] = beliefRep.add(op[i - 1][k - v], b);
                            // add the combination of ip[i][k-v], op[i][k], and outsideBelief(idx, v) to belief
                            belief = beliefRep.add(belief, beliefRep.multiply(ip[i][k - v], b));
                        }
                    }
                    setLocalBelief(idx, v, belief);
                }
            }
            idx = unBounds[0];
            s = x[idx].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                v = domainValues[j];
                setLocalBelief(idx, v, beliefRep.multiply(op[0][minState[0] + v], outsideBelief(idx, v))); // unlike propagate(), second index cannot be out of bounds (would have been filtered out already)
            }
        } else { // non-incremental version
            for (int i = 0; i < n; i++) {
                Arrays.fill(ip[i], beliefRep.zero());
            }
            // Reach forward
            ip[0][offset] = beliefRep.one();
            for (int i = 0; i < n - 1; i++) {
                s = x[i].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    for (int k = mini - (v < 0 ? v : 0); k <= maxi - (v > 0 ? v : 0); k++) {
                        if (!beliefRep.isZero(ip[i][k + offset])) {
                            // add the combination of ip[i][k+offset] and outsideBelief(i,v) to ip[i+1][k+offset+v]
                            ip[i + 1][k + offset + v] = beliefRep.add(ip[i + 1][k + offset + v], beliefRep.multiply(ip[i][k + offset], outsideBelief(i, v)));
                        }
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                Arrays.fill(op[i], beliefRep.zero());
            }
            // Reach backward and set local beliefs
            op[n - 1][offset] = beliefRep.one();
            for (int i = n - 1; i > 0; i--) {
                s = x[i].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    v = domainValues[j];
                    double belief = beliefRep.zero();
                    for (int k = mini - (v < 0 ? v : 0); k <= maxi - (v > 0 ? v : 0); k++) {
                        if (!beliefRep.isZero(op[i][k + offset + v])) {
                            // add the combination of op[i][k+offset+v] and outsideBelief(i,v) to op[i-1][k+offset]
                            double b = beliefRep.multiply(op[i][k + offset + v], outsideBelief(i, v));
                            op[i - 1][k + offset] = beliefRep.add(op[i - 1][k + offset], b);
                            // add the combination of ip[i][k+offset], op[i][k+offset+V], and outsideBelief(i, v) to belief
                            belief = beliefRep.add(belief, beliefRep.multiply(ip[i][k + offset], b));
                        }
                    }
                    setLocalBelief(i, v, belief);
                }
            }
            s = x[0].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                v = domainValues[j];
                setLocalBelief(0, v, beliefRep.multiply(op[0][offset + v], outsideBelief(0, v)));
            }

        }
    }

    @Override
    public double weightedCounting() {
        int s, v;
        for (int i = 0; i < n; i++) {
            Arrays.fill(op[i], beliefRep.zero());
        }
        // Reach backward
        op[n - 1][offset] = beliefRep.one();
        for (int i = n - 1; i > 0; i--) {
            s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                v = domainValues[j];
//                System.out.println("i v "+i+" "+v);
                for (int k = mini - (v < 0 ? v : 0); k <= maxi - (v > 0 ? v : 0); k++) {
                    if (!beliefRep.isZero(op[i][k + offset + v])) {
                        // add the combination of op[i][k+offset+v] and outsideBelief(i,v) to op[i-1][k+offset]
                            op[i - 1][k + offset] = beliefRep.add(op[i - 1][k + offset], beliefRep.multiply(op[i][k + offset + v], outsideBelief(i, v)));
//                            System.out.println("outsideBelief(i,v)="+outsideBelief(i, v));
//                            System.out.println(k+" "+op[i - 1][k + offset]);
                    }
                }
            }
        }
        double weightedCount = beliefRep.zero();
        s = x[0].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            v = domainValues[j];
            weightedCount = beliefRep.add(weightedCount, beliefRep.multiply(op[0][offset + v], outsideBelief(0, v)));
        }
        System.out.println("weighted count for "+this.getName()+" constraint: "+weightedCount);
        return weightedCount;
    }
}
