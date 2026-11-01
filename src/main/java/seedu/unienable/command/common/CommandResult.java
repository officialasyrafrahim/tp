//@@author lukelouyu
package seedu.unienable.command.common;

/**
 * Carries user-facing feedback and tells the console whether to exit or save activity changes.
 */
public class CommandResult {
    private final String message;
    private final boolean isExit;
    private final boolean didChangeActivities;

    /**
     * Creates a result for a command that does not change activity data.
     *
     * @param message user-facing result message.
     * @param isExit whether the console loop should exit.
     */
    public CommandResult(String message, boolean isExit) {
        this(message, isExit, false);
    }

    /**
     * Creates a command result with an explicit activity-mutation flag.
     *
     * @param message user-facing result message.
     * @param isExit whether the console loop should exit.
     * @param didChangeActivities whether activity data was changed successfully.
     */
    public CommandResult(String message, boolean isExit, boolean didChangeActivities) {
        this.message = message;
        this.isExit = isExit;
        this.didChangeActivities = didChangeActivities;
    }

    /**
     * Returns the complete feedback to display for this command.
     *
     * @return the user-facing command message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns whether the command requests that the console session end.
     *
     * @return true for an exit request.
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * Indicates whether the command changed activity data and should be persisted.
     *
     * @return true when activity data changed.
     */
    public boolean didChangeActivities() {
        return didChangeActivities;
    }
}
//@@author
