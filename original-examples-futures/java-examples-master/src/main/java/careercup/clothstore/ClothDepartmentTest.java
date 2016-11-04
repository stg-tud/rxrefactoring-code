package careercup.clothstore;

import careercup.clothstore.Cloth.Brand;
import careercup.clothstore.Cloth.ClothType;
import careercup.clothstore.Cloth.Size;

// It is not extensible if we have multiple number of shirts for same color,
// brand. price, and size. THINK ABT IT.
public class ClothDepartmentTest {

    public static void main(String[] args) {
        Cloth cloth = new Cloth(Brand.LEWIS.toString(), Size.XXL, ClothType.TSHIRT, 23.8f, 123456789);
        Cloth cloth2 = new Cloth(Brand.LEWIS.toString(), Size.XL, ClothType.TSHIRT, 23.8f, 123456789);
        ClothDepartment clothDept = new ClothDepartment();
        clothDept.add(cloth);
        clothDept.add(cloth2);

        boolean exist = clothDept.exist(cloth);
        System.out.println(exist);

    }
}
