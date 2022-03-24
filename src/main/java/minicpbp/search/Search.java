package minicpbp.search;

import java.util.function.Predicate;

import minicpbp.util.Procedure;

public abstract class Search {

	public abstract void onSolution(Procedure listener);

	public abstract SearchStatistics solve(Predicate<SearchStatistics> limit);

	public abstract SearchStatistics optimize(Objective obj, Predicate<SearchStatistics> limit);

	public abstract SearchStatistics solveRestarts(Predicate<SearchStatistics> limit);

}
