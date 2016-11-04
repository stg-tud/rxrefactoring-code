package careercup.phonefeatures;

import java.util.HashSet;
import java.util.Set;

public class Phone {

    Set<Feature> features;

    public Phone() {
        features = new HashSet<Feature>();
    }

    public void addFeature(Feature feature) {
        features.add(feature);
    }
    
    public void removeFeature(Feature feature) {
        features.remove(feature);
    }
}
