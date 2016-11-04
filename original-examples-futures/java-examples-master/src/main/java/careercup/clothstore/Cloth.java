package careercup.clothstore;

public class Cloth extends Article {

    String brand;
    Size size;
    ClothType type;
    float price;
    long barCode;

    public Cloth(String brand, Size size, ClothType type, float price, long barCode) {
        this.brand = brand;
        this.size = size;
        this.type = type;
        this.price = price;
        this.barCode = barCode;
    }

    public static enum Size {
        S,
        M,
        L,
        XL,
        XXL;
    }

    public static enum ClothType {
        TSHIRT,
        SHIRT,
        JEANS,
        PANT;
    }

    public static enum Brand {
        LEWIS;
    }
}
