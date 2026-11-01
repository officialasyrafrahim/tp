package seedu.unienable.model;

import java.util.List;

//@@author lukelouyu
/**
 * An immutable facility or campus hub with a stable ID and recorded features.
 */
public final class Facility {
    private final String id;
    private final String name;
    private final String description;
    private final List<FacilityFeature> features;

    /**
     * Creates a reference-data object without parsing or normalizing the supplied values.
     *
     * @param id stable facility ID, preserved as supplied.
     * @param name facility name, preserved as supplied.
     * @param description optional description, or null.
     * @param features non-null feature list with no null entries; copied defensively.
     */
    public Facility(String id, String name, String description, List<FacilityFeature> features) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.features = List.copyOf(features);
    }

    /**
     * Returns the id value recorded for this object.
     *
     * @return stable facility ID, preserved as supplied.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name value recorded for this object.
     *
     * @return facility name, preserved as supplied.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description value recorded for this object.
     *
     * @return optional description, or null.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the features value recorded for this object.
     * The returned list is immutable.
     *
     * @return non-null feature list with no null entries; copied defensively.
     */
    public List<FacilityFeature> getFeatures() {
        return features;
    }
}
//@@author
