package  minicpbp.util.binpacking.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Network {
    private final List<Layer> layers;
    private final int capacity;

    public Network(int[] itemSizes, int capacity) {
        layers = new ArrayList<>(itemSizes.length + 1);
        this.capacity = capacity;

        // Source layer
        Map<Integer, Vertex> sourceLayerVertices = new HashMap<>() {{
            put(0, new Vertex(0, 0));
        }};
        layers.add(new Layer(0, sourceLayerVertices));

        // Item layers
        int layerIndex = 1;
        for (int itemSize : itemSizes) {
            Layer previousLayer = layers.get(layers.size() - 1);

            Map<Integer, Vertex> currentLayerVertices = new HashMap<>();

            for (Vertex previousVertex : previousLayer.getVertices()) {
                int previousHeight = previousVertex.getHeight();

                // Horizontal arc
                Vertex vertex = currentLayerVertices.getOrDefault(previousHeight, new Vertex(layerIndex, previousHeight));
                currentLayerVertices.putIfAbsent(previousHeight, vertex);
                previousVertex.setHorizontalNeighbor(vertex);

                // Oblique arc
                int newHeight = previousHeight + itemSize;
                if (newHeight <= capacity) {
                    vertex = currentLayerVertices.getOrDefault(newHeight, new Vertex(layerIndex, newHeight));
                    currentLayerVertices.putIfAbsent(newHeight, vertex);
                    previousVertex.setObliqueNeighbor(vertex);
                }
            }

            layers.add(new Layer(itemSize, currentLayerVertices));

            layerIndex++;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < layers.size() - 1; i++) {
            sb.append("Layer " + i + " (item size " + layers.get(i).getItemSize() + "):\n");

            for (Vertex vertex : layers.get(i).getVertices()) {
                if (vertex.getHorizontalNeighbor() != null) {
                    sb.append("    " + vertex + " -> " + vertex.getHorizontalNeighbor() + '\n');
                }
                if (vertex.getObliqueNeighbor() != null) {
                    sb.append("    " + vertex + " -> " + vertex.getObliqueNeighbor() + '\n');
                }
            }
        }

        return sb.toString();
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public int getCapacity() {
        return capacity;
    }
}
