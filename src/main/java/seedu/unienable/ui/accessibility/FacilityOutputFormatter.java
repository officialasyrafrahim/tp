package seedu.unienable.ui.accessibility;

import java.util.List;

import seedu.unienable.model.Facility;
import seedu.unienable.model.FacilityFeature;
import seedu.unienable.model.enums.AccessibilityStatus;

//@@author lukelouyu
/**
 * Builds user-facing Facility HUB messages from data supplied by commands.
 * Formatting preserves record order and does not read input or print to the console.
 */
public final class FacilityOutputFormatter {
    private static final String LOOKUP_USAGE = "Usage: facility LOCATION\nExample: facility AS4";

    /**
     * Prevents construction of this utility class.
     */
    private FacilityOutputFormatter() {
    }

    /**
     * Formats the complete list of facilities with one disclaimer at the end.
     *
     * @param facilities facilities in the order they should be displayed.
     * @return list heading, facility details, and accessibility disclaimer.
     */
    public static String formatList(List<Facility> facilities) {
        StringBuilder output = new StringBuilder("Known facilities in the local reference:");
        if (facilities.isEmpty()) {
            output.append("\n[WARNING] No facilities recorded.");
        }
        for (Facility facility : facilities) {
            output.append("\n\n").append(FacilityDetailsFormatter.format(facility));
        }
        return appendDisclaimer(output.toString());
    }

    /**
     * Formats one facility's recorded details and the accessibility disclaimer.
     *
     * @param facility facility to display.
     * @return facility details and accessibility disclaimer.
     */
    public static String formatFacility(Facility facility) {
        return appendDisclaimer(FacilityDetailsFormatter.format(facility));
    }

    /**
     * Formats results already filtered by the facility manager.
     * UNKNOWN is explained even when the search returns no matches.
     *
     * @param matches matching facilities in the order they should be displayed.
     * @param type feature used in the search.
     * @param status recorded status used in the search.
     * @return search heading, matching IDs and names, and accessibility disclaimer.
     */
    public static String formatFindResults(List<Facility> matches,
            FacilityFeature.Type type, AccessibilityStatus status) {
        StringBuilder output = new StringBuilder("Facilities with ");
        output.append(type).append(" (status: ").append(status).append("):");

        if (matches.isEmpty()) {
            output.append("\n[WARNING] No matching facilities found.");
        } else {
            for (Facility facility : matches) {
                output.append("\n[").append(facility.getId()).append("] ")
                        .append(facility.getName());
            }
        }

        if (status == AccessibilityStatus.UNKNOWN) {
            output.append("\n\nUNKNOWN means the local dataset does not confirm the feature.");
        }
        return appendDisclaimer(output.toString());
    }

    /**
     * Formats an unknown-facility warning with supported names and lookup usage.
     *
     * @param identifier facility identifier entered by the user.
     * @param facilities supported facilities in dataset order.
     * @return warning message with supported names and a lookup example.
     */
    public static String formatUnknownFacility(String identifier, List<Facility> facilities) {
        StringBuilder output = new StringBuilder("[WARNING] Unknown facility '");
        output.append(identifier).append("'.\n\nSupported facilities:\n");
        if (facilities.isEmpty()) {
            output.append("None");
        } else {
            for (int i = 0; i < facilities.size(); i++) {
                if (i > 0) {
                    output.append(", ");
                }
                output.append(facilities.get(i).getName());
            }
        }
        output.append("\n\n").append(LOOKUP_USAGE);
        return output.toString();
    }

    /**
     * Adds the shared accessibility disclaimer after the formatted message.
     *
     * @param message content to display before the disclaimer.
     * @return complete message with the disclaimer.
     */
    private static String appendDisclaimer(String message) {
        return message + "\n\n" + AccessibilityDisclaimer.TEXT;
    }
}
//@@author
