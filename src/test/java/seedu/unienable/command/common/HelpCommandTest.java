//@@author lukelouyu
package seedu.unienable.command.common;

//@@author
//@@author Atharva005
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.unienable.exception.ParseException;
import seedu.unienable.model.ActivityList;
import seedu.unienable.parser.Parser;
import seedu.unienable.storage.Storage;

/**
 * Verifies categorized help, detailed command topics, input handling, and read-only results.
 */
class HelpCommandTest {
    private final Parser parser = new Parser();
    private final Storage noWrites = new Storage() {
        @Override
        public void save(ActivityList ignored) {
            throw new AssertionError("Help must not persist activities");
        }
    };

    @Test
    void execute_nextHelp_describesTheNormalEmptyResult() throws Exception {
        String message = parser.parse("help next").execute(new ActivityList(), noWrites).getMessage();
        assertTrue(message.contains("Sorry, no upcoming incomplete activities exist."), message);
        assertTrue(message.contains("normal no-activities message"), message);
        assertFalse(message.contains("Shows a warning when none qualifies"), message);
    }

    @Test
    void execute_overview_groupsCommandsAndExplainsTopicNavigation() {
        CommandResult result = new HelpCommand().execute(new ActivityList(), noWrites);
        String message = result.getMessage();
        assertTrue(message.startsWith("UniEnable commands:\n"));
        assertTrue(message.contains("Type help TOPIC for syntax, rules, and examples"));
        int previous = -1;
        for (String category : List.of("ACTIVITY MANAGEMENT", "PLANNING AND PRIORITIES",
                "COMPLETION AND DELETION", "FACILITIES AND ROUTES", "HELP AND EXIT")) {
            int position = message.indexOf("\n\n" + category + "\n");
            assertTrue(position > previous, category);
            previous = position;
        }
        for (String command : List.of("add", "edit INDEX", "find KEYWORD", "list demand", "list priority",
                "view today", "view tomorrow", "view week", "overdue", "next",
                "mark INDEX", "unmark INDEX", "delete INDEX", "facility list", "facility LOCATION",
                "facility find", "route", "help [TOPIC]", "bye")) {
            assertTrue(message.contains("\n  " + command + " "), command);
        }
        assertTrue(message.contains("Run list to get the indexes for edit, mark, unmark, and delete."));
        assertTrue(message.contains("Activity changes are saved automatically to data/activities.txt."));
        assertFalse(result.isExit());
        assertFalse(result.didChangeActivities());
    }

    @TestFactory
    Stream<DynamicTest> execute_topics_showUsageRulesAndExamplesWithoutReferenceData() {
        String[][] cases = {
            {"add", "add n/NAME d/YYYY-MM-DD|today|tomorrow s/HH:mm e/HH:mm", "Names use printable ASCII only",
                "add n/Study d/2030-12-01 s/10:00 e/11:00", "d/today and d/tomorrow use this computer's date",
                "add n/Study d/tomorrow s/10:00 e/11:00"},
            {"edit", "edit INDEX [n/NAME] [d/YYYY-MM-DD|today|tomorrow]", "Replacement names use printable ASCII only",
                "edit 1 n/Project meeting dem/HIGH"},
            {"find", "find KEYWORD", "literal substring", "find project meeting"},
            {"list", "list priority", "list demand groups HIGH, MEDIUM, then LOW", "list demand"},
            {"view", "view week", "today and the following six days", "view tomorrow"},
            {"overdue", "overdue", "end strictly before the current time", "overdue"},
            {"next", "next", "start is strictly after the current time", "next"},
            {"mark", "mark INDEX", "already completed activity warns", "mark 1"},
            {"unmark", "unmark INDEX", "already incomplete activity warns", "unmark 1"},
            {"delete", "delete INDEX", "There is no undo command", "delete 1"},
            {"facility", "facility find type/FEATURE [status/YES|NO|UNKNOWN]", "status defaults to YES",
                "facility find type/LIFT status/YES"},
            {"route", "route from/START to/END", "from/ before to/", "route from/AS1 to/CLB"},
            {"help", "help TOPIC", "topic matching ignores letter case", "help add"},
            {"bye", "bye", "keeps the app running", "bye"}
        };
        return Stream.of(cases).map(scenario -> DynamicTest.dynamicTest("help " + scenario[0], () -> {
            ActivityList activities = new ActivityList();
            CommandResult result = parser.parse("help " + scenario[0]).execute(activities, noWrites);
            String message = result.getMessage();

            assertTrue(message.startsWith(scenario[0].toUpperCase(Locale.ROOT) + " - "), message);
            assertTrue(message.contains("\n\nUsage:\n"), message);
            assertTrue(message.contains("\n\nDetails:\n"), message);
            assertTrue(message.contains("\n\nExamples:\n"), message);
            for (int index = 1; index < scenario.length; index++) {
                assertTrue(message.contains(scenario[index]), scenario[index]);
            }
            assertTrue(message.endsWith("Type help to return to the command menu."));
            assertFalse(message.contains("[WARNING]"), message);
            assertFalse(message.contains("%s"), message);
            if (List.of("edit", "find", "list", "view", "overdue", "next", "mark", "unmark", "delete")
                    .contains(scenario[0])) {
                assertTrue(message.contains("Sorry,"), message);
            }
            assertFalse(result.isExit());
            assertFalse(result.didChangeActivities());
            assertTrue(activities.getActivities().isEmpty());
        }));
    }

    //@@author lukelouyu
    @Test
    void execute_addTopic_saysEndedActivitiesCannotBeAddedOrEdited() throws Exception {
        String message = parser.parse("help add").execute(new ActivityList(), noWrites).getMessage();

        assertTrue(message.contains("ended activities cannot be added."), message);
        assertTrue(message.contains("Once an activity ends, it cannot be edited or rescheduled."), message);
        assertFalse(message.contains("Ended activities can be added"), message);
    }
    //@@author Atharva005

    @Test
    void removedSaveCommand_isAbsentFromHelpAndParser() {
        String menu = new HelpCommand().execute(new ActivityList(), noWrites).getMessage();
        assertFalse(menu.contains("\n  save "), menu);
        for (String input : List.of("save", "save extra", "help save")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }

    @Test
    void execute_deleteHelp_matchesSupportedIndexes() throws Exception {
        String deletion = parser.parse("help delete").execute(new ActivityList(), noWrites).getMessage();
        assertTrue(deletion.contains("list demand retains chronological indexes"), deletion);
        assertTrue(deletion.contains("Do not use numbering from the calendar dashboards"), deletion);
        assertFalse(deletion.contains("Do not use numbering from list demand"), deletion);
    }

    @Test
    void execute_routeHelp_matchesSupportedEndpoints() throws Exception {
        String route = parser.parse("help route").execute(new ActivityList(), noWrites).getMessage();
        assertTrue(route.contains("facility names (AS1-AS8 or CLB)"), route);
        assertTrue(route.contains("Facility IDs F01-F09 are also accepted"), route);
        assertFalse(route.contains("facility IDs or names"), route);
    }

    @Test
    void parse_help_acceptsMixedCaseAndWhitespace() throws Exception {
        String[][] cases = {
            {"  HeLp  ", ""},
            {"HELP ADD", "add"},
            {"\thelp\tEdIt\t", "edit"},
            {"  HeLp   FaCiLiTy  ", "facility"},
            {"help RoUtE", "route"},
            {"help BYE", "bye"}
        };
        for (String[] scenario : cases) {
            HelpCommand expected = scenario[1].isEmpty() ? new HelpCommand() : new HelpCommand(scenario[1]);
            assertEquals(expected.execute(new ActivityList(), noWrites).getMessage(),
                    parser.parse(scenario[0]).execute(new ActivityList(), noWrites).getMessage(), scenario[0]);
        }
    }

    @Test
    void parse_unknownAndExtraTopics_showWarningAndUsage() {
        for (String input : List.of("help extra", "help unknown", "help demand", "help list demand", "help add n/Study",
                "help delete 1", "help facility find", "help route from/AS1 to/CLB")) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(input), input);
            assertTrue(exception.getMessage().startsWith("[WARNING]"), input);
            assertTrue(exception.getMessage().contains("Usage: help [TOPIC]"), input);
        }
        ParseException unknown = assertThrows(ParseException.class, () -> parser.parse("help unknown"));
        assertTrue(unknown.getMessage().contains("Unknown help topic 'unknown'"));
        assertTrue(unknown.getMessage().contains("Type help to see the available commands."));
    }
}
//@@author
