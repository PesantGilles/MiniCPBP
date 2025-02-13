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
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

/**
 * Markov Constraint
 */
public class Markov extends AbstractConstraint {
    private IntVar[] actions;
    private double[][][] proba;
    private int[][][] reward;
    private int initialState;
    private List<Integer> acceptingStates;
    private IntVar totalReward;
    private int n;
    private int nbStates;
    private double[][] ip; // ip[i][]>0 for states reached by performing x[0]..x[i-1] from the initial state
    private double[][] op; // op[i][]>0 for states reaching a final state by performing x[i+1]..x[n-1]
    private int[][] iminp; // iminp[i][j] = smallest reward reaching state (i,j) by performing x[0]..x[i-1] from the initial state
    private int[][] ominp; // ominp[i][j] = smallest reward from state (i,j) to a final state by performing x[i+1]..x[n-1]
    private int[][] imaxp; // iminp[i][j] = largest reward reaching state (i,j) by performing x[0]..x[i-1] from the initial state
    private int[][] omaxp; // ominp[i][j] = largest reward from state (i,j) to a final state by performing x[i+1]..x[n-1]
    private HashMap<Integer,Double>[][] allRewards; // allRewards[i][j] = list of all <reward value, reward multiplicity> from state (i,j) to a final state by performing x[i+1]..x[n-1]

    /**
     * Creates a Markov constraint to describe a fully-observable, episodic, finite, discrete Markov Decision Process (MDP).
     * <p> This constraint holds iff the sequence of actions reaches an accepting state with non-zero probability and collects a sum of rewards (undiscounted) equal to totalReward.
     * <p> Note: Despite being episodic, in this implementation one can still move out of an accepting state and continue to collect rewards, i.e. there is no requirement for P to loop on every action with probability 1 at an accepting states and for R to be zero on these loops.
     *
     * @param x     a sequence of action variables (domain values are nonnegative and start at 0)
     * @param P     a 3D array giving the transition probability between states given an action: {states} x {domain values} x {states} -> [0,1]
     * @param R     a 3D array giving integer rewards for each combination of state, domain value, state
     * @param s     the initial state
     * @param f     a list of accepting states
     * @param tr    the total reward of sequence x computed as the sum of the corresponding integer rewards from array R.
     */
    public Markov(IntVar[] x, double[][][] P, int s, List<Integer> f, int[][][] R, IntVar tr, IntVar[] vars) {
        super(x[0].getSolver(), vars);
        setName("Markov");
        actions = x;
        n = x.length;
        nbStates = P.length;
        initialState = s;
        totalReward = tr;
        assert ((initialState >= 0) && (initialState < nbStates));
	    assert (R.length == nbStates);
	    acceptingStates = new ArrayList<Integer>();
        Iterator<Integer> itr = f.iterator();
        while (itr.hasNext()) {
            int state = itr.next().intValue();
            assert ((state >= 0) && (state < nbStates));
	        acceptingStates.add(state);
        }
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            if (x[i].max() > maxVal)
                maxVal = x[i].max();
        }
        proba = new double[nbStates][maxVal + 1][nbStates];
        for (int i = 0; i < nbStates; i++) {
            assert (P[i].length == maxVal + 1);
            for (int j = 0; j < maxVal + 1; j++) {
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
        reward = new int[nbStates][maxVal + 1][nbStates];
        for (int i = 0; i < nbStates; i++) {
            assert (R[i].length == maxVal + 1);
            for (int j = 0; j < maxVal + 1; j++) {
                assert (R[i][j].length == nbStates);
		        for (int k = 0; k < nbStates; k++) {
		            reward[i][j][k] = R[i][j][k];
		        }
            }
        }
        ip = new double[n][nbStates];
        op = new double[n][nbStates];
        iminp = new int[n][nbStates];
        ominp = new int[n][nbStates];
        imaxp = new int[n][nbStates];
        omaxp = new int[n][nbStates];
        allRewards = (HashMap<Integer, Double>[][]) new HashMap[n][nbStates];
        for (int i=0; i<n; i++) {
            for (int k = 0; k < nbStates; k++) {
                allRewards[i][k] = new HashMap<Integer, Double>();
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
                totalReward.propagateOnBoundChange(this);
        }
        propagate();
    }

    @Override
    public void propagate() {
        for (int i = 0; i < n; i++) {
            Arrays.fill(ip[i], 0);
            Arrays.fill(iminp[i], Integer.MAX_VALUE);
            Arrays.fill(imaxp[i], Integer.MIN_VALUE);
        }
        // Reach forward
        ip[0][initialState] = 1;
        iminp[0][initialState] = 0;
        imaxp[0][initialState] = 0;
        for (int i = 0; i < n - 1; i++) {
            int s = actions[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < nbStates; k++) {
                    if (ip[i][k] > 0) {
                       for (int l = 0; l < nbStates; l++) {
                    	   if (proba[k][v][l] > 0) {
                              ip[i + 1][l] = 1;
                              iminp[i + 1][l] = Math.min(iminp[i + 1][l], iminp[i][k] + reward[k][v][l]);
                              imaxp[i + 1][l] = Math.max(imaxp[i + 1][l], imaxp[i][k] + reward[k][v][l]);
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
        Iterator<Integer> itr = acceptingStates.iterator();
        while (itr.hasNext()) {
            int tmp = itr.next().intValue();
            op[n - 1][tmp] = 1;
            ominp[n - 1][tmp] = 0;
            omaxp[n - 1][tmp] = 0;
        }
        for (int i = n - 1; i > 0; i--) {
            int s = actions[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                boolean supported = false;
                for (int k = 0; k < nbStates; k++) {
                    for (int l = 0; l < nbStates; l++) {
                    	if ((proba[k][v][l] > 0) && (op[i][l] > 0) &&
                            (iminp[i][k] + reward[k][v][l] + ominp[i][l] <= totalReward.max()) && // reward-based reasoning
                            (imaxp[i][k] + reward[k][v][l] + omaxp[i][l] >= totalReward.min())) {
                            op[i - 1][k] = 1;
                            ominp[i - 1][k] = Math.min(ominp[i - 1][k], ominp[i][l] + reward[k][v][l]);
                            omaxp[i - 1][k] = Math.max(omaxp[i - 1][k], omaxp[i][l] + reward[k][v][l]);
                            if (ip[i][k] > 0) {
                               supported = true;
                            }
                        }
                    }
                }
                if (!supported) {// sat-based filtering
                    actions[i].remove(v);
                }
            }
        }
        int shortestPath = Integer.MAX_VALUE;
        int longestPath = Integer.MIN_VALUE;
        int s = actions[0].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            boolean supported = false;
            for (int l = 0; l < nbStates; l++) {
            	if ((proba[initialState][v][l] > 0) && (op[0][l] > 0) &&
                    (reward[initialState][v][l] + ominp[0][l] <= totalReward.max()) && // reward-based reasoning
                    (reward[initialState][v][l] + omaxp[0][l] >= totalReward.min())) {
                   shortestPath = Math.min(shortestPath, reward[initialState][v][l] + ominp[0][l]);
                   longestPath = Math.max(longestPath, reward[initialState][v][l] + omaxp[0][l]);
                   supported = true;
            	}
            }
	        if (!supported) {// sat-based filtering
                actions[0].remove(v);
            }
        }
        // adjust bounds of totalReward variable
        totalReward.removeBelow(shortestPath);
        totalReward.removeAbove(longestPath);
    }

    @Override
    public void updateBelief() {
        for (int i = 0; i < n; i++) {
            Arrays.fill(ip[i], beliefRep.zero());
        }
        // Reach forward
        ip[0][initialState] = beliefRep.one();
        for (int i = 0; i < n - 1; i++) {
            int s = actions[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < nbStates; k++) {
                    if (!beliefRep.isZero(ip[i][k])) {
                       for (int l = 0; l < nbStates; l++) {
                    	   if (proba[k][v][l] > 0) {
                               ip[i + 1][l] = beliefRep.add(ip[i + 1][l], beliefRep.multiply(ip[i][k], beliefRep.multiply(beliefRep.std2rep(proba[k][v][l]), outsideBelief(i, v))));
                           }
                       }
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            Arrays.fill(op[i], beliefRep.zero());
            for (int k = 0; k < nbStates; k++) {
                allRewards[i][k].clear();
            }
        }
        // Reach backward and set local beliefs
        Iterator<Integer> itr = acceptingStates.iterator();
        while (itr.hasNext()) {
            int val = itr.next().intValue();
            op[n - 1][val] = beliefRep.one();
            allRewards[n - 1][val].put(0, 1.0);
        }
        for (int i = n - 1; i > 0; i--) {
            int s = actions[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                double belief = beliefRep.zero();
                for (int k = 0; k < nbStates; k++) {
                    for (int l = 0; l < nbStates; l++) {
			            if ((proba[k][v][l] > 0) && (!beliefRep.isZero(op[i][l]))) {
                            op[i - 1][k] = beliefRep.add(op[i - 1][k], beliefRep.multiply(op[i][l], beliefRep.multiply(beliefRep.std2rep(proba[k][v][l]), outsideBelief(i, v))));
                            belief = beliefRep.add(belief, beliefRep.multiply(ip[i][k], beliefRep.multiply(beliefRep.std2rep(proba[k][v][l]), op[i][l])));
                            for (Map.Entry<Integer, Double> set :
                                    allRewards[i][l].entrySet()) {
                                int newReward = set.getKey() + reward[k][v][l];
                                if (allRewards[i - 1][k].containsKey(newReward)) {
                                    allRewards[i - 1][k].replace(newReward, allRewards[i - 1][k].get(newReward) + set.getValue() * beliefRep.rep2std(outsideBelief(i, v)) * proba[k][v][l]);
                                } else {
                                    allRewards[i - 1][k].put(newReward, set.getValue() * beliefRep.rep2std(outsideBelief(i, v)) * proba[k][v][l]);
                                }
                            }
                        }
                    }
                }
                // NOTE: does not take into account the outside beliefs of totalReward (TODO?)
                setLocalBelief(i, v, belief);
            }
        }
        int s = totalReward.fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            setLocalBelief(n, domainValues[j], beliefRep.zero());
        }
        s = actions[0].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            double belief = beliefRep.zero();
            for (int l = 0; l < nbStates; l++) {
                if ((proba[initialState][v][l] > 0) && !beliefRep.isZero(op[0][l])) {
                    belief = beliefRep.add(belief, beliefRep.multiply(op[0][l], beliefRep.std2rep(proba[initialState][v][l])));
                    // set belief for totalReward variable
                    for (Map.Entry<Integer, Double> set :
                            allRewards[0][l].entrySet()) {
                        int newReward = set.getKey() + reward[initialState][v][l];
                        if (totalReward.contains(newReward)) {
                            setLocalBelief(n, newReward, beliefRep.add(localBelief(n, newReward), beliefRep.multiply(beliefRep.std2rep(set.getValue()), beliefRep.multiply(outsideBelief(0,v), beliefRep.std2rep(proba[initialState][v][l])))));
                        }
                    }
                }
            }
            // NOTE: does not take into account the outside beliefs of totalReward (TODO?)
	        setLocalBelief(0, v, belief);
	    }
        // might as well achieve domain consistency on totalReward
        s = totalReward.fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int r = domainValues[j];
            if (beliefRep.isZero(localBelief(n, r))) {
                totalReward.remove(r);
            }
        }
    }

    @Override
    public double weightedCounting() {
        for (int i = 0; i < n; i++) {
            Arrays.fill(op[i], beliefRep.zero());
        }
        Iterator<Integer> itr = acceptingStates.iterator();
        while (itr.hasNext()) {
            int val = itr.next().intValue();
            op[n - 1][val] = beliefRep.one();
        }
        // Reach backward
        for (int i = n - 1; i > 0; i--) {
            int s = actions[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < nbStates; k++) {
                    for (int l = 0; l < nbStates; l++) {
			            if ((proba[k][v][l] > 0) && (!beliefRep.isZero(op[i][l]))) {
                            op[i - 1][k] = beliefRep.add(op[i - 1][k], beliefRep.multiply(op[i][l], beliefRep.multiply(beliefRep.std2rep(proba[k][v][l]), outsideBelief(i, v))));
                        }
                    }
                }
            }
        }
        double weightedCount = beliefRep.zero();
        int s = actions[0].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            for (int l = 0; l < nbStates; l++) {
                if ((proba[initialState][v][l] > 0) && !beliefRep.isZero(op[0][l])) {
                    weightedCount = beliefRep.add(weightedCount, beliefRep.multiply(op[0][l], beliefRep.multiply(beliefRep.std2rep(proba[initialState][v][l]), outsideBelief(0, v))));
                }
            }
	    }
        System.out.println("weighted count for "+this.getName()+" constraint: "+weightedCount);
        return weightedCount;
    }

}
