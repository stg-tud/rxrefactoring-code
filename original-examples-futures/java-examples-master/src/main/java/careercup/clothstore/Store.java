package careercup.clothstore;

import java.util.HashSet;
import java.util.Set;

public class Store {

    Set<Department> departments;

    public Store() {    
        departments = new HashSet<Department>();
    }

    public boolean addDepartment(Department d) {
        return departments.add(d);
    }
    public boolean removeDepartment(Department d) {
        return departments.remove(d);
    }

    
}
