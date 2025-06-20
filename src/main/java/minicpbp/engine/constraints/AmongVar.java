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

import static minicpbp.cp.Factory.*;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.Set;
import java.util.HashSet;

public class AmongVar extends AbstractConstraint {
    private IntVar[] x;
    private Set<Integer> V;
    private IntVar o;
    private IntVar[] y; // indicator variables: (y[i] == 1) iff (x[i] \in V)
    private int[] undecided; // indices of vars from x whose domain contains values both inside and outside of V
    private StateInt nUndecided; // current size of undecided
    private StateInt[] witnessInsideV; // for each x[i], an element from its domain that belongs to V
    private StateInt[] witnessOutsideV; // for each x[i], an element from its domain that does not belong to V

    /**
     * Creates an among constraint with the number of occurrences given as a variable.
     * <p> This constraint holds iff
     * {@code (x[0] \in V) + (x[1] \in V) + ... + (x[x.length-1] \in V) == o}.
     * <p>
     * Decomposed into constraints (y[i] == 1) iff (x[i] \in V) and sum constraint y[0]+...+y[x.length-1]==o
     *
     * @param x    an array of variables whose instantiations belonging to V we count
     * @param V    an array of values whose occurrences in x we count
     * @param o    the variable corresponding to the number of occurrences of values from V in x
     * @param y    an array of variables such that (y[i] == 1) iff (x[i] \in V)
     * @param vars = x followed by y, variables for which we compute beliefs (passed to AbstractConstraint)
     */
    public AmongVar(IntVar[] x, int[] V, IntVar o, IntVar[] y, IntVar[] vars) {
        super(x[0].getSolver(), vars);
        setName("AmongVar");
        this.x = x;
        this.o = o;
        this.y = y;
        this.V = new HashSet<Integer>();
        for (int i = 0; i < V.length; i++) {
            this.V.add(V[i]);
        }
        nUndecided = getSolver().getStateManager().makeStateInt(x.length);
        undecided = IntStream.range(0, x.length).toArray();
        witnessInsideV = new StateInt[x.length];
        witnessOutsideV = new StateInt[x.length];
        for (int i = 0; i < x.length; i++) {
            assert !x[i].contains(Integer.MIN_VALUE) : "AmongVar constraint: variable x[" + i + "] has Integer.MIN_VALUE in its domain!";
            witnessInsideV[i] = getSolver().getStateManager().makeStateInt(Integer.MIN_VALUE);
            witnessOutsideV[i] = getSolver().getStateManager().makeStateInt(Integer.MIN_VALUE);
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
                for (IntVar var : y)
                    var.propagateOnBind(this);
        }
        propagate();
        getSolver().post(sum(y, o)); // link the indicator variables to the nb of occurrences
    }

    @Override
    public void propagate() {
        int nU = nUndecided.value();
        for (int i = nU - 1; i >= 0; i--) {
            int idx = undecided[i];
            if (y[idx].isBound()) {
                // bound y var --- adjust the domain of x[idx] accordingly
                boolean takenFromV = (y[idx].min() == 1);
                int s = x[idx].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    int v = domainValues[j];
                    if ((takenFromV ? !V.contains(v) : V.contains(v))) {
                        x[idx].remove(v);
                    }
                }
            } else switch (isDecided(x[idx], idx)) {
                case 0:
                    continue; // still undecided --- skip the update of undecided[]
                case 1:
                    y[idx].assign(1); // decided as taking a value inside V
                    break;
                case -1:
                    y[idx].assign(0); // decided as not taking a value inside V
                    break;
            }
            undecided[i] = undecided[nU - 1]; // Swap the variables
            undecided[nU - 1] = idx;
            nU--;
        }
        nUndecided.setValue(nU);
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
    public void updateBelief() {
        for (int i = 0; i < x.length; i++) {
            double belief = beliefRep.zero();
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                // set belief for x[i]==v
                if (V.contains(v)) {
                    setLocalBelief(i, v, outsideBelief(x.length + i, 1));
                    belief = beliefRep.add(belief, outsideBelief(i, v));
                } else {
                    setLocalBelief(i, v, outsideBelief(x.length + i, 0));
                }
            }
            // set beliefs for y[i]
            setLocalBelief(x.length + i, 1, belief);
            setLocalBelief(x.length + i, 0, beliefRep.complement(belief));
        }
    }

    // Needed because it creates auxiliary indicator variables y
    @Override
    public void setAuxVarsMarginalsWCounting() {
        this.receiveMessagesWCounting(); // get marginals for x variables
        for (int i = 0; i < x.length; i++) {
            double belief = beliefRep.zero();
            int s = x[i].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                int v = domainValues[j];
                if (V.contains(v)) {
                    belief = beliefRep.add(belief, outsideBelief(i, v));
                }
            }
            // set marginals for y[i]
            vars[x.length + i].receiveMessage(1, belief);
            vars[x.length + i].receiveMessage(0, beliefRep.complement(belief));
            System.out.println("marginal for indicator var y["+i+"]=1 is "+belief);
        }
    }

    @Override
    public double weightedCounting() {
        // reported by Sum constraint over indicator variables
        System.out.println("weighted count for "+this.getName()+" constraint is reported by a SumDC constraint");
        return beliefRep.one();
    }

}
