package careercup.clothstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClothDepartment implements Department<Cloth> {

    ArrayList<Cloth> cloths = new ArrayList<Cloth>();
    Map<Long, Float> priceMapping = new HashMap<Long, Float>();

    public void add(Cloth cloth) {
        cloths.add(cloth);
        priceMapping.put(cloth.barCode, cloth.price);
    }
    public void remove(Cloth cloth) {
        cloths.remove(cloth);
        priceMapping.remove(cloth.barCode);
    }

    public boolean exist(Cloth cloth) {
        return cloths.contains(cloth);
    }

    public List<Cloth> getCloth(Cloth cloth, SearchBy param) {
        if (SearchBy.Size == param) {
        }
        return null;
    }

    // All cloths for a particular type
    public List<Cloth> getCloth(Cloth.ClothType type) {
        return null;
    }
    // All cloths for a particular size
    public List<Cloth> getCloth(Cloth.Size size) {
        return null;
    }

    // All cloths for a particular brand
    public List<Cloth> getCloth(Cloth.Brand brand) {
        return null;
    }

    enum SearchBy {
        Size,
        Brand,
        ClothType;
    }

}
