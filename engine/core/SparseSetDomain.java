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

package minicp.engine.core;

import minicp.state.StateManager;
import minicp.state.StateSparseWeightedSet;

import java.util.NoSuchElementException;


/**
 * Implementation of a domain with a sparse-set
 */
public class SparseSetDomain implements IntDomain {
    private StateSparseWeightedSet domain;
    private int[] domainValues; // an array large enough to hold the domain


    public SparseSetDomain(StateManager sm, int min, int max) {
        domain = new StateSparseWeightedSet(sm, max - min + 1, min);
	domainValues = new int[max - min + 1];
    }

    @Override
    public int fillArray(int[] dest) {
        return domain.fillArray(dest);
    }

    @Override
    public int min() {
        return domain.min();
    }

    @Override
    public int max() {
        return domain.max();
    }

    @Override
    public int size() {
        return domain.size();
    }

    @Override
    public boolean contains(int v) {
        return domain.contains(v);
    }

    @Override
    public boolean isBound() {
        return domain.size() == 1;
    }

    @Override
    public void remove(int v, DomainListener l) {
        if (domain.contains(v)) {
            boolean maxChanged = max() == v;
            boolean minChanged = min() == v;
            domain.remove(v);
            if (domain.size() == 0)
                l.empty();
            l.change();
            if (maxChanged) l.changeMax();
            if (minChanged) l.changeMin();
            if (domain.size() == 1) l.bind();
        }
    }

    @Override
    public void removeAllBut(int v, DomainListener l) {
        if (domain.contains(v)) {
            if (domain.size() != 1) {
                boolean maxChanged = max() != v;
                boolean minChanged = min() != v;
                domain.removeAllBut(v);
                if (domain.size() == 0)
                    l.empty();
                l.bind();
                l.change();
                if (maxChanged) l.changeMax();
                if (minChanged) l.changeMin();
            }
        } else {
            domain.removeAll();
            l.empty();
        }
    }

    @Override
    public void removeBelow(int value, DomainListener l) {
        if (domain.min() < value) {
            domain.removeBelow(value);
            switch (domain.size()) {
                case 0:
                    l.empty();
                    break;
                case 1:
                    l.bind();
                default:
                    l.changeMin();
                    l.change();
                    break;
            }
        }
    }

    @Override
    public void removeAbove(int value, DomainListener l) {
        if (domain.max() > value) {
            domain.removeAbove(value);
            switch (domain.size()) {
                case 0:
                    l.empty();
                    break;
                case 1:
                    l.bind();
                default:
                    l.changeMax();
                    l.change();
                    break;
            }
        }
    }

    @Override
    public double marginal(int v) {
        return domain.weight(v);
    }

    @Override
    public void setMarginal(int v, double m) {
        domain.setWeight(v,m);
    }

    @Override
    public void resetMarginals() {
	int s = fillArray(domainValues);
	for (int j = 0; j < s; j++) {
	    int v = domainValues[j];
	    setMarginal(v,1);
	}
    }

    @Override
    public void normalizeMarginals(double epsilon) {
	double sum = 0;
	int s = fillArray(domainValues);
	for (int j = 0; j < s; j++) {
	    int v = domainValues[j];
	    if ((marginal(v) < epsilon) && (marginal(v) > 0))
		setMarginal(v,epsilon);
	    else if ((marginal(v) > 1 - epsilon) && (marginal(v) < 1))
		setMarginal(v,1 - epsilon);
	    sum += marginal(v);
	}
        assert(sum > 0);
	for (int j = 0; j < s; j++) {
	    int v = domainValues[j];
	    double nm = marginal(v)/sum;
	    setMarginal(v,nm);
	}
    }

    @Override
    public double maxMarginal() {
        if (domain.isEmpty())
            throw new NoSuchElementException();
    	double max = -1;
	int s = fillArray(domainValues);
	for (int j = 0; j < s; j++) {
	    int v = domainValues[j];
	    if (marginal(v) > max) {
		max = marginal(v);
	    }
    	}
    	return max;
    }

    @Override
    public int valueWithMaxMarginal() {
        if (domain.isEmpty())
            throw new NoSuchElementException();
    	double max = -1;
	int valWithMax = -1;
	int s = fillArray(domainValues);
	for (int j = 0; j < s; j++) {
	    int v = domainValues[j];
	    if (marginal(v) > max) {
		max = marginal(v);
		valWithMax = v;
	    }
    	}
    	return valWithMax;
    }

    @Override
    public String toString() {
	return domain.toString();
    }

}
