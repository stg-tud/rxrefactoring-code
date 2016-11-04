package careercup.furniture;

public abstract class Furniture {

    private Dimension dimension;
    private RawMaterial type;

    public Furniture(Dimension dimension, RawMaterial type) {
        this.dimension = dimension;
        this.type = type;
    }
}

class Dimension {

    int length;
    int breadth;
    int height;

    public Dimension(int length, int breadth, int height) {
        this.length = length;
        this.breadth = breadth;
        this.height = height;
    }
}

enum RawMaterial {

    WOOD,
    METAL,
    PASTIC;
}
