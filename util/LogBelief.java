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

package minicp.util;

import java.util.Arrays;

/**
 * Operations on beliefs/marginals/probabilities
 * Adopting a log-probability representation
 */
public class LogBelief implements Belief {
    

    private static final double ONE = 0.0;
    private static final double ZERO = Double.NEGATIVE_INFINITY;

    public  double one() {
	return ONE;
    }

    public  double zero() {
	return ZERO;
    }

    public  boolean isOne(double belief) {
	return belief == ONE;
    }

    public  boolean isZero(double belief) {
	return Double.isInfinite(belief); // since log-probabilities are non-positive, this must be -infinity
    }

    private  double exp(double a){
	return Math.exp(a);
    }

    public  double std2rep(double a) {
	return Math.log(a);
    }

    public  double rep2std(double a) {
	return exp(a);
    }

    public  double multiply(double a, double b) {
	return a+b;
    }

    public  double divide(double a, double b) {
	return a-b;
    }

    public  double add(double a, double b) {
	// see https://en.wikipedia.org/wiki/Log_probability
	if (isZero(a))
	    return b;
	else if (isZero(b))
	    return a;
	else
	    return (a > b ? 
		    a + Math.log1p(exp(b-a)) : 
		    b + Math.log1p(exp(a-b)) );
    }

    public  double complement(double a) { 
	if (isZero(a))
	    return one();
	if (isOne(a))
	    return zero();
	return Math.log(1-exp(a));
    }

    public  double summation(double a[], int size) {
	// see https://en.wikipedia.org/wiki/List_of_logarithmic_identities#Summation/subtraction
 	Arrays.sort(a,0,size); // need sorted log beliefs in order to sum them
 	double largestBelief = a[size-1];
	if (isZero(largestBelief))
	    return zero();
 	double sum = 0;
	for (int j = size-2; j>=0; j--) {  // proceed in decreasing order, starting from the 2nd largest
	    sum += exp(a[j]-largestBelief);
	}
     	return largestBelief + Math.log1p(sum);
    }
}
