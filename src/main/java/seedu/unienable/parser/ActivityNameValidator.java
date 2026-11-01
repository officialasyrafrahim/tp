//@@author lukelouyu
package seedu.unienable.parser;

import seedu.unienable.exception.ParseException;

/**
 * Enforces printable-ASCII names supplied through add and edit.
 * Historical names loaded from storage are not validated here.
 */
final class ActivityNameValidator {
    /**
     * Prevents instantiation of this shared validation helper.
     */
    private ActivityNameValidator() {
    }

    /**
     * Rejects forbidden punctuation in an extracted activity name.
     * Slash escaping and quoted-name syntax are not supported.
     *
     * @param input extracted activity name.
     * @throws ParseException if a backslash or double quotation mark is present.
     */
    static void rejectForbiddenPunctuation(String input) throws ParseException {
        if (input.indexOf('"') >= 0
                || input.indexOf('\\') >= 0) {
            throw new ParseException("[WARNING] Activity names must not contain backslashes (\\)"
                    + " or double quotation marks.");
        }
    }

    /**
     * Rejects blank names, forbidden punctuation and characters outside printable ASCII before data changes.
     *
     * @param name non-null name supplied by the user.
     * @throws ParseException if the name is blank, contains forbidden punctuation, or is not printable ASCII.
     */
    static void validate(String name) throws ParseException {
        rejectForbiddenPunctuation(name);
        if (name.codePoints().allMatch(point -> Character.isWhitespace(point) || Character.isSpaceChar(point))) {
            throw new ParseException("[WARNING] Empty n/ parameter.");
        }
        if (name.chars().anyMatch(character -> character < 0x20 || character > 0x7e)) {
            throw new ParseException("[WARNING] Please write the activity name in English (ASCII characters only).");
        }
    }
}
//@@author
