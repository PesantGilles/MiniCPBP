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

import minicpbp.cp.Factory;
import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.Constraint;
import minicpbp.engine.core.IntVar;
import minicpbp.util.exception.NotImplementedException;


/**
 *
 * Pow Constraint modeling {@code x^y = z}
 *
 */
public class Pow extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final IntVar z;

    /**
     * Creates an exponentiation constraint {@code x ^ y = z}
     *
     * @param x the base variable
     * @param y the exponent variable
     * @param z the result variable
     */
    public Pow(IntVar x, IntVar y, IntVar z) {
        super(x.getSolver(), new IntVar[]{x,y,z});
        setName("Pow");
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void post() {
        int sizeX = x.fillArray(domainValues);
        int [][] table = new int [sizeX * y.size()][3];
	    int k = 0;
	    int vx;
        for (int i = 0; i < sizeX; i++) {
	        vx = domainValues[i];
	        for (int vy = y.min(); vy <= y.max(); vy++) {
		        if (y.contains(vy)) {
                    double res = Math.pow((double) vx, (double) vy);
                    if (res % 1 == 0) { // otherwise it is not an integer
                        table[k][0] = vx;
                        table[k][1] = vy;
                        table[k][2] = (int) res;
                        k++;
                    }
		        }
	        }
	    }
        getSolver().post(new TableCT(new IntVar[]{x,y,z},table,k));
        setActive(false);
    }
}
