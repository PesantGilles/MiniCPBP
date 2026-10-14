package model;

import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.search.SearchStatistics;
import minicpbp.util.exception.InconsistencyException;

import java.util.List;

public abstract class ModelFormatFrontend {
    protected final Solver minicp;
    protected boolean hasFailed;

    abstract public void initModel();

    abstract public IntVar[] getDecisionVars();

    abstract public ModelGoal getGoal();

    abstract public IntVar getObjectiveVar();

    abstract public String getSolutionStr(boolean extractSolutionStr);

    abstract public void onSolutionFound(SearchStatistics stats, String solutionStr);

    abstract public void onNoSolutionFound(SearchStatistics stats);

    abstract public void onInconclusiveSearch(SearchStatistics stats);

    abstract public void verifySolution(String solutionStr);

    abstract public void printStats(SearchStatistics stats, String statsFileStr, Long runtime);

    public void onAllSolutionsFound(SearchStatistics ignoredStats, List<String> solutionStrs) {
        System.out.println("all solutions found, found " + solutionStrs.size() + " solutions");
    }

    public void onPreInitFail() {
        System.out.println("problem failed before initiating the search");
        throw InconsistencyException.INCONSISTENCY;
    }

    public boolean hasFailed() {
        return hasFailed;
    }

    protected ModelFormatFrontend(Solver minicp) {
        this.minicp = minicp;
    }
}

