package seedu.unienable.command.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.unienable.model.ActivityList;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.Storage;

//@@author Oliver-koh
/**
 * Checks case-insensitive activity-name search and preserved chronological indexes.
 */
class FindCommandTest {
    private final Parser parser = new Parser();
    private final ActivityList activities = new ActivityList();
    private final Storage noWrites = new Storage() {
        @Override
        public void save(ActivityList ignored) {
            throw new AssertionError("FindCommand must not persist activities");
        }
    };

    @Test
    void execute_matchesCaseInsensitiveSubstringAndKeepsFullListIndexes() throws Exception {
        add("Planning meeting", "2030-01-01", "09:00", "10:00");
        add("Submit project report", "2030-01-02", "14:00", "15:00");
        add("Project review", "2030-01-03", "14:00", "15:00");

        String message = parser.parse("find PROJECT").execute(activities, noWrites).getMessage();

        assertTrue(message.startsWith("Found 2 matching activities:"));
        assertTrue(message.contains("2. Submit project report"));
        assertTrue(message.contains("3. Project review"));
        assertFalse(message.contains("1. Planning meeting"));
        assertTrue(activities.getActivities().size() == 3);
    }

    @Test
    void execute_noMatchReportsKeywordAndDoesNotChangeState() throws Exception {
        add("Lecture", "2030-01-01", "09:00", "10:00");

        String message = parser.parse("find exam").execute(activities, noWrites).getMessage();

        assertEquals("Sorry, no activity names match your search.", message);
        assertTrue(activities.getActivities().size() == 1);
    }

    private void add(String name, String date, String start, String end) throws Exception {
        parser.parse("add n/" + name + " d/" + date + " s/" + start + " e/" + end)
                .execute(activities, noWrites);
    }
}
//@@author
