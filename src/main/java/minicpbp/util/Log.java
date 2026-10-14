/*
 * mini-cpbp, replacing classic propagation by belief propagation
 * Copyright (c)  2019. by Gilles Pesant
 */

package minicpbp.util;

import minicpbp.engine.core.IntVar;
import minicpbp.state.StateStack;

import java.io.PrintStream;

/**
 * Centralized logging utility for MiniCPBP.
 * Replaces scattered System.out.println calls with category-based logging
 * that can be individually enabled/disabled via flags (same flags as before:
 * traceBP, traceSearch, traceEntropy, traceOptimization).
 *
 * Usage: Log.info("message"), Log.search("message"), Log.bpIteration(...), etc.
 */
public final class Log {

    private static boolean traceBP = false;
    private static boolean traceSearch = false;
    private static boolean traceEntropy = false;
    private static boolean traceOptimization = true;
    private static boolean traceGradient = false;
    private static boolean traceConstraint = false;

    private static PrintStream out = System.out;
    private static PrintStream err = System.err;

    private Log() {} // utility class, no instantiation

    // ===== Flag setters =====

    public static void setTraceBP(boolean flag) {
        traceBP = flag;
    }

    public static void setTraceSearch(boolean flag) {
        traceSearch = flag;
    }

    public static void setTraceEntropy(boolean flag) {
        traceEntropy = flag;
    }

    public static void setTraceOptimization(boolean flag) {
        traceOptimization = flag;
    }

    public static void setTraceGradient(boolean flag) {
        traceGradient = flag;
    }

    public static void setTraceConstraint(boolean flag) {
        traceConstraint = flag;
    }

    // ===== Flag getters =====

    public static boolean isTracingBP() {
        return traceBP;
    }

    public static boolean isTracingSearch() {
        return traceSearch;
    }

    public static boolean isTracingEntropy() {
        return traceEntropy;
    }

    public static boolean isTracingOptimization() {
        return traceOptimization;
    }

    public static boolean isTracingGradient() {
        return traceGradient;
    }

    public static boolean isTracingConstraint() {
        return traceConstraint;
    }

    // ===== Output stream configuration =====

    public static void setOutput(PrintStream output) {
        out = output;
    }

    public static void setErrorOutput(PrintStream errorOutput) {
        err = errorOutput;
    }

    // ===== BP semantic logging methods =====

    /**
     * Logs the state of all variables after a BP iteration.
     * Prints iteration header, variable names with their domains.
     */
    public static void bpIteration(int iter, StateStack<IntVar> variables) {
        if (!traceBP) return;
        out.println("##### after BP iteration " + iter + " #####");
        for (int i = 0; i < variables.size(); i++) {
            out.println(variables.get(i).getName() + variables.get(i).toString());
        }
    }

    /**
     * Logs problem and smallest variable entropy after a BP iteration.
     */
    public static void bpEntropy(double problemEntropy, double smallestVarEntropy) {
        if (!traceBP) return;
        out.println("problem entropy = " + problemEntropy);
        out.println("smallest variable entropy = " + smallestVarEntropy);
    }

    /**
     * Logs the final damping factor chosen by BP tuning.
     */
    public static void bpDampingFactor(double factor) {
        if (!traceBP) return;
        out.println("FINAL DAMPING FACTOR = " + factor);
    }

    // ===== Entropy semantic logging =====

    /**
     * Logs model entropy statistics computed from variables.
     * Iterates over variables, computes normalized min/max/model entropy, and prints.
     */
    public static void modelEntropy(StateStack<IntVar> variables, int nbBranchingVars) {
        if (!traceEntropy) return;
        double minEntropy = 1;
        double maxEntropy = 0;
        double modelEntropy = 0.0;
        for (int i = 0; i < variables.size(); i++) {
            IntVar v = variables.get(i);
            if (!v.isBound() && v.isForBranching()) {
                double normalized = v.entropy() / Math.log(v.size());
                if (minEntropy > normalized)
                    minEntropy = normalized;
                if (maxEntropy < normalized)
                    maxEntropy = normalized;
                modelEntropy += normalized;
            }
        }
        modelEntropy = modelEntropy / nbBranchingVars;
        out.println("model entropy : " + modelEntropy);
        out.println("min entropy : " + minEntropy);
        out.println("max entropy : " + maxEntropy);
    }

    // ===== Gradient semantic logging =====

    /**
     * Logs the gradients section header.
     */
    public static void gradientHeader() {
        if (!traceGradient) return;
        out.println("*** GRADIENTS ***");
    }

    /**
     * Logs a constraint name in the gradients section.
     */
    public static void gradientConstraint(String name) {
        if (!traceGradient) return;
        out.println("** Constraint " + name);
    }

    // ===== Search/Branching semantic logging =====

    /**
     * Logs a branching decision (assign variable = value), with optional extra info.
     * @param varName the variable name
     * @param value the value being assigned
     * @param extras optional key=value pairs (e.g. "marginal=0.8", "entropy=1.2")
     */
    public static void branchEqual(String varName, int value, String... extras) {
        if (!traceSearch) return;
        StringBuilder sb = new StringBuilder("### branching on ");
        sb.append(varName).append("=").append(value);
        for (String extra : extras) {
            sb.append("; ").append(extra);
        }
        out.println(sb.toString());
    }

    /**
     * Logs a branching decision (exclude variable != value), with optional extra info.
     * @param varName the variable name
     * @param value the value being removed
     * @param extras optional key=value pairs
     */
    public static void branchNotEqual(String varName, int value, String... extras) {
        if (!traceSearch) return;
        StringBuilder sb = new StringBuilder("### branching on ");
        sb.append(varName).append("!=").append(value);
        for (String extra : extras) {
            sb.append("; ").append(extra);
        }
        out.println(sb.toString());
    }

    // ===== Other category-based logging =====

    /**
     * Optimization trace output (controlled by traceOptimization flag).
     */
    public static void optim(String msg) {
        if (traceOptimization) out.println(msg);
    }

    /**
     * Gradient trace output (controlled by traceGradient flag).
     */
    public static void gradient(String msg) {
        if (traceGradient) out.println(msg);
    }

    /**
     * Constraint debug output (controlled by traceConstraint flag).
     */
    public static void constraint(String msg) {
        if (traceConstraint) out.println(msg);
    }

    /**
     * Constraint debug print without newline (controlled by traceConstraint flag).
     */
    public static void constraintPrint(String msg) {
        if (traceConstraint) out.print(msg);
    }

    // ===== Always-on output methods =====

    /**
     * Informational output that is always printed (solver protocol output, solution strings, etc.).
     */
    public static void info(String msg) {
        out.println(msg);
    }

    /**
     * Informational print without newline (always printed).
     */
    public static void infoPrint(String msg) {
        out.print(msg);
    }

    /**
     * Warning output (always printed, prefixed with "c Warning: ").
     */
    public static void warn(String msg) {
        out.println("c Warning: " + msg);
    }

    /**
     * Error output (always printed to stderr).
     */
    public static void error(String msg) {
        err.println(msg);
    }
}
