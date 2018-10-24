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

package minicp.engine.core;

import minicp.cp.Factory;
import minicp.search.Objective;
import minicp.state.StateManager;
import minicp.state.StateStack;
import minicp.util.exception.InconsistencyException;
import minicp.util.Procedure;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;


public class MiniCP implements Solver {

    private Queue<Constraint> propagationQueue = new ArrayDeque<>();
    private List<Procedure> fixPointListeners = new LinkedList<>();
    private List<Procedure> beliefPropaListeners = new LinkedList<>();

    private final StateManager sm;

    private final StateStack<IntVar> vars;

    private StateStack<IntVar> variables;
    private StateStack<Constraint> constraints;

    private static final boolean beliefPropaOn = true;
    private static final int beliefPropaMaxIter = 5;
    private static final double beliefPropaExtremeValueEpsilon= 1.0E-3;

    public MiniCP(StateManager sm) {
        this.sm = sm;
        vars = new StateStack<>(sm);
        variables = new StateStack<>(sm);
        constraints = new StateStack<>(sm);
    }

    @Override
    public StateManager getStateManager() {
        return sm;
    }

    @Override
    public void registerVar(IntVar x) {
	variables.push(x);
    }
    
    public boolean isBeliefPropa() {
	return beliefPropaOn;
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
     *
     */
    @Override
    public void beliefPropa() {

	notifyBeliefPropa();

	boolean noExtremeValue = true;

        for (int i = 0; i < variables.size(); i++) {
	    variables.get(i).normalizeMarginals(); 
	}
        try {
	    int it = 1;
	    do {
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
		    noExtremeValue = noExtremeValue && variables.get(i).normalizeMarginals(beliefPropaExtremeValueEpsilon); 
		}
		it++;
	    } while (it<=beliefPropaMaxIter && noExtremeValue);

        } catch (InconsistencyException e) {
            throw e;
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
    public void post(Constraint c, boolean enforceFixPoint) {
	constraints.push(c);
        c.post();
        if (enforceFixPoint) fixPoint();
    }

    @Override
    public void post(BoolVar b) {
        b.assign(true);
        fixPoint();
    }
}
