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

import minicpbp.engine.SolverTest;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.exception.NotImplementedException;
import minicpbp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import static minicpbp.cp.BranchingScheme.firstFail;
import static minicpbp.cp.Factory.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CircuitTest extends SolverTest {


    int[] circuit1ok = new int[]{1, 2, 3, 4, 5, 0};
    int[] circuit2ok = new int[]{1, 2, 3, 4, 5, 0};

    int[] circuit1ko = new int[]{1, 2, 3, 4, 5, 2};
    int[] circuit2ko = new int[]{1, 2, 0, 4, 5, 3};

    public static boolean checkHamiltonian(int[] circuit) {
        int[] count = new int[circuit.length];
        for (int v : circuit) {
            count[v]++;
            if (count[v] > 1) return false;
        }
        boolean[] visited = new boolean[circuit.length];
        int c = circuit[0];
        for (int i = 0; i < circuit.length; i++) {
            visited[c] = true;
            c = circuit[c];
        }
        for (int i = 0; i < circuit.length; i++) {
            if (!visited[i]) return false;
        }
        return true;
    }

    public static IntVar[] instanciate(Solver cp, int[] circuit) {
        IntVar[] x = new IntVar[circuit.length];
        for (int i = 0; i < circuit.length; i++) {
            x[i] = makeIntVar(cp, circuit[i], circuit[i]);
        }
        return x;
    }

    @Test
    public void testCircuitOk() {

        try {
            Solver cp = solverFactory.get();
            cp.post(new Circuit(instanciate(cp, circuit1ok)),true);
            cp.post(new Circuit(instanciate(cp, circuit2ok)),true);
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void testCircuitKo() {
        try {
            try {
                Solver cp = solverFactory.get();
                cp.post(new Circuit(instanciate(cp, circuit1ko)),true);
                fail("should fail");
            } catch (InconsistencyException e) {
            }
            try {
                Solver cp = makeSolver();
                cp.post(new Circuit(instanciate(cp, circuit2ko)),true);
                fail("should fail");
            } catch (InconsistencyException e) {
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void testAllSolutions() {

        try {
            Solver cp = solverFactory.get();
            IntVar[] x = makeIntVarArray(cp, 5, 5);
            cp.post(new Circuit(x),true);


            DFSearch dfs = makeDfs(cp, firstFail(x));

            dfs.onSolution(() -> {
                        int[] sol = new int[x.length];
                        for (int i = 0; i < x.length; i++) {
                            sol[i] = x[i].min();
                        }
                        assertTrue("Solution is not an hamiltonian Circuit", checkHamiltonian(sol));
                    }
            );
            SearchStatistics stats = dfs.solve();
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
