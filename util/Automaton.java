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

package minicp.util;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import minicp.util.io.InputReader;

/**
 * Automaton for classical AI planning
 */

public class Automaton {

    private int nbStates;
    private int initialState;
    private List<Integer> goalStates;
    private int nbLocalActions;
    private int[][] actionMap;
    private int[][] transitionFct;
    private int optimizationConstraint;
    private int[] actionCost;

    public Automaton(InputReader reader, int nbActions) {
	nbStates = reader.getInt();
	initialState = reader.getInt();
	int nbGoals = reader.getInt();
	goalStates = new ArrayList<Integer>();
	for(int i=0; i<nbGoals; i++) {
	    goalStates.add(reader.getInt());
	}
	nbLocalActions = reader.getInt();
	actionMap = new int[nbActions][2];
	for(int i=0; i<nbActions; i++) {
	    actionMap[i][0] = i;
	    actionMap[i][1] = reader.getInt();
	}
	transitionFct = reader.getMatrix(nbStates,nbLocalActions);
	optimizationConstraint = reader.getInt();
	if (optimizationConstraint==1) {
	    actionCost = new int[nbLocalActions];
	    for(int i=0; i<nbLocalActions; i++) {
		actionCost[i] = reader.getInt();
	    }
	}
    }

    public int initialState() {
	return initialState;
    }
    public List<Integer> goalStates() {
	return goalStates;
    }
    public int nbLocalActions() {
	return nbLocalActions;
    }
    public int[][] actionMap() {
	return actionMap;
    }
    public int[][] transitionFct() {
	return transitionFct;
    }
    public boolean optimizationConstraint() {
	return (optimizationConstraint==1);
    }
    public int[] actionCost() {
	return actionCost;
    }
}


