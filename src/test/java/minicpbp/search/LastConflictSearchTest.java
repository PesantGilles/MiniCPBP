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

package minicpbp.search;


import minicpbp.cp.BranchingScheme;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.util.NotImplementedExceptionAssume;
import minicpbp.util.exception.NotImplementedException;
import org.junit.Test;

import java.util.Arrays;

import static minicpbp.cp.Factory.*;
import static org.junit.Assert.assertEquals;

public class LastConflictSearchTest {

// TODO to reactivate once Last Conflict is implemented
//    @Test
//    public void testExample1() {
//        try {
//            Solver cp = makeSolver();
//            IntVar[] x = makeIntVarArray(cp, 8, 8);
//            for(int i = 4; i < 8; i++)
//                x[i].removeAbove(2);
//
//            // apply alldifferent on the four last variables.
//            // of course, this cannot work!
//            IntVar[] fourLast = Arrays.stream(x).skip(4).toArray(IntVar[]::new);
//            cp.post(allDifferent(fourLast),true);
//
//            DFSearch dfs = new DFSearch(cp.getStateManager(), BranchingScheme.lastConflict(
//                    () -> { //select first unbound variable in x
//                        for(IntVar z: x)
//                            if(!z.isBound())
//                                return z;
//                        return null;
//                    },
//                    IntVar::min //select smallest value
//            ));
//
//            SearchStatistics stats = dfs.solve();
//            assertEquals(0, stats.numberOfSolutions());
//            assertEquals(70, stats.numberOfFailures());
//            assertEquals(138, stats.numberOfNodes());
//        }
//        catch (NotImplementedException e) {
//            NotImplementedExceptionAssume.fail(e);
//        }
//    }


}
