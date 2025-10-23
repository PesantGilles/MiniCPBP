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
import minicpbp.state.StateInt;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FirstOccurrence extends AbstractConstraint {

    private IntVar[] x;
    private Set<Integer> V;
    private int n; // nb of vars
    private StateInt lastMinPos; // The last minimum position; to filter domains of x[lastMinPos]..x[pos.min()-1]
    private StateInt[] witnessInsideV; // for each x[i], an element from its domain that belongs to V
    private StateInt[] witnessOutsideV; // for each x[i], an element from its domain that does not belong to V
    private IntVar position; //The position of the fist occurrence of an element of V in x
    private double[] Vbelief; // for each x[i], the outside belief of belonging to V
    private double[] notinVFwdCumulBelief; // for each position i, the belief of x[0]..x[i-1] not belonging to V
    private double[] firstOccFwdCumulBelief; // for each position i, the belief of the position of 1st occurrence being among 0..i-1
    private double[] firstOccBwdCumulBelief; // for each position i, the belief of the position of 1st occurrence being among i+1..n-1

    /**
     * Creates a constraint about the first occurrence of given values in an array of variables.
     * This constraint holds if elements of a set V occur at, possibly after, but not before, position pos in array x.
     *
     * @param x         an array of variables whose instantiations belonging to V we track
     * @param V         an array of values
     * @param pos       a variable for the position of the first occurrence of an element from V in x
     *
     * Constraint originally proposed and implemented by Damien Van Meerbeeck.
     */
    public FirstOccurrence(IntVar[] x, int[] V, IntVar pos, IntVar[] vars) {
        super(x[0].getSolver(), vars);
        setName("FirstOccurrence");
        this.x = x;
        this.n = x.length;
        this.V = new HashSet<Integer>();
        for (int i = 0; i < V.length; i++) {
            this.V.add(V[i]);
        }
        lastMinPos = getSolver().getStateManager().makeStateInt(0);
        witnessInsideV = new StateInt[n];
        witnessOutsideV = new StateInt[n];
        for (int i = 0; i < n; i++) {
            assert !x[i].contains(Integer.MIN_VALUE) : "FirstOccurrence constraint: variable x[" + i + "] has Integer.MIN_VALUE in its domain!";
            witnessInsideV[i] = getSolver().getStateManager().makeStateInt(Integer.MIN_VALUE);
            witnessOutsideV[i] = getSolver().getStateManager().makeStateInt(Integer.MIN_VALUE);
        }
        Vbelief = new double[n];
        notinVFwdCumulBelief = new double[n];
        firstOccFwdCumulBelief = new double[n];
        firstOccBwdCumulBelief = new double[n];
        setExactWCounting(true);
        this.position = pos;
    }

    @Override
    public void post() {
        switch (getSolver().getMode()) {
            case BP:
                break;
            case SP:
            case SBP:
                position.propagateOnDomainChange(this);
                for (IntVar var : x)
                    var.propagateOnDomainChange(this);
        }
        propagate();
    }

    @Override
    public void propagate() {
        boolean foundPossiblePos = false;
        for (int p = lastMinPos.value(); p <= position.max(); p++) {
            if(!position.contains(p)){
                if(!foundPossiblePos) {
                    // Remove values of V for positions before the first possible position.
                    removeAllValuesAtPos(p);
                }
            }
            else {
                int decided = isDecided(x[p], p);
                if (decided == 0) {
                    foundPossiblePos = true;
                } else if (decided == 1) {
                    // If the possible values at a possible position are a subset of V then the minimum position is not higher than that position.
                    position.removeAbove(p);
                    if (position.isBound()) {
                        setActive(false);
                        return;
                    }
                    break;
                } else if (decided == -1) {
                    // no value from V at that position
                    position.remove(p);
                }
            }
        }

        if (position.isBound()){
            // If the position is fixed then the value at that position must be from V.
            forceValuesAtPos(position.min());
            setActive(false);
        } else {
            lastMinPos.setValue(position.min());
        }
    }

    private void removeAllValuesAtPos(int p){
        int s = x[p].fillArray(domainValues);
        for (int i = 0; i < s; i++) {
            if(V.contains(domainValues[i])){
                x[p].remove(domainValues[i]);
            }
        }
    }

    private void forceValuesAtPos(int p){
        int s = x[p].fillArray(domainValues);
        for (int i = 0; i < s; i++) {
            if(!V.contains(domainValues[i])){
                x[p].remove(domainValues[i]);
            }
        }
    }

    // returns 0 if var can still take a value inside and outside V
    //         1 if var can only take a value inside V
    //        -1 if var cannot take a value inside V
    private int isDecided(IntVar var, int i) {
        int j;
        if (!var.contains(witnessInsideV[i].value())) {
            // find new witness
            // iterate over smallest between var's domain and V
            if (var.size() < V.size()) {
                int s = var.fillArray(domainValues);
                for (j = 0; j < s; j++) {
                    int v = domainValues[j];
                    if (V.contains(v)) {
                        witnessInsideV[i].setValue(v);
                        break;
                    }
                }
                if (j == s) // no inside witness
                    return -1;
            }
            else { // V is smaller
                Iterator<Integer> itr = V.iterator();
                while (itr.hasNext()) {
                    int val = itr.next();
                    if (var.contains(val)) {
                        witnessInsideV[i].setValue(val);
                        break;
                    }
                }
                if (!var.contains(witnessInsideV[i].value())) // no inside witness
                    return -1;
            }
        }
        if (!var.contains(witnessOutsideV[i].value())) {
            // find new witness
            int s = var.fillArray(domainValues);
            for (j = 0; j < s; j++) {
                int v = domainValues[j];
                if (!V.contains(v)) {
                    witnessOutsideV[i].setValue(v);
                    break;
                }
            }
            if (j == s) // no outside witness
                return 1;
        }
        return 0;
    }

    @Override
    public void updateBelief() { // CAVEAT: relies on propagate() being applied first (i.e. SBP mode)
        // accumulate outside beliefs about belonging to V
        for (int i = 0; i < n; i++) {
            Vbelief[i] = beliefRep.zero();
            Iterator<Integer> itr = V.iterator();
            while (itr.hasNext()) {
                int val = itr.next();
                if (x[i].contains(val)) {
                    Vbelief[i] = beliefRep.add(Vbelief[i], outsideBelief(i, val));
                }
            }
        }
        // compute forward cumulative beliefs of x[0]..x[i-1] not belonging to V
        notinVFwdCumulBelief[0] = beliefRep.one();
        for (int i = 0; i < position.max(); i++) {
            notinVFwdCumulBelief[i+1] = beliefRep.multiply(notinVFwdCumulBelief[i], beliefRep.complement(Vbelief[i]));
        }
       // compute forward and backward cumulative beliefs of 0..i-1 and i+1..n-1 (resp.) being the position of 1st occurrence
        firstOccFwdCumulBelief[0] = beliefRep.zero();
        for (int i = 0; i < position.max(); i++) {
            firstOccFwdCumulBelief[i+1] = (position.contains(i) ?
                    beliefRep.add(firstOccFwdCumulBelief[i], beliefRep.multiply( beliefRep.multiply(notinVFwdCumulBelief[i], outsideBelief(n /* position */, i)), Vbelief[i])) :
                    firstOccFwdCumulBelief[i]);
        }
        firstOccBwdCumulBelief[n-1] = beliefRep.zero();
        for (int i = n-1; i > position.min(); i--) {
            firstOccBwdCumulBelief[i-1] = (position.contains(i) ?
                    beliefRep.add(firstOccBwdCumulBelief[i], beliefRep.multiply( beliefRep.multiply(notinVFwdCumulBelief[i], outsideBelief(n /* position */, i)), Vbelief[i])) :
                    firstOccBwdCumulBelief[i]);
        }
        // set local beliefs for position and x
	    for (int i = 0; i < position.min(); i++) { // x[0]..x[position.min()-1] are unconstrained and already restricted to be outside V
	        int s = x[i].fillArray(domainValues);
	        for (int j = 0; j < s; j++) {
		        setLocalBelief(i, domainValues[j], beliefRep.one()); // will be normalized
	        }
	    }
        for (int i = position.min(); i <= position.max(); i++) {
            if (position.contains(i)) { // i is a possible position for the 1st occurrence from V
                setLocalBelief(n /* position */, i, beliefRep.multiply(notinVFwdCumulBelief[i], Vbelief[i]));
                int s = x[i].fillArray(domainValues);
                for (int j = 0; j < s; j++) { // go through x[i]'s domain
                    int v = domainValues[j];
                    if (!V.contains(v)) {  // v is not among V
                        // v supports both earlier and later positions for 1st occurrence from V, but not position i
                        // Note: must exclude outside belief about x[i]
                        setLocalBelief(i, v, beliefRep.add(firstOccFwdCumulBelief[i], beliefRep.divide(firstOccBwdCumulBelief[i], beliefRep.complement(Vbelief[i]))));
                    } else { // v is among V
                        // v supports earlier positions for 1st occurrence from V and this position as well
                        // Note: must exclude outside belief about x[i]
                        setLocalBelief(i, v, beliefRep.add(firstOccFwdCumulBelief[i], beliefRep.multiply(notinVFwdCumulBelief[i], outsideBelief(n /* position */, i))));
                    }
                }
            } else { // i is NOT a possible position for the 1st occurrence from V
		        int s = x[i].fillArray(domainValues);
		        for (int j = 0; j < s; j++) { // go through x[i]'s domain
		            int v = domainValues[j];
                    if (!V.contains(v)) {  // v is not among V
                        // v supports both earlier and later positions for 1st occurrence from V
                        // Note: must exclude outside belief about x[i]
                        setLocalBelief(i, v, beliefRep.add(firstOccFwdCumulBelief[i], beliefRep.divide(firstOccBwdCumulBelief[i], beliefRep.complement(Vbelief[i]))));
		            } else { // v is among V
			            // v supports earlier positions for 1st occurrence from V
 			            setLocalBelief(i, v, firstOccFwdCumulBelief[i]);
		            }
		        }
	        }
        }
        for (int i = position.max()+1; i < n; i++) { // x[position.max()+1]..x[n-1] are unconstrained
	        int s = x[i].fillArray(domainValues);
	        for (int j = 0; j < s; j++) {
		        setLocalBelief(i, domainValues[j], beliefRep.one()); // will be normalized
	        }
	    }
    }

}
