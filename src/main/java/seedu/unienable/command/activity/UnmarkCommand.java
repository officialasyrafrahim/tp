package seedu.unienable.command.activity;

//@@author Oliver-koh
import seedu.unienable.command.common.Command;
import seedu.unienable.command.common.CommandResult;
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.storage.Storage;

/**
 * Marks one chronologically displayed activity as incomplete.
 */
public class UnmarkCommand extends Command {
    private final int displayedIndex;

    /**
     * Creates an unmark command.
     *
     * @param displayedIndex one-based index shown by {@code list}.
     */
    public UnmarkCommand(int displayedIndex) {
        this.displayedIndex = displayedIndex;
    }

    /**
     * Marks the selected activity as incomplete and reports whether its state changed.
     *
     * @param activities shared activity collection.
     * @param storage activity storage, not used directly.
     * @return command result for the console.
     * @throws UniEnableException when the displayed index is invalid.
     */
    @Override
    public CommandResult execute(ActivityList activities, Storage storage) throws UniEnableException {
        Activity activity = activities.getByDisplayedIndex(displayedIndex);
        boolean isChanged = activities.setDoneAtDisplayedIndex(displayedIndex, false);
        String message = isChanged
                ? "Unmarked activity: " + activity.getName()
                : "[WARNING] Activity is already unmarked: " + activity.getName();
        return new CommandResult(message, false, isChanged);
    }
}
//@@author
