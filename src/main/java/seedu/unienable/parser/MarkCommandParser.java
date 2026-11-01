package seedu.unienable.parser;

import java.util.regex.Pattern;

import seedu.unienable.command.activity.MarkCommand;
import seedu.unienable.command.activity.UnmarkCommand;
import seedu.unienable.command.common.Command;
import seedu.unienable.exception.ParseException;

//@@author Oliver-koh
/**
 * Parses the mark and unmark activity commands.
 */
public class MarkCommandParser {
    private static final String WARNING_MESSAGE = "[WARNING]";
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\d+");

    /**
     * Creates a parser for completion-command syntax.
     */
    public MarkCommandParser() {
    }

    /**
     * Parses {@code mark INDEX}.
     *
     * @param words command words beginning with mark.
     * @return a mark command.
     * @throws ParseException when the syntax is invalid.
     */
    public Command parseMark(String[] words) throws ParseException {
        return parse(words, "mark", true);
    }

    /**
     * Parses {@code unmark INDEX}.
     *
     * @param words command words beginning with unmark.
     * @return an unmark command.
     * @throws ParseException when the syntax is invalid.
     */
    public Command parseUnmark(String[] words) throws ParseException {
        return parse(words, "unmark", false);
    }

    private Command parse(String[] words, String commandName, boolean isMark) throws ParseException {
        String usage = "Usage: " + commandName + " INDEX\nExample: " + commandName + " 1";
        if (words.length != 2 || !INDEX_PATTERN.matcher(words[1]).matches()) {
            throw new ParseException(WARNING_MESSAGE + " " + commandName
                    + " takes exactly one whole-number index.\n" + usage);
        }
        try {
            int index = Integer.parseInt(words[1]);
            return isMark ? new MarkCommand(index) : new UnmarkCommand(index);
        } catch (NumberFormatException exception) {
            throw new ParseException(WARNING_MESSAGE + " Index is too large.\n" + usage);
        }
    }
}
//@@author
