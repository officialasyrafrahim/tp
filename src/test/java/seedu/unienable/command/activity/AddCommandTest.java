package seedu.unienable.command.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

//@@author lukelouyu
import seedu.unienable.command.common.CommandResult;
//@@author
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;
import seedu.unienable.storage.Storage;

//@@author Atharva005
/**
 * Tests that AddCommand changes state before reporting success.
 */
class AddCommandTest {
    @Test
    void execute_twoNonOverlappingAdds_updatesActivityListAndDisplaysCount() throws Exception {
        ActivityList activities = new ActivityList();
        AddCommand command = new AddCommand("Team Meeting", LocalDate.of(2026, 10, 20),
                LocalTime.of(9, 0), LocalTime.of(10, 0), DemandLevel.MEDIUM, null, null, null);

        CommandResult firstResult = command.execute(activities, new Storage());
        CommandResult secondResult = new AddCommand("Team Meeting", LocalDate.of(2026, 10, 20),
                LocalTime.of(10, 0), LocalTime.of(11, 0), DemandLevel.MEDIUM, null, null, null)
                .execute(activities, new Storage());

        assertEquals(2, activities.getActivities().size());
        assertEquals("Team Meeting", activities.getActivities().get(0).getName());
        assertFalse(activities.getActivities().get(0).isDone());
        assertEquals("Added activity: Team Meeting\nDate: 2026-10-20\nTime: 09:00 - 10:00\n"
                + "Demand: MEDIUM\nImportance: -\nUrgency: -\nCommitment: -\nTotal activities: 1",
                firstResult.getMessage());
        assertTrue(secondResult.getMessage().contains("Total activities: 2"));
        assertFalse(firstResult.isExit());
        assertTrue(firstResult.didChangeActivities());
    }

    //@@author
    //@@author lukelouyu
    @Test
    void execute_ratedActivity_preservesRatingsAndConfirmsTheirValues() throws Exception {
        Integer[][] ratings = {{5, null, 3}, {1, 5, 2}};
        String[] expectedRatings = {
            "Importance: 5\nUrgency: -\nCommitment: 3",
            "Importance: 1\nUrgency: 5\nCommitment: 2"
        };
        for (int i = 0; i < ratings.length; i++) {
            ActivityList activities = new ActivityList();
            AddCommand command = new AddCommand("Meeting", LocalDate.of(2026, 10, 20),
                    LocalTime.of(9, 0), LocalTime.of(10, 0), DemandLevel.HIGH,
                    ratings[i][0], ratings[i][1], ratings[i][2]);

            CommandResult result = command.execute(activities, new Storage());

            Activity added = activities.getActivities().getFirst();
            assertEquals(ratings[i][0], added.getImportance());
            assertEquals(ratings[i][1], added.getUrgency());
            assertEquals(ratings[i][2], added.getCommitment());
            assertFalse(added.isDone());
            assertEquals("Added activity: Meeting\nDate: 2026-10-20\nTime: 09:00 - 10:00\nDemand: HIGH\n"
                    + expectedRatings[i] + "\nTotal activities: 1", result.getMessage());
            assertTrue(result.didChangeActivities());
        }
    }
    //@@author
    //@@author Atharva005
}
//@@author
