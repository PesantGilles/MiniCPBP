package  minicpbp.util.binpacking.packing;

import java.util.LinkedHashMap;
import java.util.Map;

import minicpbp.util.binpacking.network.Layer;
import minicpbp.util.binpacking.network.Network;
import minicpbp.util.binpacking.network.Vertex;

public class PackingCounter {
    private final Map<LayerProfile, Long> finalLayerProfileMultiset; // Map of layer profiles to multiplicities for the final layer

    public PackingCounter(Network network, int binCount) {
        // Source layer
        LayerProfile sourceLayerProfile = new LayerProfile(network.getLayers().get(0));
        sourceLayerProfile.setVertexFlow(0, binCount);
        Map<LayerProfile, Long> sourceLayerProfileMultiset = new LinkedHashMap<>() {{
            put(sourceLayerProfile, 1L);
        }};

        // Item layers
        Map<LayerProfile, Long> previousLayerProfileMultiset = sourceLayerProfileMultiset;
        for (int i = 1; i < network.getLayers().size(); i++) {
            Layer previousLayer = network.getLayers().get(i - 1);
            Layer nextLayer = network.getLayers().get(i);

            // Generate next layer profiles from previous layer
            Map<LayerProfile, Long> nextLayerProfileMultiset = generateNextLayerProfileMultiset(previousLayerProfileMultiset,
                                                                                                previousLayer,
                                                                                                nextLayer);

            System.out.println("Layer " + i + "/" + (network.getLayers().size() - 1) + " complete: " +
                               nextLayerProfileMultiset.size() + " layer profiles");

            previousLayerProfileMultiset = nextLayerProfileMultiset;
        }

        // Save final layer profile multiset (flow profiles)
        finalLayerProfileMultiset = previousLayerProfileMultiset;
    }

    public Map<LayerProfile, Long> getFlowProfiles() {
        return finalLayerProfileMultiset;
    }

    public long getPackingCount() {
        return finalLayerProfileMultiset.values().stream().mapToLong(Long::longValue).sum();
    }

    private Map<LayerProfile, Long> generateNextLayerProfileMultiset(Map<LayerProfile, Long> previousLayerProfileMultiset,
                                                                     Layer previousLayer,
                                                                     Layer nextLayer) {
        Map<LayerProfile, Long> nextLayerProfileMultiset = new LinkedHashMap<>();

        // Iterate through all layer profiles from previous layer to generate new layer profiles for next layer
        for (Map.Entry<LayerProfile, Long> layerProfileEntry : previousLayerProfileMultiset.entrySet()) {
            LayerProfile previousLayerProfile = layerProfileEntry.getKey();
            long previousMultiplicity = layerProfileEntry.getValue();

            // Carry previous layer profile flows to new layer profile through horizontal arcs as starting point
            LayerProfile startingLayerProfile = createStartingLayerProfile(previousLayerProfile, previousLayer, nextLayer);

            // Try all valid modifications to previous layer profile to generate
            // new layer profiles by iterating through vertices in ascending height
            for (Vertex previousVertex : previousLayer.getVertices()) {
                // Check if the vertex carries a flow
                int previousVertexFlow = previousLayerProfile.getVertexFlow(previousVertex.getHeightIndex());
                if (previousVertexFlow == 0) {
                    continue;
                }

                // Check if the item can be added without exceeding the capacity (if the vertex has an oblique arc neighbor)
                Vertex previousVertexObliqueNeighbor = previousVertex.getObliqueNeighbor();
                if (previousVertexObliqueNeighbor == null) {
                    // Break early since vertices are iterated in ascending height
                    break;
                }

                // Copy starting layer profile to modify its flows
                LayerProfile layerProfile = new LayerProfile(startingLayerProfile);

                // Generate new layer profile by carrying one unit of flow through the oblique arc
                int horizontalNeighborHeightIndex = previousVertex.getHorizontalNeighbor().getHeightIndex();
                layerProfile.setVertexFlow(horizontalNeighborHeightIndex,
                                           layerProfile.getVertexFlow(horizontalNeighborHeightIndex) - 1);
                int obliqueNeighborHeightIndex = previousVertexObliqueNeighbor.getHeightIndex();
                layerProfile.setVertexFlow(obliqueNeighborHeightIndex,
                                           layerProfile.getVertexFlow(obliqueNeighborHeightIndex) + 1);

                // Add layer profile to multiset, conserving multiplicity and applying multiplying vertices' effect
                boolean isPreviousVertexMultiplying = previousVertex.getHeight() > 0;
                long multiplicity = isPreviousVertexMultiplying ? previousMultiplicity * previousVertexFlow : previousMultiplicity;
                nextLayerProfileMultiset.merge(layerProfile, multiplicity, Long::sum);
            }
        }

        return nextLayerProfileMultiset;
    }

    private LayerProfile createStartingLayerProfile(LayerProfile previousLayerProfile, Layer previousLayer, Layer nextLayer) {
        LayerProfile startingLayerProfile = new LayerProfile(nextLayer);

        int previousLayerHeightIndex = 0;
        for (Vertex previousLayerVertex : previousLayer.getVertices()) {
            int nextLayerHeightIndex = previousLayerVertex.getHorizontalNeighbor().getHeightIndex();
            startingLayerProfile.setVertexFlow(nextLayerHeightIndex, previousLayerProfile.getVertexFlow(previousLayerHeightIndex));
            previousLayerHeightIndex++;
        }

        return startingLayerProfile;
    }
}
