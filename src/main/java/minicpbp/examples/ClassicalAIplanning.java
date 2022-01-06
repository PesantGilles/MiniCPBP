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

package minicpbp.examples;

import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.LDSearch;
import minicpbp.search.Objective;
import minicpbp.search.SearchStatistics;
import minicpbp.util.io.InputReader;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.Automaton;

import java.util.Arrays;

import static minicpbp.cp.BranchingScheme.*;
import static minicpbp.cp.Factory.*;

/**
 * A generic CP-BP model&solve approach to classical AI planning
 */
public class ClassicalAIplanning {

    public static int minPlanLength;
    public static int maxPlanLength;
    public static int nbActions;
    public static int objectiveCombinator; // 0 if no objective; 1/2/3 for same/sum/max
    public static int lowerBoundB;
    public static int lowerBoundC;
    public static int nbAutomata;
    public static Automaton[] automaton;
    public static int maxActionCost;
    public static int nbOptimizationConstraints;
    public static int currentBestPlanCost;
    public static int timeout = 1000; // to look for a plan of given length, in milliseconds
    public static int failout = 100; // to look for a plan of given length
	
    public static void main(String[] args) {

	//###########################################
	// read the instance, whose name is the first argument
	InputReader reader = new InputReader("./src/main/java/minicpbp/examples/data/ClassicalAIplanning/" +args[0]);
	minPlanLength = reader.getInt();
	maxPlanLength = reader.getInt();
	nbActions = reader.getInt();
	objectiveCombinator = reader.getInt();
	if (objectiveCombinator==0) {// no action costs
	    lowerBoundB = 0;
	    lowerBoundC = 1; // lower (and upper) bound is length
	    maxActionCost = 1;
	}
	else {
	    lowerBoundB = reader.getInt();
	    lowerBoundC = reader.getInt();
	    maxActionCost = 0;
	}
	nbAutomata = reader.getInt();
	automaton = new Automaton[nbAutomata];
	nbOptimizationConstraints = 0;
	for(int i=0; i<nbAutomata; i++) {
	    automaton[i] = new Automaton(reader,nbActions);
	    if (automaton[i].optimizationConstraint()) {
		nbOptimizationConstraints++;
		int[] localActionCost = automaton[i].actionCost();
		for (int j = 0; j < automaton[i].nbLocalActions(); j++) {
		    if (localActionCost[j] > maxActionCost)
			maxActionCost = localActionCost[j];
		}
	    }
	}

	//###########################################
	currentBestPlanCost = maxPlanLength * maxActionCost + 1; // trivial strict upper bound
	long totalTime = 0;

	//###########################################
	// try plans of increasing length
 	for (int length = minPlanLength; length <= maxPlanLength; length++) {
	    if (currentBestPlanCost <= lowerBoundB + lowerBoundC*length)
		break; // current best is at least as good as lower bound on plan cost from that length on
	    System.out.println("###### searching potential plans of length " + length);
	    try{
		//###########################################
		// define the CP model
		Solver cp = makeSolver();
		// decision variables defining the sequential plan: action[0],action[1],...,action[length-1]
		IntVar[] action = new IntVar[length];
		for (int i = 0; i < length; i++) {
		    action[i] = makeIntVar(cp, 0, nbActions-1);
		}
		// objective to minimize
		IntVar planCost = makeIntVar(cp, lowerBoundB + lowerBoundC*length, currentBestPlanCost - 1);
		IntVar[] automataCosts = new IntVar[nbOptimizationConstraints];
		int k = 0;
		// for each component of factored transition system...
		for(int i=0; i<nbAutomata; i++) {
		    IntVar[] localAction = new IntVar[length];
		    // map the original actions to these local actions
		    for (int j = 0; j < length; j++) {
			localAction[j] = makeIntVar(cp, 0, automaton[i].nbLocalActions()-1);
			cp.post(table(new IntVar[]{action[j],localAction[j]},automaton[i].actionMap()));
		    }
		    // post one (cost)regular constraint
		    if (automaton[i].optimizationConstraint()) {
			if (objectiveCombinator >= 2) {
			    IntVar automatonCost = makeIntVar(cp, 0, currentBestPlanCost);
			    automataCosts[k++] = automatonCost;
			    cp.post(costRegular(localAction, automaton[i].transitionFct(), automaton[i].initialState(), automaton[i].goalStates(), automaton[i].actionCost(), automatonCost));
			}
			else { // objectiveCombinator == 1 i.e. same
			    cp.post(costRegular(localAction, automaton[i].transitionFct(), automaton[i].initialState(), automaton[i].goalStates(), automaton[i].actionCost(), planCost));
			}
		    } else
			cp.post(regular(localAction, automaton[i].transitionFct(), automaton[i].initialState(), automaton[i].goalStates()));
		}
		// express planCost as combination of automataCost
		switch(objectiveCombinator) {
		case 0: // no objective
		    equal(planCost,length);
		    break;
		case 1: // same; already taken care of
		    break;
		case 2: // sum
		    cp.post(sum(automataCosts,planCost));
		    break;
		case 3: // max
		    cp.post(maximum(automataCosts,planCost));
		    break;
		}
		
		//###########################################
		// define the search
      		LDSearch search = makeLds(cp, maxMarginal(action));
		search.onSolution(() -> {
 			System.out.println("plan: "+Arrays.toString(action));
 			System.out.println("cost: "+planCost.toString());
			/*
			for (int i = 0; i < action.length; i++) {
			    System.out.print(actionName[action[i].min()]+" ");
			}
			*/
			System.out.print("   ... a plan of cost "+planCost.min());
			currentBestPlanCost = planCost.min();
		    });
		Objective obj = cp.minimize(planCost);
     		SearchStatistics stats = search.optimize(obj, statistics -> (statistics.timeElapsed() >= timeout && statistics.numberOfFailures() >= failout));
		System.out.format("Statistics: %s\n", stats);
		totalTime += stats.timeElapsed();
	    }
	    catch(InconsistencyException e){
		System.out.println("no solution");
	    }
	}
	System.out.println("Total time: "+totalTime+" msecs");
    }
}
