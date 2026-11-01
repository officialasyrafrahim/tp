package seedu.unienable.parser;

import java.util.regex.Pattern;

import seedu.unienable.command.activity.DeleteCommand;
import seedu.unienable.command.activity.ListCommand;
import seedu.unienable.command.activity.ListDemandCommand;
import seedu.unienable.command.activity.ListPriorityCommand;
import seedu.unienable.command.common.Command;
import seedu.unienable.exception.ParseException;

//@@author officialasyrafrahim
/**
 * Parses the list, list demand, list priority, and delete commands.
 * Only syntax is validated here: whether an index is a plain ASCII integer without a sign.
 * Whether the index refers to an existing activity is decided by the model at
 * execution time, because the parser has no access to activity state.
 */
public class ListDeleteCommandParser {
    /**
     * Shared prefix for warnings reported by this parser.
     */
    private static final String WARNING_MESSAGE = "[WARNING]";

    /**
     * Matches plain ASCII digits only. A guard is needed before Integer.parseInt,
     * which would otherwise accept a leading plus sign (e.g. "+3").
     */
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\d+");

    /**
     * Creates a parser for activity listing and deletion syntax.
     */
    public ListDeleteCommandParser() {
    }

    /**
     * Recognizes list, list demand, and list priority, case-insensitively.
     *
     * @param words command words, beginning with list.
     * @return the list command to execute.
     * @throws ParseException for unknown variants or unexpected arguments.
     */
    public Command parseList(String[] words) throws ParseException {
        if (words.length == 1) {
            return new ListCommand();
        }
        if ("demand".equalsIgnoreCase(words[1])) {
            if (words.length != 2) {
                throw new ParseException(WARNING_MESSAGE
                        + " list demand does not accept arguments.\nUsage: list demand");
            }
            return new ListDemandCommand();
        }
        if ("priority".equalsIgnoreCase(words[1])) {
            if (words.length != 2) {
                throw new ParseException(WARNING_MESSAGE
                        + " list priority does not accept arguments.\nUsage: list priority");
            }
            return new ListPriorityCommand();
        }
        throw new ParseException(WARNING_MESSAGE + " Unknown list variant '" + words[1] + "'.\n"
                + "Usage: list\n       list demand\n       list priority");
    }

    /**
     * Recognizes delete INDEX, where INDEX contains only ASCII digits and fits in an int.
     * Zero is accepted by the parser and rejected by the model's range check.
     * The index is forwarded unvalidated for range; the model resolves it
     * against the canonical view and reports out-of-range values.
     *
     * @param words command words, beginning with delete.
     * @return the delete command to execute.
     * @throws ParseException for a missing, extra, or non-integer index.
     */
    public Command parseDelete(String[] words) throws ParseException {
        String usage = "Usage: delete INDEX\nExample: delete 1";
        if (words.length == 1) {
            throw new ParseException(WARNING_MESSAGE + " delete needs an activity index.\n" + usage);
        }
        if (words.length != 2) {
            throw new ParseException(WARNING_MESSAGE + " delete takes exactly one index.\n" + usage);
        }
        String token = words[1];
        if (!INDEX_PATTERN.matcher(token).matches()) {
            throw new ParseException(WARNING_MESSAGE + " Invalid index '" + token
                    + "'. Index must be a positive whole number.\n" + usage);
        }
        try {
            return new DeleteCommand(Integer.parseInt(token));
        } catch (NumberFormatException exception) {
            // Syntactically digits, but larger than int can hold.
            throw new ParseException(WARNING_MESSAGE + " Index '" + token + "' is too large.\n" + usage);
        }
    }
}
//@@author
