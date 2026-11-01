//@@author lukelouyu
package seedu.unienable.command.accessibility.connection.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.unienable.model.Facility;
import seedu.unienable.model.FacilityFeature;
import seedu.unienable.model.enums.AccessibilityStatus;

/**
 * Provides read-only lookups and feature searches over loaded facility reference data.
 */
public final class FacilityManager {
    private final List<Facility> facilities;

    /**
     * Creates a manager using a snapshot of the supplied facilities.
     *
     * @param facilities non-null list of loaded facilities with no null entries.
     */
    public FacilityManager(List<Facility> facilities) {
        this.facilities = List.copyOf(facilities);
    }

    /**
     * Returns all facilities in their original dataset order.
     *
     * @return immutable list of facilities.
     */
    public List<Facility> getFacilities() {
        return facilities;
    }

    /**
     * Finds a facility by stable ID or short facility name, ignoring case and surrounding whitespace.
     * Stable IDs take precedence over names if an identifier happens to match both.
     *
     * @param identifier stable facility ID (e.g., F01) or name (e.g., AS1).
     * @return matching facility, or an empty Optional if no match exists.
     */
    public Optional<Facility> findFacility(String identifier) {
        String query = Objects.requireNonNull(identifier, "identifier").strip();
        if (query.isEmpty()) {
            return Optional.empty();
        }
        for (Facility facility : facilities) {
            if (facility.getId().equalsIgnoreCase(query)) {
                return Optional.of(facility);
            }
        }
        for (Facility facility : facilities) {
            if (facility.getName().equalsIgnoreCase(query)) {
                return Optional.of(facility);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds facilities with an explicitly recorded YES status for the specified feature.
     *
     * @param type accessibility feature type.
     * @return matching facilities in dataset order.
     */
    public List<Facility> findByFeature(FacilityFeature.Type type) {
        return findByFeature(type, AccessibilityStatus.YES);
    }

    /**
     * Finds facilities having the specified recorded feature and exact status.
     * An absent feature does not count as UNKNOWN; it must be explicitly recorded as UNKNOWN.
     * Each facility appears at most once even if it has duplicate matching feature records.
     *
     * @param type accessibility feature type.
     * @param status required recorded accessibility status.
     * @return immutable list of matching facilities in dataset order.
     */
    public List<Facility> findByFeature(FacilityFeature.Type type, AccessibilityStatus status) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(status, "status");
        List<Facility> matchingFacilities = new ArrayList<>();
        for (Facility facility : facilities) {
            for (FacilityFeature feature : facility.getFeatures()) {
                if (feature.getType() == type && feature.getStatus() == status) {
                    matchingFacilities.add(facility);
                    break;
                }
            }
        }
        return List.copyOf(matchingFacilities);
    }
}
//@@author
