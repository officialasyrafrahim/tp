package seedu.unienable.parser;

import java.util.Locale;

//@@author lukelouyu
import seedu.unienable.command.accessibility.connection.logic.FacilityManager;
//@@author
import seedu.unienable.command.accessibility.facility.FacilityFindCommand;
import seedu.unienable.command.accessibility.facility.FacilityListCommand;
import seedu.unienable.command.accessibility.facility.FacilityViewCommand;
//@@author lukelouyu
import seedu.unienable.command.common.Command;
//@@author
import seedu.unienable.exception.ParseException;
import seedu.unienable.model.FacilityFeature;
import seedu.unienable.model.enums.AccessibilityStatus;

//@@author lukelouyu
/**
 * Parses facility lookup, listing, and feature search commands.
 */
public class FacilityCommandParser {
    /**
     * Shared prefix for warnings reported by this parser.
     */
    private static final String WARNING_MESSAGE = "[WARNING]";

    /**
     * Identifies the required feature filter of a facility search.
     */
    private static final String TYPE_PREFIX = "type/";

    /**
     * Identifies the optional status filter of a facility search.
     */
    private static final String STATUS_PREFIX = "status/";

    /**
     * Reference data used to construct facility commands; null when unavailable.
     */
    private final FacilityManager facilityManager;

    /**
     * Creates a parser with the available facility reference data.
     *
     * @param facilityManager manager used by facility commands, or null if unavailable.
     */
    public FacilityCommandParser(FacilityManager facilityManager) {
        this.facilityManager = facilityManager;
    }

    /**
     * Recognizes facility LOCATION, facility list, and facility find.
     *
     * @param words command words, beginning with facility.
     * @return the facility command to execute.
     * @throws ParseException if the facility command is invalid or unavailable.
     */
    public Command parseFacility(String[] words) throws ParseException {
        if (words.length >= 2 && "view".equalsIgnoreCase(words[1])) {
            throw new ParseException(WARNING_MESSAGE
                    + " Invalid facility command.\n"
                    + "Usage: facility LOCATION\nExample: facility AS4");
        }

        if (words.length >= 2 && "list".equalsIgnoreCase(words[1])) {
            if (words.length != 2) {
                throw new ParseException(WARNING_MESSAGE
                        + " facility list does not accept arguments.\nUsage: facility list");
            }
            requireFacilityData();
            return new FacilityListCommand(facilityManager);
        }

        if (words.length >= 2 && "find".equalsIgnoreCase(words[1])) {
            return parseFacilityFind(words);
        }

        if (words.length != 2) {
            throw new ParseException(WARNING_MESSAGE
                    + " Expected exactly one facility ID or name.\n"
                    + "Usage: facility LOCATION\nExample: facility AS4");
        }
        requireFacilityData();
        return new FacilityViewCommand(facilityManager, words[1]);
    }

    /**
     * Parses the required feature type and optional accessibility status.
     *
     * @param words words in the command line.
     * @return the facility feature search command.
     * @throws ParseException if the feature or status is invalid.
     */
    private Command parseFacilityFind(String[] words) throws ParseException {
        String usage = "Usage: facility find type/FEATURE [status/YES|NO|UNKNOWN]"
                + "\nExample: facility find type/LIFT status/NO";
        if (words.length < 3 || words.length > 4
                || !words[2].regionMatches(true, 0, TYPE_PREFIX, 0, TYPE_PREFIX.length())
                || words[2].length() == TYPE_PREFIX.length()) {
            throw new ParseException(WARNING_MESSAGE + " Invalid facility find filters.\n" + usage);
        }

        String featureName = words[2].substring(TYPE_PREFIX.length());
        FacilityFeature.Type type;
        try {
            type = FacilityFeature.Type.valueOf(featureName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(WARNING_MESSAGE
                    + " Unknown facility feature type '" + featureName + "'.\n"
                    + "Supported types: LIFT, RAMP, SHELTERED_RAMP, ACCESSIBLE_WASHROOM, "
                    + "STEP_FREE_ENTRANCE, REST_POINT, AUTOMATIC_DOOR, OTHER\n" + usage);
        }

        AccessibilityStatus status = AccessibilityStatus.YES;
        if (words.length == 4) {
            if (!words[3].regionMatches(true, 0, STATUS_PREFIX, 0, STATUS_PREFIX.length())
                    || words[3].length() == STATUS_PREFIX.length()) {
                throw new ParseException(WARNING_MESSAGE
                        + " Invalid facility find filters.\n" + usage);
            }
            String statusName = words[3].substring(STATUS_PREFIX.length());
            try {
                status = AccessibilityStatus.valueOf(statusName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new ParseException(WARNING_MESSAGE
                        + " Invalid facility status '" + statusName + "'.\n"
                        + "Supported statuses: YES, NO, UNKNOWN\n" + usage);
            }
        }

        requireFacilityData();
        return new FacilityFindCommand(facilityManager, type, status);
    }

    /**
     * Reports unavailable reference data after command arguments have been validated.
     */
    private void requireFacilityData() throws ParseException {
        if (facilityManager == null) {
            throw new ParseException(WARNING_MESSAGE + " Facility reference data is unavailable.");
        }
    }
}
//@@author
