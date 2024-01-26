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

import java.util.BitSet;

/**
 * Table constraint with short tuples (having {@code *} entries)
 */
public class ShortTableCT extends AbstractConstraint {

    private final IntVar[] x; //variables
    private int xLength;
    private final int[][] table; //the table
    private int tableLength;
    private final int star; //the "any" value
    private int[] ofs; //offsets for each variable's domain
    //supports[i][v] is the set of tuples supported by x[i]=v
    private BitSet[][] supports;
    //supportedTuples is the set of tuples supported by the current domains of the variables
    private BitSet supportedTuples;
    private BitSet supporti;
    private double[] tupleWeight;

    /**
     * Create a Table constraint with short tuples.
     * <p>Assignment of {@code x_0=v_0, x_1=v_1,...} only valid if there exists a
     * row {@code (v_0|*,v_1|*, ...)} in the table.
     *
     * @param x     the variables to constraint. x must be non empty.
     * @param table the array of valid solutions (second dimension must be of same size as the array x)
     * @param star  the {@code *} symbol representing "any" value in the table
     */
    public ShortTableCT(IntVar[] x, int[][] table, int star) {
        super(x[0].getSolver(), x);
        setName("ShortTableCT");
        this.x = x;
        this.xLength = x.length;
        this.table = table;
        this.tableLength = table.length;
	    this.star = star;
        ofs = new int[xLength];
        supportedTuples = new BitSet(tableLength);
        supporti = new BitSet(tableLength);
        tupleWeight = new double[tableLength];

        // Allocate supportedByVarVal
        supports = new BitSet[x.length][];
        for (int i = 0; i < x.length; i++) {
            ofs[i] = x[i].min(); // offsets map the variables' domain to start at 0 for supports[][]
            supports[i] = new BitSet[x[i].max() - x[i].min() + 1];
            for (int j = 0; j < supports[i].length; j++)
                supports[i][j] = new BitSet();
        }

        // Set values in supportedByVarVal, which contains all the tuples supported by each var-val pair
        for (int t = 0; t < table.length; t++) { //t is the index of the tuple (in table)
            for (int i = 0; i < x.length; i++) { //i is the index of the current variable (in x)
                if (table[t][i] == star) {
                    for (int v = 0; v < supports[i].length; v++) {
                        supports[i][v].set(t);
                    }
                } else if (x[i].contains(table[t][i])) {
                    supports[i][table[t][i] - ofs[i]].set(t);
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

        supportedTuples.set(0, tableLength); // set them all to true

        for (int i = 0; i < x.length; i++) {
            supporti.clear(); // set them all to false
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                supporti.or(supports[i][domainValues[j] - ofs[i]]);
            }
            supportedTuples.and(supporti);
        }

        for (int i = 0; i < x.length; i++) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                // The condition for removing value v from x[i] is to check if
                // there is no intersection between supportedTuples and the support[i][v]
                int v = domainValues[j];
                if (!supports[i][v - ofs[i]].intersects(supportedTuples)) {
                    x[i].remove(v);
                }
            }
        }
    }

    @Override
    public void updateBelief() {

        // Compute supportedTuples as
        // supportedTuples = (supports[0][x[0].min()] | ... | supports[0][x[0].max()] ) & ... &
        //                   (supports[x.length][x[0].min()] | ... | supports[x.length][x[0].max()] )
        //
        supportedTuples.set(0, tableLength); // set them all to true
        for (int i = 0; i < xLength; i++) {
            supporti.clear(); // set them all to false
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                supporti.or(supports[i][domainValues[j] - ofs[i]]);
            }
            supportedTuples.and(supporti);
        }

        // Each tuple has its own weight given by the product of the outside_belief of its elements.
        // Compute these products, but only for supported tuples.
        for (int k = supportedTuples.nextSetBit(0); k >= 0; k = supportedTuples.nextSetBit(k + 1)) {
            tupleWeight[k] = beliefRep.one();
            for (int i = 0; i < xLength; i++) {
		        if (table[k][i] != star) // otherwise it is the "any" value and we would multiply by one
		            tupleWeight[k] = beliefRep.multiply(tupleWeight[k], outsideBelief(i, table[k][i]));
            }
        }

        for (int i = 0; i < xLength; i++) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                double belief = beliefRep.zero();
                BitSet support_i_v = supports[i][v - ofs[i]];
                // Iterate over supports[i][v] /\ supportedTuples, accumulating the weight of tuples.
                for (int k = support_i_v.nextSetBit(0); k >= 0; k = support_i_v.nextSetBit(k + 1)) {
                    if (supportedTuples.get(k)) {
                        belief = beliefRep.add(belief, tupleWeight[k]);
                    }
                }
                setLocalBelief(i, v, belief);
            }
        }
    }

}
