package seedu.unienable.command.activity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import seedu.unienable.exception.InvalidIndexException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;
import seedu.unienable.storage.Storage;

//@@author Oliver-koh
/**
 * Tests marking activities through the canonical displayed-index contract.
 */
class MarkCommandTest {
    @Test
    void execute_marksChronologicallySelectedActivity() throws Exception {
        Activity late = activity("Late", 20, 14, false);
        Activity early = activity("Early", 19, 9, false);
        ActivityList activities = new ActivityList(List.of(late, early));

        var result = new MarkCommand(1).execute(activities, new Storage());

        assertTrue(early.isDone());
        assertFalse(late.isDone());
        assertTrue(result.didChangeActivities());
        assertTrue(result.getMessage().contains("Early"));
    }

    @Test
    void execute_doesNotReportAChangeWhenAlreadyMarked() throws Exception {
        Activity activity = activity("Lecture", 20, 9, true);
        ActivityList activities = new ActivityList(List.of(activity));

        var result = new MarkCommand(1).execute(activities, new Storage());

        assertFalse(result.didChangeActivities());
        assertTrue(result.getMessage().startsWith("[WARNING] Activity is already marked"));
        assertTrue(result.getMessage().contains("already marked"));
    }

    @Test
    void execute_rejectsAnInvalidIndexWithoutChangingTheList() {
        ActivityList activities = new ActivityList();

        assertThrows(InvalidIndexException.class,
                () -> new MarkCommand(1).execute(activities, new Storage()));
        assertTrue(activities.getActivities().isEmpty());
    }

    private static Activity activity(String name, int day, int hour, boolean isDone) {
        return new Activity(name, LocalDate.of(2026, 10, day), LocalTime.of(hour, 0),
                LocalTime.of(hour + 1, 0), DemandLevel.MEDIUM, isDone);
    }
}
//@@author
