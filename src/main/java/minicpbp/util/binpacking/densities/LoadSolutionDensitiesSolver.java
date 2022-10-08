package  minicpbp.util.binpacking.densities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minicpbp.util.binpacking.network.Layer;
import minicpbp.util.binpacking.network.Vertex;
import minicpbp.util.binpacking.packing.LayerProfile;

public class LoadSolutionDensitiesSolver {
    // List of solution densities represented as a map (load -> density) for each load variable
    private List<Map<Integer, Float>> solutionDensities;

    // Number of different solutions
    private int solutionCount;

    public LoadSolutionDensitiesSolver(Map<LayerProfile, Long> flowProfiles, Layer lastLayer, List<Set<Integer>> loadVariableDomains) {
        solutionDensities = new ArrayList<>();
        for (int i = 0; i < loadVariableDomains.size(); i++) {
            solutionDensities.add(new HashMap<>());
        }

        solutionCount = 0;

        // Sum solution counts with a given load variable assignment for each layer profile
        for (Map.Entry<LayerProfile, Long> entry : flowProfiles.entrySet()) {
            LayerProfile flowProfile = entry.getKey();
            Long multiplicity = entry.getValue();

            // Convert flow profile to a list of grouping loads
            List<Integer> groupingLoads = listGroupingLoads(flowProfile, lastLayer);

            // Enumerate all different load variable assignments (packings) for this flow profile
            List<List<Integer>> loadVariableAssignments = generateLoadVariableAssignments(groupingLoads, loadVariableDomains);

            // Sum solution counts for load variable assignments, incrementing
            // each solution count by the flow profile's multiplicity
            for (List<Integer> loadVariableAssignment : loadVariableAssignments) {
                for (int i = 0; i < loadVariableAssignment.size(); i++) {
                    Map<Integer, Float> loadVariableSolutionDensities = solutionDensities.get(i);
                    Integer groupingLoad = loadVariableAssignment.get(i);
                    loadVariableSolutionDensities.merge(groupingLoad, (float) multiplicity, Float::sum);
                }
            }

            // Update the total solution count by adding the number of load
            // variable assignments multiplied by the flow profile's multiplicity
            solutionCount += loadVariableAssignments.size() * multiplicity;
        }

        // Divide solution counts by total number of solutions to obtain solution densities
        for (Map<Integer, Float> loadVariableSolutionDensities : solutionDensities) {
            for (Map.Entry<Integer, Float> entry : loadVariableSolutionDensities.entrySet()) {
                entry.setValue(entry.getValue() / solutionCount);
            }
        }
    }

    public List<Map<Integer, Float>> getSolutionDensities() {
        return solutionDensities;
    }

    public int getSolutionCount() {
        return solutionCount;
    }

    private List<Integer> listGroupingLoads(LayerProfile layerProfile, Layer layer) {
        List<Integer> groupingLoads = new ArrayList<>();

        // Generate a list with the loads of the groupings defined by a layer profile
        int heightIndex = 0;
        for (Vertex vertex : layer.getVertices()) {
            int vertexFlow = layerProfile.getVertexFlow(heightIndex);

            // Add multiple identical loads for each unit of flow in a vertex
            // (represents multiple groupings with the same load)
            for (int i = 0; i < vertexFlow; i++) {
                // The height of a vertex corresponds to a grouping's load
                groupingLoads.add(vertex.getHeight());
            }

            heightIndex++;
        }

        return groupingLoads;
    }

    private List<List<Integer>> generateLoadVariableAssignments(List<Integer> groupingLoads, List<Set<Integer>> loadVariableDomains) {
        // Generate a list of load variable assignments for a list of grouping loads. Each load variable
        // assignment is represented by a list, with each i'th element's value giving the assigned load for
        // the i'th load variable
        List<List<Integer>> loadVariableAssignments = new ArrayList<>();

        // Generate load variable assignments recursively using backtracking
        generateLoadVariableAssignments(loadVariableAssignments, new ArrayList<Integer>(), groupingLoads, loadVariableDomains);

        return loadVariableAssignments;
    }

    private void generateLoadVariableAssignments(List<List<Integer>> loadVariableAssignments,
                                                                List<Integer> partialAssignment,
                                                                List<Integer> groupingLoads,
                                                                List<Set<Integer>> loadVariableDomains) {
        // Add to final list of load variable assignments if partial assignment is complete
        if (partialAssignment.size() == loadVariableDomains.size()) {
            loadVariableAssignments.add(partialAssignment);
            return;
        }

        // Determine the current load variable to assign to a suitable load
        int currentLoadVariable = partialAssignment.size();
        Set<Integer> currentLoadVariableDomain = loadVariableDomains.get(currentLoadVariable);

        // Extend the partial assignment by assigning the current load variable to suitable grouping loads
        for (Integer groupingLoad : groupingLoads) {
            if (currentLoadVariableDomain.contains(groupingLoad)) {
                // Append the selected grouping load to the partial assignment, assigning it to the
                // current load variable (represented by the index of the newly-added element)
                List<Integer> extendedPartialAssignment = new ArrayList<>(partialAssignment);
                extendedPartialAssignment.add(groupingLoad);

                // Remove the assigned grouping load from the remaining available loads to assign
                List<Integer> remainingGroupingLoads = new ArrayList<>(groupingLoads);
                remainingGroupingLoads.remove(groupingLoad);

                // Recursively extend partial load variable assignments
                generateLoadVariableAssignments(loadVariableAssignments,
                                                extendedPartialAssignment,
                                                remainingGroupingLoads,
                                                loadVariableDomains);
            }
        }
    }
}
