package  minicpbp.util.binpacking.network;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Layer {
    private final int itemSize;
    private final Map<Integer, Vertex> vertices; // Map of heights to vertices

    public Layer(int itemSize, Map<Integer, Vertex> vertices) {
        this.itemSize = itemSize;

        // Sort layer vertices by ascending height and store them in a LinkedHashMap to preserve their order
        this.vertices = vertices.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // Set vertices' height index field for easier lookup during counting
        int heightIndex = 0;
        for (Vertex vertex : this.vertices.values()) {
            vertex.setHeightIndex(heightIndex);
            heightIndex++;
        }
    }

    public int getItemSize() {
        return itemSize;
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public Vertex getVertex(Integer height) {
        return vertices.get(height);
    }

    public int getVertexCount() {
        return vertices.size();
    }
}
