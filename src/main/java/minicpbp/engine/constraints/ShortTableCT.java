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
import minicpbp.util.exception.NotImplementedException;

import java.util.Arrays;
import java.util.BitSet;

import static minicpbp.cp.Factory.minus;

/**
 * Table constraint with short tuples (having {@code *} entries)
 */
public class ShortTableCT extends AbstractConstraint {

    private final IntVar[] x; //variables
    private final int[][] table; //the table
    //supports[i][v] is the set of tuples supported by x[i]=v
    private BitSet[][] supports;
    private int[] dom; // domain iterator

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
        this.x = new IntVar[x.length];
        this.table = table;
        dom = new int[Arrays.stream(x).map(var -> var.size()).max(Integer::compare).get()];

        // Allocate supportedByVarVal
        supports = new BitSet[x.length][];
        for (int i = 0; i < x.length; i++) {
            this.x[i] = minus(x[i], x[i].min()); // map the variables domain to start at 0
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
                    supports[i][table[t][i] - x[i].min()].set(t);
                }
            }
        }
    }

    @Override
    public void post() {
        for (IntVar var : x)
            var.propagateOnDomainChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        // Bit-set of tuple indices all set to 0
        BitSet supportedTuples = new BitSet(table.length);
        supportedTuples.flip(0, table.length);

        for (int i = 0; i < x.length; i++) {
            BitSet supporti = new BitSet();
            int nVal = x[i].fillArray(dom);
            for (int v = 0; v < nVal; v++) {
                supporti.or(supports[i][dom[v]]);
            }
            supportedTuples.and(supporti);
        }

        for (int i = 0; i < x.length; i++) {
            int nVal = x[i].fillArray(dom);
            for (int v = 0; v < nVal; v++) {
                if (!supports[i][dom[v]].intersects(supportedTuples)) {
                    x[i].remove(dom[v]);
                }
            }
        }

    }

}
