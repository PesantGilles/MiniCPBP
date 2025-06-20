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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Markov Decision Process Constraint 
 */
public class Markov extends AbstractConstraint {
    private IntVar[] actions;
    private IntVar[] states;
    private double[][][] proba;
    private int[][][] reward;
    private int initialState;
    private IntVar totalReward;
    private int n;
    private int nbStates;
    private int[] stateDomainValues1;
    private int[] stateDomainValues2;
    List<Integer> supportedState;
    private double[] stateBelief;
    private double[][] ip; // ip[i][]>0 for states reached by performing x[0]..x[i-1] from the initial state
    private double[][] op; // op[i][]>0 for states reaching a final state by performing x[i+1]..x[n-1]
    private int[][] iminp; // iminp[i][j] = smallest reward reaching state (i,j) by performing x[0]..x[i-1] from the initial state
    private int[][] ominp; // ominp[i][j] = smallest reward from state (i,j) to a final state by performing x[i+1]..x[n-1]
    private int[][] imaxp; // iminp[i][j] = largest reward reaching state (i,j) by performing x[0]..x[i-1] from the initial state
    private int[][] omaxp; // ominp[i][j] = largest reward from state (i,j) to a final state by performing x[i+1]..x[n-1]
    private boolean marginals4tr; // flag for this computationally expensive option
    private HashMap<Integer,Double>[][] allRewards; // allRewards[i][j] = list of all <reward value, reward multiplicity> from state (i,j) to a final state by performing x[i+1]..x[n-1]

    /**
     * Creates a constraint to describe a fully-observable, finite, discrete Markov Decision Process (MDP) of order 1 with deterministic rewards.
     * <p> This constraint holds iff the sequence of actions and states (start,a0,s0,...,an-1,sn-1) may occur with non-zero probability and collects a sum of rewards (undiscounted) equal to totalReward.
     *
     * @param a     a sequence of action variables (domain values are nonnegative and start at 0)
     * @param s     a sequence of state variables (domain values are nonnegative and start at 0)
     * @param P     a 3D array giving the transition probability between states given an action: {states} x {actions} x {states} -> [0,1]
     * @param R     a 3D array giving integer rewards: {states} x {actions} x {states} -> Z
     * @param start the initial state
     * @param tr    the total reward of sequence (start,a0,s0,...,an-1,sn-1) computed as the sum of the corresponding integer rewards from array R.
     * @param marginals4tr flag for the computationally expensive option of computing marginals for tr
     */
    public Markov(IntVar[] a, IntVar[] s, double[][][] P, int[][][] R, int start, IntVar tr, boolean marginals4tr, IntVar[] vars) {
        super(a[0].getSolver(), vars);
        setName("Markov");
        actions = a;
        states = s;
        n = a.length;
        nbStates = P.length;
        initialState = start;
        totalReward = tr;
        this.marginals4tr = marginals4tr;
        assert (a.length == s.length);
        assert ((initialState >= 0) && (initialState < nbStates));
	    assert (R.length == nbStates);
        int maxActionVal = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            if (a[i].max() > maxActionVal)
                maxActionVal = a[i].max();
        }
        proba = new double[nbStates][maxActionVal + 1][nbStates];
        for (int i = 0; i < nbStates; i++) {
            assert (P[i].length == maxActionVal + 1);
            for (int j = 0; j < maxActionVal + 1; j++) {
                assert (P[i][j].length == nbStates);
		        double totalProb = 0;
		        for (int k = 0; k < nbStates; k++) {
                    double prob = P[i][j][k];
		            assert ((prob >= 0) && (prob <= 1.0));
		            proba[i][j][k] = prob;
		            totalProb += prob;
		        }
		        assert (totalProb == 1.0);
            }
        }
        reward = new int[nbStates][maxActionVal + 1][nbStates];
        for (int i = 0; i < nbStates; i++) {
            assert (R[i].length == maxActionVal + 1);
            for (int j = 0; j < maxActionVal + 1; j++) {
                assert (R[i][j].length == nbStates);
		        for (int k = 0; k < nbStates; k++) {
		            reward[i][j][k] = R[i][j][k];
		        }
            }
        }
        stateDomainValues1 = new int[nbStates];
        stateDomainValues2 = new int[nbStates];
        supportedState = new ArrayList<Integer>();
        stateBelief = new double[nbStates];
        ip = new double[n][nbStates];
        op = new double[n][nbStates];
        iminp = new int[n][nbStates];
        ominp = new int[n][nbStates];
        imaxp = new int[n][nbStates];
        omaxp = new int[n][nbStates];
        if (marginals4tr) {
            allRewards = (HashMap<Integer, Double>[][]) new HashMap[n][nbStates];
            for (int i = 0; i < n; i++) {
                for (int k = 0; k < nbStates; k++) {
                    allRewards[i][k] = new HashMap<Integer, Double>();
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
                for (IntVar var : actions)
                    var.propagateOnDomainChange(this);
                for (IntVar var : states)
                    var.propagateOnDomainChange(this);
                totalReward.propagateOnBoundChange(this);
        }
        propagate();
    }

    @Override
    public void propagate() {
        int s, size1, size2;
        for (int i = 0; i < n; i++) {
            Arrays.fill(ip[i], 0);
            Arrays.fill(iminp[i], Integer.MAX_VALUE);
            Arrays.fill(imaxp[i], Integer.MIN_VALUE);
        }
        // Reach forward
        ip[0][initialState] = 1;
        iminp[0][initialState] = 0;
        imaxp[0][initialState] = 0;
        // i = 0
        s = actions[0].fillArray(domainValues);
        size2 = states[0].fillArray(stateDomainValues2);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            for (int l = 0; l < size2; l++) {
                int sl = stateDomainValues2[l];
                if (proba[initialState][v][sl] > 0) {
                    ip[1][sl] = 1;
                    iminp[1][sl] = Math.min(iminp[1][sl], iminp[0][initialState] + reward[initialState][v][sl]);
                    imaxp[1][sl] = Math.max(imaxp[1][sl], imaxp[0][initialState] + reward[initialState][v][sl]);
                }
            }
        }
        // i > 0
        for (int i = 1; i < n - 1; i++) {
            s = actions[i].fillArray(domainValues);
            size1 = states[i-1].fillArray(stateDomainValues1);
            size2 = states[i].fillArray(stateDomainValues2);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < size1; k++) {
                    int sk = stateDomainValues1[k];
                    if (ip[i][sk] > 0) {
                        for (int l = 0; l < size2; l++) {
                            int sl = stateDomainValues2[l];
                            if (proba[sk][v][sl] > 0) {
                              ip[i + 1][sl] = 1;
                              iminp[i + 1][sl] = Math.min(iminp[i + 1][sl], iminp[i][sk] + reward[sk][v][sl]);
                              imaxp[i + 1][sl] = Math.max(imaxp[i + 1][sl], imaxp[i][sk] + reward[sk][v][sl]);
                    	   }
                       }
                    }
                }
            }
        }

       for (int i = 0; i < n; i++) {
            Arrays.fill(op[i], 0);
            Arrays.fill(ominp[i], Integer.MAX_VALUE);
            Arrays.fill(omaxp[i], Integer.MIN_VALUE);
        }
        // Reach backward and remove unsupported var/val pairs
        size1 = states[n-1].fillArray(stateDomainValues1);
        for (int k = 0; k < size1; k++) {
            int sk = stateDomainValues1[k];
            op[n - 1][sk] = 1;
            ominp[n - 1][sk] = 0;
            omaxp[n - 1][sk] = 0;
        }
        for (int i = n - 1; i > 0; i--) {
            supportedState.clear();
            s = actions[i].fillArray(domainValues);
            size1 = states[i - 1].fillArray(stateDomainValues1);
            size2 = states[i].fillArray(stateDomainValues2);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                boolean supported = false;
                for (int k = 0; k < size1; k++) {
                    int sk = stateDomainValues1[k];
                    for (int l = 0; l < size2; l++) {
                        int sl = stateDomainValues2[l];
                        if ((proba[sk][v][sl] > 0) && (op[i][sl] > 0) &&
                                (iminp[i][sk] + reward[sk][v][sl] + ominp[i][sl] <= totalReward.max()) && // reward-based reasoning
                                (imaxp[i][sk] + reward[sk][v][sl] + omaxp[i][sl] >= totalReward.min())) {
                            op[i - 1][sk] = 1;
                            ominp[i - 1][sk] = Math.min(ominp[i - 1][sk], ominp[i][sl] + reward[sk][v][sl]);
                            omaxp[i - 1][sk] = Math.max(omaxp[i - 1][sk], omaxp[i][sl] + reward[sk][v][sl]);
                            if (ip[i][sk] > 0) {
                                supported = true;
                                supportedState.add(sl);
                            }
                        }
                    }
                }
                if (!supported) {// sat-based filtering
                    actions[i].remove(v);
                }
            }
            for (int l = 0; l < size2; l++) {
                int sl = stateDomainValues2[l];
                if (!supportedState.contains(sl)) {
                    states[i].remove(sl);
                }
            }
        }
        int shortestPath = Integer.MAX_VALUE;
        int longestPath = Integer.MIN_VALUE;
        supportedState.clear();
        s = actions[0].fillArray(domainValues);
        size2 = states[0].fillArray(stateDomainValues2);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            boolean supported = false;
            for (int l = 0; l < size2; l++) {
                int sl = stateDomainValues2[l];
            	if ((proba[initialState][v][sl] > 0) && (op[0][sl] > 0) &&
                    (reward[initialState][v][sl] + ominp[0][sl] <= totalReward.max()) && // reward-based reasoning
                    (reward[initialState][v][sl] + omaxp[0][sl] >= totalReward.min())) {
                   shortestPath = Math.min(shortestPath, reward[initialState][v][sl] + ominp[0][sl]);
                   longestPath = Math.max(longestPath, reward[initialState][v][sl] + omaxp[0][sl]);
                   supported = true;
                   supportedState.add(sl);
            	}
            }
	        if (!supported) {// sat-based filtering
                actions[0].remove(v);
            }
        }
        for (int l = 0; l < size2; l++) {
            int sl = stateDomainValues2[l];
            if (!supportedState.contains(sl)) {
                states[0].remove(sl);
            }
        }
        // adjust bounds of totalReward variable
        totalReward.removeBelow(shortestPath);
        totalReward.removeAbove(longestPath);
    }

    @Override
    public void updateBelief() {
        int s, size1, size2;
        for (int i = 0; i < n; i++) {
            Arrays.fill(ip[i], beliefRep.zero());
            Arrays.fill(iminp[i], Integer.MAX_VALUE);
            Arrays.fill(imaxp[i], Integer.MIN_VALUE);
        }
        // Reach forward
        ip[0][initialState] = beliefRep.one();
        iminp[0][initialState] = 0;
        imaxp[0][initialState] = 0;
        // i = 0
        s = actions[0].fillArray(domainValues);
        size2 = states[0].fillArray(stateDomainValues2);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            for (int l = 0; l < size2; l++) {
                int sl = stateDomainValues2[l];
                if (proba[initialState][v][sl] > 0) {
                    ip[1][sl] = beliefRep.add(ip[1][sl], beliefRep.multiply(ip[0][initialState], beliefRep.multiply(beliefRep.std2rep(proba[initialState][v][sl]), outsideBelief(0, v))));
                    iminp[1][sl] = Math.min(iminp[1][sl], iminp[0][initialState] + reward[initialState][v][sl]);
                    imaxp[1][sl] = Math.max(imaxp[1][sl], imaxp[0][initialState] + reward[initialState][v][sl]);
                }
            }
        }
        // i > 0
        for (int i = 1; i < n - 1; i++) {
            s = actions[i].fillArray(domainValues);
            size1 = states[i-1].fillArray(stateDomainValues1);
            size2 = states[i].fillArray(stateDomainValues2);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < size1; k++) {
                    int sk = stateDomainValues1[k];
                    if (!beliefRep.isZero(ip[i][sk])) {
                        for (int l = 0; l < size2; l++) {
                            int sl = stateDomainValues2[l];
			                if (proba[sk][v][sl] > 0) {
                               ip[i + 1][sl] = beliefRep.add(ip[i + 1][sl], beliefRep.multiply(ip[i][sk], beliefRep.multiply(beliefRep.std2rep(proba[sk][v][sl]), outsideBelief(i, v))));
                               iminp[i + 1][sl] = Math.min(iminp[i + 1][sl], iminp[i][sk] + reward[sk][v][sl]);
                               imaxp[i + 1][sl] = Math.max(imaxp[i + 1][sl], imaxp[i][sk] + reward[sk][v][sl]);
                           }
                       }
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            Arrays.fill(op[i], beliefRep.zero());
            Arrays.fill(ominp[i], Integer.MAX_VALUE);
            Arrays.fill(omaxp[i], Integer.MIN_VALUE);
            if (marginals4tr) {
                for (int k = 0; k < nbStates; k++) {
                    allRewards[i][k].clear();
                }
            }
        }
        // Reach backward and set local beliefs
        size1 = states[n-1].fillArray(stateDomainValues1);
        for (int k = 0; k < size1; k++) {
            int sk = stateDomainValues1[k];
            op[n - 1][sk] = beliefRep.one();
            ominp[n - 1][sk] = 0;
            omaxp[n - 1][sk] = 0;
            if (marginals4tr) {
                allRewards[n - 1][sk].put(0, 1.0);
            }
        }
        for (int i = n - 1; i > 0; i--) {
            s = actions[i].fillArray(domainValues);
            size1 = states[i-1].fillArray(stateDomainValues1);
            size2 = states[i].fillArray(stateDomainValues2);
            for (int l = 0; l < size2; l++) {
                stateBelief[stateDomainValues2[l]] = beliefRep.zero();
            }
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                double belief = beliefRep.zero();
                for (int k = 0; k < size1; k++) {
                    int sk = stateDomainValues1[k];
                    for (int l = 0; l < size2; l++) {
                        int sl = stateDomainValues2[l];
			            if ((proba[sk][v][sl] > 0) && (!beliefRep.isZero(op[i][sl])) &&
			                (iminp[i][sk] + reward[sk][v][sl] + ominp[i][sl] <= totalReward.max()) && // reward-based reasoning
			                (imaxp[i][sk] + reward[sk][v][sl] + omaxp[i][sl] >= totalReward.min())) {
                            op[i - 1][sk] = beliefRep.add(op[i - 1][sk], beliefRep.multiply(op[i][sl], beliefRep.multiply(beliefRep.std2rep(proba[sk][v][sl]), outsideBelief(i, v))));
                            ominp[i - 1][sk] = Math.min(ominp[i - 1][sk], ominp[i][sl] + reward[sk][v][sl]);
                            omaxp[i - 1][sk] = Math.max(omaxp[i - 1][sk], omaxp[i][sl] + reward[sk][v][sl]);
                            belief = beliefRep.add(belief, beliefRep.multiply(ip[i][sk], beliefRep.multiply(beliefRep.std2rep(proba[sk][v][sl]), op[i][sl])));
                            stateBelief[sl] = beliefRep.add(stateBelief[sl], beliefRep.multiply(ip[i][sk], beliefRep.multiply(beliefRep.std2rep(proba[sk][v][sl]), op[i][sl])));
                            if (marginals4tr) {
                                for (Map.Entry<Integer, Double> set :
                                    allRewards[i][sl].entrySet()) {
                                    int newReward = set.getKey() + reward[sk][v][sl];
                                    if (allRewards[i - 1][sk].containsKey(newReward)) {
                                        allRewards[i - 1][sk].replace(newReward, allRewards[i - 1][sk].get(newReward) + set.getValue() * beliefRep.rep2std(outsideBelief(i, v)) * proba[sk][v][sl]);
                                    } else {
                                        allRewards[i - 1][sk].put(newReward, set.getValue() * beliefRep.rep2std(outsideBelief(i, v)) * proba[sk][v][sl]);
                                    }
                                }
                            }
                        }
                    }
                }
                // NOTE: does not take into account the outside beliefs of totalReward (TODO?)
                setLocalBelief(i, v, belief);
            }
            for (int l = 0; l < size2; l++) {
                int sl = stateDomainValues2[l];
                setLocalBelief(n+i, sl, stateBelief[sl]);
            }
        }
        if (marginals4tr) {
            s = totalReward.fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                setLocalBelief(2 * n, domainValues[j], beliefRep.zero());
            }
        }
        s = actions[0].fillArray(domainValues);
        size2 = states[0].fillArray(stateDomainValues2);
        for (int l = 0; l < size2; l++) {
            stateBelief[stateDomainValues2[l]] = beliefRep.zero();
        }
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            double belief = beliefRep.zero();
            for (int l = 0; l < size2; l++) {
                int sl = stateDomainValues2[l];
                if ((proba[initialState][v][sl] > 0) && !beliefRep.isZero(op[0][sl]) &&
                    (reward[initialState][v][sl] + ominp[0][sl] <= totalReward.max()) && // reward-based reasoning
                    (reward[initialState][v][sl] + omaxp[0][sl] >= totalReward.min())) {
                    belief = beliefRep.add(belief, beliefRep.multiply(op[0][sl], beliefRep.std2rep(proba[initialState][v][sl])));
                    stateBelief[sl] = beliefRep.add(stateBelief[sl], beliefRep.multiply(op[0][sl], beliefRep.std2rep(proba[initialState][v][sl])));
                    if (marginals4tr) {
                        // set belief for totalReward variable
                        for (Map.Entry<Integer, Double> set :
                                allRewards[0][sl].entrySet()) {
                            int newReward = set.getKey() + reward[initialState][v][sl];
                            if (totalReward.contains(newReward)) {
                                setLocalBelief(2 * n, newReward, beliefRep.add(localBelief(2 * n, newReward), beliefRep.multiply(beliefRep.std2rep(set.getValue()), beliefRep.multiply(outsideBelief(0, v), beliefRep.std2rep(proba[initialState][v][sl])))));
                            }
                        }
                    }
                }
            }
            // NOTE: does not take into account the outside beliefs of totalReward (TODO?)
	        setLocalBelief(0, v, belief);
	    }
        for (int l = 0; l < size2; l++) {
            int sl = stateDomainValues2[l];
            setLocalBelief(n, sl, stateBelief[sl]);
        }
   }

}
