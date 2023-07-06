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
 * mini-cpbp, replacing classic propagation by belief propagation 
 * Copyright (c)  2019. by Gilles Pesant
 */

package minicpbp.util;

import minicpbp.util.io.InputReader;

/**
 * Context-Free Grammar for Grammar constraint
 * (code adapted from that of Claude-Guy Quimper)
 *
 * Grammar is read in the following order: terminalCount, nonTerminalCount, productionCount, ...
 * ... for each production: left, length, right symbols
 */
// TODO: Consider splitting productions into length-1 and length-2

public class CFG {

    private int productionCount;
    private Production[] productions;
    private int terminalCount; // Any integer c s.t. 0 <= c < terminalCount is a terminal
    private int nonTerminalCount; // Start symbol is terminalCount

    public CFG(InputReader reader) {
	    terminalCount = reader.getInt();
	    nonTerminalCount = reader.getInt();
	    productionCount = reader.getInt();
	    productions = new Production[productionCount];
	    for(int i=0; i<productionCount; i++) {
	        productions[i] = new Production();
	        productions[i].left = reader.getInt();
	        productions[i].length = reader.getInt();
	        productions[i].right = new int[productions[i].length];
	        for (int j=0; j<productions[i].length; j++) {
		        productions[i].right[j] = reader.getInt();
	        }
	    }
    }

    public int terminalCount() {
	return terminalCount;
    }
    public int nonTerminalCount() {
	return nonTerminalCount;
    }
    public int productionCount() {
	return productionCount;
    }
    public Production[] productions() { return productions; }
    
}


