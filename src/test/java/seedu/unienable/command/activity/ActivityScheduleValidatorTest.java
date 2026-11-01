//@@author lukelouyu
package seedu.unienable.command.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.Storage;

/**
 * Checks duplicate identity, overlap boundaries and atomic add/edit rejection through commands.
 */
class ActivityScheduleValidatorTest {
    private final Parser parser = new Parser();
    private final Storage storage = new Storage();

    @Test
    void explicitOverride_allowsConcurrentActivitiesButNeverDuplicates() throws Exception {
        ActivityList activities = new ActivityList();
        execute(activities, "add n/Lecture d/2030-12-01 s/09:00 e/10:00");
        execute(activities, "add n/Reminder d/2030-12-01 s/09:30 e/10:30 overlap/ALLOW");
        assertEquals(2, activities.getActivities().size());
        assertThrows(UniEnableException.class, () -> execute(activities,
                "add n/Lecture d/2030-12-01 s/09:00 e/10:00 overlap/ALLOW"));
        execute(activities, "edit 2 s/09:15 overlap/allow");
        assertEquals(LocalTime.parse("09:15"), activities.getByDisplayedIndex(2).getStartTime());
        assertThrows(UniEnableException.class, () -> execute(activities, "edit 2 s/09:20"));
        execute(activities, "mark 1");
        execute(activities, "add n/Concurrent d/2030-12-01 s/09:45 e/10:15 overlap/ALLOW");
        assertEquals(3, activities.getActivities().size());
    }

    @Test
    void invalidOrUnaccompaniedOverride_isRejectedBeforeMutation() {
        assertThrows(UniEnableException.class,
                () -> parser.parse("add n/Study d/2030-12-01 s/09:00 e/10:00 overlap/YES"));
        assertThrows(UniEnableException.class, () -> parser.parse("edit 1 overlap/ALLOW"));
        assertThrows(UniEnableException.class, () -> parser.parse("edit 1 n/Study overlap/NO"));
    }

    @Test
    void nameOnlyEdit_preservesApprovedOverlapButRejectsDuplicates() throws Exception {
        ActivityList activities = new ActivityList();
        execute(activities, "add n/Lecture d/2030-12-01 s/10:00 e/12:00");
        execute(activities, "add n/Reminder d/2030-12-01 s/11:00 e/11:15 overlap/ALLOW");
        execute(activities, "edit 2 n/Reminder call Prof Tan");
        execute(activities, "edit 2 s/11:00");
        assertEquals("Reminder call Prof Tan", activities.getByDisplayedIndex(2).getName());
        Activity original = activities.getByDisplayedIndex(2);
        assertThrows(UniEnableException.class, () -> execute(activities, "edit 2 s/11:05"));
        assertSame(original, activities.getByDisplayedIndex(2));
        execute(activities, "add n/Other d/2030-12-01 s/11:00 e/11:15 overlap/ALLOW");
        assertThrows(UniEnableException.class, () -> execute(activities, "edit 2 n/Other"));
        assertSame(original, activities.getByDisplayedIndex(2));
    }

    @Test
    void duplicateIgnoresNameCaseDemandRatingsAndCompletion() throws Exception {
        ActivityList activities = new ActivityList();
        execute(activities, "add n/Study d/2030-12-01 s/10:00 e/11:00");
        execute(activities, "mark 1");
        Activity original = activities.getActivities().getFirst();

        UniEnableException error = assertThrows(UniEnableException.class, () -> execute(activities,
                "add n/study d/2030-12-01 s/10:00 e/11:00 dem/HIGH imp/5 urg/4 com/3"));

        assertTrue(error.getMessage().startsWith("[WARNING] Duplicate activity already recorded at index 1:"));
        assertEquals(List.of(original), activities.getActivities());
        assertTrue(original.isDone());
    }

    @Test
    void repeatedInternalSpaces_cannotBypassDuplicateChecksWithOverlapApproval() throws Exception {
        ActivityList activities = new ActivityList();
        execute(activities, "add n/Study group d/2030-12-01 s/10:00 e/11:00");
        UniEnableException error = assertThrows(UniEnableException.class, () -> execute(activities,
                "add n/STUDY  group d/2030-12-01 s/10:00 e/11:00 overlap/ALLOW"));
        assertTrue(error.getMessage().contains("Duplicate activity"));
        assertEquals("Study group", activities.getByDisplayedIndex(1).getName());
        execute(activities, "add n/Other d/2030-12-01 s/10:00 e/11:00 overlap/ALLOW");
        assertThrows(UniEnableException.class, () -> execute(activities, "edit 2 n/Study   group"));
        assertEquals("Other", activities.getByDisplayedIndex(2).getName());
    }

    @Test
    void allOverlappingRangesAreRejectedWithoutChangingTheList() throws Exception {
        for (String range : List.of("s/10:00 e/11:00", "s/09:30 e/10:30", "s/10:30 e/11:30",
                "s/10:15 e/10:45", "s/09:00 e/12:00", "s/10:00 e/12:00", "s/09:00 e/11:00")) {
            ActivityList activities = new ActivityList(List.of(activity("Study", "10:00", "11:00")));
            List<Activity> original = activities.getActivities();

            UniEnableException error = assertThrows(UniEnableException.class,
                    () -> execute(activities, "add n/Other d/2030-12-01 " + range), range);

            assertTrue(error.getMessage().startsWith("[WARNING] Activity clashes with index 1:"), range);
            assertEquals(original, activities.getActivities(), range);
        }
    }

    @Test
    void adjacentTimesAndDifferentDatesAreAllowed() throws Exception {
        for (String schedule : List.of("d/2030-12-01 s/09:00 e/10:00", "d/2030-12-01 s/11:00 e/12:00",
                "d/2030-12-02 s/10:00 e/11:00")) {
            ActivityList activities = new ActivityList(List.of(activity("Study", "10:00", "11:00")));
            execute(activities, "add n/Study " + schedule);
            assertEquals(2, activities.getActivities().size(), schedule);
        }
    }

    @Test
    void conflictingEditsPreserveEveryFieldAndIgnoreOnlyTheirOwnActivity() throws Exception {
        ActivityList activities = new ActivityList();
        execute(activities, "add n/Study d/2030-12-01 s/10:00 e/11:00");
        execute(activities, "add n/Meeting d/2030-12-01 s/12:00 e/13:00 imp/2");
        execute(activities, "mark 2");
        Activity original = activities.getByDisplayedIndex(2);

        UniEnableException duplicate = assertThrows(UniEnableException.class, () -> execute(activities,
                "edit 2 n/Study s/10:00 e/11:00 imp/5"));
        assertTrue(duplicate.getMessage().contains("Duplicate activity"));
        assertSame(original, activities.getByDisplayedIndex(2));

        UniEnableException overlap = assertThrows(UniEnableException.class, () -> execute(activities,
                "edit 2 n/Changed s/10:30 e/11:30 imp/5"));
        assertTrue(overlap.getMessage().contains("Activity clashes with index 1:"));
        assertSame(original, activities.getByDisplayedIndex(2));

        execute(activities, "edit 2 s/12:00 e/13:00");
        assertEquals(2, activities.getActivities().size());
        assertEquals("Meeting", activities.getByDisplayedIndex(2).getName());
        assertEquals(2, activities.getByDisplayedIndex(2).getImportance());
        assertTrue(activities.getByDisplayedIndex(2).isDone());
    }

    @Test
    void clashUsesChronologicalIndexAndIncludesCompletedActivities() {
        Activity earlier = activity("Earlier", "10:00", "11:00");
        earlier.setDone(true);
        ActivityList activities = new ActivityList(List.of(activity("Later", "14:00", "15:00"), earlier));

        UniEnableException error = assertThrows(UniEnableException.class,
                () -> execute(activities, "add n/Other d/2030-12-01 s/10:30 e/11:30"));

        assertEquals("[WARNING] Activity clashes with index 1: Earlier (2030-12-01, 10:00-11:00)."
                + " Add overlap/ALLOW to keep this intentional overlap.",
                error.getMessage());
        assertEquals(2, activities.getActivities().size());
    }

    @Test
    void existingOverlapsRemainLoadableAndAllowMetadataUpdates() throws Exception {
        ActivityList activities = new ActivityList(List.of(activity("Study", "10:00", "11:00"),
                activity("Meeting", "10:30", "11:30")));

        execute(activities, "edit 1 imp/5 dem/HIGH");

        assertEquals(2, activities.getActivities().size());
        assertEquals(5, activities.getByDisplayedIndex(1).getImportance());
        assertEquals(DemandLevel.HIGH, activities.getByDisplayedIndex(1).getDemand());
    }

    @Test
    void duplicateWarningTakesPrecedenceOverAnotherHistoricalOverlap() {
        ActivityList activities = new ActivityList(List.of(activity("Other", "09:00", "12:00"),
                activity("Study", "10:00", "11:00")));

        UniEnableException error = assertThrows(UniEnableException.class,
                () -> execute(activities, "add n/STUDY d/2030-12-01 s/10:00 e/11:00"));

        assertTrue(error.getMessage().startsWith("[WARNING] Duplicate activity already recorded at index 2:"));
    }

    /**
     * Executes commands without console persistence so failed mutations can be inspected directly.
     */
    private void execute(ActivityList activities, String input) throws UniEnableException {
        parser.parse(input).execute(activities, storage);
    }

    /**
     * Creates a stored activity without invoking new-input schedule validation.
     */
    private static Activity activity(String name, String start, String end) {
        return new Activity(name, LocalDate.of(2030, 12, 1), LocalTime.parse(start), LocalTime.parse(end),
                DemandLevel.MEDIUM, false);
    }
}
//@@author
