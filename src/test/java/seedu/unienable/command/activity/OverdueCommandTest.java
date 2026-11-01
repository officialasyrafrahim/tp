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
 * Checks strict overdue boundaries, time labels, chronological indexes, and the
 * exclusion of completed or future-starting activities.
 */
class OverdueCommandTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC);

    private final Parser parser = new Parser(FIXED_CLOCK);
    private final ActivityList activities = new ActivityList();
    private final Storage noWrites = new Storage() {
        @Override
        public void save(ActivityList ignored) {
            throw new AssertionError("OverdueCommand must not persist activities");
        }
    };

    @Test
    void execute_usesStrictEndBoundaryAndExcludesCompletedActivities() throws Exception {
        add("Tomorrow", "2026-10-22", "09:00", 10);
        add("Sunday", "2026-10-18", "08:00", 9);
        add("Exact now", "2026-10-21", "12:00", 13);
        add("Monday", "2026-10-19", "08:00", 9);
        add("Today done", "2026-10-21", "09:00", 10);
        add("Today past", "2026-10-21", "10:30", 11);
        add("Ending now", "2026-10-21", "11:00", 12);
        add("In progress", "2026-10-21", "11:30", 13);
        add("Future today", "2026-10-21", "13:00", 14);
        activities.getActivities().stream()
                .filter(activity -> activity.getName().equals("Today done"))
                .forEach(activity -> activity.setDone(true));

        String message = parser.parse("overdue").execute(activities, noWrites).getMessage();

        assertTrue(message.startsWith("Overdue activities (ended before now):"), message);
        assertTrue(message.contains("1. [OVERDUE EARLIER] Sunday (2026-10-18, 08:00-09:00)"),
                message);
        assertTrue(message.contains("2. [OVERDUE WEEK] Monday (2026-10-19, 08:00-09:00)"),
                message);
        assertTrue(message.contains("4. [OVERDUE TODAY] Today past (2026-10-21, 10:30-11:00)"),
                message);
        assertFalse(message.contains("In progress"), message);
        assertFalse(message.contains("Ending now"), message);
        assertFalse(message.contains("Today done"), message);
        assertFalse(message.contains("Exact now"), message);
        assertFalse(message.contains("Future today"), message);
        assertFalse(message.contains("Tomorrow"), message);
        assertFalse(message.contains("[X]"), message);
        assertFalse(parser.parse("overdue").execute(activities, noWrites).didChangeActivities());
    }

    @Test
    void execute_onlyBecomesOverdueAfterEndTime() throws Exception {
        activities.addActivity(new Activity("Boundary", LocalDate.parse("2026-10-21"),
                LocalTime.parse("09:00"), LocalTime.parse("09:30"), DemandLevel.LOW, false));
        for (String time : new String[]{"09:15:00", "09:30:00", "09:30:01"}) {
            Parser timed = new Parser(Clock.fixed(Instant.parse("2026-10-21T" + time + "Z"), ZoneOffset.UTC));
            String message = timed.parse("overdue").execute(activities, noWrites).getMessage();
            if (time.equals("09:30:01")) {
                assertTrue(message.contains("1. [OVERDUE TODAY] Boundary"), message);
            } else {
                assertTrue(message.endsWith("Sorry, no incomplete activities have ended."), message);
            }
        }
    }

    @Test
    void execute_onEmptyList_showsNoOverdueMessage() throws Exception {
        String message = parser.parse("overdue").execute(activities, noWrites).getMessage();
        assertTrue(message.endsWith("Sorry, no incomplete activities have ended."), message);
    }

    @Test
    void execute_acrossYearBoundary_usesCurrentMondayForWeekLabel() throws Exception {
        Clock newYearClock = Clock.fixed(Instant.parse("2027-01-01T12:00:00Z"), ZoneOffset.UTC);
        Parser newYearParser = new Parser(newYearClock);
        ActivityList newYearActivities = new ActivityList();
        newYearActivities.addActivity(new Activity("Last year", LocalDate.parse("2026-12-27"),
                LocalTime.parse("09:00"), LocalTime.parse("10:00"), DemandLevel.LOW, false));
        newYearActivities.addActivity(new Activity("This week", LocalDate.parse("2026-12-29"),
                LocalTime.parse("09:00"), LocalTime.parse("10:00"), DemandLevel.LOW, false));
        newYearActivities.addActivity(new Activity("Today", LocalDate.parse("2027-01-01"),
                LocalTime.parse("08:00"), LocalTime.parse("09:00"), DemandLevel.LOW, false));

        String message = newYearParser.parse("overdue")
                .execute(newYearActivities, noWrites).getMessage();

        assertTrue(message.contains("1. [OVERDUE EARLIER] Last year"), message);
        assertTrue(message.contains("2. [OVERDUE WEEK] This week"), message);
        assertTrue(message.contains("3. [OVERDUE TODAY] Today"), message);
    }

    @Test
    void parse_overdueRejectsArguments() {
        assertThrows(ParseException.class, () -> parser.parse("overdue extra"));
    }

    private void add(String name, String date, String start, int endHour) {
        activities.addActivity(new Activity(name, LocalDate.parse(date), LocalTime.parse(start),
                LocalTime.of(endHour, 0), DemandLevel.MEDIUM, false));
    }
}
//@@author
