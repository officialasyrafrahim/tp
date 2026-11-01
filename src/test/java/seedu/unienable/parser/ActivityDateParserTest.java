//@@author lukelouyu
package seedu.unienable.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

import seedu.unienable.exception.ParseException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;
import seedu.unienable.storage.Storage;

/**
 * Checks the values accepted by d/, on their own and through the add and edit commands.
 */
class ActivityDateParserTest {
    private static final ZoneOffset SINGAPORE = ZoneOffset.ofHours(8);
    private static final LocalDate TODAY = LocalDate.parse("2026-11-01");
    private static final LocalDate TOMORROW = LocalDate.parse("2026-11-02");

    /**
     * Fixes now at noon on 1 November 2026 for repeatable date words.
     */
    private static final Clock NOON_TODAY = clockAt("2026-11-01T12:00:00");

    private final Parser parser = new Parser(NOON_TODAY);
    private final ActivityList activities = new ActivityList();

    @Test
    void parse_today_returnsCurrentDate() {
        assertEquals(TODAY, ActivityDateParser.parse("today", NOON_TODAY));
    }

    @Test
    void parse_tomorrow_returnsNextDate() {
        assertEquals(TOMORROW, ActivityDateParser.parse("tomorrow", NOON_TODAY));
    }

    @Test
    void parse_tomorrowOnLastDayOfYear_movesToNextYear() {
        Clock lastMinuteOfYear = clockAt("2026-12-31T23:59:00");
        assertEquals(LocalDate.parse("2027-01-01"), ActivityDateParser.parse("tomorrow", lastMinuteOfYear));
    }

    @Test
    void parse_earlyMorning_usesLocalDateNotUtcDate() {
        // 00:30 on 1 November in Singapore is still 31 October in UTC.
        assertEquals(TODAY, ActivityDateParser.parse("today", clockAt("2026-11-01T00:30:00")));
    }

    @Test
    void parse_dateWords_ignoreLetterCase() {
        assertEquals(TODAY, ActivityDateParser.parse("TODAY", NOON_TODAY));
        assertEquals(TOMORROW, ActivityDateParser.parse("ToMorrow", NOON_TODAY));
    }

    @Test
    void parse_calendarDate_returnsThatDate() {
        assertEquals(LocalDate.parse("2030-12-10"), ActivityDateParser.parse("2030-12-10", NOON_TODAY));
    }

    @Test
    void parse_unsupportedValues_throwDateTimeParseException() {
        for (String value : new String[] {"yesterday", "todayy", "to day", "next week", "2026-2-03", "2026-02-30"}) {
            assertThrows(DateTimeParseException.class, () -> ActivityDateParser.parse(value, NOON_TODAY), value);
        }
    }

    @Test
    void add_birthdayPartyToday_recordsCurrentDate() throws Exception {
        parser.parse("add n/Manasi's birthday party d/today s/19:00 e/22:30 dem/low imp/4 com/4 urg/2")
                .execute(activities, new Storage());

        Activity party = activities.getByDisplayedIndex(1);
        assertEquals("Manasi's birthday party", party.getName());
        assertEquals(TODAY, party.getDate());
        assertEquals(LocalTime.parse("19:00"), party.getStartTime());
        assertEquals(LocalTime.parse("22:30"), party.getEndTime());
        assertEquals(DemandLevel.LOW, party.getDemand());
        assertEquals(4, party.getImportance());
        assertEquals(4, party.getCommitment());
        assertEquals(2, party.getUrgency());
    }

    @Test
    void add_fieldTripTomorrow_recordsNextDate() throws Exception {
        parser.parse("add n/TCM Field Trip at Thong Chai Hospital d/tomorrow s/14:00 e/17:00 dem/high "
                + "imp/5 com/2 urg/3").execute(activities, new Storage());

        Activity fieldTrip = activities.getByDisplayedIndex(1);
        assertEquals("TCM Field Trip at Thong Chai Hospital", fieldTrip.getName());
        assertEquals(TOMORROW, fieldTrip.getDate());
        assertEquals(LocalTime.parse("14:00"), fieldTrip.getStartTime());
        assertEquals(LocalTime.parse("17:00"), fieldTrip.getEndTime());
        assertEquals(DemandLevel.HIGH, fieldTrip.getDemand());
        assertEquals(5, fieldTrip.getImportance());
        assertEquals(2, fieldTrip.getCommitment());
        assertEquals(3, fieldTrip.getUrgency());
    }

    @Test
    void add_todayWithEndAlreadyPassed_isRejected() {
        Parser lateAtNight = new Parser(clockAt("2026-11-01T23:00:00"));
        ParseException exception = assertThrows(ParseException.class,
                () -> lateAtNight.parse("add n/Manasi's birthday party d/today s/19:00 e/22:30"));
        assertTrue(exception.getMessage().contains("Activities that have ended cannot be added."));
    }

    @Test
    void edit_dateWords_rescheduleActivity() throws Exception {
        parser.parse("add n/Revision d/2026-11-20 s/14:00 e/15:00").execute(activities, new Storage());

        parser.parse("edit 1 d/tomorrow").execute(activities, new Storage());
        assertEquals(TOMORROW, activities.getByDisplayedIndex(1).getDate());

        parser.parse("edit 1 d/today").execute(activities, new Storage());
        assertEquals(TODAY, activities.getByDisplayedIndex(1).getDate());
    }

    @Test
    void addAndEdit_unknownDateWord_warnWithAcceptedForms() {
        String warning = "[WARNING] Invalid date. Use a real YYYY-MM-DD date, today, or tomorrow.\nUsage: ";
        ParseException addError = assertThrows(ParseException.class,
                () -> parser.parse("add n/Outing d/yesterday s/09:00 e/10:00"));
        assertTrue(addError.getMessage().startsWith(warning + "add n/NAME d/YYYY-MM-DD|today|tomorrow"));

        ParseException editError = assertThrows(ParseException.class, () -> parser.parse("edit 1 d/yesterday"));
        assertTrue(editError.getMessage().startsWith(warning + "edit INDEX [n/NAME] [d/YYYY-MM-DD|today|tomorrow]"));
    }

    private static Clock clockAt(String localDateTime) {
        return Clock.fixed(LocalDateTime.parse(localDateTime).toInstant(SINGAPORE), SINGAPORE);
    }
}
//@@author
