package launch;

import java.io.File;
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
			FZN fzn = new FZN(args[0]);
			fzn.searchType(TreeSearchType.LDS);
			fzn.checkSolution(false);
			fzn.traceBP(false);
			fzn.traceSearch(false);
			fzn.maxIter(15);
			fzn.damp(true);
			fzn.dampingFactor(0.5);
			fzn.restart(false);
			fzn.initImpact(false);
			fzn.dynamicStopBP(true);
			fzn.traceNbIter(false);
			fzn.variationThreshold(0.01);
            //fzn.competitionOutput(true);
			fzn.solve(BranchingHeuristic.MNE, 1200, "", "");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private static void checkInputOption(String inputStr) {
		File inputFile = new File(inputStr);
		if (!inputFile.exists()) {
			System.out.println("input file " + inputStr + " does not exist!");
			System.exit(1);
		}
	}

	private static int checkTimeoutOption(String timeoutStr) {
		Integer timeout = null;
		try {
			timeout = Integer.valueOf(timeoutStr);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("invalid timeout string " + timeoutStr);
			System.exit(1);
		}

		if (timeout < 0 || timeout > Integer.MAX_VALUE) {
			System.out.println("invalid timeout " + timeout);
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
				System.out.println("can not create file " + filename);
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("can not create file " + filename);
			System.exit(1);
		}
	}

}

