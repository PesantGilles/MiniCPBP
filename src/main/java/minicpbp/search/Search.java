package minicpbp.search;

import java.util.function.Predicate;

import minicpbp.util.Procedure;
import minicpbp.engine.core.IntVar;

public abstract class Search {

	public abstract void onSolution(Procedure listener);

	public abstract SearchStatistics solve(Predicate<SearchStatistics> limit);

	public abstract SearchStatistics optimize(Objective obj, Predicate<SearchStatistics> limit);

	public abstract SearchStatistics solveRestarts(Predicate<SearchStatistics> limit, int nbFailCutof, double restartFactor);

	public abstract void initializeImpact(IntVar... x);

	public abstract void initializeImpactDomains(IntVar... x);
}
