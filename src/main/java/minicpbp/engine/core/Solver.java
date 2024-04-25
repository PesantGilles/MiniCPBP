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

import minicpbp.search.Objective;
import minicpbp.state.StateManager;
import minicpbp.state.StateStack;
import minicpbp.util.Procedure;
import minicpbp.util.Belief;

import java.util.Random;

public interface Solver {

    public enum PropaMode {
	SP /* support propagation (aka standard constraint propagation) */, 
	BP /* belief propagation */, 
        SBP /* first apply support propagation, then belief propagation, and finally support propagation again if belief propagation may have assigned or removed domain values */
    } 

    public enum ConstraintWeighingScheme {
	SAME   /* constraints all have the same weight; = 1.0 (default) */,
	ARITY  /* a constraint's weight is related to its arity; = 1 + arity/total_nb_of_vars */
    }

    /**
     * Posts the constraint, that is call {@link Constraint#post()}, and
     * DOES NOT compute the propagation fix-point. (Different from MiniCP)
     * A {@link minicpbp.util.exception.InconsistencyException} is thrown
     * if by posting the constraint it is proven that there is no solution.
     *
     * @param c the constraint to be posted
     */
    void post(Constraint c);

    /**
     * Schedules the constraint to be propagated by the fix-point.
     *
     * @param c the constraint to be scheduled
     */
    void schedule(Constraint c);

    /**
     * Posts the constraint, that is call {@link Constraint#post()},
     * and optionally computes the propagation fix-point.
     * A {@link minicpbp.util.exception.InconsistencyException} is thrown
     * if by posting the constraint it is proven that there is no solution.
     * @param c the constraint to be posted
     * @param enforceFixpoint if one wants to compute the propagation fix-point after
     */
    void post(Constraint c, boolean enforceFixpoint);

    long trigger();
    long potentialTrigger();

    /**
     * @return the propagation mode
     */
    PropaMode getMode();

    /**
     * Set the propagation mode
     * @param mode
     */
    void setMode(PropaMode mode);

    /**
     * @return the constraint weighing scheme
     */
    ConstraintWeighingScheme getWeighingScheme();

    /**
     * Activate trace of BP if @param traceBP is true
     */
    void setTraceBPFlag(boolean traceBP);
    
    /**
     * Activate trace of search if @param traceSearch is true
     */
    void setTraceSearchFlag(boolean traceSearch);

    /**
     * Activate trace of model's entropy if @param traceEntropy is true
     */
    void setTraceEntropyFlag(boolean traceEntropy);

    /**
     * Set the maximal number of BP iterations before each branching
     * @param maxIter the maximal number of BP iterations
     */
    void setMaxIter(int maxIter);

    /**
     * @return whether message damping is applied
     */
    boolean dampingMessages();

    /**
     * Set damping to true or false, according to @param damp
     */
    void setDamp(boolean damp);

    /**
     * @return damping factor
     */
    double dampingFactor();
    
    /**
     * Set damping factor
     * @param dampingFactor the damping factor
     */
    void setDampingFactor(double dampingFactor);

    /**
     * @return whether previous outside belief has been recorded
     */
    boolean prevOutsideBeliefRecorded();

    /**
     * @return whether we should take action upon zero/one beliefs i.e. remove/assign the corresponding value
     */
    boolean actingOnZeroOneBelief();

    /**
     * @return whether search should be traced
     */
    boolean tracingSearch();

    /**
     * Computes the fix-point with all the scheduled constraints.
     */
    void fixPoint();

    /**
     * Performs belief propagation with all the posted constraints.
     */
    void beliefPropa();

    /**
     * Performs no-bells-and-whistles belief propagation: runs for a specified number of iterations, without damping
     */
    void vanillaBP(int nbIterations);

    /**
     * Propagate following the right mode (fixpoint and/or belief)
     */
    void propagateSolver();

    /**
     * Returns the state manager in charge of the global
     * state of the solver.
     *
     * @return the state manager
     */
    StateManager getStateManager();

    /**
     * Returns the variables registered in the solver.
     *
     * @return the variables
     */
    StateStack<IntVar> getVariables();

    /**
     * Returns the constraints registered in the solver.
     *
     * @return the constraints
     */
    StateStack<Constraint> getConstraints();

    /**
     * Returns the random number generator for the solver.
     *
     * @return random number generator
     */
    Random getRandomNbGenerator();

    /**
     * Returns the belief representation being used (Std or Log)
     *
     * @return the belief representation
     */
    Belief getBeliefRep();

    /**
     * Adds a listener called whenever we start fixPoint.
     *
     * @param listener the listener that is called whenever fixPoint is started
     */
    void onFixPoint(Procedure listener);

    /**
     * Registers the variable for belief propagation.
     *
     * @param x the variable
     */
   void registerVar(IntVar x);
   
   /**
    * 
    * @return the number of branching variables in the model
    */
   int nbBranchingVariables();

    /**
     * Adds a listener called whenever we start beliefPropa.
     *
     * @param listener the listener that is called whenever beliefPropa is started
     */
    void onBeliefPropa(Procedure listener);

    /**
     * Creates a minimization objective on the given variable.
     *
     * @param x the variable to minimize
     * @return an objective that can minimize x
     * @see minicpbp.search.DFSearch#optimize(Objective)
     */
    Objective minimize(IntVar x);

    /**
     * Creates a maximization objective on the given variable.
     *
     * @param x the variable to maximize
     * @return an objective that can maximize x
     * @see minicpbp.search.DFSearch#optimize(Objective)
     */
    Objective maximize(IntVar x);

    /**
     * Forces the boolean variable to be true and then
     * DOES NOT compute the propagation fix-point. (Different from MiniCP)
     *
     * @param b the variable that must be set to true
     */
     void post(BoolVar b);

    /**
     * Forces the boolean variable to be true
     * and optionally computes the propagation fix-point.
     *
     * @param b the variable that must be set to true
     * @param enforceFixpoint if one wants to compute the propagation fix-point after
     */
    void post(BoolVar b, boolean enforceFixpoint);

    /**
     * Samples solutions by restricting the search space using randomly-generated linear modular constraints
     *
     * @param fraction the fraction of the number of solutions we wish to retain (0<fraction<1)
     * @param vars the original branching variables of the model
     * @return an array of branching variables
     */
    IntVar[] sample(double fraction, IntVar[] vars);

    /**
     * @return the minimal arity among all contraints
     */
    int minArity();

    /**
     * Makes MiniCP compute the minimal arity among all constraints
     */
    void computeMinArity();

    /**
     * Computes a global loss function from the constraints.
     */
    double globalLossFct();

    /**
     * Computes gradients for the globalLossFct.
     * !!! SPECIALIZED FOR PLS !!!
     */
    double[][][] globalLossFctGradients();

 }

