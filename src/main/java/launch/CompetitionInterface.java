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

import xcsp.XCSP;
import fzn.FZN;

public class CompetitionInterface {

	public static void main(String[] args) {

		
		Option xcspFileOpt = Option.builder().longOpt("input").argName("FILE").required().hasArg()
				.desc("input XCSP file").build();
		
		Option timeoutOpt = Option.builder().longOpt("timeout").argName("SECONDS").required().hasArg()
				.desc("timeout in seconds").build();

		Option tmpDirOpt = Option.builder().longOpt("tmpdir").argName("TMPDIR").hasArg()
				.desc("path to the dir for temporary files").build();



		Options options = new Options();
		options.addOption(xcspFileOpt);
		options.addOption(timeoutOpt);
		options.addOption(tmpDirOpt);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException exp) {
			Log.error(exp.getMessage());
			new HelpFormatter().printHelp("solve-XCSP", options);
			System.exit(1);
		}


		String inputStr = cmd.getOptionValue("input");
		checkInputOption(inputStr);

		String timeoutStr = cmd.getOptionValue("timeout");
		int timeout = checkTimeoutOption(timeoutStr);

		String tempDirStr = "";
        String statsFileStr = "";
        String solFileStr = "";
		if (cmd.hasOption("tmpdir")) {
			tempDirStr = cmd.getOptionValue("tmpdir");
			checkCreateFile(tempDirStr);
            //TODO : nom fichier stats et fichier solution
		}

		try {
			SolveXCSPFZN app = new SolveXCSPFZN();
			app.setHeuristic(BranchingHeuristic.MNE);
			app.setTimeout(timeout);
			app.setStatsFileStr(statsFileStr);
			app.setSolFileStr(solFileStr);
//			app.setSearchType(TreeSearchType.LDS);
			app.setCheckSolution(false); //à décider
			app.setTraceBP(false);
			app.setTraceSearch(false);
			app.setMaxIter(10);
//			app.setDamp(true);
//			app.setDampingFactor(0.5);
			app.setRestart(false);
			app.setInitImpact(false);
//			app.setDynamicStopBP(true);
//			app.setTraceNbIter(false);
//			app.setVariationThreshold(0.1);
			XCSP xcsp = new XCSP(app.getSolver(), inputStr);
			xcsp.competitionOutput(true);
			app.solve(xcsp);
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

