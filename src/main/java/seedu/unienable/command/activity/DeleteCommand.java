package seedu.unienable.command.activity;

import java.util.logging.Logger;

//@@author officialasyrafrahim
import seedu.unienable.command.common.Command;
import seedu.unienable.command.common.CommandResult;
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.storage.Storage;

/**
 * Deletes one activity chosen by its 1-based displayed index in the canonical list.
 */
public class DeleteCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(DeleteCommand.class.getName());
    private final int displayedIndex;

    /**
     * Creates a command that will delete one activity when executed.
     *
     * @param displayedIndex 1-based index shown by {@code list}, resolved by the model.
     */
    public DeleteCommand(int displayedIndex) {
        this.displayedIndex = displayedIndex;
    }

    /**
     * Deletes the activity at the displayed index and returns a confirmation message
     * for Ui.showMessage(). An out-of-range index surfaces as an InvalidIndexException,
     * whose message is displayed by ConsoleHelper. Missing activities use normal feedback;
     * invalid indexes retain a warning.
     */
    @Override
    public CommandResult execute(ActivityList activities, Storage storage) throws UniEnableException {
        Activity activity = activities.deleteByDisplayedIndex(displayedIndex);
        int remainingSize = activities.getActivities().size();
        LOGGER.fine(() -> "Deleted displayed index " + displayedIndex + "; "
                + remainingSize + " activities remain.");
        String remainingMessage = remainingSize == 0
                ? ActivityList.NO_ACTIVITIES_MESSAGE
                : "Total activities: " + remainingSize;
        String message = "Deleted activity: " + activity.getName() + "\n"
                + "Date: " + activity.getDate() + "\n"
                + "Time: " + activity.getStartTime() + " - " + activity.getEndTime() + "\n"
                + "Demand: " + activity.getDemand() + "\n"
                + remainingMessage;
        return new CommandResult(message, false, true);
    }
}
//@@author
