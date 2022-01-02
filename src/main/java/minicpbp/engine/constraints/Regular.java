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

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

/**
 * Regular Constraint
 */
public class Regular extends AbstractConstraint {
    private IntVar[] x;
    private int[][] transitionFct;
    private int initialState;
    private List<Integer> finalStates;
    private int n;
    private int nbStates;
    private double[][] ip; // ip[i][]>0 for states reached by reading x[0]..x[i-1] from the initial state
    private double[][] op; // op[i][]>0 for states reaching a final state by reading x[i+1]..x[n-1]

    /**
     * Creates a regular constraint.
     * <p> This constraint holds iff
     * {@code x is a word recognized by the automaton}.
     *
     * @param x an array of variables
     * @param A a 2D array giving the transition function of the automaton: {states} x {domain values} -> {states} (domain values are nonnegative and start at 0)
     * @param s is the initial state
     * @param f a list of accepting states
     *          <p>
     *          Note: any negative value in A indicates that there is no valid transition from the given state on that given domain value
     */
    public Regular(IntVar[] x, int[][] A, int s, List<Integer> f) {
        super(x[0].getSolver(), x);
        setName("Regular");
        this.x = x;
        n = x.length;
        transitionFct = A;
        nbStates = A.length;
        initialState = s;
        finalStates = f;
        assert ((initialState >= 0) && (initialState < nbStates));
        Iterator<Integer> itr = finalStates.iterator();
        while (itr.hasNext()) {
            int state = itr.next().intValue();
            assert ((state >= 0) && (state < nbStates));
        }
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            if (x[i].max() > maxVal)
                maxVal = x[i].max();
        }
        for (int i = 0; i < transitionFct.length; i++) {
            assert (transitionFct[i].length == maxVal + 1);
            for (int j = 0; j < transitionFct[i].length; j++) {
                assert (transitionFct[i][j] < nbStates);
            }
        }

        ip = new double[n][nbStates];
        op = new double[n][nbStates];

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
        for (int i = 0; i < n; i++) {
            Arrays.fill(ip[i], 0);
        }
        // Reach forward
        ip[0][initialState] = 1;
        for (int i = 0; i < n - 1; i++) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < nbStates; k++) {
                    int newState = transitionFct[k][v];
                    if ((newState >= 0) && (ip[i][k] > 0)) {
                        ip[i + 1][newState] = 1;
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            Arrays.fill(op[i], 0);
        }
        // Reach backward and remove unsupported var/val pairs
        Iterator<Integer> itr = finalStates.iterator();
        while (itr.hasNext()) {
            int tmp = itr.next().intValue();
            op[n - 1][tmp] = 1;
        }
        for (int i = n - 1; i > 0; i--) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                double belief = 0;
                for (int k = 0; k < nbStates; k++) {
                    int newState = transitionFct[k][v];
                    if ((newState >= 0) && (op[i][newState] > 0)) {
                        op[i - 1][k] = 1;
                        if (ip[i][k] > 0) {
                            belief = 1;
                        }
                    }
                }
                if (belief == 0)
                    x[i].remove(v);
            }
        }
        int s = x[0].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            int newState = transitionFct[initialState][v];
            if ((newState < 0) || (op[0][newState] == 0)) {
                x[0].remove(v);
            }
        }
    }

    @Override
    public void updateBelief() {

        for (int i = 0; i < n; i++) {
            Arrays.fill(ip[i], beliefRep.zero());
        }
        // Reach forward
        ip[0][initialState] = beliefRep.one();
        for (int i = 0; i < n - 1; i++) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < nbStates; k++) {
                    int newState = transitionFct[k][v];
                    if ((newState >= 0) && (!beliefRep.isZero(ip[i][k]))) {
                        // add the combination of ip[i][k] and outsideBelief(i,v) to ip[i+1][newState]
                        ip[i + 1][newState] = beliefRep.add(ip[i + 1][newState], beliefRep.multiply(ip[i][k], outsideBelief(i, v)));
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            Arrays.fill(op[i], beliefRep.zero());
        }
        // Reach backward and set local beliefs
        Iterator<Integer> itr = finalStates.iterator();
        while (itr.hasNext()) {
            op[n - 1][itr.next().intValue()] = beliefRep.one();
        }
        for (int i = n - 1; i > 0; i--) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                double belief = beliefRep.zero();
                for (int k = 0; k < nbStates; k++) {
                    int newState = transitionFct[k][v];
                    if ((newState >= 0) && (!beliefRep.isZero(op[i][newState]))) {
                        // add the combination of op[i][newState] and outsideBelief(i,v) to op[i-1][k]
                        op[i - 1][k] = beliefRep.add(op[i - 1][k], beliefRep.multiply(op[i][newState], outsideBelief(i, v)));
                        // add the combination of ip[i][k] and op[i][newState] to belief
                        belief = beliefRep.add(belief, beliefRep.multiply(ip[i][k], op[i][newState]));
                    }
                }
                setLocalBelief(i, v, belief);
            }
        }
        int s = x[0].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            int newState = transitionFct[initialState][v];
            if (newState >= 0) {
                setLocalBelief(0, v, op[0][newState]);
            } else
                setLocalBelief(0, v, beliefRep.zero());
        }
    }

}
