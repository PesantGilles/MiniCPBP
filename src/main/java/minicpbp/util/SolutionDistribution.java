package minicpbp.util;

import java.util.Map;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import minicpbp.engine.core.IntVar;

public class SolutionDistribution {
    // Variable-value marginals, they are sorted to pretty print them easier.
    // Impossible variable-value assignations are not stored.
    SortedMap<String, SortedMap<Integer, Integer>> marginals = new TreeMap<>();
    int nSolutions = 0;

    public void addSolution(IntVar[] vars) {
        nSolutions += 1;
        for (IntVar var : vars) {
            String name = var.getName();
            if (name != null) {
                SortedMap<Integer, Integer> marginalDistribution = marginals.computeIfAbsent(name, l -> new TreeMap<>());
                marginalDistribution.merge(var.min(), 1, Integer::sum);
            }
        }
    }

    @Override
    public String toString() {
        StringJoiner variableJoiner = new StringJoiner("\n");
        for (Map.Entry<String, SortedMap<Integer, Integer>> varEntry : marginals.entrySet()) {
            StringJoiner valueJoiner = new StringJoiner(", ", varEntry.getKey() + "{", "}");
            for (Map.Entry<Integer, Integer> valueEntry : varEntry.getValue().entrySet()) {
                valueJoiner.add(valueEntry.getKey() + "  <" + valueEntry.getValue() + "/" + nSolutions + ">");
            }
            variableJoiner.add(valueJoiner.toString());
        }
        return variableJoiner.toString();
    }
}
