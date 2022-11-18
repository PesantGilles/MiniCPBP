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
import minicpbp.engine.core.Solver;
import minicpbp.state.StateInt;

import javax.management.DynamicMBean;
import java.util.*;
import java.util.stream.IntStream;

import static minicpbp.cp.Factory.allDifferent;
import static minicpbp.cp.Factory.allDifferentBinary;

/**
 * Hamiltonian Circuit Constraint with a successor model
 */
public class Circuit extends AbstractConstraint {

    private final IntVar[] x;
    private final StateInt[] dest;
    private final StateInt[] orig;
    private final StateInt[] lengthToDest;

    // for updateBelief()
    private int[] unBounds;
    private StateInt nUnBounds;
    private double[][] density;
    private int[] path;
    private int pathLength;
    private boolean[] onPath;
    // ***** data structures about the available successors on path
    private class Entry {
        double weight;
        int node;
    }
    private Entry[] available;
    private int availableNb;
    private double availableMaxWeight;
    private double availableAccumulatedWeight;
    // *****
    private Random rand;
    private int max_nb_samples = 5000;
    private int target_nb_samples;

    /**
     * Creates a Hamiltonian Circuit Constraint
     * with a successor model.
     *
     * @param x the variables representing the successor array that is
     *          {@code x[i]} is the city visited after city i
     */
    public Circuit(IntVar... x) {
        super(x[0].getSolver(), x);
        setName("Circuit");
        assert (x.length > 0);
        this.x = x;
        dest = new StateInt[x.length];
        orig = new StateInt[x.length];
        lengthToDest = new StateInt[x.length];
        for (int i = 0; i < x.length; i++) {
            dest[i] = getSolver().getStateManager().makeStateInt(i);
            orig[i] = getSolver().getStateManager().makeStateInt(i);
            lengthToDest[i] = getSolver().getStateManager().makeStateInt(0);
        }

        // updateBelief
        density = new double[x.length][x.length];
        path = new int[x.length];
        onPath = new boolean[x.length];
        available = new Entry[x.length-1]; // potentially every city except itself
        for (int i = 0; i < x.length-1; i++) {
            available[i] = new Entry();
        }
        rand = x[0].getSolver().getRandomNbGenerator();
        unBounds = IntStream.range(0, x.length).toArray();
        setExactWCounting(false); // based on sampling
    }


    @Override
    public void post() {
        if (x.length == 1) {
            x[0].assign(0);
            setActive(false);
            return;
        }
        for (int i = 0; i < x.length; i++) {
            x[i].remove(i);
            x[i].removeBelow(0);
            x[i].removeAbove(x.length-1);
        }
        if (getSolver().getMode() == Solver.PropaMode.SP)
            getSolver().post(allDifferentBinary(x)); // the original filtering level
        else
            getSolver().post(allDifferent(x));
        switch (getSolver().getMode()) {
            case SBP:
	        case BP: // same as SBP since updateBelief() uses orig/dest
                // Collect bound vars
		        int nU = x.length;
		        for (int i = nU - 1; i >= 0; i--) {
		            int idx = unBounds[i];
		            if (x[idx].isBound()) {
			            unBounds[i] = unBounds[nU - 1]; // Swap the variables
			            unBounds[nU - 1] = idx;
			            nU--;
		            }
		        }
		        nUnBounds = getSolver().getStateManager().makeStateInt(nU);
		        for (int i = 0; i < nU; i++) {
		            x[unBounds[i]].propagateOnBind(this);
		        }
            case SP:
		        for (int i = 0; i < x.length; i++) {
		            if (x[i].isBound()) bind(i);
		            else {
			            final int fi = i;
			            x[i].whenBind(() -> bind(fi));
		            }
		        }
        }
    }


    @Override
    public void propagate() {
        // Update the unbound vars for updateBelief()
        int nU = nUnBounds.value();
        for (int i = nU - 1; i >= 0; i--) {
            int idx = unBounds[i];
            if (x[idx].isBound()) {
                unBounds[i] = unBounds[nU - 1]; // Swap the variables
                unBounds[nU - 1] = idx;
                nU--;
            }
        }
        nUnBounds.setValue(nU);
    }


    private void bind(int i) {
        int j = x[i].min();
        int origi = orig[i].value();
        int destj = dest[j].value();
        // orig[i] *-> i -> j *-> dest[j]
        dest[origi].setValue(destj);
        orig[destj].setValue(origi);
        int length = lengthToDest[origi].value()
                + lengthToDest[j].value() + 1;
        lengthToDest[origi].setValue(length);

        if (length < x.length - 1) {
            // avoid inner loops
            x[destj].remove(origi); // avoid inner loops
        }
    }

    private void addEntry(int node, double weight) {
        availableAccumulatedWeight = beliefRep.add(availableAccumulatedWeight,weight);
        if (weight > availableMaxWeight) {
            availableMaxWeight = weight;
        }
        available[availableNb].node = node;
        available[availableNb++].weight = weight;
    }

    private int getRandomAvailable() {
        // stochastic acceptance algorithm
        while (true) {
            Entry e = available[rand.nextInt(availableNb)];
            if (rand.nextDouble() < beliefRep.divide(e.weight, availableMaxWeight))
                return e.node;
        }
    }

    private double find_cycle() {
        return find_cycle(1);
    }

    private double find_cycle(double subpathsWeight) {

        int nU = nUnBounds.value();
//        System.out.print("unbounds: ");
//        for (int i = nU - 1; i >= 0; i--) {
//            System.out.print(unBounds[i]+" ");
//        }
//        System.out.println();
//        System.out.print("(bound to);orig/dest: ");
//        for (int i = 0; i < x.length; i++) {
//            if (x[i].isBound())
//                System.out.print(x[i].min()+";");
//            System.out.print(orig[i].value()+"/"+dest[i].value()+" ");
//        }
//        System.out.println();
        double W = subpathsWeight;
	    int totalLength = nUnBounds.value();
//        System.out.println("totalLength="+totalLength);
        pathLength = 0;
        for (int i = 0; i < x.length; i++) {
            onPath[i] = false;
        }

        int node = unBounds[rand.nextInt(totalLength)];
        path[pathLength++] = node;
        onPath[node] = true;
        int start = node;

        while (pathLength < totalLength) {
            availableAccumulatedWeight = beliefRep.zero();
            availableMaxWeight = beliefRep.zero();
            availableNb = 0;
            int s = x[node].fillArray(domainValues);
            for (int j = 0; j < s; j++) { // consider possible successors on path
                int v = domainValues[j];
                 if (!onPath[v]) { // not already on path; add it
                     addEntry( v, outsideBelief(node, v));
                 }
            }
            if (availableAccumulatedWeight == beliefRep.zero()) {
                return 0; // either there are no entries or they all have zero weight
            }

            W *= beliefRep.rep2std(availableAccumulatedWeight);
            int next = getRandomAvailable();
            onPath[next] = true;
            node = dest[next].value(); // go to the end of the partial path initiated at next
            path[pathLength++] = node; // put that end of the partial path on the path
        }

        if (x[node].contains(start)) {
            W *= beliefRep.rep2std(outsideBelief(node, start));
            if (W==0)
                return 0;
            //Browse path
//            System.out.print("path: ");
//            for (int i = 0; i < pathLength; i++) {
//                System.out.print(path[i]+" ");
//            }
//            System.out.println();
            for (int i = 0; i < pathLength - 1; i++) {
//                System.out.println(i);
                density[path[i]][orig[path[i + 1]].value()] = beliefRep.add(density[path[i]][orig[path[i + 1]].value()],
                        beliefRep.divide(beliefRep.std2rep(W),outsideBelief(path[i],orig[path[i + 1]].value())));
            }
            density[path[pathLength - 1]][path[0]] = beliefRep.add(density[path[pathLength - 1]][path[0]],
                    beliefRep.divide(beliefRep.std2rep(W),outsideBelief(path[pathLength - 1],path[0])));
            return W;
        }
        return 0;
    }

    @Override
    public void updateBelief() {

	    int nU = nUnBounds.value();

        if (nU == 0) return; // all variables in its scope are bound

	    //Collect outside beliefs of existing partial paths (orig[i] *-> i, x[i] unbound)
	    double initialWeight = 1.0;
        for (int i = 0; i < nU; i++) {
	        int k = unBounds[i];
	        int j = orig[k].value();
	        while (j != k) {
		        int succj = x[j].min();
		        initialWeight *= beliefRep.rep2std(outsideBelief(j,succj));
		        j = succj;
	        }
	    }

        //Clear densities (not necessary for bound vars)
        for (int i = 0; i < nU; i++) {
	        int k = unBounds[i];
            int s = x[k].fillArray(domainValues);
            for (int j = 0; j < s; j++) {
                density[k][domainValues[j]] = beliefRep.zero();
            }
        }

        // TODO: if nU <= some threshold, then compute them exactly

        //Compute densities
	    int success = 0;
        target_nb_samples = nU*nU; // square of path length
        for (int i = 0; i < max_nb_samples; i++) {
            if (find_cycle(initialWeight) > 0) success++;
            if (success == target_nb_samples)
                break;
        }
 	    System.out.println(success+" successfully sampled circuits (target="+target_nb_samples+") out of at most "+max_nb_samples+" attempts");

        //Set beliefs
        if (success >= 1) {
            for (int i = 0; i < nU; i++) {
		        int k = unBounds[i];
                int s = x[k].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    int v = domainValues[j];
                    setLocalBelief(k, v, density[k][v]);
//                  System.out.println("density "+unBounds[i]+" "+v+": "+density[unBounds[i]][v]);
                }
            }
        } else { // no sample obtained; give up and set default uniform beliefs
            for (int i = 0; i < nU; i++) {
		        int k = unBounds[i];
                int s = x[k].fillArray(domainValues);
                for (int j = 0; j < s; j++) {
                    setLocalBelief(k, domainValues[j], beliefRep.one()); // will be normalized
                }
            }
        }
    }
}
