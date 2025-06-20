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
 * CostRegular Constraint
 */
public class CostRegular extends AbstractConstraint {
    private IntVar[] x;
    private int[][] transitionFct;
    private int initialState;
    private List<Integer> finalStates;
    private int[][][] cost;
    private IntVar totalCost;
    private int n;
    private int nbStates;
    private double[][] ip; // ip[i][]>0 for states reached by reading x[0]..x[i-1] from the initial state
    private double[][] op; // op[i][]>0 for states reaching a final state by reading x[i+1]..x[n-1]
    private int[][] iminp; // iminp[i][j] = length of shortest path reaching state (i,j) by reading x[0]..x[i-1] from the initial state
    private int[][] ominp; // ominp[i][j] = length of shortest path from state (i,j) to a final state by reading x[i+1]..x[n-1]
    private int[][] imaxp; // iminp[i][j] = length of longest path reaching state (i,j) by reading x[0]..x[i-1] from the initial state
    private int[][] omaxp; // ominp[i][j] = length of longest path from state (i,j) to a final state by reading x[i+1]..x[n-1]
    private boolean marginals4cost; // flag for this computationally expensive option
    private HashMap<Integer,Double>[][] allCosts; // allCosts[i][j] = list of all <cost value, cost multiplicity> from state (i,j) to a final state by reading x[i+1]..x[n-1]


    /**
     * Creates a cost-regular constraint.
     * <p> This constraint holds iff
     * {@code x is a word recognized by the automaton and whose sum of costs = totalCost}.
     *
     * @param x  an array of variables
     * @param A  a 2D array giving the transition function of the automaton: {states} x {domain values} -> {states} (domain values are nonnegative and start at 0)
     * @param s  the initial state
     * @param f  a list of accepting states
     * @param c  a 3D array giving integer costs for each combination of variable, state, and domain value (in that order)
     * @param tc the total cost of word x computed as the sum of the corresponding integer costs from array c
     * @param marginals4cost flag for the computationally expensive option of computing marginals for tc
     *           <p>
     *           Note: any negative value in A indicates that there is no valid transition from the given state on that given domain value
     */
    public CostRegular(IntVar[] x, int[][] A, int s, List<Integer> f, int[][][] c, IntVar tc, boolean marginals4cost, IntVar[] vars) {
        super(x[0].getSolver(), vars);
        setName("CostRegular");
        this.x = x;
        n = x.length;
        nbStates = A.length;
        initialState = s;
        totalCost = tc;
        this.marginals4cost = marginals4cost;
        assert ((initialState >= 0) && (initialState < nbStates));
	    finalStates = new ArrayList<Integer>();
        Iterator<Integer> itr = f.iterator();
        while (itr.hasNext()) {
            int state = itr.next().intValue();
            assert ((state >= 0) && (state < nbStates));
	        finalStates.add(state);
        }
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            if (x[i].max() > maxVal)
                maxVal = x[i].max();
        }
        transitionFct = new int[nbStates][maxVal + 1];
        for (int i = 0; i < nbStates; i++) {
            assert (A[i].length == maxVal + 1);
            for (int j = 0; j < maxVal + 1; j++) {
                assert (A[i][j] < nbStates);
		        transitionFct[i][j] = A[i][j];
            }
        }
        cost = new int[n][nbStates][maxVal + 1];
        assert (c.length == n);
        for (int i = 0; i < n; i++) {
            assert (c[i].length == nbStates);
            for (int j = 0; j < nbStates; j++) {
                assert (c[i][j].length == maxVal + 1);
                for (int k = 0; k <= maxVal; k++) {
                    cost[i][j][k] = c[i][j][k];
                }
            }
        }
        ip = new double[n][nbStates];
        op = new double[n][nbStates];
        iminp = new int[n][nbStates];
        ominp = new int[n][nbStates];
        imaxp = new int[n][nbStates];
        omaxp = new int[n][nbStates];
        if (marginals4cost) {
            allCosts = (HashMap<Integer, Double>[][]) new HashMap[n][nbStates];
            for (int i = 0; i < n; i++) {
                for (int k = 0; k < nbStates; k++) {
                    allCosts[i][k] = new HashMap<Integer, Double>();
                }
            }
        }
        setExactWCounting(true);
    }

    /**
     * Special case of 2D cost matrix: state x domain value
     */
    public CostRegular(IntVar[] x, int[][] A, int s, List<Integer> f, int[][] c, IntVar tc, boolean marginals4cost, IntVar[] vars) {
        super(x[0].getSolver(), vars);
        setName("CostRegular");
        this.x = x;
        n = x.length;
        nbStates = A.length;
        initialState = s;
        totalCost = tc;
        this.marginals4cost = marginals4cost;
        assert ((initialState >= 0) && (initialState < nbStates));
	    finalStates = new ArrayList<Integer>();
        Iterator<Integer> itr = f.iterator();
        while (itr.hasNext()) {
            int state = itr.next().intValue();
            assert ((state >= 0) && (state < nbStates));
	        finalStates.add(state);
        }
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            if (x[i].max() > maxVal)
                maxVal = x[i].max();
        }
        transitionFct = new int[nbStates][maxVal + 1];
        for (int i = 0; i < nbStates; i++) {
            assert (A[i].length == maxVal + 1);
            for (int j = 0; j < maxVal + 1; j++) {
                assert (A[i][j] < nbStates);
		        transitionFct[i][j] = A[i][j];
            }
        }
        cost = new int[n][nbStates][maxVal + 1];
        assert (c.length == nbStates);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < nbStates; j++) {
                assert (c[j].length == maxVal + 1);
                for (int k = 0; k <= maxVal; k++) {
                    cost[i][j][k] = c[j][k];
                }
            }
        }
        ip = new double[n][nbStates];
        op = new double[n][nbStates];
        iminp = new int[n][nbStates];
        ominp = new int[n][nbStates];
        imaxp = new int[n][nbStates];
        omaxp = new int[n][nbStates];
        if (marginals4cost) {
            allCosts = (HashMap<Integer, Double>[][]) new HashMap[n][nbStates];
            for (int i = 0; i < n; i++) {
                for (int k = 0; k < nbStates; k++) {
                    allCosts[i][k] = new HashMap<Integer, Double>();
                }
            }
        }
        setExactWCounting(true);
    }

    /**
     * Special case of 1D cost matrix: domain value
     */
    public CostRegular(IntVar[] x, int[][] A, int s, List<Integer> f, int[] c, IntVar tc, boolean marginals4cost, IntVar[] vars) {
        super(x[0].getSolver(), vars);
        setName("CostRegular");
        this.x = x;
        n = x.length;
        nbStates = A.length;
        initialState = s;
        totalCost = tc;
        this.marginals4cost = marginals4cost;
        assert ((initialState >= 0) && (initialState < nbStates));
	    finalStates = new ArrayList<Integer>();
        Iterator<Integer> itr = f.iterator();
        while (itr.hasNext()) {
            int state = itr.next().intValue();
            assert ((state >= 0) && (state < nbStates));
	        finalStates.add(state);
        }
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            if (x[i].max() > maxVal)
                maxVal = x[i].max();
        }
        transitionFct = new int[nbStates][maxVal + 1];
        for (int i = 0; i < nbStates; i++) {
            assert (A[i].length == maxVal + 1);
            for (int j = 0; j < maxVal + 1; j++) {
                assert (A[i][j] < nbStates);
		        transitionFct[i][j] = A[i][j];
            }
        }
        cost = new int[n][nbStates][maxVal + 1];
        assert (c.length == maxVal + 1);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < nbStates; j++) {
                for (int k = 0; k <= maxVal; k++) {
                    cost[i][j][k] = c[k];
                }
            }
        }
        ip = new double[n][nbStates];
        op = new double[n][nbStates];
        iminp = new int[n][nbStates];
        ominp = new int[n][nbStates];
        imaxp = new int[n][nbStates];
        omaxp = new int[n][nbStates];
        if (marginals4cost) {
            allCosts = (HashMap<Integer, Double>[][]) new HashMap[n][nbStates];
            for (int i = 0; i < n; i++) {
                for (int k = 0; k < nbStates; k++) {
                    allCosts[i][k] = new HashMap<Integer, Double>();
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
                totalCost.propagateOnBoundChange(this);
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
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < nbStates; k++) {
                    int newState = transitionFct[k][v];
                    if ((newState >= 0) && (ip[i][k] > 0)) {
                        ip[i + 1][newState] = 1;
                        iminp[i + 1][newState] = Math.min(iminp[i + 1][newState], iminp[i][k] + cost[i][k][v]);
                        imaxp[i + 1][newState] = Math.max(imaxp[i + 1][newState], imaxp[i][k] + cost[i][k][v]);
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
        Iterator<Integer> itr = finalStates.iterator();
        while (itr.hasNext()) {
            int tmp = itr.next().intValue();
            op[n - 1][tmp] = 1;
            ominp[n - 1][tmp] = 0;
            omaxp[n - 1][tmp] = 0;
        }
        for (int i = n - 1; i > 0; i--) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                double belief = 0;
                for (int k = 0; k < nbStates; k++) {
                    int newState = transitionFct[k][v];
                    if ((newState >= 0) && (op[i][newState] > 0) &&
                            (iminp[i][k] + cost[i][k][v] + ominp[i][newState] <= totalCost.max()) && // cost-based reasoning
                            (imaxp[i][k] + cost[i][k][v] + omaxp[i][newState] >= totalCost.min())) {
                        op[i - 1][k] = 1;
                        ominp[i - 1][k] = Math.min(ominp[i - 1][k], ominp[i][newState] + cost[i][k][v]);
                        omaxp[i - 1][k] = Math.max(omaxp[i - 1][k], omaxp[i][newState] + cost[i][k][v]);
                        if (ip[i][k] > 0) {
                            belief = 1;
                        }
                    }
                }
                if (belief == 0) {// sat-based filtering
                    x[i].remove(v);
		        }
            }
        }
        int shortestPath = Integer.MAX_VALUE;
        int longestPath = Integer.MIN_VALUE;
        int s = x[0].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            int newState = transitionFct[initialState][v];
            if ((newState >= 0) && (op[0][newState] > 0) &&
                    (cost[0][initialState][v] + ominp[0][newState] <= totalCost.max()) && // cost-based reasoning
                    (cost[0][initialState][v] + omaxp[0][newState] >= totalCost.min())) {
                shortestPath = Math.min(shortestPath, cost[0][initialState][v] + ominp[0][newState]);
                longestPath = Math.max(longestPath, cost[0][initialState][v] + omaxp[0][newState]);
            } else {// sat-based filtering
                x[0].remove(v);

            }
         }
        // adjust bounds of totalCost variable
        totalCost.removeBelow(shortestPath);
        totalCost.removeAbove(longestPath);
    }

    @Override
    public void updateBelief() {
        for (int i = 0; i < n; i++) {
            Arrays.fill(ip[i], beliefRep.zero());
	        Arrays.fill(iminp[i], Integer.MAX_VALUE);
            Arrays.fill(imaxp[i], Integer.MIN_VALUE);
        }
        // Reach forward
        ip[0][initialState] = beliefRep.one();
        iminp[0][initialState] = 0;
        imaxp[0][initialState] = 0;
        for (int i = 0; i < n - 1; i++) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                for (int k = 0; k < nbStates; k++) {
                    int newState = transitionFct[k][v];
                    if ((newState >= 0) && (!beliefRep.isZero(ip[i][k]))) {
                        // add the combination of ip[i][k] and outsideBelief(i,v) to ip[i+1][newState]
                        ip[i + 1][newState] = beliefRep.add(ip[i + 1][newState], beliefRep.multiply(ip[i][k], outsideBelief(i, v)));
                        iminp[i + 1][newState] = Math.min(iminp[i + 1][newState], iminp[i][k] + cost[i][k][v]);
                        imaxp[i + 1][newState] = Math.max(imaxp[i + 1][newState], imaxp[i][k] + cost[i][k][v]);
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            Arrays.fill(op[i], beliefRep.zero());
            Arrays.fill(ominp[i], Integer.MAX_VALUE);
            Arrays.fill(omaxp[i], Integer.MIN_VALUE);
            if (marginals4cost) {
                for (int k = 0; k < nbStates; k++) {
                    allCosts[i][k].clear();
                }
            }
	}
        // Reach backward and set local beliefs
        Iterator<Integer> itr = finalStates.iterator();
        while (itr.hasNext()) {
           int val = itr.next().intValue();
            op[n - 1][val] = beliefRep.one();
            ominp[n - 1][val] = 0;
            omaxp[n - 1][val] = 0;
            if (marginals4cost) {
                allCosts[n - 1][val].put(0, 1.0);
            }
        }
        for (int i = n - 1; i > 0; i--) {
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                double belief = beliefRep.zero();
                for (int k = 0; k < nbStates; k++) {
                    int newState = transitionFct[k][v];
                    if ((newState >= 0) && (!beliefRep.isZero(op[i][newState])) &&
                            (iminp[i][k] + cost[i][k][v] + ominp[i][newState] <= totalCost.max()) && // cost-based reasoning
                            (imaxp[i][k] + cost[i][k][v] + omaxp[i][newState] >= totalCost.min())) {			
                        // add the combination of op[i][newState] and outsideBelief(i,v) to op[i-1][k]
                        op[i - 1][k] = beliefRep.add(op[i - 1][k], beliefRep.multiply(op[i][newState], outsideBelief(i, v)));
                        ominp[i - 1][k] = Math.min(ominp[i - 1][k], ominp[i][newState] + cost[i][k][v]);
                        omaxp[i - 1][k] = Math.max(omaxp[i - 1][k], omaxp[i][newState] + cost[i][k][v]);
                        // add the combination of ip[i][k] and op[i][newState] to belief
                        belief = beliefRep.add(belief, beliefRep.multiply(ip[i][k], op[i][newState]));
                        if (marginals4cost) {
                            for (Map.Entry<Integer, Double> set :
                                    allCosts[i][newState].entrySet()) {
                                int newCost = set.getKey() + cost[i][k][v];
                                if (allCosts[i - 1][k].containsKey(newCost)) {
                                    allCosts[i - 1][k].replace(newCost, allCosts[i - 1][k].get(newCost) + set.getValue() * beliefRep.rep2std(outsideBelief(i, v)));
                                } else {
                                    allCosts[i - 1][k].put(newCost, set.getValue() * beliefRep.rep2std(outsideBelief(i, v)));
                                }
                            }
                        }
                    }
                }
                // NOTE: does not take into account the outside beliefs of totalCost (TODO?)
                setLocalBelief(i, v, belief);
            }
        }
        int s = totalCost.fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            setLocalBelief(n, domainValues[j], beliefRep.zero());
        }
        s = x[0].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            int newState = transitionFct[initialState][v];
            if ((newState >= 0) &&
                    (cost[0][initialState][v] + ominp[0][newState] <= totalCost.max()) && // cost-based reasoning
                    (cost[0][initialState][v] + omaxp[0][newState] >= totalCost.min())) {
                // NOTE: does not take into account the outside beliefs of totalCost (TODO?)
                setLocalBelief(0, v, op[0][newState]);
                if (marginals4cost) {
                    // set belief for totalCost variable
                    for (Map.Entry<Integer, Double> set :
                            allCosts[0][newState].entrySet()) {
                        int newCost = set.getKey() + cost[0][initialState][v];
                        if (totalCost.contains(newCost)) {
                            setLocalBelief(n, newCost, beliefRep.add(localBelief(n, newCost), beliefRep.multiply(beliefRep.std2rep(set.getValue()), outsideBelief(0, v))));
                        }
                    }
                }
            } else {
                // NOTE: does not take into account the outside beliefs of totalCost (TODO?)
                setLocalBelief(0, v, beliefRep.zero());
            }
        }
        /*
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < nbStates; k++) {
                if (!allCosts[i][k].isEmpty())  System.out.println(i+" "+k+" "+allCosts[i][k].toString());
            }
	    }
        */
    }

    @Override
    public double weightedCounting() {
        for (int i = 0; i < n; i++) {
            Arrays.fill(op[i], beliefRep.zero());
        }
        // Reach backward
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
 //                       System.out.println((i-1)+" "+k+" op "+op[i - 1][k]);
                    }
                }
            }
        }
        double weightedCount = beliefRep.zero();
        int s = x[0].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            int newState = transitionFct[initialState][v];
            if (newState >= 0) {
                weightedCount = beliefRep.add(weightedCount, beliefRep.multiply(op[0][newState], outsideBelief(0, v)));
//                System.out.println("0 "+newState+" x "+outsideBelief(0, v));
            }
        }
        System.out.println("weighted count for "+this.getName()+" constraint: "+weightedCount);
        return weightedCount;
    }

}
