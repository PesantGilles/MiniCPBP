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

import java.util.ArrayList;
import java.util.BitSet;

import static minicpbp.cp.Factory.minus;

/**
 * Negative table constraint
 */
public class NegTableCT extends AbstractConstraint {

    private IntVar[] x; //variables
    private int xLength;
    private int[][] table; //the table
    private int tableLength;
    private int[] ofs; //offsets for each variable's domain
    //conflicts[i][v] is the set of forbidden tuples featuring x[i]=v
    private BitSet[][] conflicts;
    private BitSet menacing;
    private BitSet conflictsi;
    private double[] tupleWeight;

    /**
     * Negative Table constraint.
     * <p>Assignment of {@code x_0=v_0, x_1=v_1,...} only valid if there does not
     * exists a row {@code (v_0, v_1, ...)} in the table.
     * The table represents the infeasible assignments for the variables.
     *
     * @param x     the non empty set of variables to constrain.
     * @param table the array of invalid solutions (second dimension must be of same size as the array x)
     */
    public NegTableCT(IntVar[] x, int[][] table) {
        super(x[0].getSolver(), x);
        setName("NegTableCT");
        this.x = x;
        this.xLength = x.length;
        ofs = new int[xLength];

        // remove duplicate (the negative ct algo does not support it)
        ArrayList<int[]> tableList = new ArrayList<>();
        boolean[] duplicate = new boolean[table.length];
        for (int i = 0; i < table.length; i++) {
            if (!duplicate[i]) {
                tableList.add(table[i]);
                for (int j = i + 1; j < table.length; j++) {
                    if (i != j && !duplicate[j]) {
                        boolean same = true;
                        for (int k = 0; k < xLength; k++) {
                            same &= table[i][k] == table[j][k];
                        }
                        if (same) {
                            duplicate[j] = true;
                        }
                    }
                }
            }
        }
        this.table = tableList.toArray(new int[0][]);
        this.tableLength = this.table.length;
        menacing = new BitSet(tableLength);
        conflictsi = new BitSet(tableLength);
        tupleWeight = new double[tableLength];

        // Allocate conflicts
        conflicts = new BitSet[xLength][];
        for (int i = 0; i < xLength; i++) {
            ofs[i] = x[i].min(); // offsets map the variables' domain to start at 0 for conflicts[][]
            conflicts[i] = new BitSet[x[i].max() - x[i].min() + 1];
            for (int j = 0; j < conflicts[i].length; j++)
                conflicts[i][j] = new BitSet();
        }

        // Set values in conflicts, which contains all the forbidden tuples featuring each var-val pair
        for (int i = 0; i < tableLength; i++) { //i is the index of the tuple (in table)
            for (int j = 0; j < xLength; j++) { //j is the index of the current variable (in x)
                if (x[j].contains(this.table[i][j])) {
                    conflicts[j][this.table[i][j] - ofs[j]].set(i);
                }
            }
        }
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
                    var.propagateOnDomainChange(this);
        }
        propagate();
    }

    @Override
    public void propagate() {
        menacing.set(0, tableLength); // set them all to true

        for (int i = 0; i < xLength; i++) {
            conflictsi.clear(); // set them all to false
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                conflictsi.or(conflicts[i][domainValues[j] - ofs[i]]);
            }
            menacing.and(conflictsi);
        }

        Long prodDomains = 1L;
        for (int i = 0; i < xLength; i++) {
            prodDomains *= x[i].size();
        }

        for (int i = 0; i < xLength; i++) {
            int prodDomainsi = (int) (prodDomains / x[i].size());
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                // The condition for removing value v from x[i] is to check if
                // there are enough (distinct) forbidden tuples to cover all possible supports
                int v = domainValues[j];
		BitSet menacingIntersect = (BitSet) menacing.clone();
		menacingIntersect.and(conflicts[i][v - ofs[i]]);
		if (menacingIntersect.cardinality() >= prodDomainsi) {
		    x[i].remove(v);
		}
	    }
	}
    }

    @Override
    public void updateBelief() {
        menacing.set(0, tableLength); // set them all to true

        for (int i = 0; i < xLength; i++) {
            conflictsi.clear(); // set them all to false
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                conflictsi.or(conflicts[i][domainValues[j] - ofs[i]]);
            }
            menacing.and(conflictsi);
        }

        // Each tuple has its own weight given by the product of the outside_belief of its elements.
        // Compute these products, but only for menacing tuples.
        for (int k = menacing.nextSetBit(0); k >= 0; k = menacing.nextSetBit(k + 1)) {
            tupleWeight[k] = beliefRep.one();
            for (int i = 0; i < xLength; i++) {
                tupleWeight[k] = beliefRep.multiply(tupleWeight[k], outsideBelief(i, table[k][i]));
            }
        }

        for (int i = 0; i < xLength; i++) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                double belief = beliefRep.zero();
                double outsideBelief_i_v = outsideBelief(i, v);
                BitSet conflicts_i_v = conflicts[i][v - ofs[i]];
                // Iterate over conflicts[i][v] /\ menacing, accumulating the weight of tuples.
                if (!beliefRep.isZero(outsideBelief_i_v)) {
                    for (int k = conflicts_i_v.nextSetBit(0); k >= 0; k = conflicts_i_v.nextSetBit(k + 1)) {
                        if (menacing.get(k)) {
                            belief = beliefRep.add(belief, beliefRep.divide(tupleWeight[k], outsideBelief_i_v));
                        }
                    }
                } else { // special case of null outside belief (avoid division by zero)
                    for (int k = conflicts_i_v.nextSetBit(0); k >= 0; k = conflicts_i_v.nextSetBit(k + 1)) {
                        if (menacing.get(k)) {
                            double weight = beliefRep.one();
                            for (int i2 = 0; i2 < i; i2++) {
                                weight = beliefRep.multiply(weight, outsideBelief(i2, table[k][i2]));
                            }
                            for (int i2 = i + 1; i2 < xLength; i2++) {
                                weight = beliefRep.multiply(weight, outsideBelief(i2, table[k][i2]));
                            }
                            belief = beliefRep.add(belief, weight);
                        }
                    }
		}
                setLocalBelief(i, v, beliefRep.complement(belief));
	    }
	}
    }

}
