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
 * Element Constraint modeling {@code matrix[x][y] = z}
 *
 */
public class Element2DDomainConsistent extends AbstractConstraint {

    private final int[][] m;
    private final IntVar x, y;
    private final IntVar z;

    /**
     * Creates an element constraint {@code matrix[x][y] = z}
     *
     * @param matrix the 2d array representing a matrix to index
     * @param x   the first dimension index variable
     * @param y   the second dimention index variable
     * @param z   the result variable
     */
    public Element2DDomainConsistent(int[][] matrix, IntVar x, IntVar y, IntVar z) {
        super(y.getSolver(), new IntVar[]{x,y,z});
        setName("Element2DDomainConsistent");
        this.m = matrix;
        this.x = x;
        this.y = y;
        this.z = z;
	setExactWCounting(true);
    }

    @Override
    public void post() {
        int xDim = m.length;
        int yDim = m[0].length;
        int [][] table = new int [xDim*yDim][3];
        for (int i = 0; i < xDim; i++) {
	    for (int j = 0; j < yDim; j++) {
		table[i*yDim + j][0] = i;
		table[i*yDim + j][1] = j;
		table[i*yDim + j][2] = m[i][j];
	    }
        }
        getSolver().post(new TableCT(new IntVar[]{x,y,z},table));
        setActive(false);
    }
}
