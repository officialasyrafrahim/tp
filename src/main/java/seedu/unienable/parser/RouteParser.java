package seedu.unienable.parser;

import seedu.unienable.command.accessibility.connection.RouteCommand;
import seedu.unienable.command.accessibility.connection.logic.ConnectionManager;
import seedu.unienable.command.accessibility.connection.logic.FacilityManager;
import seedu.unienable.exception.ParseException;

//@@author lukelouyu
/**
 * Parses route commands and reports any missing source or destination.
 */
public class RouteParser {
    /**
     * Shared prefix for warnings reported by this parser.
     */
    private static final String WARNING_MESSAGE = "[WARNING]";

    /**
     * Shows the required prefixes when a route request is incomplete or malformed.
     */
    private static final String ROUTE_USAGE = "\nUsage: route from/START to/END";

    /**
     * Identifies the starting facility argument.
     */
    private static final String ROUTE_FROM_PREFIX = "from/";

    /**
     * Identifies the destination facility argument.
     */
    private static final String ROUTE_TO_PREFIX = "to/";

    /**
     * Loaded connection reference data used by route commands; null when unavailable.
     */
    private final ConnectionManager connectionManager;
    /** Resolves stable facility IDs before the graph receives hub names. */
    private final FacilityManager facilityManager;

    /**
     * Creates a parser with the available connection reference data.
     *
     * @param connectionManager manager used by route commands, or null when unavailable.
     */
    public RouteParser(ConnectionManager connectionManager) {
        this(connectionManager, null);
    }

    /**
     * Creates a route parser with optional ID lookup; name-only routing still works without facilities.
     */
    public RouteParser(ConnectionManager connectionManager, FacilityManager facilityManager) {
        this.connectionManager = connectionManager;
        this.facilityManager = facilityManager;
    }

    /**
     * Parses route prefixes and asks for any missing source or destination.
     *
     * @param words command words beginning with route, with optional from/ and to/ arguments.
     * @return the route command with both endpoints supplied.
     * @throws ParseException if arguments are missing, malformed or reference data is unavailable.
     */
    public RouteCommand parseRoute(String[] words) throws ParseException {
        if (words.length > 3) {
            throw new ParseException(WARNING_MESSAGE + " Invalid route command." + ROUTE_USAGE);
        }
        if (words.length == 3) {
            boolean hasFromPrefix = hasPrefix(words[1], ROUTE_FROM_PREFIX);
            boolean hasToPrefix = hasPrefix(words[2], ROUTE_TO_PREFIX);
            if (!hasFromPrefix || !hasToPrefix) {
                throw new ParseException(WARNING_MESSAGE + " Invalid route command." + ROUTE_USAGE);
            }
        }

        String from = "";
        String to = "";
        for (int i = 1; i < words.length; i++) {
            String argument = words[i];
            if (hasPrefix(argument, ROUTE_FROM_PREFIX)) {
                from = argument.substring(ROUTE_FROM_PREFIX.length());
            } else if (hasPrefix(argument, ROUTE_TO_PREFIX)) {
                to = argument.substring(ROUTE_TO_PREFIX.length());
            } else {
                throw new ParseException(WARNING_MESSAGE + " Invalid route command." + ROUTE_USAGE);
            }
        }

        if (from.isEmpty() && to.isEmpty()) {
            throw new ParseException(WARNING_MESSAGE + " Please enter a source and destination." + ROUTE_USAGE);
        }
        if (from.isEmpty()) {
            throw new ParseException(WARNING_MESSAGE + " Please enter a source." + ROUTE_USAGE);
        }
        if (to.isEmpty()) {
            throw new ParseException(WARNING_MESSAGE + " Please enter a destination." + ROUTE_USAGE);
        }
        if (connectionManager == null) {
            throw new ParseException(WARNING_MESSAGE + " Connection reference data is unavailable.");
        }
        if (facilityManager != null) {
            from = facilityManager.findFacility(from).map(facility -> facility.getName()).orElse(from);
            to = facilityManager.findFacility(to).map(facility -> facility.getName()).orElse(to);
        }
        return new RouteCommand(connectionManager, from, to);
    }

    /**
     * Checks an ASCII command prefix without changing the facility name or depending on the locale.
     */
    private static boolean hasPrefix(String argument, String prefix) {
        return argument.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
//@@author
