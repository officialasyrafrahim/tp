package seedu.unienable.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.unienable.exception.ParseException;

/**
 * Checks strict slash-prefix validation and suggestions for misspelled fields.
 */
class ActivityParameterValidatorTest {
    @Test
    void validate_digitInMisspelledPrefix_reportsSuggestedParameter() {
        String[][] cases = {{"dem1", "dem"}, {"1dem", "dem"}, {"de1", "dem"},
            {"imp2", "imp"}, {"ur3", "urg"}, {"co4", "com"}, {"overlap1", "overlap"}};
        for (String[] scenario : cases) {
            ParseException exception = assertThrows(ParseException.class,
                    () -> ActivityParameterValidator.validate("n/Class " + scenario[0] + "/HIGH"));
            assertEquals("[WARNING] Unknown parameter '" + scenario[0] + "/'. Did you mean '"
                    + scenario[1] + "/'?", exception.getMessage());
        }
    }

    @Test
    void validate_everyUnsupportedSlashToken_isRejectedIncludingTheFirstNameWord() {
        for (String name : new String[] {"Planning and/or review", "Lunch w/o friends", "CS2113/notes",
            "Call Mom/Dad", "Team demo/presentation", "Install url/proxy", "Sem/Exam planning", "/", "A//B"}) {
            assertThrows(ParseException.class, () -> ActivityParameterValidator.validate("n/" + name), name);
        }
    }

    @Test
    void validate_allSupportedPrefixes_acceptsCaseAndRequiresAFieldBoundary() {
        assertDoesNotThrow(() -> ActivityParameterValidator.validate(
                "N/Meeting D/2027-12-10 S/09:00 E/10:00 DEM/HIGH IMP/5 URG/4 COM/3 OVERLAP/ALLOW"));
        assertThrows(ParseException.class, () -> ActivityParameterValidator.validate("n/Name/n/Nested"));
    }
}
