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

package minicpbp.engine.core;

import minicpbp.cp.Factory;
import minicpbp.search.Objective;
import minicpbp.state.StateManager;
import minicpbp.state.StateStack;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.Procedure;
import minicpbp.util.Belief;
import minicpbp.util.StdBelief;
import minicpbp.util.LogBelief;
import minicpbp.engine.constraints.LinEqSystemModP;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class MiniCP implements Solver {

    private Queue<Constraint> propagationQueue = new ArrayDeque<>();
    private List<Procedure> fixPointListeners = new LinkedList<>();
    private List<Procedure> beliefPropaListeners = new LinkedList<>();

    private final StateManager sm;

    private StateStack<IntVar> variables;
    private StateStack<Constraint> constraints;

    //******** PARAMETERS ********
    // SP  /* support propagation (aka standard constraint propagation) */
    // BP  /* belief propagation */
    // SBP /* first apply support propagation, then belief propagation */
    private static PropaMode mode = PropaMode.SBP;
    // nb of BP iterations performed
    private static int beliefPropaMaxIter = 5;
    // apply damping to variable-to-constraint messages
    private static boolean damping = true;
    // damping factor in interval [0,1] where 1 is equivalent to no damping
    private static double dampingFactor = 0.5;
    // reset marginals, local beliefs, and previous outside belief before applying BP at each search-tree node
    private static final boolean resetMarginalsBeforeBP = true;
    // take action upon zero/one beliefs: remove/assign the corresponding value
    private static final boolean actOnZeroOneBelief = false;
    // representation of beliefs: either standard (StdBelief: [0..1]) or log (LogBelief: [-infinity..0])
    private final Belief beliefRep = new StdBelief();
    // SAME   /* constraints all have the same weight; = 1.0 (default) */
    // ARITY  /* a constraint's weight is related to its arity; = 1 + arity/total_nb_of_vars */
    private static final ConstraintWeighingScheme Wscheme = ConstraintWeighingScheme.SAME;
    //****************************

    //***** TRACING SWITCHES *****
    private static boolean traceBP = true;
    private static boolean traceSearch = true;
    //****************************


    // for message damping
    private boolean prevOutsideBeliefRecorded = false;

    public MiniCP(StateManager sm) {
        this.sm = sm;
        variables = new StateStack<>(sm);
        constraints = new StateStack<>(sm);
    }

    @Override
    public StateManager getStateManager() {
        return sm;
    }

    @Override
    public StateStack<IntVar> getVariables() {
        return variables;
    }

    @Override
    public Belief getBeliefRep() {
        return beliefRep;
    }

    @Override
    public void registerVar(IntVar x) {
        variables.push(x);
    }

    public void setMode(PropaMode mode) {
        MiniCP.mode = mode;
    }

    public PropaMode getMode() {
        return mode;
    }

    public ConstraintWeighingScheme getWeighingScheme() {
        return Wscheme;
    }

    public void setTraceBPFlag(boolean traceBP) {
        MiniCP.traceBP = traceBP;
    }

    public void setTraceSearchFlag(boolean traceSearch) {
        MiniCP.traceSearch = traceSearch;
    }

    public void setMaxIter(int maxIter) {
        MiniCP.beliefPropaMaxIter = maxIter;
    }

    public boolean dampingMessages() {
        return damping;
    }

    public void setDamp(boolean damp) {
        MiniCP.damping = damp;
    }

    public double dampingFactor() {
        return dampingFactor;
    }

    public void setDampingFactor(double dampingFactor) {
        MiniCP.dampingFactor = dampingFactor;
    }

    public boolean prevOutsideBeliefRecorded() {
        return prevOutsideBeliefRecorded;
    }

    public boolean actingOnZeroOneBelief() {
        return actOnZeroOneBelief;
    }

    public boolean tracingSearch() {
        return traceSearch;
    }

    public void schedule(Constraint c) {
        if (c.isActive() && !c.isScheduled()) {
            c.setScheduled(true);
            propagationQueue.add(c);
        }
    }

    @Override
    public void onFixPoint(Procedure listener) {
        fixPointListeners.add(listener);
    }

    private void notifyFixPoint() {
        fixPointListeners.forEach(s -> s.call());
    }

    @Override
    public void fixPoint() {
        notifyFixPoint();
        try {
            while (!propagationQueue.isEmpty()) {
                propagate(propagationQueue.remove());
            }
        } catch (InconsistencyException e) {
            // empty the queue and unset the scheduled status
            while (!propagationQueue.isEmpty())
                propagationQueue.remove().setScheduled(false);
            throw e;
        }
    }

    @Override
    public void onBeliefPropa(Procedure listener) {
        beliefPropaListeners.add(listener);
    }

    private void notifyBeliefPropa() {
        beliefPropaListeners.forEach(s -> s.call());
    }

    /**
     * Belief Propagation
     * standard version, with two distinct message-passing phases
     * first from variables to constraints, and then from constraints to variables
     */
    @Override
    public void beliefPropa() {
        notifyBeliefPropa();
        try {
            if (resetMarginalsBeforeBP) {
                // start afresh at each search-tree node
                for (int i = 0; i < variables.size(); i++) {
                    variables.get(i).resetMarginals();
                }
                for (int i = 0; i < constraints.size(); i++) {
                    constraints.get(i).resetLocalBelief();
                }
                prevOutsideBeliefRecorded = false;
            }
            for (int iter = 1; iter <= beliefPropaMaxIter; iter++) {
                BPiteration();
                if (dampingMessages())
                    prevOutsideBeliefRecorded = true;
                if (traceBP) {
                    System.out.println("##### after BP iteration " + iter + " #####");
                    for (int i = 0; i < variables.size(); i++) {
                        System.out.println(variables.get(i).getName() + " taille : "+variables.get(i).size()+" " + variables.get(i).toString());
                    }
                }
            }

        } catch (InconsistencyException e) {
            // empty the queue and unset the scheduled status
            while (!propagationQueue.isEmpty())
                propagationQueue.remove().setScheduled(false);
            throw e;
        }
    }

    /**
     * Propagate following the right mode
     */
    public void propagateSolver(){
        switch (this.getMode()) {
            case SP:
                this.fixPoint();
                break;
            case BP:
                this.beliefPropa();
                break;
            case SBP:
                this.fixPoint();
                this.beliefPropa();
                break;
        }
    }

    /**
     * a single iteration of Belief Propagation:
     * from variables to constraints, and then from constraints to variables
     */
    private void BPiteration() {
        Constraint c;
        for (int i = 0; i < constraints.size(); i++) {
            c = constraints.get(i);
            if (c.isActive())
                c.receiveMessages();
       }
        for (int i = 0; i < variables.size(); i++) {
            variables.get(i).resetMarginals(); // prepare to receive all the messages from constraints
        }
        for (int i = 0; i < constraints.size(); i++) {
            c = constraints.get(i);
            if (c.isActive())
                c.sendMessages();
        }
        for (int i = 0; i < variables.size(); i++) {
            variables.get(i).normalizeMarginals();
        }
    }

    private void propagate(Constraint c) {
        c.setScheduled(false);
        if (c.isActive())
            c.propagate();
    }

    @Override
    public Objective minimize(IntVar x) {
        return new Minimize(x);
    }

    @Override
    public Objective maximize(IntVar x) {
        return minimize(Factory.minus(x));
    }

    @Override
    public void post(Constraint c) {
        // no incremental propagation -- wait until all constraints have been posted
        post(c, false);
    }

    @Override
    public void post(Constraint c, boolean enforceFixpoint) {
        constraints.push(c);
        c.post();
        if (enforceFixpoint) {
            this.fixPoint();
        }
    }

    @Override
    public void post(BoolVar b) {
        post(b,false);
    }

    @Override
    public void post(BoolVar b, boolean enforceFixpoint){
        b.assign(true);
        if (enforceFixpoint) {
            this.fixPoint();
        }
    }

    @Override
    public IntVar[] sample(double fraction, IntVar[] vars) {
 	final double initialAccuracy = 0.01; // relative error threshold of cell size wrt fraction
 	final double maxNumerator = 100.0; // beyond this, we relax the accuracy
	assert (fraction > 0) && (fraction < 1.0);
	Random rand = new Random();
	// the prime numbers under 100
	int primes[] = {5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97}; // don't use very small primes because it leaves very little room for rhs of inequalities
	int i;
	// find largest domain element
	int maxDomElt = 0;
	for(i=0; i<vars.length; i++) {
	    if (vars[i].max() > maxDomElt)
		maxDomElt = vars[i].max();
	}
	// find the smallest prime at least as large as maxDomElt
	for(i=0; i<primes.length; i++) 
	    if (primes[i] >= maxDomElt) break;
	if (i == primes.length) {
	    System.out.println("Domain values larger than currently recorded primes!");
	    System.exit(0);
	}
	int p = primes[i];
	assert (p <= maxNumerator);
//  	System.out.println("p = "+p);
	// compute a sufficiently accurate combination of m linear modular constraints
	double accuracy = initialAccuracy;
	int m;
	double exactNumerator, floorNum, ceilNum;
	LinkedList<Integer> factors = new LinkedList<>();
	do {
	    m = 0;
	    exactNumerator = fraction;
	    while (factors.isEmpty()) {
		m++;
		exactNumerator *= p;
//   		System.out.println("m="+m+"  exactNum="+exactNumerator+"  epsilon="+accuracy);
		if (Math.abs(exactNumerator - 1.0)/exactNumerator <= accuracy) break; // a system of equality constraints is sufficient
		if (exactNumerator > maxNumerator) {
//   		    System.out.println("numerator is getting too big --- relax accuracy");
		    break;
		}
		floorNum = Math.floor(exactNumerator);
		ceilNum = Math.ceil(exactNumerator);
		if (exactNumerator - floorNum < ceilNum - exactNumerator) {
		    // try with floor first
		    factors = accurateFactorization(floorNum, exactNumerator, accuracy, m, p);
		    if (factors.isEmpty())
			factors = accurateFactorization(ceilNum, exactNumerator, accuracy, m, p);
		}
		else {
		    // try with ceil first
		    factors = accurateFactorization(ceilNum, exactNumerator, accuracy, m, p);
		    if (factors.isEmpty())
			factors = accurateFactorization(floorNum, exactNumerator, accuracy, m, p);
		}
	    }
	    accuracy *= 2;
	} while (exactNumerator > maxNumerator);
//    	System.out.println("factors: "+factors.toString());
	int nbIneq = factors.size();
	int nbEq = m - nbIneq;
//     	System.out.println("nb eq ; ineq "+nbEq+" ; "+nbIneq);
	// set up the linear modular constraints
 	Constraint L = null;
	IntVar[] paramVars;
	if (nbEq>0) {
	    int[][] Ae = new int[nbEq][vars.length];
	    int[] be = new int[nbEq];
	    for (i=0; i<nbEq; i++) {
		be[i] = rand.nextInt(p);
		for (int j=0; j<Ae[i].length; j++) {
		    Ae[i][j] = rand.nextInt(p);
		}
	    }
	    L = Factory.linEqSystemModP(Ae,vars,be,p);
	    this.post(L);
	    paramVars = ((LinEqSystemModP) L).getParamVars(); // parametric variables of GJE solved form
	}
	else {
	    paramVars = vars;
	}
	if (nbIneq>0) { 
	    IntVar[] augmentedVars = Arrays.copyOf(paramVars, paramVars.length + 1);
	    augmentedVars[paramVars.length] = Factory.makeIntVar(this, 1, 1);
	    int[][] Ai = new int[nbIneq][augmentedVars.length];
	    int[] bi = new int[nbIneq];
	    for (i=0; i<nbIneq; i++) {
		bi[i] = factors.remove() - 1;
//   		System.out.println("ineq rhs: "+bi[i]);
		for (int j=0; j<Ai[i].length; j++) {
		    Ai[i][j] = rand.nextInt(p);
		}
	    }
	    L = Factory.linIneqSystemModP(Ai,augmentedVars,bi,p);
	    this.post(L);
	}
	return paramVars; 
    }	    

    private LinkedList<Integer> accurateFactorization(double intNum, double exactNum, double accuracy, int m, int p) {
	if (intNum <= 1.0)
	    return new LinkedList<>(); // cannot be factorized
	double relError = Math.abs(exactNum - intNum) / exactNum;
//   	System.out.println("exactNum="+exactNum+" intNum="+intNum+" rel error="+relError);
	if (relError > accuracy)
	    return new LinkedList<>(); // not accurate enough
	// decompose intNum into the fewest factors (and no more than m), all less than p
	return factorize((int) intNum, m, p-1);
    }
    
    private LinkedList<Integer> factorize(int n, int m, int f) {
	LinkedList<Integer> factors = new LinkedList<>();
	while ((f>1) && (n>1)) {
	    while (n%f==0) {
		factors.add(f);
		n /= f;
	    }
	    f--;
	}
 	if ((n==1) && (factors.size()<=m))
	    return factors;
	else
	    return new LinkedList<>(); // n could not be factorized (in at most m factors)
    }
}

