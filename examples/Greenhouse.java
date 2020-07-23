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

package minicp.examples;

import minicp.cp.Factory;
import minicp.engine.constraints.CostRegular;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.LDSearch;
import minicp.search.Objective;
import minicp.search.SearchStatistics;
import minicp.util.io.InputReader;
import minicp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;

/**
 * The Scanalyzer planning domain.
 * <a href="http://gki.informatik.uni-freiburg.de/papers/helmert-lasinger-icaps2010.pdf">description</a>.
 */
public class Greenhouse {

    public static int nbBatches;
    public static List<Integer>[] goalLocation;
    public static int cycleLength;
    public static int[][] rotate;
    public static int[][] rotate_and_analyze;
    public static int nbOperations;
    public static String[] operationName;
    public static int raCost = 3;
    public static int rCost = 1;
    public static int[] opCost;
    public static int planLengthUB = 100;
    public static int bestPlanCost;
	
    public static void main(String[] args) {

	//###########################################
	// read the instance
	InputReader reader = new InputReader("minicp/examples/data/Greenhouse/"+args[0]);
	nbBatches = reader.getInt();
	goalLocation = new List[nbBatches];
	for(int i=0; i<nbBatches; i++) {
	    goalLocation[i] = new ArrayList<Integer>();
	    goalLocation[i].add(reader.getInt()+nbBatches);// analyzed state
	}
	cycleLength = reader.getInt();
	int n = reader.getInt();
	rotate = new int[n][cycleLength];
	for(int i=0; i<n; i++) {
	    for(int j=0; j<cycleLength; j++) {
		rotate[i][j] = reader.getInt();
	    }
	}
	int m = reader.getInt();
	rotate_and_analyze = new int[m][cycleLength];
	for(int i=0; i<m; i++) {
	    for(int j=0; j<cycleLength; j++) {
		rotate_and_analyze[i][j] = reader.getInt();
	    }
	}
	nbOperations = n+m;
	opCost = new int[nbOperations];
	operationName = new String[nbOperations];
	for(int i=0; i<n; i++) {
	    opCost[i] = rCost;
	    if (cycleLength == 2)
		operationName[i] = "r("+rotate[i][0]+","+rotate[i][1]+")";
	    else // cycleLength == 4
		operationName[i] = "r("+rotate[i][0]+","+rotate[i][1]+","+rotate[i][2]+","+rotate[i][3]+")";
	}
	for(int i=0; i<m; i++) {
	    opCost[n+i] = raCost;
	    if (cycleLength == 2)
		operationName[n+i] = "ra("+rotate_and_analyze[i][0]+","+rotate_and_analyze[i][1]+")";
	    else // cycleLength == 4
		operationName[n+i] = "ra("+rotate_and_analyze[i][0]+","+rotate_and_analyze[i][1]+","+rotate_and_analyze[i][2]+","+rotate_and_analyze[i][3]+")";
	}
	//###########################################
	
	bestPlanCost = planLengthUB*raCost;
	
	int totalTime = 0;

	// try plans of increasing length
 	for (int length = nbBatches; length <= planLengthUB; length++) {
	    if (bestPlanCost <= nbBatches*raCost + (length-nbBatches)*Math.min(rCost,raCost))
		break; // current best is at least as good as lower bound on plan cost from that length on
	    System.out.println("###### trying a plan length of " + length);

	    try{
		//###########################################
		// define the CP model

		Solver cp = makeSolver();
		
		// operation[i] identifies the operation (rotate(X,Y),rotate_and_analyze(X,Y)) taken at step i+1 of the plan;
		IntVar[] operation = new IntVar[length];
		for (int i = 0; i < operation.length; i++) {
		    operation[i] = Factory.makeIntVar(cp, 0, nbOperations-1);
		    operation[i].setName("operation"+"["+i+"]");
		}
		// objective to minimize
		IntVar planCost = Factory.makeIntVar(cp, nbBatches*raCost + (length-nbBatches)*Math.min(rCost,raCost), bestPlanCost - 1);
		planCost.setName("planCost");

		// build layout automaton for the batch state variables
		int[][] automaton = new int[2*nbBatches][nbOperations]; // states for segments with unanalyzed batch, then states for segments with analyzed batch
		createLayoutAutomaton(automaton);
		// post one regular constraint per batch
		for(int i=0; i<nbBatches; i++) {
		    cp.post(costRegular(operation, automaton, i, goalLocation[i], opCost, planCost));
		}
		//###########################################

		//###########################################
		// define the search
     		LDSearch search = makeLds(cp, maxMarginal(operation));
		search.onSolution(() -> {
 			System.out.println(Arrays.toString(operation));
 			System.out.println(planCost.toString());
			for (int i = 0; i < operation.length; i++) {
			    System.out.print(operationName[operation[i].min()]+" ");
			}
			System.out.print("   ... a plan of cost "+planCost.min());
			bestPlanCost = planCost.min();
		    });
		Objective obj = cp.minimize(planCost);
    		SearchStatistics stats = search.optimize(obj, statistics -> (statistics.numberOfFailures() > operation.length));
//     		SearchStatistics stats = search.optimize(obj, statistics -> (statistics.timeElapsed() >= 1000 && statistics.numberOfFailures() >= 100));
		//###########################################

		System.out.format("Statistics: %s\n", stats);
		totalTime += stats.timeElapsed();
	    }
	    catch(InconsistencyException e){
		System.out.println("no solution");
	    }
	}
	System.out.println("Total time: "+totalTime+" msecs");
    }

    public static void createLayoutAutomaton(int[][] automaton) {

	for(int i=0; i<automaton.length; i++) {
	    // no forbidden operation in any state; loops by default
	    for(int j=0; j<nbOperations; j++)
		automaton[i][j] = i;
	}
	if (cycleLength == 2) {
	    // rotate operations
	    for(int k=0; k<rotate.length; k++) {
		automaton[rotate[k][0]][k] = rotate[k][1];
		automaton[rotate[k][1]][k] = rotate[k][0];
		// analyzed batches
		automaton[nbBatches+rotate[k][0]][k] = nbBatches+rotate[k][1];
		automaton[nbBatches+rotate[k][1]][k] = nbBatches+rotate[k][0];
	    }
	    // rotate_and_analyze operations
	    for(int k=0; k<rotate_and_analyze.length; k++) {
		automaton[rotate_and_analyze[k][0]][rotate.length+k] = nbBatches+rotate_and_analyze[k][1];
		automaton[rotate_and_analyze[k][1]][rotate.length+k] = rotate_and_analyze[k][0];
		// analyzed batches
		automaton[nbBatches+rotate_and_analyze[k][0]][rotate.length+k] = nbBatches+rotate_and_analyze[k][1];
		automaton[nbBatches+rotate_and_analyze[k][1]][rotate.length+k] = nbBatches+rotate_and_analyze[k][0];
	    }
	}
	else {// cycleLength == 4
	    // rotate operations
	    for(int k=0; k<rotate.length; k++) {
		automaton[rotate[k][0]][k] = rotate[k][3];
		automaton[rotate[k][1]][k] = rotate[k][0];
		automaton[rotate[k][2]][k] = rotate[k][1];
		automaton[rotate[k][3]][k] = rotate[k][2];
		// analyzed batches
		automaton[nbBatches+rotate[k][0]][k] = nbBatches+rotate[k][3];
		automaton[nbBatches+rotate[k][1]][k] = nbBatches+rotate[k][0];
		automaton[nbBatches+rotate[k][2]][k] = nbBatches+rotate[k][1];
		automaton[nbBatches+rotate[k][3]][k] = nbBatches+rotate[k][2];
	    }
	    // rotate_and_analyze operations
	    for(int k=0; k<rotate_and_analyze.length; k++) {
		automaton[rotate_and_analyze[k][0]][rotate.length+k] = nbBatches+rotate_and_analyze[k][3];
		automaton[rotate_and_analyze[k][1]][rotate.length+k] = rotate_and_analyze[k][0];
		automaton[rotate_and_analyze[k][2]][rotate.length+k] = rotate_and_analyze[k][1];
		automaton[rotate_and_analyze[k][3]][rotate.length+k] = rotate_and_analyze[k][2];
		// analyzed batches
		automaton[nbBatches+rotate_and_analyze[k][0]][rotate.length+k] = nbBatches+rotate_and_analyze[k][3];
		automaton[nbBatches+rotate_and_analyze[k][1]][rotate.length+k] = nbBatches+rotate_and_analyze[k][0];
		automaton[nbBatches+rotate_and_analyze[k][2]][rotate.length+k] = nbBatches+rotate_and_analyze[k][1];
		automaton[nbBatches+rotate_and_analyze[k][3]][rotate.length+k] = nbBatches+rotate_and_analyze[k][2];
	    }
	}
    }
    
}
