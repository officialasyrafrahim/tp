package seedu.unienable.parser;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.unienable.command.activity.EditCommand;
import seedu.unienable.exception.ParseException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.enums.DemandLevel;

//@@author Oliver-koh
/**
 * Parses the V2.0 edit command using optional activity and rating fields.
 */
public class EditCommandParser {
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\d+");
    private static final Pattern PREFIX = Pattern.compile("(?i)(?<!\\S)(overlap|dem|imp|urg|com|n|d|s|e)/");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String USAGE = "Usage: edit INDEX [n/NAME] [d/YYYY-MM-DD|today|tomorrow] [s/HH:mm] "
            + "[e/HH:mm] [dem/LOW|MEDIUM|HIGH] [imp/IMP] [urg/URG] [com/COM] [overlap/ALLOW]";

    private final Clock clock;

    /**
     * Creates a parser using the system clock.
     */
    public EditCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a parser with a supplied clock for deterministic validation.
     */
    EditCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Parses an edit command after the leading {@code edit} word.
     *
     * @param arguments index followed by one or more editable fields.
     * @return validated edit command.
     * @throws ParseException for malformed or incomplete input.
     */
    public EditCommand parse(String arguments) throws ParseException {
        if (arguments.contains("|") || arguments.indexOf('\n') >= 0 || arguments.indexOf('\r') >= 0) {
            throw createUsageError("The | character and line breaks are not allowed in command parameters.");
        }
        ActivityParameterValidator.validate(arguments);
        String trimmed = arguments.trim();
        if (trimmed.isEmpty()) {
            throw createUsageError("edit needs an activity index.");
        }
        int separator = firstWhitespaceIndex(trimmed);
        String indexToken = separator < 0 ? trimmed : trimmed.substring(0, separator);
        int index = parseIndex(indexToken);
        if (separator < 0) {
            throw createUsageError("At least one field must be supplied.");
        }

        Map<String, String> fields = readFields(trimmed.substring(separator).trim());
        if (fields.size() == 1 && fields.containsKey("overlap")) {
            throw createUsageError("At least one editable field must be supplied with overlap/ALLOW.");
        }
        String name = fields.get("n");
        if (name != null && name.isBlank()) {
            throw createUsageError("Empty n/ parameter.");
        }
        if (name != null && (name.contains("|") || name.indexOf('\n') >= 0 || name.indexOf('\r') >= 0)) {
            throw createUsageError("The activity name must not contain | or line breaks.");
        }
        if (name != null) {
            ActivityNameValidator.validate(name);
        }
        LocalDate date = fields.containsKey("d") ? parseDate(fields.get("d")) : null;
        LocalTime start = fields.containsKey("s") ? parseTime(fields.get("s")) : null;
        LocalTime end = fields.containsKey("e") ? parseTime(fields.get("e")) : null;
        DemandLevel demand = fields.containsKey("dem") ? parseDemand(fields.get("dem")) : null;
        Integer importance = parseRating(fields.get("imp"), "imp");
        Integer urgency = parseRating(fields.get("urg"), "urg");
        Integer commitment = parseRating(fields.get("com"), "com");
        EditCommand.Fields changes = new EditCommand.Fields(name, date, start, end, demand,
                importance, urgency, commitment);
        return new EditCommand(index, changes, clock,
                ActivityParameterValidator.allowsOverlap(fields.get("overlap")));
    }

    private int parseIndex(String token) throws ParseException {
        if (!INDEX_PATTERN.matcher(token).matches()) {
            throw createUsageError("Invalid index '" + token + "'. Index must be a positive whole number.");
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException exception) {
            throw createUsageError("Index '" + token + "' is too large.");
        }
    }

    private int firstWhitespaceIndex(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private Map<String, String> readFields(String arguments) throws ParseException {
        Map<String, String> fields = new HashMap<>();
        Matcher matcher = PREFIX.matcher(arguments);
        int previousValueStart = -1;
        String previousKey = null;
        while (matcher.find()) {
            if (previousKey != null) {
                store(fields, previousKey, arguments.substring(previousValueStart, matcher.start()).trim());
            } else if (!arguments.substring(0, matcher.start()).isBlank()) {
                throw createUsageError("Unexpected text before the first parameter.");
            }
            previousKey = matcher.group(1).toLowerCase(Locale.ROOT);
            previousValueStart = matcher.end();
        }
        if (previousKey == null) {
            throw createUsageError("At least one field must be supplied.");
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

    private LocalDate parseDate(String value) throws ParseException {
        try {
            return ActivityDateParser.parse(value, clock);
        } catch (DateTimeParseException exception) {
            throw createUsageError(ActivityDateParser.INVALID_DATE_MESSAGE);
        }
    }

    private LocalTime parseTime(String value) throws ParseException {
        try {
            if (!TIME_PATTERN.matcher(value).matches()) {
                throw new DateTimeParseException("Wrong time format", value, 0);
            }
            return LocalTime.parse(value, TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            throw createUsageError("Invalid time. Use 24-hour HH:mm (for example, 09:30).");
        }
    }

    private DemandLevel parseDemand(String value) throws ParseException {
        try {
            return DemandLevel.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw createUsageError("Demand must be LOW, MEDIUM, or HIGH.");
        }
    }

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

    private ParseException createUsageError(String message) {
        return new ParseException("[WARNING] " + message + "\n" + USAGE);
    }
}
//@@author
