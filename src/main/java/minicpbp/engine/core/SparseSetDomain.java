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

package minicpbp.engine.core;

import minicpbp.state.StateSparseWeightedSet;
import minicpbp.util.Belief;

import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Implementation of a domain with a sparse-set
 */
public class SparseSetDomain implements IntDomain {
    private StateSparseWeightedSet domain;
    private int[] domainValues; // an array large enough to hold the domain
    private double[] beliefValues; // an auxiliary array as large as domainValues
    private Solver cp;
    private Belief beliefRep;

    static Random rand = new Random();

    public SparseSetDomain(Solver cp, int min, int max) {
        domain = new StateSparseWeightedSet(cp, max - min + 1, min);
        domainValues = new int[max - min + 1];
        beliefValues = new double[max - min + 1];
        this.cp = cp;
        beliefRep = cp.getBeliefRep();
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
    public int randomValue() {
        if (domain.isEmpty())
            throw new NoSuchElementException();
        int s = fillArray(domainValues);
        return domainValues[rand.nextInt(s)];
    }


    @Override
    public int biasedWheelValue() {
        if (domain.isEmpty())
            throw new NoSuchElementException();
        int s = fillArray(domainValues);
	// to avoid this linear-time step, could replace max by upper bound 1
	// alternatively, could decide to maintain max marginal of domain
        double max = beliefRep.zero();
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            if (marginal(v) > max) {
                max = marginal(v);
            }
        }
	// stochastic acceptance algorithm
	while (true) {
            int v = domainValues[rand.nextInt(s)];
	    if (Math.random() < marginal(v)/max)
		return v;
	}
    }

    @Override
    public double marginal(int v) {
        return domain.weight(v);
    }

    @Override
    public void setMarginal(int v, double m) {
        domain.setWeight(v, m);
    }

    @Override
    public void resetMarginals() {
        int s = fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            setMarginal(domainValues[j], beliefRep.one());
        }
    }

    @Override
    public void normalizeMarginals() {

        int s = fillArray(domainValues);
        if (s == 1) { // corresponding variable is bound
            setMarginal(domainValues[0], beliefRep.one());
            return;
        }
        for (int j = 0; j < s; j++) {
            beliefValues[j] = marginal(domainValues[j]);
        }
        double normalizingConstant = beliefRep.summation(beliefValues, s);
        if (beliefRep.isZero(normalizingConstant)) // all marginals are zero (actOnZeroOneBelief set to false?)
            return;
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            setMarginal(v, beliefRep.divide(marginal(v), normalizingConstant));
            assert marginal(v) <= beliefRep.one() && marginal(v) >= beliefRep.zero() : "marginal(v) = " + marginal(v);
        }
    }

    @Override
    public double maxMarginal() {
        if (domain.isEmpty())
            throw new NoSuchElementException();
        double max = beliefRep.zero();
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
        int s = fillArray(domainValues);
        int valWithMax = domainValues[0];
        double max = marginal(valWithMax);
        for (int j = 1; j < s; j++) {
            int v = domainValues[j];
            if (marginal(v) > max) {
                max = marginal(v);
                valWithMax = v;
            }
        }
        return valWithMax;
    }

    @Override
    public double minMarginal() {
        if (domain.isEmpty())
            throw new NoSuchElementException();
        double min = beliefRep.one();
        int s = fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            int v = domainValues[j];
            if (marginal(v) < min) {
                min = marginal(v);
            }
        }
        return min;
    }

    @Override
    public int valueWithMinMarginal() {
        if (domain.isEmpty())
            throw new NoSuchElementException();
        int s = fillArray(domainValues);
        int valWithMin = domainValues[0];
        double min = marginal(valWithMin);
        for (int j = 1; j < s; j++) {
            int v = domainValues[j];
            if (marginal(v) < min) {
                min = marginal(v);
                valWithMin = v;
            }
        }
        return valWithMin;
    }

    @Override
    public double maxMarginalRegret() {
        if (domain.isEmpty())
            throw new NoSuchElementException();
        double max = beliefRep.zero();
        double nextMax = beliefRep.zero();
        int s = fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            double m = marginal(domainValues[j]);
            if (m > max) {
                nextMax = max;
                max = m;
            } else if (m > nextMax) {
                nextMax = m;
            }
        }
        return max - nextMax;
    }

    @Override
    public double entropy() {
        double H = 0;
        int s = fillArray(domainValues);
        for (int j = 0; j < s; j++) {
            double m = beliefRep.rep2std(marginal(domainValues[j]));
	    if (m > 0)
		H += m * Math.log(m);
        }
        return -H;
    }

    @Override
    public String toString() {
        return domain.toString();
    }

}
