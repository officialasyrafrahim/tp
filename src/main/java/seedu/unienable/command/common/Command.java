//@@author lukelouyu
package seedu.unienable.command.common;

import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.ActivityList;
import seedu.unienable.storage.Storage;

/**
 * Common execution contract for commands introduced by feature owners.
 */
public abstract class Command {
    /**
     * Initializes the shared base for concrete command implementations.
     */
    protected Command() {
    }

    /**
     * Executes against the shared activity collection and persistence service.
     *
     * @param activities shared activity collection used by the command.
     * @param storage activity persistence service available to the command.
     * @return user-facing feedback and flags for exit and activity changes.
     * @throws UniEnableException if the command cannot execute successfully.
     */
    public abstract CommandResult execute(ActivityList activities, Storage storage) throws UniEnableException;
}
//@@author
