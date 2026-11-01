package seedu.unienable.command.activity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.Storage;

//@@author officialasyrafrahim
/**
 * Checks priority scoring, ordering, completed activities, unavailable scores,
 * and full chronological indexes for the list priority command.
 */
class ListPriorityCommandTest {
    private final Parser parser = new Parser();
    private final ActivityList activities = new ActivityList();
    private final Storage noWrites = new Storage() {
        @Override
        public void save(ActivityList ignored) {
            throw new AssertionError("ListPriorityCommand must not persist activities");
        }
    };

    @Test
    void execute_onEmptyList_showsOnlySorryMessage() throws Exception {
        var result = parser.parse("list priority").execute(activities, noWrites);
        assertFalse(result.isExit());
        assertFalse(result.didChangeActivities());
        assertTrue(result.getMessage().equals("Sorry, the activity list is empty."));
        assertTrue(result.getMessage().endsWith("Sorry, the activity list is empty."));
    }

    @Test
    void execute_ordersByScoreThenChronologyAndKeepsCompletedActivities() throws Exception {
        add("Missing", "2026-10-11", "09:00", 5, null, 3, false);
        add("Middle", "2026-10-12", "09:00", 5, 4, 4, false);
        add("Low", "2026-10-13", "09:00", 2, 3, 2, false);
        add("Middle tie", "2026-10-14", "09:00", 5, 4, 4, false);
        add("Top", "2026-10-15", "09:00", 5, 5, 5, true);

        String message = parser.parse("list priority").execute(activities, noWrites).getMessage();

        assertTrue(message.contains("5. Top (2026-10-15, 09:00-10:00) [MEDIUM] [X] "
                + "| Priority 250 (IMP 5, URG 5, COM 5)"), message);
        assertTrue(message.indexOf("2. Middle") < message.indexOf("4. Middle tie"));
        assertTrue(message.indexOf("4. Middle tie") < message.indexOf("3. Low"));
        assertTrue(message.indexOf("3. Low") < message.indexOf("1. Missing"));
        assertTrue(message.contains("1. Missing (2026-10-11, 09:00-10:00) [MEDIUM] [ ] "
                + "| Priority NA (IMP 5, URG -, COM 3)"), message);
    }

    @Test
    void execute_equalScoresAndTimes_keepAddOrder() throws Exception {
        add("First", "2026-10-20", "09:00", 3, 3, 3, false);
        add("Second", "2026-10-20", "09:00", 3, 3, 3, false);

        String message = parser.parse("list priority").execute(activities, noWrites).getMessage();

        assertTrue(message.indexOf("1. First") < message.indexOf("2. Second"), message);
    }

    private void add(String name, String date, String start, Integer importance, Integer urgency,
            Integer commitment, boolean isDone) {
        activities.addActivity(new Activity(name, LocalDate.parse(date), LocalTime.parse(start),
                LocalTime.parse(start).plusHours(1), DemandLevel.MEDIUM, isDone,
                importance, urgency, commitment));
    }
}
//@@author
