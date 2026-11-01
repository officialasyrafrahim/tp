//@@author Atharva005
package seedu.unienable.command.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

//@@author
//@@author lukelouyu
import seedu.unienable.command.common.CommandResult;
//@@author
//@@author Atharva005
import seedu.unienable.exception.ParseException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.Storage;

/**
 * Verifies empty calendar warnings, date filtering, and aligned console rating columns.
 */
class ViewCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(10, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-10-25T04:00:00Z"), ZoneId.of("Asia/Singapore"));

    private final Parser parser = new Parser(FIXED_CLOCK);
    private final Storage noWrites = new Storage() {
        @Override
        public void save(ActivityList ignored) {
            throw new AssertionError("View commands must not persist activities");
        }
    };

    @Test
    void scheduleRows_alignRatingsForDifferentNameAndIndexLengths() throws Exception {
        ActivityList activities = new ActivityList();
        for (int i = 0; i < 12; i++) {
            activities.addActivity(new Activity(i == 0 ? "Short" : "A longer activity " + i,
                    TODAY, START, END, DemandLevel.LOW, false));
        }
        String message = parser.parse("view today").execute(activities, noWrites).getMessage();
        List<String> rows = message.lines().filter(line -> line.contains("    IMP ")).toList();
        assertEquals(12, rows.size());
        int ratingColumn = rows.getFirst().indexOf("IMP");
        for (String row : rows) {
            assertEquals(ratingColumn, row.indexOf("IMP"), row);
        }
        assertEquals("Short", activities.getActivities().getFirst().getName());
    }

    @Test
    void execute_unicodeNames_alignAndTruncateWithoutSplittingCharacters() throws Exception {
        ActivityList activities = new ActivityList(List.of(
                new Activity("复习".repeat(20), TODAY, START, END, DemandLevel.LOW, false),
                new Activity("🎉".repeat(20), TODAY, START, END, DemandLevel.HIGH, false),
                new Activity("👩‍💻".repeat(20), TODAY, START, END, DemandLevel.HIGH, false),
                new Activity("Cafe\u0301".repeat(10), TODAY, START, END, DemandLevel.LOW, false)));
        String message = parser.parse("view today").execute(activities, noWrites).getMessage();
        assertTrue(message.contains("| " + "复习".repeat(5) + "复...  |"), message);
        assertTrue(message.contains("| " + "🎉".repeat(11) + "...  |"), message);
        assertTrue(message.contains("| " + "👩‍💻".repeat(11) + "...  |"), message);
        assertTrue(message.contains("| " + "Cafe\u0301".repeat(5) + "Caf... |"), message);
        assertTrue(message.contains("复习".repeat(20)), message);
        assertTrue(message.contains("👩‍💻".repeat(20)), message);
    }

    @Test
    void execute_emptyList_reportsNormalResultWithoutShowingEmptyCharts() throws Exception {
        assertEmptyViews(new ActivityList());
    }

    @Test
    void execute_activitiesOutsideRange_reportsNormalResultWithoutChangingActivities() throws Exception {
        ActivityList activities = new ActivityList();
        activities.addActivity(new Activity("Previous week", TODAY.minusDays(7), START, END,
                DemandLevel.LOW, false));
        activities.addActivity(new Activity("Next week", TODAY.plusDays(7), START, END,
                DemandLevel.HIGH, true));

        assertEmptyViews(activities);
    }

    @Test
    void parse_removedDemandCommand_reportsUnrecognizedCommand() {
        for (String input : List.of("demand", "demand today", "DEMAND TODAY", "demand today extra")) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertEquals("[WARNING] Unrecognized command. Type help to see available commands.",
                    exception.getMessage(), input);
        }
    }

    @Test
    void execute_highDemandCount_includesCompletedAndAllTodayTimes() throws Exception {
        ActivityList activities = new ActivityList(List.of(
                new Activity("Yesterday", TODAY.minusDays(1), START, END, DemandLevel.HIGH, false),
                new Activity("Tomorrow", TODAY.plusDays(1), START, END, DemandLevel.HIGH, false),
                new Activity("Done earlier", TODAY, LocalTime.MIDNIGHT, LocalTime.of(1, 0),
                        DemandLevel.HIGH, true),
                new Activity("Starting now", TODAY, LocalTime.NOON, LocalTime.of(13, 0),
                        DemandLevel.HIGH, false),
                new Activity("Later today", TODAY, LocalTime.of(23, 0), LocalTime.of(23, 30),
                        DemandLevel.HIGH, false),
                new Activity("Low", TODAY, START, END, DemandLevel.LOW, false),
                new Activity("Medium", TODAY, START, END, DemandLevel.MEDIUM, true)));
        List<Activity> original = activities.getActivities();
        List<Boolean> completion = original.stream().map(Activity::isDone).toList();

        CommandResult result = parser.parse("view today").execute(activities, noWrites);

        assertTrue(result.getMessage().contains("SUMMARY\nTotal activities: 5\nCompleted: 2\nRemaining: 3\n"
                + "High-demand activities: 3"));
        assertFalse(result.getMessage().contains("Yesterday"));
        assertFalse(result.getMessage().contains("Tomorrow"));
        assertFalse(result.isExit());
        assertFalse(result.didChangeActivities());
        assertEquals(original, activities.getActivities());
        assertEquals(completion, activities.getActivities().stream().map(Activity::isDone).toList());
    }

    @Test
    void execute_noHighDemand_keepsPopulatedDashboardAndReportsZero() throws Exception {
        ActivityList activities = new ActivityList(List.of(
                new Activity("Low", TODAY, START, END, DemandLevel.LOW, false),
                new Activity("Medium", TODAY, START, END, DemandLevel.MEDIUM, true)));

        String message = parser.parse("view today").execute(activities, noWrites).getMessage();

        assertTrue(message.contains("High-demand activities: 0"));
        assertTrue(message.contains("RATING CHART"));
        assertFalse(message.contains("[WARNING]"));
    }

    @Test
    void execute_highDemandCount_refreshesAcrossLocalNewYearMidnight() throws Exception {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-12-31T15:59:59Z"));
        Parser midnightParser = new Parser(mutableClock(now, ZoneId.of("Asia/Singapore")));
        var command = midnightParser.parse("view today");
        LocalDate newYear = LocalDate.of(2027, 1, 1);
        ActivityList activities = new ActivityList(List.of(
                new Activity("Last year", newYear.minusDays(1), START, END, DemandLevel.HIGH, false),
                new Activity("New year", newYear, START, END, DemandLevel.HIGH, false),
                new Activity("New year done", newYear, START, END, DemandLevel.HIGH, true),
                new Activity("Low next day", newYear.plusDays(1), START, END, DemandLevel.LOW, false)));

        // Parse before midnight, then execute after Singapore's date changes while UTC is still December 31.
        now.set(Instant.parse("2026-12-31T16:00:00Z"));
        String firstDay = command.execute(activities, noWrites).getMessage();
        assertTrue(firstDay.startsWith("TODAY - Friday, 01 January 2027"));
        assertTrue(firstDay.contains("High-demand activities: 2"));
        assertFalse(firstDay.contains("Last year"));

        now.set(Instant.parse("2027-01-01T16:00:00Z"));
        String secondDay = command.execute(activities, noWrites).getMessage();
        assertTrue(secondDay.startsWith("TODAY - Saturday, 02 January 2027"));
        assertTrue(secondDay.contains("High-demand activities: 0"));
        assertFalse(secondDay.contains("New year"));
    }

    @Test
    void execute_populatedRanges_preservesSchedulesAndLabelsMissingDays() throws Exception {
        ActivityList activities = new ActivityList();
        activities.addActivity(new Activity("Today revision", TODAY, START, END,
                DemandLevel.HIGH, true, 5, 4, 3));
        activities.addActivity(new Activity("Tomorrow meeting", TODAY.plusDays(1), START, END,
                DemandLevel.LOW, false));
        List<Activity> original = activities.getActivities();

        CommandResult today = parser.parse("view today").execute(activities, noWrites);
        assertTrue(today.getMessage().contains("Today revision"));
        assertFalse(today.getMessage().contains("Tomorrow meeting"));
        assertFalse(today.getMessage().contains("[WARNING]"));
        assertTrue(today.getMessage().contains("SUMMARY\nTotal activities: 1\nCompleted: 1\nRemaining: 0\n"
                + "High-demand activities: 1"));
        assertTrue(today.getMessage().contains("RATING TOTALS\nIMP 5\nURG 4\nCOM 3\n"));
        assertTrue(today.getMessage().contains("RATING CHART"));

        CommandResult tomorrow = parser.parse("view tomorrow").execute(activities, noWrites);
        assertTrue(tomorrow.getMessage().contains("Tomorrow meeting"));
        assertFalse(tomorrow.getMessage().contains("Today revision"));
        assertFalse(tomorrow.getMessage().contains("[WARNING]"));
        assertTrue(tomorrow.getMessage().contains("SUMMARY\nTotal activities: 1\nCompleted: 0\nRemaining: 1\n"
                + "High-demand activities: 0"));
        assertTrue(tomorrow.getMessage().contains("RATING TOTALS\nIMP 0\nURG 0\nCOM 0\n"));

        CommandResult week = parser.parse("view week").execute(activities, noWrites);
        String weeklyMessage = week.getMessage();
        assertTrue(weeklyMessage.startsWith("WEEK - Sunday, 25 October 2026 to Saturday, 31 October 2026"));
        assertTrue(weeklyMessage.contains("Tue 27 Oct - No activities scheduled for this day."));
        assertEquals(5, weeklyMessage.split("No activities scheduled for this day\\.", -1).length - 1);
        assertFalse(weeklyMessage.contains("[WARNING]"));
        assertTrue(weeklyMessage.contains("Today revision"));
        assertTrue(weeklyMessage.contains("Tomorrow meeting"));
        assertTrue(weeklyMessage.contains("SUMMARY\nTotal activities: 2\nCompleted: 1\nRemaining: 1\n"
                + "High-demand activities: 1"));
        assertTrue(weeklyMessage.contains("RATING TOTALS\nIMP 5\nURG 4\nCOM 3\n"));
        assertTrue(weeklyMessage.contains("RATING CHART"));

        for (CommandResult result : List.of(today, tomorrow, week)) {
            assertFalse(result.getMessage().contains("Ratings set:"));
            assertFalse(result.getMessage().contains("rating totals use recorded ratings only"));
            assertFalse(result.isExit());
            assertFalse(result.didChangeActivities());
        }
        assertEquals(original, activities.getActivities());
        assertTrue(original.get(0).isDone());
        assertFalse(original.get(1).isDone());
    }

    @Test
    void execute_weekRange_includesSevenDaysAcrossYearBoundary() throws Exception {
        Clock yearEndClock = Clock.fixed(Instant.parse("2026-12-31T04:00:00Z"), ZoneId.of("Asia/Singapore"));
        Parser yearEndParser = new Parser(yearEndClock);
        LocalDate start = LocalDate.of(2026, 12, 31);
        ActivityList activities = new ActivityList();
        activities.addActivity(new Activity("Yesterday only", start.minusDays(1), START, END,
                DemandLevel.LOW, false));
        activities.addActivity(new Activity("Today only", start, START, END,
                DemandLevel.MEDIUM, false));
        activities.addActivity(new Activity("Final included day", start.plusDays(6), START, END,
                DemandLevel.HIGH, false));
        activities.addActivity(new Activity("Day eight", start.plusDays(7), START, END,
                DemandLevel.LOW, false));

        CommandResult result = yearEndParser.parse("view week").execute(activities, noWrites);
        String message = result.getMessage();

        assertTrue(message.startsWith("WEEK - Thursday, 31 December 2026 to Wednesday, 06 January 2027"));
        for (String day : List.of("Thu 31 Dec", "Fri 01 Jan", "Sat 02 Jan", "Sun 03 Jan", "Mon 04 Jan",
                "Tue 05 Jan", "Wed 06 Jan")) {
            assertTrue(message.contains("\n\n" + day), day);
        }
        assertTrue(message.contains("Today only"));
        assertTrue(message.contains("Final included day"));
        assertFalse(message.contains("Yesterday only"));
        assertFalse(message.contains("Day eight"));
        assertTrue(message.contains("Total activities: 2\nCompleted: 0\nRemaining: 2"));
        assertFalse(result.isExit());
        assertFalse(result.didChangeActivities());
    }

    @Test
    void execute_ratingChart_alignsTableColumnsAndAsciiCommitmentMarks() throws Exception {
        ActivityList activities = new ActivityList();
        activities.addActivity(new Activity("Practice quiz", TODAY, START, END,
                DemandLevel.HIGH, false, 4, 5, 3));
        activities.addActivity(new Activity("Stretch break", TODAY, START, END,
                DemandLevel.LOW, false, 1, 1, 1));
        activities.addActivity(new Activity("Plan tomorrow", TODAY, START, END,
                DemandLevel.MEDIUM, false, 3, null, 2));
        activities.addActivity(new Activity("Project notes", TODAY, START, END,
                DemandLevel.MEDIUM, false, null, 2, null));
        activities.addActivity(new Activity("An activity name longer than the old heading", TODAY, START, END,
                DemandLevel.HIGH, false, 5, 5, 5));

        String message = parser.parse("view today").execute(activities, noWrites).getMessage();
        String chart = message.substring(message.indexOf("RATING CHART\n") + "RATING CHART\n".length(),
                message.indexOf("\n\nRATING TOTALS"));
        String[] lines = chart.split("\n");
        assertTrue(lines[1].matches("\\|[-|]+\\|"), lines[1]);
        for (String line : lines) {
            assertTrue(line.startsWith("|") && line.endsWith("|"), line);
            assertEquals(7L, line.chars().filter(character -> character == '|').count(), line);
            assertEquals(lines[0].length(), line.length(), line);
        }
        for (int position = 0; position < lines[0].length(); position++) {
            if (lines[0].charAt(position) == '|') {
                assertEquals('|', lines[1].charAt(position), lines[1]);
            }
        }
        String[] headings = {"Activity", "IMP", "URG", "COM", "PRIORITY SCORE", "DEMAND"};
        int[] starts = new int[headings.length];
        for (int column = 0; column < headings.length; column++) {
            starts[column] = lines[0].indexOf(headings[column]);
            assertTrue(starts[column] >= 0, headings[column]);
            if (column > 0) {
                assertTrue(starts[column] - starts[column - 1] - headings[column - 1].length() >= 3);
            }
        }
        String[][] expectedRows = {
            {"Practice quiz", "****", "!!!!!", "+++", "175", "### H"},
            {"Stretch break", "*", "!", "+", "2", "# L"},
            {"Plan tomorrow", "***", "-", "++", "N", "## M"},
            {"Project notes", "-", "!!", "-", "N", "## M"},
            {"An activity name longer...", "*****", "!!!!!", "+++++", "250", "### H"}
        };
        assertEquals(expectedRows.length + 2, lines.length);
        for (int row = 0; row < expectedRows.length; row++) {
            for (int column = 0; column < headings.length; column++) {
                String expected = expectedRows[row][column];
                assertEquals(expected, lines[row + 2].substring(starts[column], starts[column] + expected.length()));
                if (column + 1 < headings.length) {
                    String gap = lines[row + 2].substring(starts[column] + expected.length(), starts[column + 1]);
                    assertTrue(gap.endsWith(" | ") && gap.replace("|", "").isBlank(), lines[row + 2]);
                }
            }
        }
        assertTrue(chart.chars().allMatch(character -> character < 128));
        assertTrue(message.contains("Legend: * Importance; ! Urgency; + Commitment; # Demand\n"
                + "Priority score = URG x URG x (IMP + COM)."));
    }

    @Test
    void execute_ratingChart_limitsNameWidthWithoutChangingStoredNames() throws Exception {
        List<String> names = List.of("Short", "A".repeat(25), "B".repeat(26),
                "C".repeat(27), "D".repeat(200));
        ActivityList activities = new ActivityList();
        for (int day : new int[]{0, 1, 6}) {
            for (String name : names) {
                activities.addActivity(new Activity(name, TODAY.plusDays(day), START, END,
                        DemandLevel.HIGH, false, 5, 5, 5));
            }
        }
        List<Activity> original = activities.getActivities();

        for (String command : List.of("view today", "view tomorrow", "view week")) {
            CommandResult result = parser.parse(command).execute(activities, noWrites);
            String message = result.getMessage();
            String chart = message.substring(message.indexOf("RATING CHART\n") + "RATING CHART\n".length(),
                    message.indexOf("\n\nRATING TOTALS"));
            for (String line : chart.split("\n")) {
                assertTrue(line.length() <= 80, "Rating table exceeds 80 columns: " + line.length());
            }
            assertTrue(chart.contains("| " + "A".repeat(25) + "  |"));
            assertTrue(chart.contains("| " + "B".repeat(26) + " |"));
            assertTrue(chart.contains("| " + "C".repeat(23) + "... |"));
            assertTrue(chart.contains("| " + "D".repeat(23) + "... |"));
            assertFalse(chart.contains("C".repeat(27)));
            assertFalse(chart.contains("D".repeat(200)));
            for (String name : names) {
                assertTrue(message.matches("(?s).*\\] " + Pattern.quote(name)
                        + "\\s+IMP.*"), "Full schedule name missing");
            }
            assertFalse(result.didChangeActivities());
            assertEquals(original, activities.getActivities());
        }
    }

    /**
     * Checks all empty date ranges against their complete messages and preserves the input list.
     */
    private void assertEmptyViews(ActivityList activities) throws Exception {
        String[][] cases = {
            {"view today", "Sorry, no activities fall within the selected period."},
            {"view tomorrow", "Sorry, no activities fall within the selected period."},
            {"view week", "Sorry, no activities fall within the selected period."}
        };
        List<Activity> original = activities.getActivities();
        for (String[] scenario : cases) {
            CommandResult result = parser.parse(scenario[0]).execute(activities, noWrites);
            assertEquals(scenario[1], result.getMessage(), scenario[0]);
            assertFalse(result.isExit());
            assertFalse(result.didChangeActivities());
            assertEquals(original, activities.getActivities());
        }
    }

    /**
     * Creates a clock whose zone views share an instant that can advance between command executions.
     */
    private static Clock mutableClock(AtomicReference<Instant> now, ZoneId zone) {
        return new Clock() {
            @Override
            public ZoneId getZone() {
                return zone;
            }

            @Override
            public Clock withZone(ZoneId newZone) {
                return mutableClock(now, newZone);
            }

            @Override
            public Instant instant() {
                return now.get();
            }
        };
    }
}
//@@author
