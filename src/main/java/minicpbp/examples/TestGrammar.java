package minicpbp.examples;

import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.DFSearch;
import minicpbp.util.CFG;
import minicpbp.util.io.InputReader;

import static minicpbp.cp.BranchingScheme.firstFail;
import static minicpbp.cp.BranchingScheme.maxMarginal;
import static minicpbp.cp.Factory.*;

public class TestGrammar {
    public static void main(String[] args) {

        InputReader reader = new InputReader("data/simpleCFG.txt");
        CFG g = new CFG(reader);
        int wordLength = 6;

        Solver cp = makeSolver();
        IntVar[] w = makeIntVarArray(cp, wordLength, 0, g.terminalCount()-1);
        for (int i = 0; i < wordLength; i++)
            w[i].setName("w["+i+"]");

        cp.post(grammar(w,g));
  //      cp.post(lessOrEqual(w[1],w[4])); // some other arbitrary constraint

        cp.fixPoint();
        cp.setTraceBPFlag(true);
        cp.vanillaBP(3);

        /*
        DFSearch dfs = makeDfs(cp, maxMarginal(w));
        cp.setTraceSearchFlag(true);
        dfs.onSolution(() -> {
            for (int i = 0; i < wordLength; i++) {
                System.out.print(w[i].min());
            }
            System.out.println();
        });
        dfs.solve();
         */
    }
}
