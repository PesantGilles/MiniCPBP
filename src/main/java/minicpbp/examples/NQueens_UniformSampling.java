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

package minicpbp.examples;

import minicpbp.cp.Factory;
import minicpbp.cp.BranchingScheme;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;

import java.util.Arrays;

/**
 * The N-Queens problem. Sampling solutions.
 * <a href="http://csplib.org/Problems/prob054/">CSPLib</a>.
 */
/*
public class NQueens_UniformSampling {

    public static void main(String[] args) {

	int n = Integer.parseInt(args[0]); // n x n chess board
  	double fraction = Double.parseDouble(args[1]);

        Solver cp = Factory.makeSolver();
        IntVar[] q = Factory.makeIntVarArray(cp, n, n);
	IntVar[] ql = Factory.makeIntVarArray(n,i -> Factory.plus(q[i],i));
	IntVar[] qr = Factory.makeIntVarArray(n,i -> Factory.plus(q[i],-i));
	    for(int i = 0; i<q.length; i++){
		q[i].setName("q"+i);
 		ql[i].setName("ql"+i);
 		qr[i].setName("qr"+i);
	    }

	// n-ary alldifferent model
	cp.post(Factory.allDifferentAC(q));
	cp.post(Factory.allDifferentAC(ql));
	cp.post(Factory.allDifferentAC(qr));

	// sampling a "fraction" of the solutions
 	IntVar[] branchingVars = cp.sample(fraction,q);

 	DFSearch search = Factory.makeDfs(cp, BranchingScheme.firstFail(branchingVars));

	/*
        search.onSolution(() -> {
		for(int i = 0; i<q.length; i++){
		    System.out.print(q[i].min()+" ");
		}
		System.out.println();
	    }
	);


   	SearchStatistics stats = search.solve();

//  	System.out.format("#Solutions: %s\n", stats.numberOfSolutions());
 	System.out.format("Statistics: %s\n", stats);

    }
}
*/