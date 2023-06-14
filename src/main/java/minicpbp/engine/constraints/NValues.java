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

import java.util.Arrays;

import minicpbp.engine.core.*;

import static minicpbp.cp.Factory.*;

/**
 * Creates a NValues constraint.
 * This relation is currently enforced by decomposing it into
 * table constraints linking each x[i] to its indicator variables y[i][j] (y[i][j]==1 iff x[i]==vals[j]),
 * isOr constraints o[j] = y[0][j] \/ ... \/ y[x.length-1][j], and
 * a sum constraint o[0]+...+o[nbVals-1]==nbDistinct;
 * hence it is not domain consistent
 *
 * @param x            an array of variables
 * @param nDistinct    a variable corresponding to the number of distinct values occurring in x
 * @return a NValues constraint
 */
public class NValues extends AbstractConstraint {

    private IntVar[] x;
    private IntVar nDistinct;
    private Solver cp;
    private int offset;
    private int nVals;
    private IntVar[][] y;
    private int[][][] indicatorTable;

    public NValues(IntVar[] x, IntVar nDistinct, IntVar dummy) {
        super(x[0].getSolver(), new IntVar[]{dummy}); // not a real constraint so we want a minimal footprint in the superclass' constructor, but with a domain large enough to define an appropriate domainValues array
        setName("NValues");
        this.x = x;
        this.nDistinct = nDistinct;
        cp = x[0].getSolver();
        int minVal = x[0].min();
        int maxVal = x[0].max();
        for (IntVar y : x) {
            if (y.min() < minVal)
                minVal = y.min();
            if (y.max() > maxVal)
                maxVal = y.max();
        }
        offset = minVal;
        nVals = maxVal-minVal+1;
        // TODO: do not consider holes from minVal..maxVal interval
        y = new IntVar[x.length][nVals]; // indicator variables: (y[i][j] == 1) iff (x[i] == pffset+j)
        indicatorTable = new int[x.length][][];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < nVals; j++) {
                y[i][j] = makeIntVar(cp,0,1);
                y[i][j].setName("y" + "[" + i + "]" + "[" + j + "]");
            }
            int s = x[i].fillArray(domainValues);
            indicatorTable[i] = new int[s][nVals + 1];
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < nVals; k++) {
                    indicatorTable[i][j][k] = (offset+k == v ? 1 : 0);
                }
                indicatorTable[i][j][nVals] = v;
            }
        }
        setExactWCounting(false);
    }

    @Override
    public void post() {
        for (int i = 0; i < x.length; i++) {
            IntVar[] vars = Arrays.copyOf(y[i], nVals + 1);
            vars[nVals] = x[i];
            cp.post(table(vars, indicatorTable[i]));
        }
        IntVar[] occurs = new IntVar[nVals];
        for (int j = 0; j < nVals; j++) {
            BoolVar[] vars = new BoolVar[x.length];
            for (int i = 0; i < x.length; i++) {
                vars[i] = makeBoolVar(cp);
                cp.post(equal(vars[i],y[i][j]));
            }
            occurs[j] = makeIntVar(cp,0,1);
            cp.post(equal(occurs[j],isOr(vars)));
        }
        cp.post(sum(occurs,nDistinct));
        setActive(false);
    }

}
