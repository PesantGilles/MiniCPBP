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

package minicpbp.engine.constraints;

import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;
import static minicpbp.cp.Factory.equal;
import minicpbp.util.exception.NotImplementedException;

public class Element1DVar extends AbstractConstraint {

    private final IntVar[] array;
    private final IntVar y;
    private final IntVar z;
    private final IntVar[] vars;

    private final int[] yValues;
    private IntVar supMin;
    private IntVar supMax;
    private int zMin;
    private int zMax;
    

    public Element1DVar(IntVar[] array, IntVar y, IntVar z, IntVar[] vars) {
        super(y.getSolver(), vars);
	    setName("Element1DVar");
        this.array = array;
        this.y = y;
        this.z = z;
        this.vars = vars;

        yValues = new int[y.size()];

        if (z.isBound())
            setExactWCounting(true);
        else
            setExactWCounting(false);
    }

    @Override
    public void post() {
        y.removeBelow(0);
        y.removeAbove(array.length - 1);

        if (z.isBound()) { // special case, important for inverse constraint
            for (IntVar t : array) {
                t.propagateOnDomainChange(this);
            }
            y.propagateOnDomainChange(this);
        }
        else {
            for (IntVar t : array) {
                t.propagateOnBoundChange(this);
            }
            y.propagateOnDomainChange(this);
            z.propagateOnBoundChange(this);
        }

        propagate();
    }

    @Override
    public void propagate() {
        if (y.isBound()) {
            y.getSolver().post(equal(z,array[y.min()]));
            setActive(false);
        }
        else if (z.isBound()) { // special case, important for inverse constraint
            filterYwhenZbound();
            if (y.isBound()) {
                y.getSolver().post(equal(z,array[y.min()]));
                setActive(false);
            }
        }
        else {
            zMin = z.min();
            zMax = z.max();
            filterY();
            if (y.isBound()) {
                y.getSolver().post(equal(z,array[y.min()]));
                setActive(false);
            }
            else {
                z.removeBelow(supMin.min());
                z.removeAbove(supMax.max());
            }
        }

    }

    private void filterY() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int i = y.fillArray(yValues);
        while (i > 0) {
            i -= 1;
            int id = yValues[i];
            IntVar tVar = array[id];
            int tMin = tVar.min();
            int tMax = tVar.max();
            if (tMax < zMin || tMin > zMax) {
                y.remove(id);
            } else {
                if (tMin < min) {
                    min = tMin;
                    supMin = tVar;
                }
                if (tMax > max) {
                    max = tMax;
                    supMax = tVar;
                }
            }
        }
    }

    private void filterYwhenZbound() {
        int i = y.fillArray(yValues);
        while (i > 0) {
            i -= 1;
            int id = yValues[i];
            if (!array[id].contains(z.min())) {
                y.remove(id);
            }
        }
    }

    public void updateBelief() {
        if (z.isBound()) { // special case: compute exact beliefs
            double cumul = beliefRep.zero();
            // for y
            int nVal = y.fillArray(domainValues);
            for (int j = 0; j < nVal; j++) {
                int v = domainValues[j];
                setLocalBelief(array.length /* i.e. y */, v, outsideBelief(v, z.min()));
                cumul = beliefRep.add(cumul, beliefRep.multiply(outsideBelief(v,z.min()),
                        outsideBelief(array.length,v)));
            }
            // for array
            for (int i = 0; i < array.length; i++) {
                if (y.contains(i)) {
                    double cumulOthers = beliefRep.subtract(cumul, beliefRep.multiply(outsideBelief(i,z.min()), outsideBelief(array.length,i)));
                    nVal = array[i].fillArray(domainValues);
                    for (int j = 0; j < nVal; j++) {
                        int v = domainValues[j];
                        if (v == z.min()) {
                            setLocalBelief(i, v, beliefRep.add(cumulOthers, outsideBelief(array.length, i)));
                        } else {
                            setLocalBelief(i, v, cumulOthers);
                        }
                    }
                }
                else { // its values equally support every solution
                    nVal = array[i].fillArray(domainValues);
                    for (int j = 0; j < nVal; j++) {
                        setLocalBelief(i, domainValues[j], beliefRep.one()); // will be normalized
                    }
                }
            }
        }
        else { // default uniform belief
            for (int i = 0; i < vars.length; i++) {
                int nVal = vars[i].fillArray(domainValues);
                for (int j = 0; j < nVal; j++) {
                    setLocalBelief(i, domainValues[j], beliefRep.one()); // will be normalized
                }
            }
        }
    }
}
