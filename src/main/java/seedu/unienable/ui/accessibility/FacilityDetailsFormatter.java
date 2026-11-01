package seedu.unienable.ui.accessibility;

import seedu.unienable.model.Facility;
import seedu.unienable.model.FacilityFeature;

//@@author lukelouyu
/**
 * Formats the recorded details shared by facility list and lookup commands.
 */
public final class FacilityDetailsFormatter {
    /**
     * Prevents construction of this utility class.
     */
    private FacilityDetailsFormatter() {
    }

    /**
     * Formats a facility's ID, name, description, features, statuses, and notes.
     * Missing optional text is omitted and feature order is preserved.
     *
     * @param facility facility whose recorded details should be displayed.
     * @return facility details without a heading or accessibility disclaimer.
     */
    public static String format(Facility facility) {
        StringBuilder output = new StringBuilder();
        output.append("[").append(facility.getId()).append("] ").append(facility.getName());
        if (facility.getDescription() != null && !facility.getDescription().isBlank()) {
            output.append(" - ").append(facility.getDescription());
        }
        output.append("\n\nAccessibility Features:");
        if (facility.getFeatures().isEmpty()) {
            output.append("\n[WARNING] No accessibility features recorded.");
        }
        for (FacilityFeature feature : facility.getFeatures()) {
            output.append("\n").append(feature.getType()).append(" | ").append(feature.getStatus());
            if (feature.getNotes() != null && !feature.getNotes().isBlank()) {
                output.append(" | ").append(feature.getNotes());
            }
        }
        return output.toString();
    }
}
//@@author
