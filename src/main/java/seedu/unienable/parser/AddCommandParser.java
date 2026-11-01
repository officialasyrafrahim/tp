package seedu.unienable.parser;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.unienable.command.activity.AddCommand;
import seedu.unienable.exception.ParseException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.enums.DemandLevel;

//@@author Atharva005
/**
 * Parses the add command using named prefixes that may appear in any order.
 */
public class AddCommandParser {
    private static final Pattern PREFIX = Pattern.compile("(?i)(?<!\\S)(overlap|dem|imp|urg|com|n|d|s|e)/");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String USAGE = "Usage: add n/NAME d/YYYY-MM-DD|today|tomorrow s/HH:mm e/HH:mm "
            + "[dem/LOW|MEDIUM|HIGH] [imp/IMP] [urg/URG] [com/COM] [overlap/ALLOW]";

    private final Clock clock;

    /**
     * Creates a parser using the local system time.
     */
    public AddCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a parser with a shared clock for repeatable date/time validation.
     */
    public AddCommandParser(Clock clock) {
        this.clock = clock;
    }

    /**
     * Validates add arguments and constructs an AddCommand without modifying activity state.
     *
     * @param arguments everything after the leading add command word.
     * @return fully validated add command.
     * @throws ParseException for absent, duplicate, unknown, or malformed arguments.
     */
    public AddCommand parse(String arguments) throws ParseException {
        if (arguments.contains("|") || arguments.indexOf('\n') >= 0 || arguments.indexOf('\r') >= 0) {
            throw createUsageError("The | character and line breaks are not allowed in command parameters.");
        }
        ActivityParameterValidator.validate(arguments);
        Map<String, String> fields = readFields(arguments);
        for (String required : new String[] {"n", "d", "s", "e"}) {
            if (!fields.containsKey(required)) {
                throw createUsageError("Missing " + required + "/ parameter.");
            }
        }

        String name = fields.get("n");
        if (name.isBlank()) {
            throw createUsageError("Empty n/ parameter.");
        }
        if (name.contains("|") || name.indexOf('\n') >= 0 || name.indexOf('\r') >= 0) {
            throw createUsageError("The activity name must not contain | or line breaks.");
        }
        ActivityNameValidator.validate(name);

        LocalDate date;
        LocalTime startTime;
        LocalTime endTime;
        try {
            date = ActivityDateParser.parse(fields.get("d"), clock);
        } catch (DateTimeParseException exception) {
            throw createUsageError(ActivityDateParser.INVALID_DATE_MESSAGE);
        }
        try {
            startTime = parseTime(fields.get("s"));
            endTime = parseTime(fields.get("e"));
        } catch (DateTimeParseException exception) {
            throw createUsageError("Invalid time. Use 24-hour HH:mm (for example, 09:30).");
        }

        if (!endTime.isAfter(startTime)) {
            throw createUsageError("End time must be later than start time on the same day.");
        }
        if (!LocalDateTime.of(date, endTime).isAfter(LocalDateTime.now(clock))) {
            throw createUsageError("Activities that have ended cannot be added.");
        }

        DemandLevel demand = DemandLevel.MEDIUM;
        if (fields.containsKey("dem")) {
            try {
                demand = DemandLevel.valueOf(fields.get("dem").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw createUsageError("Demand must be LOW, MEDIUM, or HIGH.");
            }
        }
        //@@author
        //@@author lukelouyu
        Integer importance = parseRating(fields.get("imp"), "imp");
        Integer urgency = parseRating(fields.get("urg"), "urg");
        Integer commitment = parseRating(fields.get("com"), "com");
        return new AddCommand(name, date, startTime, endTime, demand, importance, urgency, commitment,
                ActivityParameterValidator.allowsOverlap(fields.get("overlap")));
        //@@author
        //@@author Atharva005
    }

    /**
     * Collects prefix/value pairs while allowing whitespace inside the activity name.
     * The shared validator first rejects unsupported slash tokens. Supported prefixes
     * delimit values at whitespace boundaries; literal slashes in names are unsupported.
     */
    private Map<String, String> readFields(String arguments) throws ParseException {
        Map<String, String> fields = new HashMap<>();
        Matcher matcher = PREFIX.matcher(arguments);
        int previousValueStart = -1;
        String previousKey = null;

        while (matcher.find()) {
            if (previousKey == null) {
                if (!arguments.substring(0, matcher.start()).isBlank()) {
                    throw createUsageError("Unexpected text before the first parameter.");
                }
            } else {
                store(fields, previousKey, arguments.substring(previousValueStart, matcher.start()).trim());
            }
            previousKey = matcher.group(1).toLowerCase(Locale.ROOT);
            previousValueStart = matcher.end();
        }
        if (previousKey == null) {
            throw createUsageError("No add parameters supplied.");
        }
        store(fields, previousKey, arguments.substring(previousValueStart).trim());
        return fields;
    }

    private void store(Map<String, String> fields, String key, String value) throws ParseException {
        if (value.isEmpty()) {
            throw createUsageError("Empty " + key + "/ parameter.");
        }
        if (fields.putIfAbsent(key, value) != null) {
            throw createUsageError("Duplicate " + key + "/ parameter.");
        }
    }

    //@@author
    //@@author lukelouyu
    /**
     * Parses a supplied whole-number rating using the model's shared range rule.
     * Missing fields remain unset; empty supplied fields are rejected by the field reader.
     *
     * @param value rating text, or null when the field was omitted.
     * @param prefix field prefix used to identify invalid input.
     * @return rating from 1 to 5, or null when omitted.
     * @throws ParseException if a supplied value is not a whole number from 1 to 5.
     */
    private Integer parseRating(String value, String prefix) throws ParseException {
        if (value == null) {
            return null;
        }
        try {
            Integer rating = Integer.valueOf(value);
            Activity.validateRating(rating);
            return rating;
        } catch (IllegalArgumentException exception) {
            throw createUsageError("The " + prefix + "/ rating must be a whole number from 1 to 5.");
        }
    }
    //@@author
    //@@author Atharva005

    private LocalTime parseTime(String value) {
        if (!TIME_PATTERN.matcher(value).matches()) {
            throw new DateTimeParseException("Wrong time format", value, 0);
        }
        return LocalTime.parse(value, TIME_FORMAT);
    }

    private ParseException createUsageError(String message) {
        return new ParseException("[WARNING] " + message + "\n" + USAGE);
    }
}
//@@author
