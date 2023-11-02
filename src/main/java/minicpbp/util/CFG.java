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
 * Grammar is read in the following order: terminalCount, nonTerminalCount, length1productionCount, length2productionCount...
 * ... for each length-1 production: left, right symbol; for each length-2 production: left, right symbols
 */

public class CFG {

    private int length1productionCount, length2productionCount;
    private Production[] length1productions, length2productions;
    private int terminalCount; // Any integer c s.t. 0 <= c < terminalCount is a terminal
    private int nonTerminalCount; // Start symbol is terminalCount

    public CFG(InputReader reader) {
	    terminalCount = reader.getInt();
	    nonTerminalCount = reader.getInt();
	    length1productionCount = reader.getInt();
		length2productionCount = reader.getInt();
	    length1productions = new Production[length1productionCount];
		length2productions = new Production[length2productionCount];
		for(int i=0; i<length1productionCount; i++) {
			length1productions[i] = new Production();
			length1productions[i].left = reader.getInt();
			length1productions[i].right = new int[1];
			length1productions[i].right[0] = reader.getInt();
		}
		for(int i=0; i<length2productionCount; i++) {
			length2productions[i] = new Production();
			length2productions[i].left = reader.getInt();
			length2productions[i].right = new int[2];
			length2productions[i].right[0] = reader.getInt();
			length2productions[i].right[1] = reader.getInt();
		}
    }

    public int terminalCount() {
	return terminalCount;
    }
    public int nonTerminalCount() {
	return nonTerminalCount;
    }
    public int length1productionCount() {
	return length1productionCount;
    }
    public Production[] length1productions() { return length1productions; }
	public int length2productionCount() {
		return length2productionCount;
	}
	public Production[] length2productions() { return length2productions; }

}


