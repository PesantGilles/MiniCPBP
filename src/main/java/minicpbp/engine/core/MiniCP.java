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
import minicpbp.state.StateInt;
import minicpbp.state.StateManager;
import minicpbp.state.StateStack;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.Procedure;
import minicpbp.util.Belief;
import minicpbp.util.StdBelief;
import minicpbp.util.LogBelief;
import minicpbp.engine.constraints.LinEqSystemModP;

import java.util.*;

public class MiniCP implements Solver {

    private Queue<Constraint> propagationQueue = new ArrayDeque<>();
    private List<Procedure> fixPointListeners = new LinkedList<>();
    private List<Procedure> beliefPropaListeners = new LinkedList<>();

    private final StateManager sm;

    private StateStack<IntVar> variables;
    private StateStack<Constraint> constraints;

    private Random rand;

    //******** PARAMETERS ********
    // SP  /* support propagation (aka standard constraint propagation) */
    // BP  /* belief propagation */
    // SBP /* first apply support propagation, then belief propagation */
    private static PropaMode mode = PropaMode.SBP;
    // nb of BP iterations performed
    private static int beliefPropaMaxIter = 10;
    // apply damping to variable-to-constraint messages
    private static boolean damping = true;
    // damping factor in interval [0,1] where 1 is equivalent to no damping
    private static double dampingFactor = 0.75;
    // entropy threshold for a variable; below it we should consider that its value is almost certain
    private final double MIN_VAR_ENTROPY = 1.0E-3;
    // entropy tolerance beyond which it is considered different
    private final double ENTROPY_TOLERANCE = 0.01;
    // reset marginals, local beliefs, and previous outside belief before applying BP at each search-tree node
    private static final boolean resetMarginalsBeforeBP = true;
    // take action upon zero/one beliefs: remove/assign the corresponding value
    private static final boolean actOnZeroOneBelief = false;
    // relative decrease of metric to trigger BP; in interval [0,1] where 0 means always trigger
    private static double beliefUpdateThreshold = 0.05;
    // representation of beliefs: either standard (StdBelief: [0..1]) or log (LogBelief: [-infinity..0])
    private final Belief beliefRep = new StdBelief();
    // SAME   /* constraints all have the same weight; = 1.0 (default) */
    // ARITY  /* a constraint's weight is related to its arity; = 1 + (arity - min_arity)/total_nb_of_vars */
    private static final ConstraintWeighingScheme Wscheme = ConstraintWeighingScheme.SAME;

    //****************************

    //***** TRACING SWITCHES *****
    private static boolean traceBP = false;
    private static boolean traceSearch = false;
    private static boolean traceEntropy = false;
    //****************************


    // for message damping
    private boolean prevOutsideBeliefRecorded = false;
    private boolean tuneDamping = true;

    // for weighing constraints
    private double minArity;

    // metric to decide whether or not to update beliefs at a search tree node
    private StateInt sumDomainSizes;
    private long trigger = 0;
    private long potentialTrigger = 0;

    public MiniCP(StateManager sm) {
        this.sm = sm;
        variables = new StateStack<>(sm);
        constraints = new StateStack<>(sm);
        rand = new Random();
        sumDomainSizes = sm.makeStateInt(Integer.MAX_VALUE);
    }

    public MiniCP(StateManager sm, long seed) {
        this.sm = sm;
        variables = new StateStack<>(sm);
        constraints = new StateStack<>(sm);
        rand = new Random(seed);
        sumDomainSizes = sm.makeStateInt(Integer.MAX_VALUE);
    }

    public long trigger() {return trigger;}
    public long potentialTrigger() {return potentialTrigger;}

    @Override
    public StateManager getStateManager() {
        return sm;
    }

    @Override
    public StateStack<IntVar> getVariables() {
        return variables;
    }

    @Override
    public Random getRandomNbGenerator() { return rand; }

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

    public void setTraceEntropyFlag(boolean traceEntropy) {
        MiniCP.traceEntropy = traceEntropy;
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
    public void computeMinArity() {
        this.minArity = Double.MAX_VALUE;
        Iterator<Constraint> iterator = constraints.iterator();
        Constraint c;
        while (iterator.hasNext()) {
            c = iterator.next();
            if (this.minArity > c.arity())
                this.minArity = c.arity();
        }
       iterator = constraints.iterator();
        while (iterator.hasNext()) {
            c = iterator.next();
            double w = 1.0 + (c.arity() - this.minArity)/ ((double) constraints.size());
            c.setWeight(w);
        }

    }

    @Override
    public double minArity() {
        return this.minArity;
    }

    @Override
    public void fixPoint() {
        notifyFixPoint();
        try {
            while(!propagationQueue.isEmpty()) {
                Constraint c = propagationQueue.remove();
                try {
                    propagate(c);
                }
                catch(InconsistencyException e) {
                    // empty the queue and unset the scheduled status
                    c.incrementFailureCount();
                    while (!propagationQueue.isEmpty())
                        propagationQueue.remove().setScheduled(false);
                    throw e;
                }

            }
        }
        catch (NoSuchElementException e) {}
    }

    @Override
    public void onBeliefPropa(Procedure listener) {
        beliefPropaListeners.add(listener);
    }

    private void notifyBeliefPropa() {
        beliefPropaListeners.forEach(s -> s.call());
    }

    @Override
    public int nbBranchingVariables() {
        int count = 0;
        Iterator<IntVar> iterator = variables.iterator();
        while (iterator.hasNext()) {
            if(iterator.next().isForBranching())
                count += 1;
        }
        return count;
    }

    /**
     * Belief Propagation
     * standard version, with two distinct message-passing phases
     * first from variables to constraints, and then from constraints to variables
     */
    @Override
    public void beliefPropa() {
        // First decide whether we trigger BP or simply reuse current marginals
        int sum = 0;
        Iterator<IntVar> iterator = variables.iterator();
        while (iterator.hasNext()) {
            sum += iterator.next().size();
        }
        potentialTrigger++;
        if (sum >= (1.0 - beliefUpdateThreshold) * sumDomainSizes.value()) { // trigger BP only if domains sufficiently changed
            iterator = variables.iterator();
            while (iterator.hasNext()) {
                iterator.next().normalizeMarginals();
            }
            return; // reuse current marginals
        }
        else {
            trigger++;
            sumDomainSizes.setValue(sum);
        }
        notifyBeliefPropa();
        try {
            if (tuneDamping) { // decide & tune message damping; default: once at the root node
                BPtuneDamping();
                tuneDamping = false;
            }
            if (resetMarginalsBeforeBP) {
                // start afresh at each search-tree node
                iterator = variables.iterator();
                while (iterator.hasNext()) {
                    iterator.next().resetMarginals();
                }
                 Iterator<Constraint> iteratorC = constraints.iterator();
                while (iteratorC.hasNext()) {
                    iteratorC.next().resetLocalBelief();
                }
                prevOutsideBeliefRecorded = false;
            }
            double previousEntropy, currentEntropy = 1.0;
            for (int iter = 1; iter <= beliefPropaMaxIter; iter++) {
                BPiteration();
                if (traceBP) {
                    System.out.println("##### after BP iteration " + iter + " #####");
                    for (int i = 0; i < variables.size(); i++) {
                        System.out.print(variables.get(i).getName());
                        System.out.println(variables.get(i).toString());
                    }
                }
                previousEntropy = currentEntropy;
                currentEntropy = problemEntropy();
                double smallEntropy = smallestVariableEntropy();
                if (dampingMessages())
                    prevOutsideBeliefRecorded = true;
                if (traceBP) {
                    System.out.println("problem entropy = " + currentEntropy);
                    System.out.println("smallest variable entropy = " + smallEntropy);
                }
                if (traceEntropy) {
                    double minEntropy = 1;
                    double maxEntropy = 0;
                    double modelEntropy = 0.0;
                    for (int i = 0; i < variables.size(); i++) {
                        if (!variables.get(i).isBound() && variables.get(i).isForBranching()) {
                            if (minEntropy > variables.get(i).entropy() / Math.log(variables.get(i).size()))
                                minEntropy = variables.get(i).entropy() / Math.log(variables.get(i).size());
                            if (maxEntropy < variables.get(i).entropy() / Math.log(variables.get(i).size()))
                                maxEntropy = variables.get(i).entropy() / Math.log(variables.get(i).size());
                            modelEntropy += variables.get(i).entropy() / Math.log(variables.get(i).size());
                        }
                    }
                    modelEntropy = modelEntropy / nbBranchingVariables();
                    System.out.println("model entropy : " + modelEntropy);
                    System.out.println("min entropy : " + minEntropy);
                    System.out.println("max entropy : " + maxEntropy);
                }
                // stopping criteria
                if (currentEntropy == 0) { // either all branching vars are bound or BP says there's no solution
                    break;
                }
                if (smallEntropy <= MIN_VAR_ENTROPY) { // at least one variable with low uncertainty about the value it should take
                    break;
                }
                if ((iter > 1) /* give it a chance to kick in */ && (currentEntropy == previousEntropy)) { // marginals probably did not change either (and won't in the future)
                    break;
                }
                if ((iter > 2) /* give it a chance to stabilize */ && (currentEntropy - previousEntropy > ENTROPY_TOLERANCE)) { // entropy actually increased
                    break;
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
     * No-bells-and-whistles Belief Propagation
     * runs for a specified number of iterations, without message damping
     */
    public void vanillaBP(int nbIterations) {
        notifyBeliefPropa();
        setDamp(false);
        try {
            if (resetMarginalsBeforeBP) {
                // start afresh at each search-tree node
                Iterator<IntVar> iterator = variables.iterator();
                while (iterator.hasNext()) {
                    iterator.next().resetMarginals();
                }
                Iterator<Constraint> iteratorC = constraints.iterator();
                while (iteratorC.hasNext()) {
                    iteratorC.next().resetLocalBelief();
                }
            }
            for (int iter = 1; iter <= nbIterations; iter++) {
                BPiteration();
                if (traceBP) {
                    System.out.println("##### after BP iteration " + iter + " #####");
                    for (int i = 0; i < variables.size(); i++) {
                        System.out.print(variables.get(i).getName());
                        System.out.println(variables.get(i).toString());
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
     * tunes message damping for Belief Propagation according to observed entropy
     */
    private void BPtuneDamping() {
        final double MIN_DAMPING_FACTOR = 0.5;
        final double DAMPING_FACTOR_DELTA = 0.15;
        setDamp(true);
        setDampingFactor(1.0);
        boolean dampingFactorDetermined = false;
        double previousEntropy, currentEntropy;
        double previousDeltaEntropy, currentDeltaEntropy;
        int valleyCount;
        while (!dampingFactorDetermined) {
//            System.out.println("trying DAMPING FACTOR = " + dampingFactor());
            // start afresh
           Iterator<IntVar> iterator = variables.iterator();
            while (iterator.hasNext()) {
                iterator.next().resetMarginals();
            }
            Iterator<Constraint> iteratorC = constraints.iterator();
            while (iteratorC.hasNext()) {
                iteratorC.next().resetLocalBelief();
            }
            prevOutsideBeliefRecorded = false;
            currentEntropy = 1.0;
            currentDeltaEntropy = 0;
            valleyCount = 0;
            dampingFactorDetermined = true;
            // BP dive
            for (int iter = 1; iter <= beliefPropaMaxIter; iter++) {
                BPiteration();
                previousEntropy = currentEntropy;
                previousDeltaEntropy = currentDeltaEntropy;
                currentEntropy = problemEntropy();
                currentDeltaEntropy = currentEntropy - previousEntropy;
                prevOutsideBeliefRecorded = true;
 //               System.out.println("iteration " + iter + "; problem entropy = " + currentEntropy + "; previous entropy = " + previousEntropy);
                if (currentEntropy == 0) { // either all branching vars are bound or BP says there's no solution
                    break;
                }
                if (previousDeltaEntropy <= 0 && currentDeltaEntropy > ENTROPY_TOLERANCE) {
//                    System.out.println("valley");
                    valleyCount++;
                    if (valleyCount >= 2) {
 //                       System.out.println("two valleys ==> oscillation");
                        if (dampingFactor() - DAMPING_FACTOR_DELTA >= MIN_DAMPING_FACTOR) {
                            setDampingFactor(dampingFactor() - DAMPING_FACTOR_DELTA); // increase damping
                            dampingFactorDetermined = false;
                        }
                        break;
                    }
                }
            }
        }
        if (traceBP) {
            System.out.println("FINAL DAMPING FACTOR = " + dampingFactor());
        }
        if (dampingFactor() == 1.0) {
            setDamp(false);
        }
    }

    /**
     * a single iteration of Belief Propagation:
     * from variables to constraints, and then from constraints to variables
     */
    private void BPiteration() {
        Constraint c;
        Iterator<Constraint> iteratorC = constraints.iterator();
        while (iteratorC.hasNext()) {
            c = iteratorC.next();
            if (c.isActive())
                c.receiveMessages();
        }
        Iterator<IntVar> iterator = variables.iterator();
        while (iterator.hasNext()) {
            iterator.next().resetMarginals(); // prepare to receive all the messages from constraints
        }
       iteratorC = constraints.iterator();
        while (iteratorC.hasNext()) {
            c = iteratorC.next();
            if (c.isActive())
                c.sendMessages();
        }
       iterator = variables.iterator();
        while (iterator.hasNext()) {
            iterator.next().normalizeMarginals();
        }
    }

    /**
     * Computes a global loss function from the constraints.
     * Currently it sums the losses from each constraint.
     */
    public double globalLossFct() {
        Iterator<Constraint> iteratorC = constraints.iterator();
        while (iteratorC.hasNext()) {
            iteratorC.next().setAuxVarsMarginalsWCounting();
        }
        double loss = 0;
        Constraint c;
        iteratorC = constraints.iterator();
        while (iteratorC.hasNext()) {
            c = iteratorC.next();
            c.receiveMessagesWCounting();
            loss -= Math.log(beliefRep.rep2std(c.weightedCounting()));
        }
        return loss;
    }

    /**
     * computes and returns the current problem entropy (avg normalized entropy of the variables)
     * note: only considers unbound branching variables
     */
    private double problemEntropy() {
        double sumNormalizedEntropy = 0.0;
        int nbUnboundBranchingVar = 0;
        Iterator<IntVar> iterator = variables.iterator();
        while (iterator.hasNext()) {
            IntVar v = iterator.next();
            if(!v.isBound() && v.isForBranching()){
                sumNormalizedEntropy += v.entropy()/Math.log(v.size());
                nbUnboundBranchingVar ++;
            }
        }
        return (nbUnboundBranchingVar == 0 ? 0.0 : sumNormalizedEntropy / nbUnboundBranchingVar);
    }

    /**
     * computes and returns the smallest variable entropy
     * note: only considers unbound branching variables
     */
    private double smallestVariableEntropy() {
        double minEntropy = Double.MAX_VALUE;
       Iterator<IntVar> iterator = variables.iterator();
        while (iterator.hasNext()) {
            IntVar v = iterator.next();
            if (!v.isBound() && v.isForBranching()) {
                if (v.entropy() < minEntropy)
                    minEntropy = v.entropy();
            }
        }
        return minEntropy;
    }

    /**
     * computes and returns the smallest marginal
     * note: only considers unbound branching variables
     */
    private double smallestMarginal() {
        double minMarginal = Double.MAX_VALUE;
        Iterator<IntVar> iterator = variables.iterator();
        while (iterator.hasNext()) {
            IntVar v = iterator.next();
            if (!v.isBound() && v.isForBranching()) {
                if (v.minMarginal() < minMarginal)
                    minMarginal = v.minMarginal();
            }
        }
        return minMarginal;
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

