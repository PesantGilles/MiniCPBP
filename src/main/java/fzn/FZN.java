package fzn;

import minicpbp.engine.core.Solver;
import model.ModelFormatFrontend;
import model.ModelGoal;
import minicpbp.engine.core.IntVar;
import minicpbp.search.SearchStatistics;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import fzn.parser.FZParser;
import fzn.parser.intermediatemodel.*;
import minicpbp.util.exception.NotImplementedException;

import minicpbp.util.Log;

public class FZN extends ModelFormatFrontend {
	//Model containing all constraints, parameters, variables and functions of the problem
	private final Model m;

    public FZN(Solver minicp, String filename) throws Exception {
		super(minicp);
        hasFailed = false;
		//read the Flatzinc File
		this.m = FZParser.readFlatZincModelFromFile(filename, false);
    }

	public static boolean printStats = false;
	public void printStats(boolean printStats) {
		FZN.printStats = printStats;
	}


	public void initModel() {
		m.addSolver(minicp);

		//build the model from the Flatzinc file
		m.buildModel();
	}

	public String getSolutionStr(boolean extractSolutionStr) {
		String solutionStr = m.getSolutionOutput();
		Log.infoPrint(solutionStr);
		return solutionStr;
	}

	public IntVar[] getDecisionVars() {
		return m.getDecisionsVar();
	}

	public ModelGoal getGoal() {
		switch (m.getGoal()) {
			case ASTSolve.MAX:
				return ModelGoal.MAX;
			case ASTSolve.MIN:
				return ModelGoal.MIN;
			default:
				return ModelGoal.SAT;
		}
	}

	public IntVar getObjectiveVar() {
		return m.getObjective();
	}

	public void onSolutionFound(SearchStatistics stats, String solutionStr) {
		if(stats.isCompleted()) {
			Log.info("==========");
		}
	}

	public void onNoSolutionFound(SearchStatistics stats) {
		Log.info("=====UNSATISFIABLE=====");
	}

	public void onInconclusiveSearch(SearchStatistics stats) {
		Log.info("=====UNKNOWN=====");
	}

	public void verifySolution(String solutionStr) {
		// TODO: implement this
		throw new NotImplementedException("solution verification not implemented");
	}

	/**
	 * Prints statistic about the search
	 * @param stats statistics about the search
	 * @param statsFileStr a path to save the stats 
	 * @param runtime
	 */
	public void printStats(SearchStatistics stats, String statsFileStr, Long runtime) {
		if(!printStats) {
			return;
		}

		//out.println("status: " + statusStr);
		Log.info("%%%mzn-stat: failures=" + stats.numberOfFailures());
		if(m.getGoal() != ASTSolve.SAT)
			Log.info("%%%mzn-stat: objective=" + m.getObjective().min());
		Log.info("%%%mzn-stat: nodes=" + stats.numberOfNodes());
		Log.info("%%%mzn-stat: solveTime=" + runtime);
		Log.info("%%%mzn-stat-end");

		if(!Objects.equals(statsFileStr, "")) {
			PrintStream out = null;
			
			try {
				out = new PrintStream(statsFileStr);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.info("unable to create file " + statsFileStr);
				System.exit(1);
			}

			String statusStr;
			if (stats.numberOfSolutions() > 0)
				statusStr = "SAT";
			else if (stats.isCompleted())
				statusStr = "UNSAT";
			else
				statusStr = "TIMEOUT";

			out.println("status: " + statusStr);
			out.println("failures: " + stats.numberOfFailures());
			out.println("nodes: " + stats.numberOfNodes());
			out.println("runtime (ms): " + runtime);

			out.close();
		}

	}

}
