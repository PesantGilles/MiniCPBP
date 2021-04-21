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
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.engine.core.Constraint;
import minicp.engine.constraints.*;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;

import java.util.Arrays;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Random;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;

/*
Spatially-Balanced Latin Square Problem, for Model Counting
symbols are the integers 0..n-1
 */
public class SBLS_ModelCounting {
    
    static Random rand = new Random();

    public static void main(String[] args) {

	int n = Integer.parseInt(args[0]); // n x n Latin Square
 	int m = Integer.parseInt(args[1]); // m linear modular equality constraints added
	
	if (n % 3 == 1) {
	    System.out.println("no spatially-balanced latin square possible");
	    System.exit(1);
	}
	int bal = n*(n+1)/3;
	
        Solver cp = Factory.makeSolver();
	
	////////////////////////
	// streamlined model representing a specially-structured subset of the solutions
	// taken from "Streamlining Local Search for Spatially Balanced Latin Squares"
	// by Smith, Gomes, and Fernandez, IJCAI 2005
	////////////////////////
	//
	// Start with the cyclic latin square and explore the space of column permutations
	// to achieve a LS with perfect spatial balance
	//
	// x[i] is the position of symbol i in the first row (completely defines the LS)
	//
        IntVar[] x = new IntVar[n];
	for (int i = 0; i < n; i++) {
	    x[i] = makeIntVar(cp, 0, n-1);
	    x[i].setName("x"+"["+i+"]");
	}
	x[0].assign(0); // breaking value symmetry
	cp.post(allDifferentAC(x)); // define the search space of column permutations
	// Only floor(n/2) distances need to be tracked
        // d[j][i] is the ith distance between integers 1 and 2+j in the LS
	IntVar[][] d = new IntVar[n/2][n];
	for (int j = 0; j < n/2; j++) {
	    for (int i = 0; i < n; i++) {
		IntVar[] vars = new IntVar[2];
		vars[0] = x[i];
		vars[1] = minus(x[(i+j+1)%n]);
		IntVar tmp = sum(vars);
		d[j][i] = abs(tmp);
	    }	    
	}
	// Spatial Balance constraints
	for (int j = 0; j < n/2; j++) {
	    cp.post(sum(d[j],bal));
	}

	DFSearch dfs;

	if (m==0) {
	    dfs = makeDfs(cp, firstFail(x));
	}
	else {
	    int p = 17; // some prime number at least as large as n
	    
	    int[][] A = new int[m][x.length];
	    int[] b = new int[m];
	    
	    for (int i=0; i<m; i++) {
		b[i] = rand.nextInt(p);
		for (int j=0; j<A[i].length; j++) {
		    A[i][j] = rand.nextInt(p);
		}
	    }
	    
	    Constraint L = linEqSystemModP(A,x,b,p);
	    cp.post(L);

  	    dfs = makeDfs(cp, firstFail(((LinEqSystemModP) L).getParamVars())); // branch on parametric variables of GJE solved form
	}	    
	    
  	dfs.onSolution(() -> {
		for (int i = 0; i < n; i++)
		    System.out.print("-");
		System.out.println();
		for (int i = 0; i < n; i++) {
		    for (int j = 0; j < n; j++) {
			for (int k = 0; k < n; k++) {
			    if (!x[k].isBound()) {
				System.out.println("Var "+x[k].getName()+" is not bound!");
				return;
			    }
			    if (x[k].min()==j) {
				System.out.print(((i+k)%n+1)+" ");
				break;
			    }
			}
		    }
		    System.out.println();
		}
  	    }
	 );

   	SearchStatistics stats = dfs.solve();
	System.out.println(stats);
    }
}
