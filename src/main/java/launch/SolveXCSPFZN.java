package launch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import xcsp.XCSP;
import fzn.FZN;

public class SolveXCSPFZN {
    public enum BranchingHeuristic {
		FFRV, // first-fail, random value
		MXMS, // maximum marginal strength
		MNMS, // minimum marginal strength
		MXM, // maximum marginal
		MNM, // minimum marginal
		MNE, //minimum entropy
		IE, //impact entropy
		MIE, //min-entropy followed by impact entropy after first restart
	}

	private static Map<String, BranchingHeuristic> branchingMap = new HashMap<String, BranchingHeuristic>() {
		private static final long serialVersionUID = 4936849715939593675L;
		{
			put("first-fail-random-value", BranchingHeuristic.FFRV);
			put("max-marginal-strength", BranchingHeuristic.MXMS);
			put("min-marginal-strength", BranchingHeuristic.MNMS);
			put("max-marginal", BranchingHeuristic.MXM);
			put("min-marginal", BranchingHeuristic.MNM);
			put("min-entropy", BranchingHeuristic.MNE);
			put("impact-entropy", BranchingHeuristic.IE);
			put("impact-min-entropy", BranchingHeuristic.MIE);
		}
	};

	public enum TreeSearchType {
		DFS, LDS, DFSR
	}

	private static Map<String, TreeSearchType> searchTypeMap = new HashMap<String, TreeSearchType>() {
		private static final long serialVersionUID = 8428231233538651558L;

		{
			put("dfs", TreeSearchType.DFS);
			put("lds", TreeSearchType.LDS);
		}
	};

	public static void main(String[] args) {

		String quotedValidBranchings = branchingMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
				.collect(Collectors.joining(",\n"));

		String quotedValidSearchTypes = searchTypeMap.keySet().stream().sorted().map(x -> "\"" + x + "\"")
				.collect(Collectors.joining(",\n"));
		
		Option xcspFileOpt = Option.builder().longOpt("input").argName("FILE").required().hasArg()
				.desc("input FZN or XCSP file").build();

		Option branchingOpt = Option.builder().longOpt("branching").argName("STRATEGY").required().hasArg()
				.desc("branching strategy.\nValid branching strategies are:\n" + quotedValidBranchings).build();

		Option searchOpt = Option.builder().longOpt("search-type").argName("SEARCH").required().hasArg()
				.desc("search type.\nValid search types are:\n" + quotedValidSearchTypes).build();
		
		Option timeoutOpt = Option.builder().longOpt("timeout").argName("SECONDS").required().hasArg()
				.desc("timeout in seconds").build();

		Option statsFileOpt = Option.builder().longOpt("stats").argName("FILE").hasArg()
				.desc("file for storing the statistics").build();

		Option solFileOpt = Option.builder().longOpt("solution").argName("FILE").hasArg()
				.desc("file for storing the solution").build();

		Option maxIterOpt = Option.builder().longOpt("max-iter").argName("ITERATIONS").hasArg()
				.desc("maximum number of belief propagation iterations").build();

		Option dFactorOpt = Option.builder().longOpt("damping-factor").argName("LAMBDA").hasArg()
				.desc("the damping factor used for damping the messages").build();

		Option checkOpt = Option.builder().longOpt("verify").hasArg(false)
				.desc("check the correctness of obtained solution").build();

		Option dampOpt = Option.builder().longOpt("damp-messages").hasArg(false).desc("damp messages").build();

		Option traceBPOpt = Option.builder().longOpt("trace-bp").hasArg(false)
				.desc("trace the belief propagation progress").build();

		Option traceSearchOpt = Option.builder().longOpt("trace-search").hasArg(false).desc("trace the search progress")
				.build();
		Option restartSearchOpt = Option.builder().longOpt("restart").hasArg(false).desc("authorized restart during search (available with dfs only)")
				.build();
		Option nbFailsCutofOpt = Option.builder().longOpt("cutoff").argName("CUTOF").hasArg()
				.desc("number of failure before restart").build();

		Option restartFactorOpt = Option.builder().longOpt("restart-factor").argName("restartFactor").hasArg()
				.desc("factor to increase number of failure before restart").build();		

		Option variationThresholdOpt = Option.builder().longOpt("var-threshold").argName("variationThreshold").hasArg()
				.desc("threshold on entropy's variation under to stop belief propagation").build();	

		Option initImpactOpt = Option.builder().longOpt("init-impact").hasArg(false).desc("initialize impact before search")
				.build();

		Option dynamicStopBPOpt = Option.builder().longOpt("dynamic-stop").hasArg(false).desc("BP iterations are stopped dynamically instead of a fixed number of iteration")
				.build();

		Option traceNbIterOpt = Option.builder().longOpt("trace-iter").hasArg(false).desc("trace the number of BP iterations before each branching")
				.build();

		Options options = new Options();
		options.addOption(xcspFileOpt);
		options.addOption(branchingOpt);
		options.addOption(searchOpt);
		options.addOption(timeoutOpt);
		options.addOption(statsFileOpt);
		options.addOption(solFileOpt);
		options.addOption(maxIterOpt);
		options.addOption(checkOpt);
		options.addOption(traceBPOpt);
		options.addOption(traceSearchOpt);
		options.addOption(dampOpt);
		options.addOption(dFactorOpt);
		options.addOption(restartSearchOpt);
		options.addOption(nbFailsCutofOpt);
		options.addOption(restartFactorOpt);
		options.addOption(variationThresholdOpt);
		options.addOption(initImpactOpt);
		options.addOption(dynamicStopBPOpt);
		options.addOption(traceNbIterOpt);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println(exp.getMessage());
			new HelpFormatter().printHelp("solve-XCSP", options);
			System.exit(1);
		}

		String branchingStr = cmd.getOptionValue("branching");
		checkBranchingOption(branchingStr);
		BranchingHeuristic heuristic = branchingMap.get(branchingStr);

		String searchTypeStr = cmd.getOptionValue("search-type");
		checkSearchTypeOption(searchTypeStr);
		TreeSearchType searchType = searchTypeMap.get(searchTypeStr);


		String inputStr = cmd.getOptionValue("input");
		checkInputOption(inputStr);

		String timeoutStr = cmd.getOptionValue("timeout");
		int timeout = checkTimeoutOption(timeoutStr);

		String statsFileStr = "";
		if (cmd.hasOption("stats")) {
			statsFileStr = cmd.getOptionValue("stats");
			checkCreateFile(statsFileStr);
		}

		String solFileStr = "";
		if (cmd.hasOption("solution")) {
			solFileStr = cmd.getOptionValue("solution");
			checkCreateFile(solFileStr);
		}

		int maxIter = 5;
		if (cmd.hasOption("max-iter"))
			maxIter = Integer.parseInt(cmd.getOptionValue("max-iter"));

		double dampingFactor = 0.5;
		if (cmd.hasOption("damping-factor"))
			dampingFactor = Double.parseDouble(cmd.getOptionValue("damping-factor"));

		int nbFailCutof = 100;
		if(cmd.hasOption("cutoff"))
			nbFailCutof = Integer.parseInt(cmd.getOptionValue("cutoff"));

		double restartFactor = 1.5;
		if(cmd.hasOption("restart-factor"))
			restartFactor = Double.parseDouble(cmd.getOptionValue("restart-factor"));

		double variationThreshold = -Double.MAX_VALUE;
		if(cmd.hasOption("var-threshold"))
			variationThreshold = Double.parseDouble(cmd.getOptionValue("var-threshold"));

		boolean checkSolution = (cmd.hasOption("verify"));
		boolean traceBP = (cmd.hasOption("trace-bp"));
		boolean traceSearch = (cmd.hasOption("trace-search"));
		boolean damp = (cmd.hasOption("damp-messages"));
		boolean restart = (cmd.hasOption("restart"));
		boolean initImpact = (cmd.hasOption("init-impact"));
		boolean dynamicStopBP = (cmd.hasOption("dynamic-stop"));
		boolean traceNbIter = (cmd.hasOption("trace-iter"));

		try {
			System.out.println(inputStr.substring(inputStr.lastIndexOf('.')+1));
			if(inputStr.substring(inputStr.lastIndexOf('.')+1).equals("fzn")) {
				FZN fzn = new FZN(inputStr);
				fzn.searchType(searchType);
				fzn.checkSolution(checkSolution);
				fzn.traceBP(traceBP);
				fzn.traceSearch(traceSearch);
				fzn.maxIter(maxIter);
				fzn.damp(damp);
				fzn.dampingFactor(dampingFactor);
				fzn.restart(restart);
				fzn.nbFailCutof(nbFailCutof);
				fzn.restartFactor(restartFactor);
				fzn.variationThreshold(variationThreshold);
				fzn.initImpact(initImpact);
				fzn.dynamicStopBP(dynamicStopBP);
				fzn.traceNbIter(traceNbIter);
				fzn.solve(heuristic, timeout, statsFileStr, solFileStr);
			}
			else {
				System.out.println("XCSP");
				XCSP xcsp = new XCSP(inputStr);
				xcsp.searchType(searchType);
				xcsp.checkSolution(checkSolution);
				xcsp.traceBP(traceBP);
				xcsp.traceSearch(traceSearch);
				xcsp.maxIter(maxIter);
				xcsp.damp(damp);
				xcsp.dampingFactor(dampingFactor);
				xcsp.restart(restart);
				xcsp.nbFailCutof(nbFailCutof);
				xcsp.restartFactor(restartFactor);
				xcsp.variationThreshold(variationThreshold);
				xcsp.initImpact(initImpact);
				xcsp.dynamicStopBP(dynamicStopBP);
				xcsp.traceNbIter(traceNbIter);
				xcsp.solve(heuristic, timeout, statsFileStr, solFileStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void checkBranchingOption(String branchingStr) {

		if (!branchingMap.containsKey(branchingStr)) {
			System.out.println("invalid branching strategy " + branchingStr);
			System.out.println("Branching strategy should be one of the following: ");
			for (String branching : branchingMap.keySet())
				System.out.println(branching);
			System.exit(1);
		}
	}

	private static void checkSearchTypeOption(String searchTypeStr) {

		if (!searchTypeMap.containsKey(searchTypeStr)) {
			System.out.println("invalid search type " + searchTypeStr);
			System.out.println("Search type should be one of the following: ");
			for (String branching : searchTypeMap.keySet())
				System.out.println(branching);
			System.exit(1);
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

