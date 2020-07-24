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


package minicp.engine.core;

import minicp.util.Procedure;
import minicp.util.Belief;

/**
 * A view on a variable of type {@code x+o}
 */
public class IntVarViewOffset implements IntVar {

    private final IntVar x;
    private final int o;
    private String name;
    private Belief beliefRep;

    public IntVarViewOffset(IntVar x, int offset) { // y = x + o
        this.x = x;
        this.o = offset;
	beliefRep = x.getSolver().getBeliefRep();
    }

    @Override
    public Solver getSolver() {
        return x.getSolver();
    }

    @Override
    public void whenBind(Procedure f) {
        x.whenBind(f);
    }

    @Override
    public void whenBoundsChange(Procedure f) {
        x.whenBoundsChange(f);
    }

    @Override
    public void whenDomainChange(Procedure f) {
        x.whenDomainChange(f);
    }

    @Override
    public void propagateOnDomainChange(Constraint c) {
        x.propagateOnDomainChange(c);
    }

    @Override
    public void propagateOnBind(Constraint c) {
        x.propagateOnBind(c);
    }

    @Override
    public void propagateOnBoundChange(Constraint c) {
        x.propagateOnBoundChange(c);
    }

    @Override
    public int min() {
        return x.min() + o;
    }

    @Override
    public int max() {
        return x.max() + o;
    }

    @Override
    public int size() {
        return x.size();
    }

    @Override
    public int fillArray(int[] dest) {
        int s = x.fillArray(dest);
        for (int i = 0; i < s; i++) {
            dest[i] += o;
        }
        return s;
    }

    @Override
    public boolean isBound() {
        return x.isBound();
    }

    @Override
    public boolean contains(int v) {
        return x.contains(v - o);
    }

    @Override
    public void remove(int v) {
        x.remove(v - o);
    }

    @Override
    public void assign(int v) {
        x.assign(v - o);
    }

    @Override
    public void removeBelow(int v) {
        x.removeBelow(v - o);
    }

    @Override
    public void removeAbove(int v) {
        x.removeAbove(v - o);
    }

    @Override
    public int randomValue() {
	return x.randomValue() + o;
    }

    @Override
    public double marginal(int v) {
    	return x.marginal(v - o);
    }

    @Override
    public void setMarginal(int v, double m) {
    	x.setMarginal(v - o,m);
    }

    @Override
    public void resetMarginals() {
	x.resetMarginals();
    }

    @Override
    public void normalizeMarginals() {
	x.normalizeMarginals();
    }

    @Override
    public double maxMarginal() {
	return x.maxMarginal();
    }

    @Override
    public int valueWithMaxMarginal() {
	return x.valueWithMaxMarginal() + o;
    }

    @Override
    public double minMarginal() {
	return x.minMarginal();
    }

    @Override
    public int valueWithMinMarginal() {
	return x.valueWithMinMarginal() + o;
    }

    @Override
    public double maxMarginalRegret() {
	return x.maxMarginalRegret();
    }

    @Override
    public double sendMessage(int v, double b) {
	assert b<=beliefRep.one() && b>=beliefRep.zero() : "b = "+b ;
	assert x.marginal(v - o)<=beliefRep.one() && x.marginal(v - o)>=beliefRep.zero() : "x.marginal(v - o) = "+x.marginal(v - o) ;
	return (beliefRep.isZero(b)? x.marginal(v - o) : beliefRep.divide(x.marginal(v - o),b));
    }

    @Override
    public void receiveMessage(int v, double b) {
	assert b<=beliefRep.one() && b>=beliefRep.zero() : "b = "+b ;
	assert x.marginal(v - o)<=beliefRep.one() && x.marginal(v - o)>=beliefRep.zero() : "x.marginal(v - o) = "+x.marginal(v - o) ;
	x.setMarginal(v - o,beliefRep.multiply(x.marginal(v - o),b));
    }

    @Override
    public String getName() {
	if (this.name!=null)
	    return this.name;
	else
	    return x.getName()+"'s view (offset)";
    }
    
    @Override
    public void setName(String name) {
	this.name = name;
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        for (int i = min(); i <= max() - 1; i++) {
            if (contains((i))) {
                b.append(i);
		b.append("  <");
		b.append(marginal(i));
		b.append(">, ");
            }
        }
        if (size() > 0) {
	    b.append(max());
	    b.append("  <");
	    b.append(marginal(max()));
	    b.append(">, ");
	}
        b.append("}");
        return b.toString();

    }
}
