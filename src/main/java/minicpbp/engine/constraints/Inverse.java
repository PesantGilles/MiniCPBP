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
 * Inverse constraint {@code invf is the inverse function of f}
 *
 */
public class Inverse extends AbstractConstraint {

    private final IntVar[] f;
    private final IntVar[] invf;
    private final int n;

    /**
     * Creates an inverse constraint {@code invf is the inverse function of f}
     *
     * @param f    an array of variables
     * @param invf an array of variables
     */
    public Inverse(IntVar[] f, IntVar[] invf) {
        super(f[0].getSolver(), new IntVar[]{});
	    n = f.length;
	    assert (invf.length == n);
        setName("Inverse");
        this.f = f;
        this.invf = invf;
    }

    @Override
    public void post() {
      for (int i = 0; i < n; i++) {
          invf[i].removeBelow(0);
          invf[i].removeAbove(n-1);
      }
      for (int i = 0; i < n; i++) {
          getSolver().post(Factory.element(invf,f[i],Factory.makeIntVar(getSolver(),i,i)));
      }
      setActive(false);
    }
}
