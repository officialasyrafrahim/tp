package seedu.unienable;

import java.time.Clock;

//@@author lukelouyu
import seedu.unienable.command.accessibility.connection.logic.ConnectionManager;
import seedu.unienable.command.accessibility.connection.logic.FacilityManager;
import seedu.unienable.command.common.CommandResult;
//@@author
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.Connection;
import seedu.unienable.model.Facility;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.ConnectionStorage;
import seedu.unienable.storage.FacilityStorage;
import seedu.unienable.storage.LoadResult;
import seedu.unienable.storage.Storage;
import seedu.unienable.ui.Ui;

/**
 * Handles reference-data loading and individual command execution for the console.
 */
final class ConsoleHelper {
    //@@author Oliver-koh
    private static final String UNSAVED_NOTICE =
            "[WARNING] Unsaved activity changes. Fix the save error and enter 'bye' to retry saving and exit.";
    //@@author

    /**
     * Prevents instantiation of this stateless helper.
     */
    private ConsoleHelper() {
    }

    //@@author lukelouyu
    /**
     * Loads facilities before connections so connection endpoints can be validated.
     * A connection load failure keeps facility commands available; a facility load failure
     * reports the problem and keeps bootstrap commands available.
     *
     * @param ui user interface for dataset warnings and loading errors.
     * @return parser with available reference data, or a fallback when facilities cannot load.
     */
    static Parser createParser(Ui ui) {
        return createParser(ui, Clock.systemDefaultZone());
    }
    //@@author

    //@@author lukelouyu
    /**
     * Loads reference data and shares the clock with add/edit validation and date/time queries.
     *
     * @param ui user interface for dataset warnings and loading errors.
     * @param clock time source used by add/edit validation and date/time queries.
     * @return parser with available reference data, or a fallback when facilities cannot load.
     */
    static Parser createParser(Ui ui, Clock clock) {
        try {
            LoadResult<Facility> facilities = new FacilityStorage().load();
            for (String warning : facilities.getWarnings()) {
                ui.showMessage("Facility dataset warning: " + warning);
            }

            ConnectionManager connectionManager = null;
            try {
                LoadResult<Connection> connections = new ConnectionStorage(facilities.getRecords()).load();
                for (String warning : connections.getWarnings()) {
                    ui.showMessage("Connection dataset warning: " + warning);
                }
                connectionManager = new ConnectionManager(connections.getRecords());
            } catch (UniEnableException exception) {
                ui.showMessage("Connection dataset unavailable: " + exception.getMessage());
            }

            return new Parser(new FacilityManager(facilities.getRecords()), connectionManager, clock);
        } catch (UniEnableException exception) {
            ui.showMessage("Facility dataset unavailable: " + exception.getMessage());
            return new Parser(clock);
        }
    }
    //@@author

    /**
     * Reads and executes one command, then displays its result or a recoverable warning.
     * A failed command keeps the console running so the user can enter another command.
     *
     * @param ui user interface for command input and output.
     * @param parser parser that recognizes the user's command.
     * @param activities shared activity collection.
     * @param storage activity persistence service.
     * @return true when the command requests exit; false after other commands or warnings.
     */
    static boolean processNextCommand(Ui ui, Parser parser, ActivityList activities, Storage storage) {
        try {
            CommandResult result = parser.parse(ui.readLine()).execute(activities, storage);
            //@@author Oliver-koh
            if (result.didChangeActivities()) {
                storage.markUnsavedChanges();
            }
            if (result.didChangeActivities() || result.isExit() && storage.hasUnsavedChanges()) {
                storage.save(activities);
            }
            if (result.isExit() && storage.hasUnsavedChanges()) {
                ui.showMessage(UNSAVED_NOTICE);
                return false;
            }
            //@@author
            ui.showMessage(result.getMessage());
            showUnsavedNotice(ui, storage);
            return result.isExit();
        } catch (UniEnableException exception) {
            ui.showMessage(exception.getMessage());
            showUnsavedNotice(ui, storage);
            return false;
        }
    }

    //@@author Oliver-koh
    /**
     * Keeps unsaved state visible after queries, warnings, and unsuccessful save retries.
     */
    private static void showUnsavedNotice(Ui ui, Storage storage) {
        if (storage.hasUnsavedChanges()) {
            ui.showMessage(UNSAVED_NOTICE);
        }
    }
    //@@author
}
