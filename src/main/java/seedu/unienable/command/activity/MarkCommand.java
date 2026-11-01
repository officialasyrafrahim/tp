package seedu.unienable.command.activity;

//@@author Oliver-koh
import seedu.unienable.command.common.Command;
import seedu.unienable.command.common.CommandResult;
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.storage.Storage;

/**
 * Marks one chronologically displayed activity as completed.
 */
public class MarkCommand extends Command {
    private final int displayedIndex;

    /**
     * Creates a mark command.
     *
     * @param displayedIndex one-based index shown by {@code list}.
     */
    public MarkCommand(int displayedIndex) {
        this.displayedIndex = displayedIndex;
    }

    /**
     * Marks the selected activity and reports whether its state changed.
     *
     * @param activities shared activity collection.
     * @param storage activity storage, not used directly.
     * @return command result for the console.
     * @throws UniEnableException when the displayed index is invalid.
     */
    @Override
    public CommandResult execute(ActivityList activities, Storage storage) throws UniEnableException {
        Activity activity = activities.getByDisplayedIndex(displayedIndex);
        boolean isChanged = activities.setDoneAtDisplayedIndex(displayedIndex, true);
        String message = isChanged
                ? "Marked activity as done: " + activity.getName()
                : "[WARNING] Activity is already marked as done: " + activity.getName();
        return new CommandResult(message, false, isChanged);
    }
}
//@@author
