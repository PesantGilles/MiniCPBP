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

import minicpbp.state.StateBool;
import minicpbp.state.StateDouble;

import minicpbp.util.Belief;

import minicpbp.util.exception.NotImplementedException;

/**
 * Abstract class most of the constraints
 * should extend.
 */
public abstract class AbstractConstraint implements Constraint {

    private String name;
    /**
     * The solver in which the constraint is created
     */
    private final Solver cp;
    private boolean scheduled = false;
    private final StateBool active;

    private StateDouble[][] localBelief;
    private double[][] outsideBelief;
    private StateDouble[][] prevOutsideBelief; // needed for message damping
    private double weight; // an optional nonnegative weight applied to the constraint's local belief
    protected Belief beliefRep;
    private int[] ofs;
    protected IntVar[] vars; // all the variables in the scope of the constraint
    private int maxDomainSize;
    protected int[] domainValues; // an array large enough to hold any domain of vars
    protected double[] beliefValues; // an auxiliary array as large as domainValues
    private boolean exactWCounting = false;
    private boolean updateBeliefWarningPrinted = false;
    private boolean weightedCountingWarningPrinted = false;

    private int failureCount;

    public AbstractConstraint(Solver cp, IntVar[] vars) {
        this.cp = cp;
        active = cp.getStateManager().makeStateBool(true);
        beliefRep = cp.getBeliefRep();
        this.vars = new IntVar[vars.length];
        System.arraycopy(vars,0,this.vars,0,vars.length); // required if constraint sets up offseted vars in the same array
        switch (cp.getWeighingScheme()) {
            case SAME:
                weight = 1.0;
                break;
            case ARITY:
                // will be set in MiniCP.computeMinArity()
                break;
        }
        localBelief = new StateDouble[vars.length][];
        ofs = new int[vars.length];
        outsideBelief = new double[vars.length][];
        prevOutsideBelief = new StateDouble[vars.length][];

        maxDomainSize = 0;
        for (int i = 0; i < vars.length; i++) {
            vars[i].registerConstraint(this);
            ofs[i] = vars[i].min();
            localBelief[i] = new StateDouble[vars[i].max() - vars[i].min() + 1];
            outsideBelief[i] = new double[vars[i].max() - vars[i].min() + 1];
            prevOutsideBelief[i] = new StateDouble[outsideBelief[i].length];
            for (int j = 0; j < localBelief[i].length; j++) {
                localBelief[i][j] = cp.getStateManager().makeStateDouble(beliefRep.one()); // no belief yet; initialized to ONE (certainly true) in order to retrieve the first var-to-constraint msg correctly
                prevOutsideBelief[i][j] = cp.getStateManager().makeStateDouble(beliefRep.one()); // arbitrary
            }
            maxDomainSize = Math.max(maxDomainSize, vars[i].max() - vars[i].min() + 1);
        }
        domainValues = new int[maxDomainSize];
        beliefValues = new double[maxDomainSize];
        failureCount = 0;
    }

    public double arity() {
        return (double) vars.length;
    }

    /*public double getWeight() {
		switch(cp.getWeighingScheme()) {
			case SAME:
				return 1.0;
			case ARITY:
				// assumes all model variables have already been declared/registered
				return 1.0 + ((double) vars.length - cp.minArity())/ ((double) cp.getVariables().size()); 
			case ANTI:
                return 1.0 - ((double) vars.length - cp.minArity()) / ((double) cp.getVariables().size());
			default:
				throw new NotImplementedException();
			}

	}*/

	public void incrementFailureCount() {
		failureCount+=1;
	}

	public int getFailureCount() {
		return failureCount;
	}

    public void post() {}

    public Solver getSolver() {
        return cp;
    }

    public void propagate() {}

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

    public void setWeight(double w) {
        assert w >= 0 : "c A constraint's weight should be nonnegative";
        weight = w;
    }

    public double weight() {
        return weight;
    }

    protected double localBelief(int i, int val) {
        return localBelief[i][val - ofs[i]].value();
    }

    protected double setLocalBelief(int i, int val, double b) {
        return localBelief[i][val - ofs[i]].setValue(b);
    }

    protected double outsideBelief(int i, int val) {
        return outsideBelief[i][val - ofs[i]];
    }

    protected double setOutsideBelief(int i, int val, double b) {
        outsideBelief[i][val - ofs[i]] = b;
        return b;
    }

    protected double prevOutsideBelief(int i, int val) {
        return prevOutsideBelief[i][val - ofs[i]].value();
    }

    protected double setPrevOutsideBelief(int i, int val, double b) {
        return prevOutsideBelief[i][val - ofs[i]].setValue(b);
    }

    interface getBelief {
        double get(int i, int val);
    }

    interface setBelief {
        double set(int i, int val, double b);
    }

    private void normalizeBelief(int i, getBelief f1, setBelief f2) {
        int s = vars[i].fillArray(domainValues);
        if (s == 1) { // variable is bound
            f2.set(i, domainValues[0], beliefRep.one());
            return;
        }
        for (int j = 0; j < s; j++) {
            beliefValues[j] = f1.get(i, domainValues[j]);
        }
        double normalizingConstant = beliefRep.summation(beliefValues, s);
        if (beliefRep.isZero(normalizingConstant)) // temporary state of a soon-to-be-empty domain
            return;
        for (int j = 0; j < s; j++) {
            int val = domainValues[j];
            f2.set(i, val, beliefRep.divide(f1.get(i, val), normalizingConstant));
            assert f1.get(i, val) <= beliefRep.one() && f1.get(i, val) >= beliefRep.zero() : "c Should be normalized! f1.get(i,val) = " + f1.get(i, val);
        }
    }

    public void resetLocalBelief() {
        for (int i = 0; i < vars.length; i++) {
            int s = vars[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                setLocalBelief(i, domainValues[j], beliefRep.one());
            }
        }
    }

    private void dampenMessages(int i) {
        double lambda = beliefRep.std2rep(cp.dampingFactor());
        double oneMinusLambda = beliefRep.complement(lambda);
        int s = vars[i].fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int val = domainValues[j];
            setOutsideBelief(i, val, beliefRep.add(beliefRep.multiply(lambda, outsideBelief(i, val)), beliefRep.multiply(oneMinusLambda, prevOutsideBelief(i, val))));
        }
        normalizeBelief(i, (j, val) -> outsideBelief(j, val), (j, val, b) -> setOutsideBelief(j, val, b));
    }

    public void receiveMessages() {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].isBound()) {
                setOutsideBelief(i, vars[i].min(), beliefRep.one());
            } else {
                int s = vars[i].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    int val = domainValues[j];
                    assert localBelief(i, val) <= beliefRep.one() && localBelief(i, val) >= beliefRep.zero() : "c Should be normalized! localBelief(i,val) = " + localBelief(i, val);
                    setOutsideBelief(i, val, vars[i].sendMessage(val, beliefRep.pow(localBelief(i, val), this.weight)));
                }
                normalizeBelief(i, (j, val) -> outsideBelief(j, val),
                        (j, val, b) -> setOutsideBelief(j, val, b));
                if (cp.dampingMessages()) {
                    if (cp.prevOutsideBeliefRecorded())
                        dampenMessages(i);
                    for (int j = 0; j < s; j++) {
                        int val = domainValues[j];
                        setPrevOutsideBelief(i, val, outsideBelief(i, val));
                    }
                }
            }
        }
    }

    public void sendMessages() {
        updateBelief();
 //       System.out.println(getName()+".sendMessages()");
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isBound()) { // if the variable is bound, it is pointless to send a "certainly true" message
                normalizeBelief(i, (j, val) -> localBelief(j, val),
                        (j, val, b) -> setLocalBelief(j, val, b));
                int s = vars[i].fillArray(domainValues);
 //               System.out.print(vars[i].getName()+": ");
                for (int j = 0; j < s; j++) {
                    int val = domainValues[j];
                    double localB = localBelief(i, val);
 //                   System.out.print(val+" "+localB+", ");
                    assert localB <= beliefRep.one() && localB >= beliefRep.zero() : "c Should be normalized! localB = " + localB;
                    if (getSolver().actingOnZeroOneBelief() && isExactWCounting()) {
                        if (beliefRep.isZero(localB)) { // no support from this constraint
//  			    System.out.println(getName()+".sendMessages(): removing value "+val+" from the domain of "+vars[i].getName()+vars[i].toString()+" because its local belief is ZERO");
                            vars[i].remove(val); // standard domain consistency filtering
                            getSolver().fixPoint();
                        } else if (beliefRep.isOne(localB)) { // backbone var for this constraint (and hence for all of them)
//  			    System.out.println(getName()+".sendMessages(): assigning value "+val+" from the domain of "+vars[i].getName()+vars[i].toString()+" because its local belief is ONE");
                            vars[i].assign(val);
                            getSolver().fixPoint();
                            break; // all other values in this loop will have been removed from the domain
                        } else
                            vars[i].receiveMessage(val, beliefRep.pow(localB, this.weight));
                    } else
                        vars[i].receiveMessage(val, beliefRep.pow(localB, this.weight));
                }
            }
        }
 //       System.out.println();
    }

    /**
     * Updates its local belief given the outside beliefs.
     * To be defined in the actual constraint.
     * <p>
     * Default behaviour: uniform belief
     * CAVEAT: may set zero/one beliefs but should not directly remove domain values (only done in sendMessages() if actOnZeroOneBelief flag is set)
     */
    protected void updateBelief() {
        if (!updateBeliefWarningPrinted) {
            if (getName() != null) // do not print warning for unnamed constraint
                System.out.println("c Warning: method updateBelief not implemented yet for " + getName() + " constraint. Using uniform belief instead.");
            updateBeliefWarningPrinted = true;
        }
        for (int i = 0; i < vars.length; i++) {
            for (int j = 0; j < localBelief[i].length; j++) {
                localBelief[i][j].setValue(beliefRep.one()); // will be normalized
            }
        }
    }

    /**
     * Collects messages (outside beliefs) from the variables in its scope.
     * Used to compute a loss function via weighted counting
     */
    public void receiveMessagesWCounting() {
        for (int i = 0; i < vars.length; i++) {
            int s = vars[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int val = domainValues[j];
                setOutsideBelief(i, val, vars[i].sendMessage(val, beliefRep.one()));
            }
        }
    }

    /**
     * Optionally computes and sets the marginals of auxiliary variables created in the constraint's implementation.
     * To be optionally defined in the actual constraint.
     * <p>
     * Default behaviour: does nothing
     */
    public void setAuxVarsMarginalsWCounting() {}

    /**
     * Computes and returns the weighted count of solutions (i.e. weighted model counting) given the outside beliefs.
     * To be defined in the actual constraint.
     * !!!IMPORTANT NOTE!!!: the computation may rely on the fact that variables have all their beliefs initialized to beliefRep.one() upon creation (in StateSparseWeightedSet)
     * <p>
     * Default behaviour: returns beliefRep.one() (tautology constraint)
     */
    public double weightedCounting() {
        if (!weightedCountingWarningPrinted) {
            if (getName() != null) // do not print warning for unnamed constraint
                System.out.println("c Warning: method weightedCounting not implemented yet for " + getName() + " constraint. Returning beliefRep.one() instead.");
            weightedCountingWarningPrinted = true;
        }
        return beliefRep.one();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
