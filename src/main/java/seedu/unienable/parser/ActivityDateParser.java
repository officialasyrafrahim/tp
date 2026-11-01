//@@author Atharva005
package seedu.unienable.parser;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Converts a {@code d/} value into a date so add and edit accept the same input.
 * The words today and tomorrow become real dates here, which means a saved activity
 * keeps its date instead of moving as the calendar advances.
 */
final class ActivityDateParser {
    /** Shared wording for add and edit, which each append their own usage line. */
    static final String INVALID_DATE_MESSAGE = "Invalid date. Use a real YYYY-MM-DD date, today, or tomorrow.";

    private static final String TODAY = "today";
    private static final String TOMORROW = "tomorrow";
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Prevents instantiation of this shared parser helper.
     */
    private ActivityDateParser() {
    }

    /**
     * Reads a date word, ignoring letter case, or a strict {@code YYYY-MM-DD} date.
     *
     * @param value text supplied after {@code d/}.
     * @param clock source of the current date, fixed by tests for repeatable results.
     * @return the date the value refers to.
     * @throws DateTimeParseException if the value is neither a date word nor a real calendar date.
     */
    static LocalDate parse(String value, Clock clock) {
        return switch (value.toLowerCase(Locale.ROOT)) {
        case TODAY -> LocalDate.now(clock);
        case TOMORROW -> LocalDate.now(clock).plusDays(1);
        default -> parseCalendarDate(value);
        };
    }

    private static LocalDate parseCalendarDate(String value) {
        if (!DATE_PATTERN.matcher(value).matches()) {
            throw new DateTimeParseException("Wrong date format", value, 0);
        }
        return LocalDate.parse(value, DATE_FORMAT);
    }
}
//@@author
