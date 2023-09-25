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


package minicpbp.engine.constraints;

import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;
import minicpbp.util.ArrayUtil;

import java.util.Arrays;

/**
 * Maximum Constraint
 */
public class Maximum extends AbstractConstraint {

    private final IntVar[] x;
    private final IntVar y;
    private int n;
    private double[][] beliefLessOrEqual;
    private double[] bProductLessOrEqual;
    private int offset;
    private double yBeliefDifference[];
    private int yOffset;

    /**
     * Creates the maximum constraint y = maximum(x[0],x[1],...,x[n])
     *
     * @param x the variable on which the maximum is to be found
     * @param y the variable that is equal to the maximum on x
     */
    public Maximum(IntVar[] x, IntVar y) {
        super(x[0].getSolver(), ArrayUtil.append(x, y));
        setName("Maximum");
        n = x.length;
        assert (n > 0);
        this.x = x;
        this.y = y;
        setExactWCounting(true);
    }

    @Override
    public void post() {
        for (IntVar xi : x) {
            xi.propagateOnBoundChange(this);
        }
        y.propagateOnBoundChange(this);
        propagate();
        // determine range of domain values; sufficient to look at x[i]'s because y was narrowed to [max(min),..,max(max)] in propagate()
        // TODO: give up on updateBellief() if range is too large?
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            if (x[i].max() > max) {
                max = x[i].max();
            }
            if (x[i].min() < min) {
                min = x[i].min();
            }
        }
        beliefLessOrEqual = new double[n][max-min+1];
        bProductLessOrEqual = new double[max-min+1];
        offset = min;
        yBeliefDifference = new double[y.max()-y.min()+1];
        yOffset = y.min();
    }


    @Override
    public void propagate() {
        int max = Integer.MIN_VALUE;
        int min = Integer.MIN_VALUE;
        int nSupport = 0;
        int supportIdx = -1;
        for (int i = 0; i < n; i++) {
            x[i].removeAbove(y.max());

            if (x[i].max() > max) {
                max = x[i].max();
            }
            if (x[i].min() > min) {
                min = x[i].min();
            }

            if (x[i].max() >= y.min()) {
                nSupport += 1;
                supportIdx = i;
            }
        }
        if (nSupport == 1) {
            x[supportIdx].removeBelow(y.min());
        }
        y.removeAbove(max);
        y.removeBelow(min);
    }

    public void updateBelief() {
        // accumulate belief for each x[i] wrt values and compute their product for each value
        for (int j = 0; j < bProductLessOrEqual.length; j++) {
            bProductLessOrEqual[j] = beliefRep.one();
            int v = j+offset;
            for (int i = 0; i < n; i++) {
                beliefLessOrEqual[i][j] = (j==0? beliefRep.zero() : beliefLessOrEqual[i][j-1]);
                if (x[i].contains(v)) {
                    beliefLessOrEqual[i][j] = beliefRep.add( beliefLessOrEqual[i][j], outsideBelief(i, v));
                }
 //               System.out.println("beliefLE "+ i + " " + v + ": "+beliefLessOrEqual[i][j]);
                bProductLessOrEqual[j] = beliefRep.multiply( bProductLessOrEqual[j], beliefLessOrEqual[i][j]);
            }
//            System.out.println("bProductLE "+ v + ": "+bProductLessOrEqual[j]);
        }
        // precompute belief difference between consecutive values in the range of the domain of y
        for (int v = y.min(); v <= y.max(); v++) {
            yBeliefDifference[v-yOffset] = beliefRep.subtract(
                    (y.contains(v)? outsideBelief( n, v) : beliefRep.zero()),
                    (y.contains(v+1)? outsideBelief( n, v+1) : beliefRep.zero()));
        }
        // Compute beliefs for y
        int s = y.fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            // belief for y=v is that of all x[i]'s being at most v minus that of all x[i]"s being less than v
            setLocalBelief(n, v, (v>offset? beliefRep.subtract(bProductLessOrEqual[v-offset], bProductLessOrEqual[v-1-offset]) : bProductLessOrEqual[v-offset]));
        }
        // Compute beliefs for x[i]s
        for (int i = 0; i < n; i++) {
//            System.out.println("i="+i);
            double runningSum = beliefRep.zero();
            // process values in the range of the domain of y in decreasing order...
            for (int v = y.max(); v >= y.min(); v--) {
//                System.out.println("at y value " + v);
                runningSum = beliefRep.add( runningSum, beliefRep.multiply( yBeliefDifference[v-yOffset], beliefRep.divide(bProductLessOrEqual[v-offset], beliefLessOrEqual[i][v-offset])));
                if (x[i].contains(v)) {
                    // belief for x[i]=v is (y=v and all other x[j]<=v) + (y=v'>v and all other x[j]<=v' and some x[k]=v')
                    setLocalBelief(i, v, runningSum);
//                    System.out.println(runningSum);
                }
            }
            //...and continue until x[i].min()
            if (x[i].min()<y.min()) { // a last adjustment
                runningSum = beliefRep.subtract( runningSum, beliefRep.multiply( outsideBelief( n, y.min()), beliefRep.divide(bProductLessOrEqual[y.min()-1-offset], beliefLessOrEqual[i][y.min()-1-offset])));
            }
            for (int v = y.min()-1; v >= x[i].min(); v--) {
//                System.out.println("at value " + v);
                if (x[i].contains(v)) {
                    // belief for x[i]=v<y.min() is (y=v'>v and all other x[j]<=v' and some x[k]=v')
                    setLocalBelief(i, v, runningSum);
//                    System.out.println(runningSum);
                }
            }
        }
    }

}
