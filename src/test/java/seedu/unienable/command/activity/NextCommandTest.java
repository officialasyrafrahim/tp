package seedu.unienable.command.activity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.unienable.exception.ParseException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.Storage;

//@@author officialasyrafrahim
/**
 * Checks the strict next-start boundary, completed activity exclusion, stable
 * ties, and chronological index display for the next command.
 */
class NextCommandTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC);

    private final Parser parser = new Parser(FIXED_CLOCK);
    private final ActivityList activities = new ActivityList();
    private final Storage noWrites = new Storage() {
        @Override
        public void save(ActivityList ignored) {
            throw new AssertionError("NextCommand must not persist activities");
        }
    };

    @Test
    void execute_skipsExactNowAndCompletedAndChoosesEarliestFutureStart() throws Exception {
        add("Later", "2026-10-23", "09:00", false);
        add("Second tie", "2026-10-22", "09:00", false);
        add("Exact now", "2026-10-21", "12:00", false);
        add("First tie", "2026-10-22", "09:00", false);
        add("Done first", "2026-10-22", "08:00", true);

        String message = parser.parse("next").execute(activities, noWrites).getMessage();

        assertTrue(message.startsWith("Next activity (earliest upcoming):"), message);
        assertTrue(message.contains("3. Second tie (2026-10-22, 09:00-10:00) [MEDIUM] [ ]"),
                message);
        assertFalse(message.contains("First tie"), message);
        assertFalse(message.contains("Exact now"), message);
        assertFalse(message.contains("Done first"), message);
        assertFalse(parser.parse("next").execute(activities, noWrites).didChangeActivities());
    }

    @Test
    void execute_noIncompleteFutureActivity_showsEmptyMessage() throws Exception {
        add("Exact now", "2026-10-21", "12:00", false);
        add("Done", "2026-10-22", "09:00", true);

        String message = parser.parse("next").execute(activities, noWrites).getMessage();

        assertTrue(message.endsWith("Sorry, no upcoming incomplete activities exist."), message);
    }

    @Test
    void execute_onEmptyList_showsEmptyMessage() throws Exception {
        String message = parser.parse("next").execute(activities, noWrites).getMessage();
        assertTrue(message.endsWith("Sorry, no upcoming incomplete activities exist."), message);
    }

    @Test
    void parse_nextRejectsArguments() {
        assertThrows(ParseException.class, () -> parser.parse("next extra"));
    }

    private void add(String name, String date, String start, boolean isDone) {
        activities.addActivity(new Activity(name, LocalDate.parse(date), LocalTime.parse(start),
                LocalTime.parse(start).plusHours(1), DemandLevel.MEDIUM, isDone));
    }
}
//@@author
