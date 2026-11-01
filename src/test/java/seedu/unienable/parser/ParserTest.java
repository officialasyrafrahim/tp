//@@author lukelouyu
package seedu.unienable.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.unienable.exception.ParseException;

/**
 * Checks command-language warnings without replacing command-specific input validation.
 */
class ParserTest {
    private final Parser parser = new Parser();

    @Test
    void forbiddenNamePunctuation_keepsNameWarning() {
        for (String name : List.of("\"Discuss notes\"", "\"Unclosed", "A\"B", "Trailing\"",
                "A\\B")) {
            for (String input : List.of("add n/" + name + " d/2030-12-10 s/09:00 e/10:00",
                    "edit 1 n/" + name + " imp/5")) {
                ParseException exception = assertThrows(ParseException.class, () -> parser.parse(input));
                assertEquals("[WARNING] Activity names must not contain backslashes (\\)"
                        + " or double quotation marks.",
                        exception.getMessage());
            }
        }
    }

    @Test
    void punctuationInOtherFields_reportsThatField() {
        String[][] cases = {{"imp/1,5", "imp/ rating must be a whole number"},
            {"urg/'3'", "urg/ rating must be a whole number"},
            {"com/3\\4", "com/ rating must be a whole number"},
            {"d/'2030-12-10'", "Invalid date"}, {"s/\"09:00\"", "Invalid time"}};
        for (String[] scenario : cases) {
            String base = "n/Meeting d/2030-12-10 s/09:00 e/10:00";
            String prefix = scenario[0].substring(0, scenario[0].indexOf('/'));
            if (prefix.equals("d")) {
                base = base.replace("d/2030-12-10", scenario[0]);
            } else if (prefix.equals("s")) {
                base = base.replace("s/09:00", scenario[0]);
            } else {
                base += " " + scenario[0];
            }
            for (String command : List.of("add " + base, "edit 1 " + base)) {
                ParseException error = assertThrows(ParseException.class, () -> parser.parse(command));
                org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains(scenario[1]),
                        error.getMessage());
            }
        }
    }

    @Test
    void nonAsciiCommandWords_showEnglishOnlyWarning() {
        for (String input : List.of("加入", "加入 extra", "வணக்கம்", "படி", "café", "🎉", "he李lp", "lİst")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertEquals("[WARNING] Please write the command in English (ASCII characters only).",
                    error.getMessage(), input);
        }
    }

    @Test
    void unknownAsciiAndBlankCommands_keepUnrecognizedWarning() {
        for (String input : List.of("bogus", "", " \t ")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertEquals("[WARNING] Unrecognized command. Type help to see available commands.",
                    error.getMessage(), input);
        }
    }

    @Test
    void byeWithArguments_explainsTheRecognizedCommandSyntax() {
        ParseException error = assertThrows(ParseException.class, () -> parser.parse("bye now"));
        assertEquals("[WARNING] bye takes no arguments. Usage: bye", error.getMessage());
    }

    @Test
    void unknownPrefixes_reportTheTokenInsteadOfAnUnrelatedFieldError() {
        for (String input : List.of("add n/Y d/2027-12-02 s/11:00 e/12:00 t/tag",
                "edit 1 x/1", "edit 1 e/12:00 t/tag")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input), input);
            String prefix = input.contains("x/1") ? "x" : "t";
            org.junit.jupiter.api.Assertions.assertTrue(
                    error.getMessage().startsWith("[WARNING] Unknown parameter '" + prefix + "/'."),
                    error.getMessage());
            org.junit.jupiter.api.Assertions.assertFalse(error.getMessage().contains("Invalid time"));
            org.junit.jupiter.api.Assertions.assertFalse(error.getMessage().contains("At least one field"));
        }
    }

    @Test
    void nonAsciiActivityNames_showNameWarningInsteadOfCommandWarning() {
        for (String input : List.of("add n/复习 d/2030-12-12 s/09:00 e/10:00", "edit 1 n/படி")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertEquals("[WARNING] Please write the activity name in English (ASCII characters only).",
                    error.getMessage(), input);
        }
    }

    @Test
    void invalidUnicodeNames_keepDataAndConsoleDelimitersSafe() {
        for (String name : List.of("Bad\u0080name", "Bad\ud800name", "Bad\u2028name", "\u00a0")) {
            assertThrows(ParseException.class, () -> parser.parse("edit 1 n/" + name));
        }
    }
}
//@@author
