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
import minicpbp.util.GraphUtil;
import minicpbp.util.GraphUtil.Graph;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.exception.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Arc Consistent AllDifferent Constraint
 *
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

    public AllDifferentDC(IntVar... x) {
        super(x[0].getSolver(),x);
        setName("AllDifferentDC");
        this.x = x;
        maximumMatching = new MaximumMatching(x);
        match = new int[x.length];
        this.nVar = x.length;
    }

    @Override
    public void post() {
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
        for (int j = 0; j < nNodes; j++) {
            in[j].clear();
            out[j].clear();
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
        int size = maximumMatching.compute(match);
        if (size < x.length) {
            throw new InconsistencyException();
        }
        updateRange();
        updateGraph();
        int[] scc = GraphUtil.stronglyConnectedComponents(g);
        for (int i = 0; i < nVar; i++) {
            for (int v = minVal; v <= maxVal; v++) {
                if (match[i] != v && scc[i] != scc[v - minVal + nVar]) {
                    x[i].remove(v);
                }
            }
        }
    }
}
