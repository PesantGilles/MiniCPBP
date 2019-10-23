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

package minicp.engine.constraints;

import minicp.cp.Factory;
import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.IntVarImpl;
import minicp.state.StateInt;
import minicp.util.exception.InconsistencyException;
import minicp.util.*;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Sum Constraint
 */
public class Sum extends AbstractConstraint {
    private int[] unBounds;
    private StateInt nUnBounds;
    private StateInt sumBounds;
    private IntVar[] x;
    private int n;
    private double[][] ip; // ip[i][]>0 for partial sums x[0]+x[1]+...+x[i-1]
    private double[][] op; // op[i][]>0 for partial sums x[i+1]+...+x[n-1]
    private int offset;
    private int mini;
    private int maxi;

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == y}.
     *
     * @param x the non empty left hand side of the sum
     * @param y the right hand side of the sum
     */
    public Sum(IntVar[] x, IntVar y) {
        this(Arrays.copyOf(x, x.length + 1));
        this.x[x.length] = Factory.minus(y);
    }

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == y}.
     *
     * @param x the non empty left hand side of the sum
     * @param y the right hand side of the sum
     */
    public Sum(IntVar[] x, int y) {
        this(Arrays.copyOf(x, x.length + 1));
        this.x[x.length] = Factory.makeIntVar(getSolver(), -y, -y);
    }

    /**
     * Creates a sum constraint.
     * <p> This constraint holds iff
     * {@code x[0]+x[1]+...+x[x.length-1] == 0}.
     *
     * @param x the non empty set of variables that should sum to zero
     */
    public Sum(IntVar[] x) {
        super(x);
        this.x = x;
        this.n = x.length;
        nUnBounds = getSolver().getStateManager().makeStateInt(n);
        sumBounds = getSolver().getStateManager().makeStateInt(0);
        unBounds = IntStream.range(0, n).toArray();
   	setExactWCounting(true);
	// compute the extent of the dynamic programming tables ip and op	
	int fwd, bwd;
	fwd = 0;
	bwd = 0;
	for (int i = 0 ; i < n; i++) {
	    bwd -= x[i].min();
	}
	maxi = 0;
	for( int i = 0; i < n; i++) {
	    maxi = Math.max( maxi, Math.min( fwd, bwd ));
	    fwd += x[i].max();
	    bwd += x[i].min();
	}
	fwd = 0;
	bwd = 0;
	for (int i = 0; i < n; i++) {
	    bwd -= x[i].max();
	}
	mini = 0;
	for(int i = 0; i < n; i++) {
	    mini = Math.min( mini, Math.max( fwd, bwd ));
	    fwd += x[i].min();
	    bwd += x[i].max();
	}
	this.offset = -mini; 

	this.op = new double[n][maxi-mini+1];
	this.ip = new double[n][maxi-mini+1];
    }

    @Override
    public void post() {
	switch(getSolver().getMode()) {
	case BP:
	    break;
	case SP:
	case SBP:
	    for (IntVar var : x)
		var.propagateOnBoundChange(this);
	}
        propagate();
    }

    @Override
    public void propagate() {
        // Filter the unbound vars and update the partial sum
        int nU = nUnBounds.value();
        int sumMin = sumBounds.value(), sumMax = sumBounds.value();
        for (int i = nU - 1; i >= 0; i--) {
            int idx = unBounds[i];
            sumMin += x[idx].min(); // Update partial sum
            sumMax += x[idx].max();
            if (x[idx].isBound()) {
                sumBounds.setValue(sumBounds.value() + x[idx].min());
                unBounds[i] = unBounds[nU - 1]; // Swap the variables
                unBounds[nU - 1] = idx;
                nU--;
            }
        }
        nUnBounds.setValue(nU);
        if (sumMin > 0 || sumMax < 0)
            throw new InconsistencyException();
        for (int i = nU - 1; i >= 0; i--) {
            int idx = unBounds[i];
	    int idxMin = x[idx].min();
	    int idxMax = x[idx].max();
 	    x[idx].removeAbove(-(sumMin - idxMin));
 	    x[idx].removeBelow(-(sumMax - idxMax));
        }
    }

    @Override
    public void updateBelief(){

	for(int i = 0; i<n; i++){
	    Arrays.fill(ip[i],beliefRep.zero());
	}
	// Reach forward
	ip[0][offset] = beliefRep.one();
	for(int i = 0; i<n-1; i++){
	    int s = x[i].fillArray(domainValues);
	    for (int j = 0; j < s; j++) {
		int v = domainValues[j];
		for(int k = mini-(v<0?v:0); k <= maxi-(v>0?v:0); k++){
		    if(!beliefRep.isZero(ip[i][k+offset])) {
			// add the combination of ip[i][k+offset] and outsideBelief(i,v) to ip[i+1][k+offset+v]
			ip[i+1][k+offset+v] = beliefRep.add(ip[i+1][k+offset+v], beliefRep.multiply(ip[i][k+offset],outsideBelief(i,v)));
		    }
		}
	    }
	}

	for(int i = 0; i<n; i++){
	    Arrays.fill(op[i],beliefRep.zero());
	}
	// Reach backward and set local beliefs
	op[n-1][offset] = beliefRep.one();
	for(int i = n-1; i>0; i--){
	    int s = x[i].fillArray(domainValues);
	    for (int j = 0; j < s; j++) {
		int v = domainValues[j];
		double belief = beliefRep.zero();
		for(int k = mini-(v<0?v:0); k <= maxi-(v>0?v:0); k++){
		    if(!beliefRep.isZero(op[i][k+offset+v])) {
			// add the combination of op[i][k+offset+v] and outsideBelief(i,v) to op[i-1][k+offset]
			op[i-1][k+offset] = beliefRep.add(op[i-1][k+offset], beliefRep.multiply(op[i][k+offset+v],outsideBelief(i,v)));
			// add the combination of ip[i][k+offset] and op[i][k+offset+V] to belief
			belief = beliefRep.add(belief, beliefRep.multiply(ip[i][k+offset],op[i][k+offset+v]));
		    }
		}
		setLocalBelief(i,v,belief);
	    }
	}
	int s = x[0].fillArray(domainValues);
	for (int j = 0; j < s; j++) {
	    int v = domainValues[j];
	    setLocalBelief(0,v,op[0][offset+v]);
	}

    }

}
