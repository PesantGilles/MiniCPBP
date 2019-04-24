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

package minicp.engine.core;

import minicp.state.StateBool;
import minicp.state.StateDouble;

import java.util.Queue;

/**
 * Abstract class the most of the constraints
 * should extend.
 */
public abstract class AbstractConstraint implements Constraint {

    /**
     * The solver in which the constraint is created
     */
    private final Solver cp;
    private boolean scheduled = false;
    private final StateBool active;

    private StateDouble[][] localBelief;
    private double[][] outsideBelief;
    private int[] ofs;
    private IntVar[] vars; // all the variables in the scope of the constraint
    private int maxDomainSize;
    protected int[] domainValues; // an array large enough to hold any domain of vars
    private boolean exactWCounting = false;

    public AbstractConstraint(IntVar[] vars) {
        this.cp = vars[0].getSolver();
        active = cp.getStateManager().makeStateBool(true);
	this.vars = vars;

	localBelief = new StateDouble[vars.length][];
	ofs = new int[vars.length];
	outsideBelief = new double[vars.length][];
	
	maxDomainSize = 0;
	for(int i = 0; i<vars.length; i++){
	    ofs[i] = vars[i].min();
	    localBelief[i] = new StateDouble[vars[i].max() - vars[i].min() + 1];
	    outsideBelief[i] = new double[vars[i].max() - vars[i].min() + 1];
	    for(int j = 0; j<localBelief[i].length; j++){
		localBelief[i][j] = cp.getStateManager().makeStateDouble(1); // no belief yet; initialized to 1 in order to retrieve the first var-to-constraint msg correctly
	    }
	    maxDomainSize = Math.max(maxDomainSize, vars[i].max() - vars[i].min() + 1);
	}
	domainValues = new int[maxDomainSize];
    }

    public void post() {
    }

    public Solver getSolver() {
        return cp;
    }

    public void propagate() {
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setActive(boolean active) {
        this.active.setValue(active);
    }

    public boolean isActive() {
        return active.value();
    }

    protected void setExactWCounting(boolean exact) {
        this.exactWCounting = exact;
    }

    protected boolean isExactWCounting() {
        return exactWCounting;
    }

    protected double localBelief(int i, int val) {
	return localBelief[i][val-ofs[i]].value();
    }

    protected double setLocalBelief(int i, int val, double b) {
	return localBelief[i][val-ofs[i]].setValue(b);
    }

    protected double outsideBelief(int i, int val) {
	return outsideBelief[i][val-ofs[i]];
    }

    protected double setOutsideBelief(int i, int val, double b) {
	outsideBelief[i][val-ofs[i]] = b;
	return b;
    }

    interface getBelief {
	double get(int i, int val);
    }

    interface setBelief {
	double set(int i, int val, double b);
    }

    private void normalizeBelief(int i, getBelief f1, setBelief f2) {
	double sum = 0;
        int s = vars[i].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
	    sum += f1.get(i,domainValues[j]);
    	}
        if (sum == 0) return; // temporary state of a soon-to-be-empty domain
        for (int j = 0; j < s; j++) {
	    int val = domainValues[j];
	    f2.set(i,val,f1.get(i,val)/sum);
	}
    }

    public void resetLocalBelief(){
	for(int i = 0; i<vars.length; i++){
	    int s = vars[i].fillArray(domainValues);
	    for (int j = 0; j < s; j++) {
		setLocalBelief(i,domainValues[j],1);
	    }
	}
    }

    public void receiveMessages() {
	for(int i = 0; i<vars.length; i++){
	    int s = vars[i].fillArray(domainValues);
	    for (int j = 0; j < s; j++) {
		int val = domainValues[j];
		setOutsideBelief(i,val,vars[i].sendMessage(val,localBelief(i,val)));
	    }
	    normalizeBelief(i, (j,val) -> outsideBelief(j,val), 
			    (j,val,b) -> setOutsideBelief(j,val,b));
	}
    }

    public void sendMessages() {
	updateBelief();
	// Note: does not discriminate between exact and approximate weighted counting
	for(int i = 0; i<vars.length; i++){
	    normalizeBelief(i, (j,val) -> localBelief(j,val), 
			    (j,val,b) -> setLocalBelief(j,val,b));
	    int s = vars[i].fillArray(domainValues);
	    for (int j = 0; j < s; j++) {
		int val = domainValues[j];
		double localB = localBelief(i,val);
		// CAVEAT: approximate weighted counting should be sound wrt returning 0/1 beliefs
		if (localB==0) { // no support from this constraint 
		    vars[i].remove(val); // standard domain consistency filtering
		}
		else if (localB==1) { // backbone var for this constraint (and hence for all of them)
		    vars[i].assign(val);
		}
		vars[i].receiveMessage(val,localB);
	    }
	}
    }

    /**
     * Updates its local belief given the outside beliefs.
     * To be defined in the actual constraint.
     *
     * Default behaviour: uniform belief
     */
    protected void updateBelief() {
	for(int i = 0; i<vars.length; i++){
	    for(int j = 0; j<localBelief[i].length; j++){
		localBelief[i][j].setValue(1); // will be normalized
	    }
	}
    }

}
