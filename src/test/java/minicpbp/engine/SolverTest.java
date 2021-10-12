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

package minicpbp.engine;


import minicpbp.engine.core.MiniCP;
import minicpbp.engine.core.Solver;
import minicpbp.state.Copier;
import minicpbp.state.Trailer;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public abstract class SolverTest {

    @Parameterized.Parameters
    public static Supplier<Solver>[] data() {
        return new Supplier[]{
                () -> new MiniCP(new Trailer()),
                () -> new MiniCP(new Copier()),
        };
    }

    @Parameterized.Parameter
    public Supplier<Solver> solverFactory;
}
