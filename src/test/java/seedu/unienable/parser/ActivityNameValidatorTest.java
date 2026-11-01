package seedu.unienable.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.unienable.exception.ParseException;

/**
 * Checks the shared printable-ASCII input policy independently of field parsing and storage.
 */
class ActivityNameValidatorTest {
    @Test
    void validate_printableAsciiNames_acceptsSpacesAndPunctuation() {
        for (String name : new String[] {"A", "CS2113 Tutorial", "Planning and/or review", "Lunch w/o friends",
            "O'Brien", "'Meeting'", "Group-2 (Lab)!", "C++ @ COM1 #2", " ~ "}) {
            assertDoesNotThrow(() -> ActivityNameValidator.validate(name), name);
        }
    }

    @Test
    void validate_nonAsciiOrNonPrintableNames_throwsExactNameWarning() {
        for (String name : new String[] {"复习", "வணக்கம்", "படி", "Café", "Study 🎉", "Study\tGroup",
            "Bad\u0000Name", "Bad\u001fName", "Bad\u007fName", "Bad\u0080Name", "Study\u2003Group",
            "Bad\u2028Name", "Bad\u2029Name", "Bad\ud800Name", "Bad\udfffName"}) {
            ParseException exception = assertThrows(ParseException.class, () -> ActivityNameValidator.validate(name));
            assertEquals("[WARNING] Please write the activity name in English (ASCII characters only).",
                    exception.getMessage());
        }
    }

    @Test
    void validate_forbiddenPunctuation_throwsSpecificWarning() {
        for (String name : new String[] {"\"Meeting\"", "A\"B", "\"Unclosed", "Trailing\"",
            "A\\B", "A/\\B", "A\\/B"}) {
            ParseException exception = assertThrows(ParseException.class, () -> ActivityNameValidator.validate(name));
            assertEquals("[WARNING] Activity names must not contain backslashes (\\)"
                    + " or double quotation marks.",
                    exception.getMessage());
        }
    }

    @Test
    void validate_blankNames_preservesEmptyParameterWarning() {
        for (String name : new String[] {"", "   ", "\t", "\u00a0", "\u2003", "\u3000", " \u2003\t\u3000 "}) {
            ParseException exception = assertThrows(ParseException.class, () -> ActivityNameValidator.validate(name));
            assertEquals("[WARNING] Empty n/ parameter.", exception.getMessage());
        }
    }
}
