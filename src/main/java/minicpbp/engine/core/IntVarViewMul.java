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


import minicpbp.util.Procedure;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.Belief;
import minicpbp.util.exception.IntOverFlowException;
import minicpbp.util.exception.NotImplementedException;

import java.util.Set;

/**
 * A view on a variable of type {@code a*x}
 */
public class IntVarViewMul implements IntVar {

    private final int a;
    private final IntVar x;
    private String name;
    private Belief beliefRep;

    public IntVarViewMul(IntVar x, int a) {
        if ((1L + x.min()) * a <= (long) Integer.MIN_VALUE)
            throw new IntOverFlowException("consider applying a smaller mul cte as the min domain on this view is <= Integer.MIN _VALUE");
        if ((1L + x.max()) * a >= (long) Integer.MAX_VALUE)
            throw new IntOverFlowException("consider applying a smaller mul cte as the max domain on this view is >= Integer.MAX _VALUE");
        assert (a > 0);
        this.a = a;
        this.x = x;
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
    public void registerConstraint(Constraint c) {
        x.registerConstraint(c);
    }

    @Override
    public int min() {
        if (a >= 0)
            return a * x.min();
        else return a * x.max();
    }

    @Override
    public int max() {
        if (a >= 0)
            return a * x.max();
        else return a * x.min();
    }

    @Override
    public int size() {
        return x.size();
    }

    @Override
    public int wDeg() {
        return x.wDeg();
    }

    @Override
    public int fillArray(int[] dest) {
        int s = x.fillArray(dest);
        for (int i = 0; i < s; i++) {
            dest[i] *= a;
        }
        return s;
    }

    @Override
    public boolean isBound() {
        return x.isBound();
    }

    @Override
    public boolean contains(int v) {
        return (v % a != 0) ? false : x.contains(v / a);
    }

    @Override
    public void remove(int v) {
        if (v % a == 0) {
            x.remove(v / a);
        }
    }

    @Override
    public void assign(int v) {
        if (v % a == 0) {
            x.assign(v / a);
        } else {
            throw new InconsistencyException();
        }
    }

    @Override
    public void removeBelow(int v) {
        x.removeBelow(ceilDiv(v, a));
    }

    @Override
    public void removeAbove(int v) {
        x.removeAbove(floorDiv(v, a));
    }

    @Override
    public void removeOutside(Set<Integer> S) {
        for (int i = x.min(); i <= x.max(); i++) {
            if (!S.contains(i*a)) {
                try {
                    x.remove(i);
                } catch (InconsistencyException e) {
                }
            }
        }
    }

    // Java's division always rounds to the integer closest to zero, but we need flooring/ceiling versions.
    private int floorDiv(int a, int b) {
        int q = a / b;
        return (a < 0 && q * b != a) ? q - 1 : q;
    }

    private int ceilDiv(int a, int b) {
        int q = a / b;
        return (a > 0 && q * b != a) ? q + 1 : q;
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

    @Override
    public int randomValue() {
	return x.randomValue() * a;
    }

    @Override
    public int biasedWheelValue() {
	return x.biasedWheelValue() * a;
    }

    @Override
    public double marginal(int v) {
	if (v % a == 0) {
	    return x.marginal(v/a);
        } else {
            throw new InconsistencyException();
        }
    }

    @Override
    public void setMarginal(int v, double m) {
	if (v % a == 0) {
	    x.setMarginal(v/a, m);
        } else {
            throw new InconsistencyException();
        }
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
	return x.valueWithMaxMarginal() * a;
    }

    @Override
    public double minMarginal() {
	return x.minMarginal();
    }

    @Override
    public int valueWithMinMarginal() {
	return x.valueWithMinMarginal() * a;
    }

    @Override
    public double maxMarginalRegret() {
	return x.maxMarginalRegret();
    }

    @Override
    public double entropy() {
	return x.entropy();
    }

    @Override
    public double impact() {
        return x.impact();
    }

    @Override
    public int valueWithMinImpact() {
        return x.valueWithMinImpact() * a;
    }

    @Override
    public int valueWithMaxImpact() {
        return x.valueWithMaxImpact() * a;
    }

    @Override
    public void registerImpact(int value, double impact) {
        if (value % a == 0) {
            x.registerImpact(value/a, impact);
        }
        else {
            throw new InconsistencyException();
        }
    }

    @Override
    public double sendMessage(int v, double b) {
	assert b<=beliefRep.one() && b>=beliefRep.zero() : "b = "+b ;
	if (v % a == 0) {
	    assert x.marginal(v/a)<=beliefRep.one() && x.marginal(v/a)>=beliefRep.zero() : "x.marginal(v/a) = "+x.marginal(v/a) ;
	    return (beliefRep.isZero(b)? x.marginal(v/a) : beliefRep.divide(x.marginal(v/a),b));
        } else {
            throw new InconsistencyException();
	}
    }

    @Override
    public void receiveMessage(int v, double b) {
	assert b<=beliefRep.one() && b>=beliefRep.zero() : "b = "+b ;
	if (v % a == 0) {
	    assert x.marginal(v/a)<=beliefRep.one() && x.marginal(v/a)>=beliefRep.zero() : "x.marginal(v/a) = "+x.marginal(v/a) ;
	    x.setMarginal(v/a,beliefRep.multiply(x.marginal(v/a),b));
        } else {
            throw new InconsistencyException();
	}
    }

    @Override
    public void setForBranching(boolean b) {
        x.setForBranching(b);;
    }

    @Override
    public boolean isForBranching() {
        return x.isForBranching();
    }

    @Override
    public String getName() {
	if (this.name!=null)
	    return this.name;
	else
	    return x.getName()+"'s view (mul)";
    }
    
    @Override
    public void setName(String name) {
	this.name = name;
    }
    
}
