package seedu.unienable;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;

import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.ActivityList;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.Storage;
import seedu.unienable.ui.Ui;

/**
 * Runs the UniEnable console until bye or end of input.
 */
public final class UniEnable {
    /**
     * Prevents construction of this application entry-point utility.
     */
    private UniEnable() {
    }

    //@@author Atharva005
    /**
     * Starts the application and loads saved activity and facility data.
     *
     * @param args unused command-line arguments.
     */
    public static void main(String[] args) {
        // Windows redirected stdout can use a legacy encoding even when the JVM default is UTF-8.
        PrintStream output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        run(System.in, output, Path.of("data", "activities.txt"));
    }
    //@@author

    /**
     * Runs a session with an injectable activity file for isolated integration tests.
     *
     * @param input console input.
     * @param output console output.
     * @param activityFile activity persistence file.
     */
    static void run(InputStream input, PrintStream output, Path activityFile) {
        run(input, output, activityFile, Clock.systemDefaultZone());
    }

    /**
     * Runs a session with an injectable activity file and clock for deterministic integration tests.
     *
     * @param input console input.
     * @param output console output.
     * @param activityFile activity persistence file.
     * @param clock time source used by add/edit validation and date/time queries.
     */
    static void run(InputStream input, PrintStream output, Path activityFile, Clock clock) {
        Ui ui = new Ui(input, output);
        //@@author Oliver-koh
        Storage storage = new Storage(activityFile);
        ActivityList activities;
        try {
            activities = storage.load();
            for (String warning : storage.getWarnings()) {
                ui.showMessage("Activity data warning: " + warning);
            }
        } catch (UniEnableException exception) {
            ui.showMessage("Activity data unavailable: " + exception.getMessage());
            activities = new ActivityList();
        }
        //@@author
        Parser parser = ConsoleHelper.createParser(ui, clock);

        ui.showWelcome();
        while (ui.hasNextLine()) {
            if (ConsoleHelper.processNextCommand(ui, parser, activities, storage)) {
                break;
            }
        }
    }
}
