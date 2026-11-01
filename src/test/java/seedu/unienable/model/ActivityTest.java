package seedu.unienable.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.unienable.model.enums.DemandLevel;

/**
 * Tests optional rating values, shared validation and compatibility with existing activity state.
 */
class ActivityTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(10, 0);

    @Test
    void constructor_legacySignature_preservesFieldsAndLeavesRatingsUnset() {
        Activity activity = new Activity("Planning and/or review w/o slides", DATE, START, END,
                DemandLevel.HIGH, true);

        assertEquals("Planning and/or review w/o slides", activity.getName());
        assertEquals(DATE, activity.getDate());
        assertEquals(START, activity.getStartTime());
        assertEquals(END, activity.getEndTime());
        assertEquals(DemandLevel.HIGH, activity.getDemand());
        assertTrue(activity.isDone());
        assertNull(activity.getImportance());
        assertNull(activity.getUrgency());
        assertNull(activity.getCommitment());
    }

    //@@author lukelouyu
    @Test
    void constructor_optionalRatings_preservesEverySetUnsetCombination() {
        Integer[][] combinations = {
            {null, null, null},
            {5, null, null},
            {null, 4, null},
            {null, null, 3},
            {5, 4, null},
            {5, null, 3},
            {null, 4, 3},
            {5, 4, 3}
        };
        for (Integer[] ratings : combinations) {
            Activity activity = new Activity("Meeting", DATE, START, END, DemandLevel.LOW, true,
                    ratings[0], ratings[1], ratings[2]);

            assertEquals(ratings[0], activity.getImportance());
            assertEquals(ratings[1], activity.getUrgency());
            assertEquals(ratings[2], activity.getCommitment());
            assertEquals(DemandLevel.LOW, activity.getDemand());
            assertTrue(activity.isDone());
        }
    }
    //@@author

    //@@author lukelouyu
    @Test
    void constructor_invalidRating_rejectsEachPosition() {
        for (int value : new int[] {-1, 0, 6}) {
            assertThrows(IllegalArgumentException.class, () -> new Activity("Meeting", DATE, START, END,
                    DemandLevel.MEDIUM, false, value, 3, 3), "importance: " + value);
            assertThrows(IllegalArgumentException.class, () -> new Activity("Meeting", DATE, START, END,
                    DemandLevel.MEDIUM, false, 3, value, 3), "urgency: " + value);
            assertThrows(IllegalArgumentException.class, () -> new Activity("Meeting", DATE, START, END,
                    DemandLevel.MEDIUM, false, 3, 3, value), "commitment: " + value);
        }
    }
    //@@author

    //@@author lukelouyu
    @Test
    void validateRating_unsetOrOneToFive_acceptsValue() {
        for (Integer value : new Integer[] {null, 1, 2, 3, 4, 5}) {
            assertDoesNotThrow(() -> Activity.validateRating(value), "rating: " + value);
        }
    }
    //@@author

    //@@author officialasyrafrahim
    @Test
    void getPriorityScore_allRatingsPresent_usesUrgencySquaredTimesRatingSum() {
        Activity incomplete = new Activity("Meeting", DATE, START, END, DemandLevel.MEDIUM, false, 5, 4, 3);
        Activity completed = new Activity("Meeting", DATE, START, END, DemandLevel.HIGH, true, 5, 4, 3);

        assertEquals(Integer.valueOf(128), incomplete.getPriorityScore());
        assertEquals(Integer.valueOf(128), completed.getPriorityScore(),
                "completion must not change the score");
        assertEquals(Integer.valueOf(2), new Activity("Low", DATE, START, END, DemandLevel.LOW, false, 1, 1, 1)
                .getPriorityScore());
        assertEquals(Integer.valueOf(250), new Activity("High", DATE, START, END, DemandLevel.HIGH, false, 5, 5, 5)
                .getPriorityScore());
    }
    //@@author

    //@@author officialasyrafrahim
    @Test
    void getPriorityScore_anyRatingMissing_returnsNull() {
        assertNull(new Activity("A", DATE, START, END, DemandLevel.LOW, false, null, 4, 3).getPriorityScore());
        assertNull(new Activity("B", DATE, START, END, DemandLevel.LOW, false, 5, null, 3).getPriorityScore());
        assertNull(new Activity("C", DATE, START, END, DemandLevel.LOW, false, 5, 4, null).getPriorityScore());
        assertNull(new Activity("D", DATE, START, END, DemandLevel.LOW, false, null, null, null)
                .getPriorityScore());
    }
    //@@author

    //@@author Oliver-koh
    @Test
    void setDone_changesCompletionAndPreservesRatings() {
        Activity activity = new Activity("Meeting", DATE, START, END, DemandLevel.MEDIUM, false, 5, null, 3);
        assertFalse(activity.isDone());

        for (boolean done : new boolean[] {true, false}) {
            activity.setDone(done);

            assertEquals(done, activity.isDone());
            assertEquals(Integer.valueOf(5), activity.getImportance());
            assertNull(activity.getUrgency());
            assertEquals(Integer.valueOf(3), activity.getCommitment());
        }
    }
    //@@author
}
