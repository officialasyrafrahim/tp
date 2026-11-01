package seedu.unienable.command.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Checks demand-order display and read-only behavior for the list demand command.
 */
class ListDemandCommandTest {
    private final Parser parser = new Parser();
    private final ActivityList activities = new ActivityList();
    private final Storage noWrites = new Storage() {
        @Override
        public void save(ActivityList ignored) {
            throw new AssertionError("ListDemandCommand must not persist activities");
        }
    };

    @Test
    void execute_onEmptyList_showsOnlySorryMessage() throws Exception {
        var result = parser.parse("list demand").execute(activities, noWrites);
        assertFalse(result.isExit());
        assertTrue(result.getMessage().equals("Sorry, the activity list is empty."));
        assertTrue(result.getMessage().endsWith("Sorry, the activity list is empty."));
    }

    @Test
    void execute_ordersByDemandHighToLow() throws Exception {
        add("Lab", "2026-10-13", "14:00", "16:00", "MEDIUM");
        add("Exam", "2026-10-15", "09:00", "11:00", "HIGH");
        add("Run", "2026-10-12", "07:00", "08:00", "LOW");
        String message = parser.parse("list demand").execute(activities, noWrites).getMessage();
        assertTrue(message.contains("3. Exam (2026-10-15, 09:00-11:00) [HIGH]"));
        assertTrue(message.contains("2. Lab (2026-10-13, 14:00-16:00) [MEDIUM]"));
        assertTrue(message.contains("1. Run (2026-10-12, 07:00-08:00) [LOW]"));
        assertTrue(message.indexOf("3. Exam") < message.indexOf("2. Lab"));
        assertTrue(message.indexOf("2. Lab") < message.indexOf("1. Run"));
    }

    @Test
    void execute_keepsCanonicalOrderWithinSameDemand() throws Exception {
        add("Late", "2026-10-14", "09:00", "10:00", "HIGH");
        add("Run", "2026-10-12", "07:00", "08:00", "LOW");
        add("Early", "2026-10-12", "09:00", "10:00", "HIGH");
        String message = parser.parse("list demand").execute(activities, noWrites).getMessage();
        assertTrue(message.indexOf("2. Early") < message.indexOf("3. Late"),
                "within a demand group, canonical order (by date) applies, not add order");
        assertTrue(message.indexOf("3. Late") < message.indexOf("1. Run"));
    }

    @Test
    void execute_displayedIndexes_targetSameActivityAcrossChanges() throws Exception {
        add("Earlier low", "2026-12-01", "09:00", "10:00", "LOW");
        add("Later high", "2026-12-02", "09:00", "10:00", "HIGH");
        var original = activities.getActivities();

        int highIndex = demandIndexFor("Later high");
        assertEquals(original, activities.getActivities());
        parser.parse("mark " + highIndex).execute(activities, noWrites);
        assertFalse(activities.getByDisplayedIndex(1).isDone());
        assertTrue(activities.getByDisplayedIndex(2).isDone());

        parser.parse("unmark " + demandIndexFor("Later high")).execute(activities, noWrites);
        assertFalse(activities.getByDisplayedIndex(2).isDone());

        parser.parse("edit " + demandIndexFor("Later high") + " n/Later updated").execute(activities, noWrites);
        assertEquals("Earlier low", activities.getByDisplayedIndex(1).getName());
        assertEquals("Later updated", activities.getByDisplayedIndex(2).getName());

        parser.parse("delete " + demandIndexFor("Later updated")).execute(activities, noWrites);
        assertEquals(1, activities.getActivities().size());
        assertEquals("Earlier low", activities.getByDisplayedIndex(1).getName());
        assertEquals(1, demandIndexFor("Earlier low"));
    }

    /**
     * Reads the index a user sees beside a named activity in the demand view.
     */
    private int demandIndexFor(String name) throws Exception {
        var result = parser.parse("list demand").execute(activities, noWrites);
        assertFalse(result.didChangeActivities());
        String row = result.getMessage().lines()
                .filter(line -> line.contains(". " + name + " ("))
                .findFirst().orElseThrow();
        return Integer.parseInt(row.substring(0, row.indexOf('.')));
    }

    /**
     * Seeds existing activities without applying the add parser's current-time restriction.
     */
    private void add(String name, String date, String start, String end, String demand) {
        activities.addActivity(new Activity(name, LocalDate.parse(date), LocalTime.parse(start),
                LocalTime.parse(end), DemandLevel.valueOf(demand), false));
    }
}
//@@author
