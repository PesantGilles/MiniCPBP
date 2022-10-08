package  minicpbp.util.binpacking.network;

public class Vertex {
    private final int layer;
    private final int height;
    private int heightIndex;

    private Vertex horizontalNeighbor; // Horizontal arc to vertex in next layer
    private Vertex obliqueNeighbor; // Oblique arc to vertex in next layer

    public Vertex(int layer, int height) {
        this.layer = layer;
        this.height = height;
    }

    @Override
    public String toString() {
        return "v" + layer + height;
    }

    public int getHeight() {
        return height;
    }

    public int getHeightIndex() {
        return heightIndex;
    }

    public Vertex getHorizontalNeighbor() {
        return horizontalNeighbor;
    }

    public Vertex getObliqueNeighbor() {
        return obliqueNeighbor;
    }

    void setHeightIndex(int heightIndex) {
        this.heightIndex = heightIndex;
    }

    void setHorizontalNeighbor(Vertex vertex) {
        horizontalNeighbor = vertex;
    }

    void setObliqueNeighbor(Vertex vertex) {
        obliqueNeighbor = vertex;
    }
}
