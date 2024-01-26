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
import minicpbp.util.GraphUtil;
import minicpbp.util.GraphUtil.Graph;
import minicpbp.state.StateSparseSet;
import minicpbp.util.exception.InconsistencyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Domain Consistent AllDifferent Constraint
 * <p>
 * Algorithm described in
 * "A filtering algorithm for constraints of difference in CSPs" J-C. Régin, AAAI-94
 */
public class AllDifferentDC extends AbstractConstraint {

    private IntVar[] x;

    private final MaximumMatching maximumMatching;

    private final int nVar;
    private int nVal;

    // residual graph
    private ArrayList<Integer>[] in;
    private ArrayList<Integer>[] out;
    private int nNodes;
    private Graph g = new Graph() {
        @Override
        public int n() {
            return nNodes;
        }

        @Override
        public Iterable<Integer> in(int idx) {
            return in[idx];
        }

        @Override
        public Iterable<Integer> out(int idx) {
            return out[idx];
        }
    };

    private int[] match;
    private boolean[] matched;

    private int minVal;
    private int maxVal;

    private static final int exactPermanentThreshold = 6;
    private double[][] beliefs;
    private StateSparseSet freeVars; // holds an index for vars
    private StateSparseSet freeVals; // holds the actual values of freeVals
    private double[] gamma;
    private int[] c;
    private int[] permutation;
    private int[] varIndices;
    private int[] vals;
    private double[] rowMax;
    private double[] rowMaxSecondBest;

    public AllDifferentDC(IntVar... x) {
        super(x[0].getSolver(), x);
        setName("AllDifferentDC");
        this.x = x;
        maximumMatching = new MaximumMatching(x);
        match = new int[x.length];
        this.nVar = x.length;

        freeVars = new StateSparseSet(getSolver().getStateManager(), x.length, 0);
        // accumulate values from domains
        SortedSet<Integer> allVals = new TreeSet<Integer>();
        for (IntVar var : x) {
            int s = var.fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                allVals.add(domainValues[j]);
            }
        }
        // remove assigned variables and their values from further consideration
        for (int i = 0; i < x.length; i++) {
            if (x[i].isBound()) {
                freeVars.remove(i);
                int val = x[i].min();
                allVals.remove(val);
                // apply basic fwd checking (because we may not call propagate())
                for (int k = 0; k < i; k++) {
                    x[k].remove(val);
                }
                for (int k = i + 1; k < x.length; k++) {
                    x[k].remove(val);
                }
            }
        }
        if (freeVars.isEmpty()) {
            freeVals = new StateSparseSet(getSolver().getStateManager(), 0, 0); // make it empty as well
            return; // special case of all variables in its scope already being bound
        }
        freeVals = new StateSparseSet(getSolver().getStateManager(), allVals.last().intValue() - allVals.first().intValue() + 1, allVals.first().intValue());
        // remove missing intermediate values from interval domain
        for (int i = allVals.first().intValue() + 1; i < allVals.last().intValue(); i++) {
            if (!allVals.contains(i))
                freeVals.remove(i);
        } // from now on freeVals will be maintained as a superset of the available values,
        // only removing values as they are taken on by a variable

        // allocate enough space for the data structures, even though we will need less and less as we go down the search tree
        beliefs = new double[freeVals.size()][freeVals.size()];
        c = new int[freeVals.size()];
        permutation = new int[freeVals.size()];
        varIndices = new int[freeVars.size()];
        vals = new int[freeVals.size()];
        rowMax = new double[freeVals.size()];
        rowMaxSecondBest = new double[freeVals.size()];
        if (freeVals.size() - 1 <= exactPermanentThreshold) {
            setExactWCounting(true);
        } else {
            setExactWCounting(false); // actually, it will be exact below the threshold, which may happen lower in the search tree
        }
        precompute_gamma(freeVals.size());
    }

    @Override
    public void post() {
        switch (getSolver().getMode()) {
            case BP:
                break;
            case SP:
            case SBP:
                for (int i = 0; i < nVar; i++) {
                    x[i].propagateOnDomainChange(this);
                }
                updateRange();
                matched = new boolean[nVal];
                nNodes = nVar + nVal + 1;
                in = new ArrayList[nNodes];
                out = new ArrayList[nNodes];
                for (int i = 0; i < nNodes; i++) {
                    in[i] = new ArrayList<>();
                    out[i] = new ArrayList<>();
                }
                propagate();
        }
    }

    private void updateRange() {
        minVal = Integer.MAX_VALUE;
        maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < nVar; i++) {
            minVal = Math.min(minVal, x[i].min());
            maxVal = Math.max(maxVal, x[i].max());
        }
        nVal = maxVal - minVal + 1;
    }


    private void updateGraph() {
        nNodes = nVar + nVal + 1;
        int sink = nNodes - 1;
        for (int i = 0; i < nNodes; i++) {
            in[i].clear();
            out[i].clear();
        }
        Arrays.fill(matched, 0, nVal, false);
        for (int i = 0; i < x.length; i++) {
            in[i].add(match[i] - minVal + x.length);
            out[match[i] - minVal + nVar].add(i);
            matched[match[i] - minVal] = true;
        }
        for (int i = 0; i < nVar; i++) {
            for (int v = x[i].min(); v <= x[i].max(); v++) {
                if (x[i].contains(v) && match[i] != v) {
                    in[v - minVal + nVar].add(i);
                    out[i].add(v - minVal + nVar);
                }
            }
        }
        for (int v = minVal; v <= maxVal; v++) {
            if (!matched[v - minVal]) {
                in[sink].add(v - minVal + nVar);
                out[v - minVal + nVar].add(sink);
            } else {
                in[v - minVal + nVar].add(sink);
                out[sink].add(v - minVal + nVar);
            }
        }
    }


    @Override
    public void propagate() {
        // update the maximum matching
        int size = maximumMatching.compute(match);
        if (size < x.length) {
            throw new InconsistencyException();
        }
        // update the range of values
        updateRange();
        // update the residual graph
        updateGraph();
        // compute SCC's
        int[] scc = GraphUtil.stronglyConnectedComponents(g);
        for (int i = 0; i < nVar; i++) {
            for (int v = minVal; v <= maxVal; v++) {
                if (match[i] != v && scc[i] != scc[v - minVal + nVar]) {
                    x[i].remove(v);
                }
            }
        }
    }

    @Override
    public void updateBelief() {
        int nbVar, nbVal;
        // update freeVars/Vals according to bound variables
        nbVar = freeVars.fillArray(varIndices);
        for (int j = 0; j < nbVar; j++) {
            int i = varIndices[j];
            if (x[i].isBound()) {
                freeVars.remove(i);
                int val = x[i].min();
                freeVals.remove(val);
                // set trivial local belief for bound var...
                setLocalBelief(i, val, beliefRep.one());
                // ...and for other vars on that value
                for (int k = 0; k < j; k++) {
                    int l = varIndices[k];
                    if (x[l].contains(val))
                        setLocalBelief(l, val, beliefRep.zero());
                }
                for (int k = j + 1; k < nbVar; k++) {
                    int l = varIndices[k];
                    if (x[l].contains(val))
                        setLocalBelief(l, val, beliefRep.zero());
                }
            }
        }
        nbVar = freeVars.fillArray(varIndices);
        nbVal = freeVals.fillArray(vals);
        /*  upon experimentation, does not appear to speed up the computation
        if (nbVal - 1 > exactPermanentThreshold // && "domain size much smaller than nbVal"
            ) {
            // set local beliefs by computing the approximate permanent of beliefs directly from the domains (no matrix representation)
            setExactWCounting(false);
            costBasedPermanent_UB3_precomputeRowMax_sparseMatrix(nbVar);
            for (int j = 0; j < nbVar; j++) {
                int i = varIndices[j];
                int s = x[i].fillArray(domainValues);
                for (int k = 0; k < s; k++) {
                    int val = domainValues[k];
                    // note: will be normalized later in AbstractConstraint.sendMessages()
                    // put beliefs back to their original representation
                    setLocalBelief(i, val, beliefRep.std2rep(costBasedPermanent_UB3_faster_sparseMatrix(j, val, nbVar)));
                }
            }
            return;
        }
        */
        // initialize outside beliefs matrix (MUST BE IN STANDARD [0,1] REPRESENTATION)
        for (int j = 0; j < nbVar; j++) {
            int i = varIndices[j];
            for (int k = 0; k < nbVal; k++) {
                int val = vals[k];
                beliefs[j][k] = (x[i].contains(val) ? beliefRep.rep2std(outsideBelief(i, val)) : 0);
            }
        }
        // may need to add dummy rows in order to make the beliefs matrix square
        for (int j = 0; j < nbVal - nbVar; j++) {
            for (int k = 0; k < nbVal; k++) {
                // make row sum to 1 because we use this property in costBasedPermanent_UB3_faster()
                beliefs[nbVar + j][k] = 1.0 / nbVal; // (STANDARD REPRESENTATION)
            }
        }
        // set local beliefs by computing the permanent of beliefs sub-matrices
        if (nbVal - 1 <= exactPermanentThreshold) {
            // exact permanent
            setExactWCounting(true);
            for (int j = 0; j < nbVar; j++) {
                int i = varIndices[j];
                for (int k = 0; k < nbVal; k++) {
                    int val = vals[k];
                    if (x[i].contains(val)) {
                        // note: will be normalized later in AbstractConstraint.sendMessages()
                        // put beliefs back to their original representation
                        setLocalBelief(i, val, beliefRep.multiply(outsideBelief(i, val), beliefRep.std2rep(costBasedPermanent_exact(j, k, nbVal))));
                    }
                }
            }
        } else {
            // approximate permanent
            setExactWCounting(false);
            costBasedPermanent_UB3_precomputeRowMax(nbVal);
            for (int j = 0; j < nbVar; j++) {
                int i = varIndices[j];
                for (int k = 0; k < nbVal; k++) {
                    int val = vals[k];
                    if (x[i].contains(val)) {
                        // note: will be normalized later in AbstractConstraint.sendMessages()
                        // put beliefs back to their original representation
                        setLocalBelief(i, val, beliefRep.multiply(outsideBelief(i, val), beliefRep.std2rep(costBasedPermanent_UB3_faster(j, k, nbVal, nbVal - nbVar))));
                    }
                }
            }
        }
    }

    @Override
    public double weightedCounting() {
        double weightedCount = 1.0;
        // contribution of bound variables to the weighted count
        for (int i = 0; i < nVar; i++) {
            if (x[i].isBound()) {
                weightedCount *= beliefRep.rep2std(outsideBelief(i, x[i].min()));
            }
        }
        int nbVar, nbVal;
        // update freeVars/Vals according to bound variables
        nbVar = freeVars.fillArray(varIndices);
        for (int j = 0; j < nbVar; j++) {
            int i = varIndices[j];
            if (x[i].isBound()) {
                freeVars.remove(i);
                freeVals.remove(x[i].min());
            }
        }
        nbVar = freeVars.fillArray(varIndices);
        nbVal = freeVals.fillArray(vals);
        // initialize outside beliefs matrix (MUST BE IN STANDARD [0,1] REPRESENTATION)
        for (int j = 0; j < nbVar; j++) {
            int i = varIndices[j];
            for (int k = 0; k < nbVal; k++) {
                int val = vals[k];
                beliefs[j][k] = (x[i].contains(val) ? beliefRep.rep2std(outsideBelief(i, val)) : 0);
            }
        }
        // may need to add dummy rows in order to make the beliefs matrix square
        for (int j = 0; j < nbVal - nbVar; j++) {
            for (int k = 0; k < nbVal; k++) {
                beliefs[nbVar + j][k] = 1.0 ; // (STANDARD REPRESENTATION)
            }
        }
        if (nbVal <= exactPermanentThreshold) {
            // exact permanent
            setExactWCounting(true);
            weightedCount *= permanent(beliefs, nbVal);
            // that value should actually be divided by (# dummy rows)!
            for (int i=2; i <= nbVal - nbVar; i++)
                weightedCount /= (double) i;
        } else {
            // approximate permanent
            setExactWCounting(false);
            weightedCount *= costBasedPermanent_UB3(-1, -1, nbVal, nbVal - nbVar);
        }
        System.out.println("weighted count for "+this.getName()+" constraint: "+beliefRep.std2rep(weightedCount));
        return beliefRep.std2rep(weightedCount); // put beliefs back to their original representation
    }

    // precompute gamma function up to n+1, to account for small floating-point errors
    private void precompute_gamma(int n) {
        int gamma_threshold = 100; // value of n beyond which we approximate n!
        double factorial = 1.0;
        gamma = new double[n + 2];
        gamma[0] = 1.0;
        for (int i = 1; (i <= n + 1) && (i <= gamma_threshold); i++) {
            factorial *= (double) i;
            gamma[i] = Math.pow(factorial, 1.0 / ((double) i));
        }
        for (int i = gamma_threshold + 1; i <= n + 1; i++) {
            // from n>gamma_threshold, Stirling's formula is a decent approximation of factorial which will avoid intermediate overflow
            gamma[i] = (double) i / Math.E * Math.pow(2 * Math.PI * i, 1.0 / ((double) 2 * i));
        }
    }

    private double costBasedPermanent_UB3(int var, int val, int dim, int nbDummyRows) {
        // permanent upper bound U^3 for nonnegative matrices (from Soules 2003)
        // for matrix m without row of var and column of val
        double U3 = 1.0;
        double rowSum, rowMax, tmp;
        int tmpFloor, tmpCeil;
        int dummyRowCount = nbDummyRows;

        for (int i = 0; i < dim; i++) {
            if (i != var) { // exclude row of var whose belief we are computing
                rowSum = rowMax = 0;
                for (int j = 0; j < dim; j++) {
                    tmp = beliefs[i][j];
                    if (j != val) { // exclude column of val whose belief we are computing
                        rowSum += tmp;
                        if (tmp > rowMax)
                            rowMax = tmp;
                    }
                }
                if (rowMax == 0)
                    return 0;
                tmp = rowSum / rowMax;
                tmpFloor = (int) Math.floor(tmp);
                tmpCeil = (int) Math.ceil(tmp);
                U3 *= rowMax * (gamma[tmpFloor] + (tmp - tmpFloor) * (gamma[tmpCeil] - gamma[tmpFloor]));
                if (dummyRowCount > 1) {
                    // that upper bound should be divided by (# dummy rows)!
                    U3 /= (double) dummyRowCount;
                    dummyRowCount--;
                }
            }
        }
        return U3;
    }

   private void costBasedPermanent_UB3_precomputeRowMax(int dim) {
        double tmp;
        for (int i = 0; i < dim; i++) {
            rowMax[i] = rowMaxSecondBest[i] = 0;
            for (int j = 0; j < dim; j++) {
                tmp = beliefs[i][j];
                if (tmp > rowMax[i]) {
                    rowMaxSecondBest[i] = rowMax[i];
                    rowMax[i] = tmp;
                }
                else if (tmp > rowMaxSecondBest[i]) {
                    rowMaxSecondBest[i] = tmp;
                }
            }
        }
    }
    private void costBasedPermanent_UB3_precomputeRowMax_sparseMatrix(int dim) {
        double tmp;
        for (int i = 0; i < dim; i++) {
            rowMax[i] = rowMaxSecondBest[i] = 0;
            int var = varIndices[i];
            int s = x[var].fillArray(domainValues);
            for (int k = 0; k < s; k++) {
                tmp = beliefRep.rep2std(outsideBelief(var, domainValues[k]));
                if (tmp > rowMax[i]) {
                    rowMaxSecondBest[i] = rowMax[i];
                    rowMax[i] = tmp;
                }
                else if (tmp > rowMaxSecondBest[i]) {
                    rowMaxSecondBest[i] = tmp;
                }
            }
        }
    }
    private double costBasedPermanent_UB3_faster(int var, int val, int dim, int nbDummyRows) {
        // permanent upper bound U^3 for nonnegative matrices (from Soules 2003)
        // for matrix m without row of var and column of val
        // assumes that each row of m sums to one
        double U3 = 1.0;
        double rSum, rMax, tmp;
        int tmpFloor, tmpCeil;
        int dummyRowCount = nbDummyRows;

        for (int i = 0; i < dim; i++) {
            if (i != var) { // exclude row of var whose belief we are computing
                rSum = 1.0 - beliefs[i][val]; // each row of m (beliefs) sums to one
                rMax = (rowMax[i]==beliefs[i][val]? rowMaxSecondBest[i] : rowMax[i]);
                if (rMax == 0)
                    return 0;
                tmp = rSum / rMax;
                tmpFloor = (int) Math.floor(tmp);
                tmpCeil = (int) Math.ceil(tmp);
                U3 *= rMax * (gamma[tmpFloor] + (tmp - tmpFloor) * (gamma[tmpCeil] - gamma[tmpFloor]));
                if (dummyRowCount > 1) {
                    // that upper bound should be divided by (# dummy rows)!
                    U3 /= dummyRowCount;
                    dummyRowCount--;
                }
            }
        }
        return U3;
    }

    private double costBasedPermanent_UB3_faster_sparseMatrix(int var, int val, int dim) {
        // permanent upper bound U^3 for nonnegative matrices (from Soules 2003)
        // for matrix m without row of var and column of val
        // assumes that each row of m sums to one
        double U3 = 1.0;
        double rSum, rMax, tmp;
        int tmpFloor, tmpCeil;

        for (int i = 0; i < dim; i++) {
            if (i != var) { // exclude row of var whose belief we are computing
                int j = varIndices[i];
                if (x[j].contains(val)) {
                    tmp = beliefRep.rep2std(outsideBelief(j, val));
                    rSum = 1.0 - tmp; // each row of m (beliefs) sums to one
                    rMax = (rowMax[i] == tmp ? rowMaxSecondBest[i] : rowMax[i]);
                }
                else {
                    rSum = 1.0;
                    rMax = rowMax[i];
                }
//                System.out.println(var+" "+val+"; "+rSum+" " + rMax);
                if (rMax == 0)
                    return 0;
                tmp = rSum / rMax;
                tmpFloor = (int) Math.floor(tmp);
                tmpCeil = (int) Math.ceil(tmp);
                U3 *= rMax * (gamma[tmpFloor] + (tmp - tmpFloor) * (gamma[tmpCeil] - gamma[tmpFloor]));
            }
        }
        return U3;
    }

    private double costBasedPermanent_exact(int var, int val, int dim) {
        // exact permanent for matrix m without row of var and column of val
        // to be used when m is not too large

        double tmp;

        // swap row "var" and column "val" with last row & column
        for (int j = 0; j < dim; j++) { // swap rows
            tmp = beliefs[var][j];
            beliefs[var][j] = beliefs[dim - 1][j];
            beliefs[dim - 1][j] = tmp;
        }
        for (int i = 0; i < dim; i++) { // swap columns
            tmp = beliefs[i][val];
            beliefs[i][val] = beliefs[i][dim - 1];
            beliefs[i][dim - 1] = tmp;
        }

        // compute permanent of m without last row & column
        double p = permanent(beliefs, dim - 1);

        // swap back
        for (int j = 0; j < dim; j++) { // swap rows
            tmp = beliefs[var][j];
            beliefs[var][j] = beliefs[dim - 1][j];
            beliefs[dim - 1][j] = tmp;
        }
        for (int i = 0; i < dim; i++) { // swap columns
            tmp = beliefs[i][val];
            beliefs[i][val] = beliefs[i][dim - 1];
            beliefs[i][dim - 1] = tmp;
        }

        return p; // that value should actually be divided by (# dummy rows)!
    }

    // compute the permanent of a real matrix through a simple adaptation of Heap's Algorithm that generates all permutations
    private double permanent(double[][] A, int n) {
        double prod = 1.0;
        for (int i = 0; i < n; i++) {
            c[i] = 0;
            permutation[i] = i;
            prod *= A[i][i];
        }
        double perm = prod;
        int i = 0;
        while (i < n) {
            if (c[i] < i) {
                if (i % 2 == 0)
                    prod = swap(A, permutation, 0, i, n, prod);
                else
                    prod = swap(A, permutation, c[i], i, n, prod);
                perm += prod;
                c[i]++;
                i = 0;
            } else {
                c[i] = 0;
                i++;
            }
        }
        return perm;
    }

    // swap the elements at indices i and j in the permutation for Heap's algorithm
    // returns the new prod   
    private double swap(double[][] A, int[] permutation, int i, int j, int n, double prod) {
        int e = permutation[i];
        permutation[i] = permutation[j];
        permutation[j] = e;
        double newFactor = A[i][permutation[i]] * A[j][permutation[j]];
        double oldFactor = A[i][permutation[j]] * A[j][permutation[i]];
        if (newFactor == 0)
            return 0;
        else if (oldFactor == 0) {
            // cannot divide it out -- compute from scratch
            double newProd = 1.0;
            for (int k = 0; k < n; k++)
                newProd *= A[k][permutation[k]];
            return newProd;
        } else
            return prod * newFactor / oldFactor;
    }

}
