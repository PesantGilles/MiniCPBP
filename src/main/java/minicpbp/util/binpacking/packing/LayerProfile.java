package  minicpbp.util.binpacking.packing;

import java.util.Arrays;

import minicpbp.util.binpacking.network.Layer;

public class LayerProfile {
    // Use a contiguous array and height indexes in vertices rather than a map of vertices to flows to improve performance
    private final int[] vertexFlows;

    // Cache hash code to improve performance
    private int hashCode;

    public LayerProfile(Layer layer) {
        vertexFlows = new int[layer.getVertexCount()];
    }

    public LayerProfile(LayerProfile layerProfile) {
        vertexFlows = Arrays.copyOf(layerProfile.vertexFlows, layerProfile.vertexFlows.length);
        hashCode = layerProfile.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LayerProfile layerProfile = (LayerProfile) obj;
        return Arrays.equals(vertexFlows, layerProfile.vertexFlows);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Arrays.hashCode(vertexFlows);
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return Arrays.toString(vertexFlows);
    }

    public int getVertexFlow(int heightIndex) {
        return vertexFlows[heightIndex];
    }

    public void setVertexFlow(int heightIndex, int flow) {
        vertexFlows[heightIndex] = flow;
        hashCode = 0;
    }
}
