package careercup.furniture;

public class Table extends Furniture {

    public Table(Dimension dimension, RawMaterial type) {
        super(dimension, type);
    }
    public Table() {
        super(new Dimension(14, 24, 45), RawMaterial.WOOD);
    }
}
