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
 * Checks canonical-order display and read-only behavior for the list command.
 */
class ListCommandTest {
    private final Parser parser = new Parser();
    private final ActivityList activities = new ActivityList();
    private final Storage noWrites = new Storage() {
        @Override
        public void save(ActivityList ignored) {
            throw new AssertionError("ListCommand must not persist activities");
        }
    };

    @Test
    void execute_onEmptyList_showsPlaceholderAndStaysReadOnly() throws Exception {
        var result = parser.parse("list").execute(activities, noWrites);
        assertFalse(result.isExit());
        assertTrue(result.getMessage().endsWith("Sorry, the activity list is empty."));
        assertTrue(activities.getActivities().isEmpty());
    }

    @Test
    void execute_ordersActivitiesByDateThenStartTime() throws Exception {
        add("Lab", "2026-10-13", "14:00", "16:00", "MEDIUM");
        add("Gym", "2026-10-12", "07:00", "09:00", "HIGH");
        String message = parser.parse("list").execute(activities, noWrites).getMessage();
        assertTrue(message.contains("1. Gym (2026-10-12, 07:00-09:00) [HIGH]"));
        assertTrue(message.contains("2. Lab (2026-10-13, 14:00-16:00) [MEDIUM]"));
        assertTrue(message.indexOf("Gym") < message.indexOf("Lab"),
                "displayed order must be canonical, not insertion order");
    }

    @Test
    void execute_keepsAddOrderForIdenticalDateAndStartTime() throws Exception {
        add("Alpha", "2026-10-12", "09:00", "10:00", "LOW");
        add("Beta", "2026-10-12", "09:00", "10:00", "LOW");
        String message = parser.parse("list").execute(activities, noWrites).getMessage();
        assertTrue(message.indexOf("1. Alpha") < message.indexOf("2. Beta"),
                "stable sort must break date and time ties by insertion order");
    }

    @Test
    void execute_listsSingleActivityWithIndexOne() throws Exception {
        add("Solo", "2026-10-12", "08:00", "09:00", "LOW");
        String message = parser.parse("list").execute(activities, noWrites).getMessage();
        assertTrue(message.contains("Activities (by date, start time):"));
        assertTrue(message.contains("1. Solo (2026-10-12, 08:00-09:00) [LOW]"));
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
