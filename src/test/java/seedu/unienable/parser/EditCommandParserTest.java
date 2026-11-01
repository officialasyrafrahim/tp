package seedu.unienable.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.unienable.command.activity.EditCommand;
import seedu.unienable.exception.ParseException;

//@@author Oliver-koh
/**
 * Checks edit syntax errors before any activity state can be changed.
 */
class EditCommandParserTest {
    private final Parser parser = new Parser();

    @TestFactory
    Stream<DynamicTest> parse_nonAsciiOrNonPrintableReplacement_throwsExactNameWarning() {
        String[][] names = {{"Chinese", "复习"}, {"Tamil greeting", "வணக்கம்"}, {"Tamil study", "படி"},
            {"accent", "Café"}, {"emoji", "Study 🎉"}, {"tab", "Study\tGroup"},
            {"NUL", "Bad\u0000Name"}, {"DEL", "Bad\u007fName"}};
        return Stream.of(names).map(scenario -> DynamicTest.dynamicTest(scenario[0], () -> {
            ParseException exception = assertThrows(ParseException.class,
                    () -> parser.parse("edit 1 n/" + scenario[1] + " imp/5"));
            assertEquals("[WARNING] Please write the activity name in English (ASCII characters only).",
                    exception.getMessage());
        }));
    }

    @Test
    void parse_printableAsciiReplacement_acceptsSpacesAndPunctuation() throws Exception {
        for (String name : new String[] {"CS2113 Tutorial", "Planning review", "Lunch without friends",
            "Group-2 (Lab)!", "C++ @ COM1 #2"}) {
            assertInstanceOf(EditCommand.class, parser.parse("edit 1 n/" + name));
        }
    }

    @Test
    void parse_nameDelimiterViolations_preservesSpecificWarning() {
        for (String name : new String[] {"A|B", "A\nB", "A\rB"}) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse("edit 1 n/" + name));
            assertTrue(exception.getMessage().startsWith(
                    "[WARNING] The | character and line breaks are not allowed in command parameters.\nUsage: edit"));
        }
    }

    @Test
    void parse_delimiterInRating_reportsGeneralParameterWarning() {
        ParseException exception = assertThrows(ParseException.class,
                () -> parser.parse("edit 1 imp/1|2"));
        assertTrue(exception.getMessage().startsWith(
                "[WARNING] The | character and line breaks are not allowed in command parameters."));
    }

    @Test
    void parse_mistypedPrefixInName_rejectsWholeEdit() {
        assertThrows(ParseException.class, () -> parser.parse("edit 1 n/Class demm/HIGH"));
        assertThrows(ParseException.class, () -> parser.parse("edit 1 n/Class imp/5 comn/3"));
        ParseException exception = assertThrows(ParseException.class,
                () -> parser.parse("edit 1 n/Class dem1/HIGH imp/5"));
        assertEquals("[WARNING] Unknown parameter 'dem1/'. Did you mean 'dem/'?", exception.getMessage());
    }

    @Test
    void parse_unsupportedLongRatingPrefix_rejectsWholeEdit() {
        ParseException exception = assertThrows(ParseException.class,
                () -> parser.parse("edit 1 n/Updated urgency/5"));
        assertEquals("[WARNING] Unknown parameter 'urgency/'. Did you mean 'urg/'?", exception.getMessage());
    }

    @Test
    void parse_rejectsMissingIndexOrFields() {
        assertThrows(ParseException.class, () -> parser.parse("edit"));
        assertThrows(ParseException.class, () -> parser.parse("edit 1"));
    }

    @Test
    void parse_rejectsDuplicateAndUnknownFields() {
        assertThrows(ParseException.class, () -> parser.parse("edit 1 n/One n/Two"));
        assertThrows(ParseException.class, () -> parser.parse("edit 1 loc/NUS"));
    }

    @Test
    void parse_acceptsFieldsInAnyOrder() throws Exception {
        assertInstanceOf(EditCommand.class,
                parser.parse("edit 1 dem/HIGH e/15:00 n/Updated d/2030-01-03 s/14:00"));
        assertInstanceOf(EditCommand.class, parser.parse("edit\t1\tn/Updated"));
    }

    @TestFactory
    Stream<DynamicTest> parse_malformedFields_warnWithUsage() {
        String[] arguments = {
            "-1 n/Updated", "2147483648 n/Updated", "1 n/", "1 unexpected n/Updated",
            "1 d/2030-2-03", "1 d/2030-02-29", "1 s/9:00", "1 e/24:00",
            "1 dem/EXTREME", "1 imp/0", "1 urg/6", "1 com/5.0"
        };
        return Stream.of(arguments).map(input -> DynamicTest.dynamicTest("edit " + input, () -> {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse("edit " + input));
            assertTrue(exception.getMessage().startsWith("[WARNING]"), input);
            assertTrue(exception.getMessage().contains("Usage: edit INDEX"), input);
        }));
    }
}
//@@author
