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

package minicpbp.engine.constraints;

import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;

import static minicpbp.cp.Factory.firstOccurrence;
import static minicpbp.cp.Factory.lastOccurrence;

/**
 * Span-of-Occurrences constraint (rewritten as firstOccurrence and lastOccurrence)
 */
public class SpanOccurrence extends AbstractConstraint {

    private final IntVar[] x;
    private final int[] V;
    private final IntVar firstPos, lastPos;

    /**
     * Creates a SpanOccurrence constraint.
     *
     * @param x         an array of variables whose instantiations belonging to V we consider
     * @param V         an array of values
     * @param firstPos  the index of the first occurrence of a value from V in x
     * @param lastPos   the index of the last occurrence of a value from V in x
     * Note: This constraint is currently decomposed into a firstOccurrence and a lastOccurrence constraint.
     */

     public SpanOccurrence(IntVar[] x, int[] V, IntVar firstPos, IntVar lastPos, IntVar[] vars) {
         super(firstPos.getSolver(), vars);
         setName("SpanOccurrence");
         this.x = x;
         this.V = V;
         this.firstPos = firstPos;
         this.lastPos = lastPos;
         setExactWCounting(false);
     }

    @Override
    public void post() {
    	firstPos.getSolver().post(firstOccurrence(x,V,firstPos));
	    firstPos.getSolver().post(lastOccurrence(x,V,lastPos));
        setActive(false);
    }

}
