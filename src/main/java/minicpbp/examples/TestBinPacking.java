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
import minicpbp.engine.constraints.*;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;
import minicpbp.util.io.InputReader;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;

import static minicpbp.cp.BranchingScheme.*;
import static minicpbp.cp.Factory.*;

public class TestBinPacking {
    public static void main(String[] args) {

        // Paper instance
        // int[] itemSizes = {2, 2, 3, 5, 5};
        // int capacity = 7;
        // int binCount = 3;

        // Lemma proof instance
        // int[] itemSizes = {1, 1, 1, 1, 2, 1, 1};
        // int capacity = 3;
        // int binCount = 4;

        // Handwritten notes instance
        // int[] itemSizes = {1, 1, 1, 1, 1};
        // int capacity = 2;
        // int binCount = 3;

        // Handwritten notes instance
        // int[] itemSizes = {1, 1, 1, 2, 2};
        // int capacity = 3;
        // int binCount = 3;

        // Large instance
//         int[] itemSizes = {11,
//                            10, 10, 10, 10, 10, 10, 10, 10, 10,
//                            9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
//                            8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
// 			   7, 7, 7, 7};
//         // Arrays.sort(itemSizes); // Sorting the items by decreasing size yields faster results
//         int capacity = 32;
//         int binCount = 13;

	// Instance Waescher_TEST0022.txt from http://or.dei.unibo.it/library/bpplib
        // int[] itemSizes = {4812, 4812, 4812, 4783, 4778, 4769, 4769, 4738, 4199, 4199, 4122, 4122, 3959, 3787, 3534, 3534, 3534, 3412, 3412, 3412, 3326, 3326, 3168, 3168, 3168, 3168, 2649, 2317, 2317, 2156, 2067, 2067, 1912, 1897, 1762, 1762, 1762, 1594, 1574, 1492, 1492, 1308, 1308, 1274, 1274, 724, 511, 511, 468, 246, 246, 117, 117, 63, 63, 55, 26};
	// Arrays.sort(itemSizes); // Sorting the items by decreasing size yields faster results
	// int capacity = 10000;
	// int binCount = 15; // optimal nb

	// modified Waescher_TEST0022.txt instance (http://or.dei.unibo.it/library/bpplib) with fewer items per bin
        // int[] itemSizes = {4812, 4812, 4812, 4783, 4778, 4769, 4769, 4738, 4199, 4199, 4122, 4122, 3959, 3787, 3534, 3534, 3534, 3412, 3412, 3412, 3326, 3326, 3168, 3168, 3168, 3168, 2649, 2317, 2317, 2156, 2067, 2067, 1912, 1897, 1762, 1762, 1762, 1594, 1574, 1492, 1492, 1308, 1308, 1274, 1274, 724, 511, 511, 468, 246, 246, 117, 117, 63, 63, 55, 26};
	// Arrays.sort(itemSizes); // Sorting the items by decreasing size yields faster results
 	// int capacity = 5000;
 	// int binCount = 30;

	// Instance with small nb of bins and tight
  	int[] itemSizes = {48, 41, 39, 37, 35, 34, 33, 31, 26, 21, 20, 17, 16, 15, 14, 14, 13, 12, 10, 8, 5, 4, 2, 1};
  	int capacity = 100;
  	int binCount = 5;

	Solver cp = makeSolver();
	IntVar[] b = new IntVar[itemSizes.length]; // bin assignment variables for items
	for( int i=0; i<itemSizes.length; i++) {
	    b[i] = makeIntVar(cp,0,binCount-1);
	    b[i].setName("b" + i);
	}
	IntVar[] l = new IntVar[binCount]; // load variables for bins
	for( int j=0; j<binCount; j++) {
	    l[j] = makeIntVar(cp,0,capacity);
	    l[j].setName("l" + j);
	}
	IntVar[] vars = new IntVar[b.length + l.length];
	System.arraycopy(b, 0, vars, 0, b.length);
	System.arraycopy(l, 0, vars, b.length, l.length);
	cp.post(new BinPacking(b,itemSizes,l,capacity,vars));
	DFSearch search = makeDfs(cp, minEntropy(vars));
	cp.setTraceBPFlag(false);
	cp.setTraceSearchFlag(false);
	/*
	search.onSolution(() -> {
		System.out.println(b.toString()+"\n"+l.toString());
        });
	*/
	SearchStatistics stats = search.solve();
	//        System.out.format("#Solutions: %s\n", stats.numberOfSolutions());
	System.out.format("Statistics: %s\n", stats);
    }
}
