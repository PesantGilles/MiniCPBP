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

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
    private static final PropaMode mode = PropaMode.SBP;
    // nb of BP iterations performed
    private static final int beliefPropaMaxIter = 5;
    // apply damping to variable-to-constraint messages
    private static final boolean damping = false;
    // damping factor in interval [0,1] where 1 is equivalent to no damping
    private static final double dampingFactor = 0.5;
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
    private static final boolean traceBP = false;
    private static final boolean traceSearch = false;
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

    public PropaMode getMode() {
        return mode;
    }

    public ConstraintWeighingScheme getWeighingScheme() {
        return Wscheme;
    }

    public boolean dampingMessages() {
        return damping;
    }

    public double dampingFactor() {
        return dampingFactor;
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
                        System.out.println(variables.get(i).getName() + variables.get(i).toString());
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
        for (int i = 0; i < constraints.size(); i++) {
            constraints.get(i).receiveMessages();
        }
        for (int i = 0; i < variables.size(); i++) {
            variables.get(i).resetMarginals(); // prepare to receive all the messages from constraints
        }
        for (int i = 0; i < constraints.size(); i++) {
            constraints.get(i).sendMessages();
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
}

