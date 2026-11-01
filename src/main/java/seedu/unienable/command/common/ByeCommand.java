//@@author lukelouyu
package seedu.unienable.command.common;

import seedu.unienable.model.ActivityList;
import seedu.unienable.storage.Storage;

/**
 * Displays the goodbye message and requests that the console loop exit.
 */
public class ByeCommand extends Command {
    /**
     * Creates a command that requests a normal console exit.
     */
    public ByeCommand() {
    }

    /**
     * Returns the goodbye message without changing activities or stored data.
     *
     * @param activities activity collection, not modified by this command.
     * @param storage activity storage, not used by this command.
     * @return command result requesting normal application exit.
     */
    @Override
    public CommandResult execute(ActivityList activities, Storage storage) {
        return new CommandResult("Goodbye from UniEnable!", true);
    }
}
//@@author
