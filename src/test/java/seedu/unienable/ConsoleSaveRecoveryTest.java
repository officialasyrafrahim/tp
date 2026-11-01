//@@author Oliver-koh
package seedu.unienable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.unienable.exception.ParseException;
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.ActivityList;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.Storage;
import seedu.unienable.ui.Ui;

/** Checks that failed saves remain visible, can be retried, and cannot be lost through bye. */
class ConsoleSaveRecoveryTest {
    @Test
    void cleanBye_exitsWithoutWritingAgain(@TempDir Path directory) {
        Storage storage = new Storage(directory.resolve("activities.txt")) {
            @Override
            public void save(ActivityList ignored) {
                throw new AssertionError("A clean exit must not write activity data.");
            }
        };
        Ui ui = new Ui(new ByteArrayInputStream("bye\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        assertTrue(ConsoleHelper.processNextCommand(ui, new Parser(), new ActivityList(), storage));
        assertFalse(storage.hasUnsavedChanges());
    }

    @Test
    void failedSave_byeRetriesPersistenceBeforeExiting(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("activities.txt");
        Storage storage = new Storage(file) {
            private boolean shouldFail = true;

            @Override
            public void save(ActivityList activities) throws UniEnableException {
                if (shouldFail) {
                    shouldFail = false;
                    throw new UniEnableException("Unable to save activities: simulated failure");
                }
                super.save(activities);
            }
        };
        ActivityList activities = new ActivityList();
        String input = "add n/Keep me d/2030-12-10 s/09:00 e/10:00\nlist\nbye\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));
        Parser parser = new Parser();

        assertFalse(ConsoleHelper.processNextCommand(ui, parser, activities, storage));
        assertTrue(storage.hasUnsavedChanges());
        assertFalse(Files.exists(file));
        assertFalse(ConsoleHelper.processNextCommand(ui, parser, activities, storage));
        assertFalse(output.toString(StandardCharsets.UTF_8).contains("Goodbye"));

        assertTrue(ConsoleHelper.processNextCommand(ui, parser, activities, storage));
        assertFalse(storage.hasUnsavedChanges());
        assertTrue(Files.readString(file).startsWith("Keep me|"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Goodbye from UniEnable!"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Unsaved activity changes"));
    }

    @Test
    void failedByeRetry_keepsDirtyStateUntilStorageIsRepaired(@TempDir Path directory) throws Exception {
        Path blocked = directory.resolve("blocked");
        Files.writeString(blocked, "not a directory");
        Storage storage = new Storage(blocked.resolve("activities.txt"));
        storage.markUnsavedChanges();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(new ByteArrayInputStream("bye\nlist\nbye\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));
        Parser parser = new Parser();
        ActivityList activities = new ActivityList();
        assertFalse(ConsoleHelper.processNextCommand(ui, parser, activities, storage));
        assertTrue(storage.hasUnsavedChanges());
        assertFalse(output.toString(StandardCharsets.UTF_8).contains("Goodbye"));
        assertFalse(ConsoleHelper.processNextCommand(ui, parser, activities, storage));
        assertTrue(storage.hasUnsavedChanges());
        Files.delete(blocked);
        Files.createDirectory(blocked);
        assertTrue(ConsoleHelper.processNextCommand(ui, parser, activities, storage));
        assertFalse(storage.hasUnsavedChanges());
        assertTrue(Files.exists(blocked.resolve("activities.txt")));
        assertThrows(ParseException.class, () -> parser.parse("save"));
    }
}
//@@author
