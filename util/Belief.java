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

/**
 * Operations on beliefs/marginals/probabilities
 * min value is ZERO; max value is ONE
 */
public interface Belief {
    
    /**
     * returns value ONE
     */
    double one();

    /**
     * returns value ZERO
     */
    double zero();

    /**
     * returns true iff belief == ONE
     */
    boolean isOne(double belief);

    /**
     * returns true iff belief == ZERO
     */
    boolean isZero(double belief);

    /**
     * returns the conversion of value a from the standard representation to whatever the implemented representation is
     */
    double std2rep(double a);

    /**
     * returns the conversion of value a from whatever the implemented representation is to the standard representation
     */
    double rep2std(double a);

    /**
     * returns the product of beliefs a and b
     */
    double multiply(double a, double b);

    /**
     * returns the division of belief a by b
     */
    double divide(double a, double b);

    /**
     * returns the sum of beliefs a and b
     */
    double add(double a, double b);

    /**
     * returns b s.t. add(a,b)==ONE
     */
    double complement(double a);

    /**
     * returns the sum of beliefs in array a of given size 
     */
    double summation(double a[], int size);
}
