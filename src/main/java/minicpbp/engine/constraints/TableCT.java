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

import minicpbp.util.Log;

import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;

import java.util.BitSet;

/**
 * Implementation of Compact Table algorithm described in
 * <p><i>Compact-Table: Efficiently Filtering Table Constraints with Reversible Sparse Bit-Sets</i>
 * Jordan Demeulenaere, Renaud Hartert, Christophe Lecoutre, Guillaume Perez, Laurent Perron, Jean-Charles Régin, Pierre Schaus
 * <p>See <a href="https://www.info.ucl.ac.be/~pschaus/assets/publi/cp2016-compacttable.pdf">The article.</a>
 */
public class TableCT extends AbstractConstraint {
    private IntVar[] x; //variables
    private int xLength;
    private int[][] table; //the table
    private int tableLength;
    private int[] ofs; //offsets for each variable's domain
    //supports[i][v] is the set of tuples supported by x[i]=v
    private BitSet[][] supports;
    //beliefs[i][v] is the accumulated weight of tuples supported by x[i]=v
    private double[][] beliefs;
    //supportedTuples is the set of tuples supported by the current domains of the variables
    private BitSet supportedTuples;
    private BitSet supporti;

    /**
     * Table constraint.
     * <p>The table constraint ensures that
     * {@code x} is a row from the given table.
     * More exactly, there exist some row <i>i</i>
     * such that
     * {@code x[0]==table[i][0], x[1]==table[i][1], etc}.
     *
     * <p>This constraint is sometimes called <i>in extension</i> constraint
     * as the user enumerates the set of solutions that can be taken
     * by the variables.
     *
     * @param x     the non empty set of variables to constrain.
     * @param table the possible set of solutions for x.
     *              The second dimension must be of the same size as the array x.
     */
    public TableCT(IntVar[] x, int[][] table) {
        super(x[0].getSolver(), x);
        setName("TableCT");
        this.x = x;
        this.xLength = x.length;
        this.table = table;
        this.tableLength = table.length;
        ofs = new int[xLength];
        supportedTuples = new BitSet(tableLength);
        supporti = new BitSet(tableLength);

        // Allocate supports and beliefs
        supports = new BitSet[xLength][];
        beliefs = new double[xLength][];
        for (int i = 0; i < xLength; i++) {
            ofs[i] = x[i].min(); // offsets map the variables' domain to start at 0 for supports[][] and beliefs[][]
            supports[i] = new BitSet[x[i].max() - x[i].min() + 1];
            beliefs[i] = new double[x[i].max() - x[i].min() + 1];
            for (int j = 0; j < supports[i].length; j++) {
                supports[i][j] = new BitSet();
            }
        }

        // Set values in supports, which contains all the tuples supported by each var-val pair
        for (int i = 0; i < tableLength; i++) { //i is the index of the tuple (in table)
            for (int j = 0; j < xLength; j++) { //j is the index of the current variable (in x)
                if (x[j].contains(table[i][j])) {
                    supports[j][table[i][j] - ofs[j]].set(i);
                }
            }
        }
        setExactWCounting(true);
    }

    /**
     * Special case using only the first "tableLength" tuples
     *
     * @param x     the non empty set of variables to constrain
     * @param table the possible set of solutions for x.
     *              The second dimension must be of the same size as the array x.
     * @param tableLength the number of tuples i.e. the actual size of the first dimension of table
     */
    public TableCT(IntVar[] x, int[][] table, int tableLength) {
        super(x[0].getSolver(), x);
        setName("TableCT");
        this.x = x;
        this.xLength = x.length;
        this.table = table;
        this.tableLength = tableLength;
        ofs = new int[xLength];
        supportedTuples = new BitSet(tableLength);
        supporti = new BitSet(tableLength);

        // Allocate supports and beliefs
        supports = new BitSet[xLength][];
        beliefs = new double[xLength][];
        for (int i = 0; i < xLength; i++) {
            ofs[i] = x[i].min(); // offsets map the variables' domain to start at 0 for supports[][] and beliefs[][]
            supports[i] = new BitSet[x[i].max() - x[i].min() + 1];
            beliefs[i] = new double[x[i].max() - x[i].min() + 1];
            for (int j = 0; j < supports[i].length; j++) {
                supports[i][j] = new BitSet();
            }
        }

        // Set values in supports, which contains all the tuples supported by each var-val pair
        for (int i = 0; i < tableLength; i++) { //i is the index of the tuple (in table)
            for (int j = 0; j < xLength; j++) { //j is the index of the current variable (in x)
                if (x[j].contains(table[i][j])) {
                    supports[j][table[i][j] - ofs[j]].set(i);
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

        for (int i = 0; i < xLength; i++) {
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
        // Initialize beliefs as well.
        supportedTuples.set(0, tableLength); // set them all to true
        for (int i = 0; i < xLength; i++) {
            supporti.clear(); // set them all to false
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                supporti.or(supports[i][domainValues[j] - ofs[i]]);
                beliefs[i][domainValues[j] - ofs[i]] = beliefRep.zero();
            }
            supportedTuples.and(supporti);
        }

        // Each tuple has its own weight given by the product of the outside_belief of its elements.
        // Compute these products, but only for supported tuples.
        // At the same time, distribute these weights to the appropriate i,v pairs
        for (int k = supportedTuples.nextSetBit(0); k >= 0; k = supportedTuples.nextSetBit(k + 1)) {
            double tupleWeight = beliefRep.one();
            for (int i = 0; i < xLength; i++) {
                tupleWeight = beliefRep.multiply(tupleWeight, outsideBelief(i, table[k][i]));
            }
            for (int i = 0; i < xLength; i++) {
                int j = table[k][i] - ofs[i];
                if (!beliefRep.isZero(outsideBelief(i, table[k][i])))
                    beliefs[i][j] = beliefRep.add(beliefs[i][j], beliefRep.divide(tupleWeight, outsideBelief(i, table[k][i])));
                else { // Do we really need to handle the special case of null outside belief (division by zero)?
                    double weight = beliefRep.one();
                    for (int i2 = 0; i2 < i; i2++) {
                        weight = beliefRep.multiply(weight, outsideBelief(i2, table[k][i2]));
                    }
                    for (int i2 = i + 1; i2 < xLength; i2++) {
                        weight = beliefRep.multiply(weight, outsideBelief(i2, table[k][i2]));
                    }
                    beliefs[i][j] = beliefRep.add(beliefs[i][j], weight);
                }
            }
        }

        for (int i = 0; i < xLength; i++) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                setLocalBelief(i, v, beliefs[i][v - ofs[i]]);
            }
        }
    }

    // FOR SIMPLE COUNTING:
    // the frequency of x[i]=v is given by (supports[i][v] /\ supportedTuples).cardinality()

    @Override
    public double weightedCounting() {

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

        double weightedCount = beliefRep.zero();

        // Each tuple has its own weight given by the product of the outside_belief of its elements.
        // Compute these products, but only for supported tuples.
        for (int k = supportedTuples.nextSetBit(0); k >= 0; k = supportedTuples.nextSetBit(k + 1)) {
            double tupleWeight = beliefRep.one();
            for (int i = 0; i < xLength; i++) {
                tupleWeight = beliefRep.multiply(tupleWeight, outsideBelief(i, table[k][i]));
            }
            weightedCount = beliefRep.add(weightedCount, tupleWeight);
        }
        Log.constraint("weighted count for "+this.getName()+" constraint: "+weightedCount);
        return weightedCount;
    }
}
