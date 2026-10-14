package launch;

import java.io.File;
import minicpbp.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import launch.SolveXCSPFZN.TreeSearchType;
import launch.SolveXCSPFZN.BranchingHeuristic;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fzn.FZN;

public class MznCompetition {

	public static void main(String[] args) {

		try {
			SolveXCSPFZN app = new SolveXCSPFZN();
			app.setHeuristic(BranchingHeuristic.MNE);
			app.setTimeout(1200);
			app.setSearchType(TreeSearchType.DFS);
			app.setCheckSolution(false);
			app.setTraceBP(false);
			app.setTraceSearch(false);
			app.setMaxIter(10);
//			app.setDamp(false);
//			app.setDampingFactor(0.75);
			app.setRestart(false);
			app.setInitImpact(false);
//			app.setDynamicStopBP(false);
//			app.setTraceNbIter(true);
//			app.setVariationThreshold(0.001);
			FZN fzn = new FZN(app.getSolver(), args[0]);
			fzn.printStats(false);
			app.solve(fzn);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private static void checkInputOption(String inputStr) {
		File inputFile = new File(inputStr);
		if (!inputFile.exists()) {
			Log.info("input file " + inputStr + " does not exist!");
			System.exit(1);
		}
	}

	private static int checkTimeoutOption(String timeoutStr) {
		Integer timeout = null;
		try {
			timeout = Integer.valueOf(timeoutStr);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Log.info("invalid timeout string " + timeoutStr);
			System.exit(1);
		}

		if (timeout < 0 || timeout > Integer.MAX_VALUE) {
			Log.info("invalid timeout " + timeout);
			System.exit(1);
		}

		return timeout.intValue();
	}

	private static void checkCreateFile(String filename) {
		File f = new File(filename);
		if (f.exists())
			f.delete();
		try {
			if (!f.createNewFile()) {
				Log.info("can not create file " + filename);
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.info("can not create file " + filename);
			System.exit(1);
		}
	}

}

