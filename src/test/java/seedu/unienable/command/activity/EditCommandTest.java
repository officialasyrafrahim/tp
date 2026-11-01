package seedu.unienable.command.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.Storage;

//@@author Oliver-koh
/**
 * Checks partial edits, completion preservation, rescheduling and atomic validation.
 */
class EditCommandTest {
    private final Parser parser = new Parser();
    private final ActivityList activities = new ActivityList();
    private final Storage noWrites = new Storage() {
        @Override
        public void save(ActivityList ignored) {
            throw new AssertionError("EditCommand must not persist directly");
        }
    };

    @Test
    void execute_changesOnlySuppliedFieldsAndPreservesCompletion() throws Exception {
        add("Old name", "2030-01-02", "09:00", "10:00", "LOW");
        parser.parse("mark 1").execute(activities, noWrites);

        var result = parser.parse("edit 1 n/New name dem/HIGH").execute(activities, noWrites);

        assertTrue(result.didChangeActivities());
        String message = parser.parse("list").execute(activities, noWrites).getMessage();
        assertTrue(message.contains("1. New name (2030-01-02, 09:00-10:00) [HIGH] [X]"));
    }

    @Test
    void execute_reschedulesFutureActivity() throws Exception {
        add("Meeting", "2030-01-02", "09:00", "10:00", "MEDIUM");

        parser.parse("edit 1 d/2030-01-03 s/14:00 e/15:00").execute(activities, noWrites);

        assertTrue(parser.parse("list").execute(activities, noWrites).getMessage()
                .contains("1. Meeting (2030-01-03, 14:00-15:00) [MEDIUM]"));
    }

    @Test
    void execute_changesOneRatingAndPreservesOtherRatings() throws Exception {
        parser.parse("add n/Meeting d/2030-01-02 s/09:00 e/10:00 imp/5 urg/4 com/2")
                .execute(activities, noWrites);

        parser.parse("edit 1 com/3").execute(activities, noWrites);

        var activity = activities.getByDisplayedIndex(1);
        assertEquals(5, activity.getImportance());
        assertEquals(4, activity.getUrgency());
        assertEquals(3, activity.getCommitment());
    }

    @Test
    void execute_nameOnlyEdit_preservesUnsetRatings() throws Exception {
        add("Original", "2030-01-02", "09:00", "10:00", "LOW");

        parser.parse("edit 1 n/Updated").execute(activities, noWrites);

        Activity updated = activities.getByDisplayedIndex(1);
        assertEquals("Updated", updated.getName());
        assertNull(updated.getImportance());
        assertNull(updated.getUrgency());
        assertNull(updated.getCommitment());
    }

    @TestFactory
    Stream<DynamicTest> execute_singleFieldEdits_preserveEveryOmittedValue() {
        String[][] cases = {
            {"n/Updated", "Updated|2030-01-02|09:00|10:00|LOW|true|2|3|4"},
            {"d/2030-01-03", "Original|2030-01-03|09:00|10:00|LOW|true|2|3|4"},
            {"s/09:30", "Original|2030-01-02|09:30|10:00|LOW|true|2|3|4"},
            {"e/11:00", "Original|2030-01-02|09:00|11:00|LOW|true|2|3|4"},
            {"dem/HIGH", "Original|2030-01-02|09:00|10:00|HIGH|true|2|3|4"},
            {"imp/5", "Original|2030-01-02|09:00|10:00|LOW|true|5|3|4"},
            {"urg/1", "Original|2030-01-02|09:00|10:00|LOW|true|2|1|4"},
            {"com/2", "Original|2030-01-02|09:00|10:00|LOW|true|2|3|2"}
        };
        return Stream.of(cases).map(scenario -> DynamicTest.dynamicTest(scenario[0], () -> {
            Parser timedParser = new Parser(Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), ZoneOffset.UTC));
            Activity original = new Activity("Original", LocalDate.of(2030, 1, 2),
                    LocalTime.of(9, 0), LocalTime.of(10, 0), DemandLevel.LOW, true, 2, 3, 4);
            ActivityList current = new ActivityList(List.of(original));

            var result = timedParser.parse("edit 1 " + scenario[0]).execute(current, noWrites);

            Activity updated = current.getByDisplayedIndex(1);
            String state = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s", updated.getName(), updated.getDate(),
                    updated.getStartTime(), updated.getEndTime(), updated.getDemand(), updated.isDone(),
                    updated.getImportance(), updated.getUrgency(), updated.getCommitment());
            assertEquals(scenario[1], state);
            assertEquals(1, current.getActivities().size());
            assertTrue(result.didChangeActivities());
        }));
    }

    @Test
    void execute_rejectedPartialTimeEdits_preserveAllHistoricalFields() throws Exception {
        Parser timedParser = new Parser(Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), ZoneOffset.UTC));
        Activity original = new Activity("Historical", LocalDate.of(2026, 1, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), DemandLevel.HIGH, true, 5, 4, 3);
        activities.addActivity(original);

        for (String command : List.of("edit 1 s/11:00", "edit 1 e/11:00", "edit 1 d/2026-10-28 e/11:00",
                "edit 1 n/Changed", "edit 1 dem/LOW", "edit 1 imp/1 urg/1 com/1", "edit 1 d/2030-01-01")) {
            UniEnableException exception = assertThrows(UniEnableException.class,
                    () -> timedParser.parse(command).execute(activities, noWrites), command);
            assertEquals("[WARNING] Activities that have ended cannot be edited.", exception.getMessage(), command);
            assertEquals(1, activities.getActivities().size());
            assertSame(original, activities.getByDisplayedIndex(1), command);
            assertEquals(5, original.getImportance());
            assertEquals(4, original.getUrgency());
            assertEquals(3, original.getCommitment());
            assertTrue(original.isDone());
        }
    }

    @Test
    void execute_rejectsInvalidResultWithoutChangingActivity() throws Exception {
        add("Meeting", "2030-01-02", "09:00", "10:00", "MEDIUM");

        assertThrows(UniEnableException.class,
                () -> parser.parse("edit 1 s/14:00 e/13:00").execute(activities, noWrites));

        String message = parser.parse("list").execute(activities, noWrites).getMessage();
        assertTrue(message.contains("1. Meeting (2030-01-02, 09:00-10:00) [MEDIUM] [ ]"));
        assertFalse(message.contains("14:00-13:00"));
    }

    @Test
    void execute_existingEndAtOrBeforeNow_rejectsButOngoingActivityRemainsEditable() throws Exception {
        Parser timedParser = new Parser(Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), ZoneOffset.UTC));
        for (LocalTime end : List.of(LocalTime.of(11, 59), LocalTime.NOON, LocalTime.of(12, 1))) {
            Activity original = new Activity("Boundary", LocalDate.of(2026, 10, 28),
                    LocalTime.of(11, 0), end, DemandLevel.LOW, false);
            ActivityList current = new ActivityList(List.of(original));
            if (end.isAfter(LocalTime.NOON)) {
                timedParser.parse("edit 1 n/Updated").execute(current, noWrites);
                assertEquals("Updated", current.getByDisplayedIndex(1).getName());
            } else {
                UniEnableException exception = assertThrows(UniEnableException.class,
                        () -> timedParser.parse("edit 1 n/Updated").execute(current, noWrites));
                assertEquals("[WARNING] Activities that have ended cannot be edited.", exception.getMessage());
                assertSame(original, current.getByDisplayedIndex(1));
            }
        }
    }

    private void add(String name, String date, String start, String end, String demand)
            throws Exception {
        parser.parse("add n/" + name + " d/" + date + " s/" + start + " e/" + end
                + " dem/" + demand).execute(activities, noWrites);
    }
}
//@@author
