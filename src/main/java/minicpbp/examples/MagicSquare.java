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
 * Copyright (c)  2017. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 *
 * mini-cpbp, replacing classic propagation by belief propagation 
 * Copyright (c)  2019. by Gilles Pesant
 */

package minicpbp.examples;


import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;
import static minicpbp.cp.Factory.*;
import static minicpbp.cp.BranchingScheme.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * The Magic Square Completion problem.
 * <a href="http://csplib.org/Problems/prob019/">CSPLib</a>.
 */
public class MagicSquare {

    public static void main(String[] args) {

	int n = Integer.parseInt(args[0]);
	int nbFilled = Integer.parseInt(args[1]);
	int nbFile = Integer.parseInt(args[2]);

	boolean notEqual = false;

	Solver cp = makeSolver();

	IntVar[] xFlat = makeMagicSquare(cp,n,notEqual,nbFilled,nbFile);

	IntVar[][] x = new IntVar[n][n];
	for(int i = 0; i<n; i++){
	    for(int j = 0; j<n; j++){
		x[i][j] = xFlat[i*n+j];
	    }
	}

//    	DFSearch dfs = makeDfs(cp, firstFailRandomVal(xFlat));
   	DFSearch dfs = makeDfs(cp, maxMarginalStrength(xFlat));

        dfs.onSolution(() -> {
                    for (int i = 0; i < n; i++) {
                        System.out.println(Arrays.toString(x[i]));
                    }
                }
        );

        SearchStatistics stats = dfs.solve(stat -> stat.numberOfSolutions() >= 1); // stop on first solution

        System.out.println(stats);

    }

	public static void partialAssignments(IntVar[][] vars, int n, int nbFilled, int nbFile){
	    try {
		Scanner scanner = new Scanner(new FileReader("minicpbp/examples/data/MagicSquare/magicSquare" +n+"-filled"+nbFilled+"-"+nbFile+".dat"));
		
		scanner.nextInt();
		scanner.nextInt();
		
		while(scanner.hasNextInt()){
		    int row = scanner.nextInt()-1;
		    int column = scanner.nextInt()-1;
		    int value = scanner.nextInt();
		    vars[row][column].assign(value);
		}
		scanner.close();
	    }
	    catch (IOException e) {
		System.err.println("Error : " + e.getMessage()) ;
		System.exit(2) ;
	    }
	}

	public static IntVar[] makeMagicSquare(Solver cp, int n, boolean notEqual, int nbFilled, int nbFile) {
		int M = n*(n*n+1)/2;
		IntVar[][] x = new IntVar[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				x[i][j] = makeIntVar(cp, 1, n*n);
				x[i][j].setName("x"+"["+(i+1)+","+(j+1)+"]");
			}
		}

		IntVar[] xFlat = new IntVar[x.length * x.length];
		for (int i = 0; i < x.length; i++) {
			System.arraycopy(x[i],0,xFlat,i * x.length,x.length);
		}

		partialAssignments(x,n,nbFilled,nbFile);

		// Sum on lines
		for (int i = 0; i < n; i++) {
			cp.post(sum(x[i],M));
		}

		// Sum on columns
		for (int j = 0; j < x.length; j++) {
			IntVar[] column = new IntVar[n];
			for (int i = 0; i < x.length; i++)
				column[i] = x[i][j];
			cp.post(sum(column,M));
		}

		// Sum on diagonals
		IntVar[] diagonalLeft = new IntVar[n];
		IntVar[] diagonalRight = new IntVar[n];
		for (int i = 0; i < x.length; i++){
			diagonalLeft[i] = x[i][i];
			diagonalRight[i] = x[n-i-1][i];
		}
		cp.post(sum(diagonalLeft, M));
		cp.post(sum(diagonalRight, M));

		// AllDifferent
		if(notEqual)
 		    cp.post(allDifferent(xFlat));
		else
		    cp.post(allDifferentAC(xFlat));

		return xFlat;
	}

}
