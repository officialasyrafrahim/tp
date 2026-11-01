package seedu.unienable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import seedu.unienable.ui.Ui;
import seedu.unienable.ui.accessibility.AccessibilityDisclaimer;

/**
 * Exercises the console entry point and the user's full command sequence.
 */
class UniEnableTest {
    /**
     * Fixed afternoon clock before every console fixture date (2026-10-15 onward).
     */
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-10-14T16:00:00Z"), ZoneOffset.UTC);

    @TestFactory
    Stream<DynamicTest> commands() throws Exception {
        return consoleCases().stream().map(row -> DynamicTest.dynamicTest(row[0], () -> {
            String output = console(row[1] + "\nbye\nignored\n");
            assertTrue(output.contains(row[2]), output);
            if (!row[3].isEmpty()) {
                assertFalse(output.contains(row[3]), output);
            }
            assertTrue(output.endsWith("Goodbye from UniEnable!\n" + "_".repeat(60) + "\n"), output);
        }));
    }

    @Test
    void fullSession_retainsFiveActivitiesAndRecoversAfterWarnings() throws Exception {
        String input = String.join("\n", sessionCases().stream().map(row -> row[1]).toList());
        String output = console(input + "\n");
        for (String[] row : sessionCases()) {
            assertTrue(output.contains(row[2]), row[0] + "\n" + output);
        }
        assertEquals(5, output.split("Added activity:", -1).length - 1);
        assertEquals(15, output.split("\\[WARNING\\]", -1).length - 1);
        assertTrue(output.contains("Total activities: 5"));
        assertTrue(output.contains(AccessibilityDisclaimer.TEXT));
    }

    @Test
    void emptyInput_preservesWelcomeFormatting() throws Exception {
        String border = "_".repeat(60) + "\n";
        assertEquals(border + "Welcome to UniEnable! Type bye to exit.\n" + Ui.getLogo() + "\n" + border,
                console(""));
    }

    //@@author lukelouyu
    @Test
    void mistypedActivityParameters_warnWithoutAddingEditingOrSaving(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        byte[] original = "Existing|2030-12-10|09:00|10:00|LOW|false|-|-|-\n"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        var originalTime = Files.getLastModifiedTime(activityFile);

        String output = consoleAtPath("add n/Class demm/HIGH d/2030-12-11 s/09:00 e/10:00\n"
                + "edit 1 n/Changed impp/5\nlist\nbye\n", activityFile);

        assertEquals(2, output.split("Unknown parameter", -1).length - 1, output);
        assertFalse(output.contains("Added activity:"), output);
        assertFalse(output.contains("Edited activity:"), output);
        assertTrue(output.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [ ]"), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
        assertEquals(originalTime, Files.getLastModifiedTime(activityFile));
    }
    //@@author

    //@@author Atharva005
    @Test
    void topicHelp_showsInstructionsWithoutChangingActivitiesOrExiting(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        byte[] original = "Existing|2030-12-10|09:00|10:00|LOW|true|2|3|4\n"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        var originalModifiedTime = Files.getLastModifiedTime(activityFile);
        List<String> topics = List.of("add", "edit", "find", "list", "view", "overdue", "next",
                "mark", "unmark", "delete", "facility", "route", "help", "bye");
        List<String> commands = new ArrayList<>();
        commands.add("help");
        commands.addAll(topics.stream().map(topic -> "help " + topic).toList());
        commands.addAll(List.of("help unknown", "help add extra", "list", "bye", ""));

        String output = consoleAtPath(String.join("\n", commands), activityFile);

        assertTrue(output.contains("ACTIVITY MANAGEMENT"));
        assertTrue(output.contains("PLANNING AND PRIORITIES"));
        for (String topic : topics) {
            assertTrue(output.contains(topic.toUpperCase(Locale.ROOT) + " - "), topic);
        }
        assertTrue(output.contains("[WARNING] Unknown help topic 'unknown'"));
        assertTrue(output.contains("[WARNING] help accepts at most one command name."));
        assertEquals(2, output.split(Pattern.quote("[WARNING]"), -1).length - 1);
        assertEquals(1, output.split(Pattern.quote("Goodbye from UniEnable!"), -1).length - 1);
        assertTrue(output.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [X]"));
        assertArrayEquals(original, Files.readAllBytes(activityFile));
        assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));
    }
    //@@author

    //@@author lukelouyu
    @Test
    void sameRouteEndpoints_warnWithoutChangingActivities(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        byte[] original = "Project meeting|2030-10-25|16:00|17:00|HIGH|false|-|-|-\n"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);

        String output = consoleAtPath(String.join("\n", "route from/clb to/clb",
                "route from/CLB to/cLb", "route from/as6 to/AS6", "list", "bye", ""), activityFile);

        String warning = "[WARNING] Sorry, the source and destination are the same.";
        assertEquals(3, output.split(Pattern.quote(warning), -1).length - 1);
        assertFalse(output.contains("Shortest recorded accessible route:"));
        assertFalse(output.contains("Total distance: 0 metres"));
        assertTrue(output.contains("1. Project meeting (2030-10-25, 16:00-17:00) [HIGH] [ ]"));
        assertTrue(output.contains("Goodbye from UniEnable!"));
        assertArrayEquals(original, Files.readAllBytes(activityFile));
    }
    //@@author

    //@@author Atharva005
    @Test
    void viewToday_countsLoadedHighDemandWithoutRewritingLegacyStorage(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        byte[] original = String.join("\n",
                "Past high|2026-10-13|09:00|10:00|HIGH|false",
                "Today done|2026-10-14|08:00|09:00|HIGH|true",
                "Today late|2026-10-14|23:00|23:30|HIGH|false",
                "Today medium|2026-10-14|12:00|13:00|MEDIUM|false",
                "Tomorrow high|2026-10-15|09:00|10:00|HIGH|false", "")
                .getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        var originalModifiedTime = Files.getLastModifiedTime(activityFile);

        String output = consoleAtPath("ViEw ToDaY\nlist\nbye\n", activityFile);

        assertTrue(output.contains("SUMMARY\nTotal activities: 3\nCompleted: 1\nRemaining: 2\n"
                + "High-demand activities: 2"), output);
        assertTrue(output.contains("Today done (2026-10-14, 08:00-09:00) [HIGH] [X]"), output);
        assertTrue(output.contains("Today late (2026-10-14, 23:00-23:30) [HIGH] [ ]"), output);
        assertTrue(output.contains("Goodbye from UniEnable!"), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
        assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));
    }
    //@@author

    @Test
    void deletingFinalActivity_reportsEmptyStateAndPersistsDeletion(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        String output = consoleAtPath(String.join("\n",
                "add n/Project meeting d/2030-10-25 s/16:00 e/17:00 dem/HIGH",
                "delete 1", "list", "delete 1", "bye", ""), activityFile);

        assertTrue(output.contains("Deleted activity: Project meeting\nDate: 2030-10-25\n"
                + "Time: 16:00 - 17:00\nDemand: HIGH\nSorry, the activity list is empty.\n"));
        assertFalse(output.contains("Total activities: 0"));
        assertTrue(output.contains("Sorry, the activity list is empty."));
        assertTrue(output.contains("Sorry, the activity list is empty."));
        assertEquals("", Files.readString(activityFile));

        String restartedOutput = consoleAtPath("list\nbye\n", activityFile);
        assertTrue(restartedOutput.contains("Sorry, the activity list is empty."));
        assertFalse(restartedOutput.contains("Project meeting"));
    }

    //@@author Oliver-koh
    @Test
    void activityCompletion_survivesAnApplicationRestart() throws Exception {
        Path tempDir = Files.createTempDirectory("unienable-restart-test");
        Path activityFile = tempDir.resolve("activities.txt");
        try {
            String firstSession = consoleAtPath(
                    "add n/Exam d/2026-10-18 s/09:00 e/11:00 dem/HIGH\nmark 1\nbye\n", activityFile);
            String secondSession = consoleAtPath("list\nbye\n", activityFile);

            assertTrue(firstSession.contains("Marked activity as done: Exam"));
            assertTrue(secondSession.contains("Exam (2026-10-18, 09:00-11:00) [HIGH] [X]"));
        } finally {
            Files.deleteIfExists(activityFile);
            Files.deleteIfExists(tempDir);
        }
    }
    //@@author

    //@@author Oliver-koh
    @Test
    void activityRatings_surviveRestartAndCompletionChanges(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        String partial = "add n/Planning and-or review d/2030-10-25 s/10:00 e/11:00 imp/5 com/3";
        String firstSession = consoleAtPath(String.join("\n",
                "add n/Study d/2030-10-25 s/09:00 e/10:00",
                partial,
                "ADD CoM/3 n/Project UrG/4 d/2030-10-25 iMp/5 e/12:00 DeM/hIgH s/11:00",
                "add n/Planning and-or review d/2030-10-25 s/12:00 e/13:00 imp/5 com/3",
                "mark 2", "mark 3", "bye", ""), activityFile);
        String partialRow = "Planning and-or review|2030-10-25|10:00|11:00|MEDIUM|";
        List<String> expectedRows = new ArrayList<>(List.of(
                "Study|2030-10-25|09:00|10:00|MEDIUM|false|-|-|-",
                partialRow + "true|5|-|3",
                "Project|2030-10-25|11:00|12:00|HIGH|true|5|4|3",
                "Planning and-or review|2030-10-25|12:00|13:00|MEDIUM|false|5|-|3"));

        assertFalse(firstSession.contains("[WARNING]"), firstSession);
        assertEquals(4, firstSession.split("Added activity:", -1).length - 1);
        assertEquals(expectedRows, Files.readAllLines(activityFile, StandardCharsets.UTF_8));

        String secondSession = consoleAtPath("list\nunmark 2\nbye\n", activityFile);

        assertTrue(secondSession.contains("2. Planning and-or review (2030-10-25, 10:00-11:00) [MEDIUM] [X]"));
        assertTrue(secondSession.contains("Unmarked activity: Planning and-or review"));
        expectedRows.set(1, partialRow + "false|5|-|3");
        assertEquals(expectedRows, Files.readAllLines(activityFile, StandardCharsets.UTF_8));
    }
    //@@author

    @Test
    void emptyQueries_reportNoResultsWithoutCreatingAnActivityFile(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        String[][] cases = {
            {"list", "Sorry, the activity list is empty."},
            {"list demand", "Sorry, the activity list is empty."},
            {"list priority", "Sorry, the activity list is empty."},
            //@@author Atharva005
            {"view today", "Sorry, no activities fall within the selected period."},
            {"view tomorrow", "Sorry, no activities fall within the selected period."},
            {"view week", "Sorry, no activities fall within the selected period."},
            //@@author
            {"overdue", "Sorry, no incomplete activities have ended."},
            {"next", "Sorry, no upcoming incomplete activities exist."},
            {"find missing", "Sorry, no activity names match your search."},
            {"facility find type/AUTOMATIC_DOOR", "[WARNING] No matching facilities found."}
        };

        for (String[] scenario : cases) {
            String output = consoleAtPath(scenario[0] + "\nbye\n", activityFile);
            int expectedWarnings = scenario[1].startsWith("[WARNING]") ? 1 : 0;

            assertTrue(output.contains(scenario[1]), scenario[0] + "\n" + output);
            assertEquals(expectedWarnings, output.split(Pattern.quote("[WARNING]"), -1).length - 1, scenario[0]);
            assertTrue(output.contains("Goodbye from UniEnable!"), scenario[0]);
            assertFalse(Files.exists(activityFile), scenario[0]);
        }
    }

    //@@author Oliver-koh
    @Test
    void repeatedCompletion_warnsWithoutSavingOrChangingState(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        byte[] original = String.join("\n",
                "Done|2030-12-01|09:00|10:00|HIGH|true|-|-|-",
                "Pending|2030-12-02|09:00|10:00|LOW|false|-|-|-", "")
                .getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        var originalModifiedTime = Files.getLastModifiedTime(activityFile);

        String output = consoleAtPath("mark 1\nunmark 2\nlist\nbye\n", activityFile);

        assertTrue(output.contains("[WARNING] Activity is already marked as done: Done"), output);
        assertTrue(output.contains("[WARNING] Activity is already unmarked: Pending"), output);
        assertEquals(2, output.split(Pattern.quote("[WARNING]"), -1).length - 1);
        assertTrue(output.contains("Done (2030-12-01, 09:00-10:00) [HIGH] [X]"), output);
        assertTrue(output.contains("Pending (2030-12-02, 09:00-10:00) [LOW] [ ]"), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
        assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));
    }
    //@@author

    @Test
    void populatedQueries_keepNormalMessagesAndDoNotSave(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        byte[] original = String.join("\n",
                "Past|2026-01-01|09:00|10:00|LOW|false|1|1|1",
                "Today|2026-10-14|09:00|10:00|HIGH|false|1|1|1",
                "Future|2030-12-01|09:00|10:00|HIGH|false|5|5|5", "")
                .getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);

        String output = consoleAtPath(String.join("\n", "list", "list demand", "list priority", "overdue",
                "next", "find Today", "view today", "facility list", "facility find type/LIFT status/YES",
                "route from/AS6 to/AS7", "bye", ""), activityFile);

        assertFalse(output.contains("[WARNING]"), output);
        assertTrue(output.contains("High-demand activities: 1"), output);
        assertTrue(output.contains("Total distance: 200 metres"), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
    }

    //@@author lukelouyu
    @Test
    void invalidRatingCommands_preserveActivityListAndStoredBytes(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        byte[] original = "Existing|2026-01-01|09:00|10:00|LOW|true\n".getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        String[] invalidRatings = {
            "imp/0", "urg/6", "com/-1", "imp/1.5", "urg/text", "com/-", "imp/", "urg/1 URG/2"
        };
        List<String> commands = new ArrayList<>();
        for (String ratings : invalidRatings) {
            commands.add("add n/Rejected d/2030-10-26 s/09:00 e/10:00 " + ratings);
        }
        commands.add("list");
        commands.add("bye");

        String output = consoleAtPath(String.join("\n", commands) + "\n", activityFile);

        assertEquals(invalidRatings.length, output.split("\\[WARNING\\]", -1).length - 1);
        assertFalse(output.contains("Added activity:"), output);
        assertTrue(output.contains("1. Existing (2026-01-01, 09:00-10:00) [LOW] [X]"));
        assertFalse(output.contains("Rejected ("), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
    }
    //@@author

    //@@author Atharva005
    @Test
    void addWithUnicodeWhitespaceName_isRejectedAndPreservesSavedData(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("activities.txt");
        byte[] original = "Existing|2030-12-10|09:00|10:00|LOW|true|2|3|4\n"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        var originalModifiedTime = Files.getLastModifiedTime(activityFile);

        for (String name : new String[] {"\u2003", "\u3000", " \u2003\t\u3000 "}) {
            String output = consoleAtPath("add n/" + name
                    + " d/2030-12-11 s/09:00 e/10:00\nlist\nbye\n", activityFile);

            assertTrue(output.contains("[WARNING] Empty n/ parameter."), output);
            assertFalse(output.contains("Added activity:"), output);
            assertTrue(output.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [X]"), output);
            assertArrayEquals(original, Files.readAllBytes(activityFile));
            assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));

            String restartOutput = consoleAtPath("list\nbye\n", activityFile);
            assertFalse(restartOutput.contains("Activity data warning:"), restartOutput);
            assertTrue(restartOutput.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [X]"),
                    restartOutput);
            assertArrayEquals(original, Files.readAllBytes(activityFile));
        }
    }
    //@@author

    //@@author lukelouyu
    @Test
    void nonAsciiNames_rejectAddAndEditAndPreserveSavedData(@TempDir Path tempDir) throws Exception {
        String[] names = {"复习", "வணக்கம்", "படி", "Café", "📚 Study", "Study\u2003group"};
        for (int i = 0; i < names.length; i++) {
            Path file = tempDir.resolve("unicode-" + i + ".txt");
            byte[] original = "Existing|2030-12-10|09:00|10:00|LOW|true|2|3|4\n"
                    .getBytes(StandardCharsets.UTF_8);
            Files.write(file, original);
            String output = consoleAtPath("add n/" + names[i] + " d/2030-12-10 s/09:00 e/10:00\n"
                    + "edit 1 n/" + names[i] + " revised imp/5\nlist\nbye\n", file);
            String warning = "[WARNING] Please write the activity name in English (ASCII characters only).";
            assertEquals(2, output.split(Pattern.quote(warning), -1).length - 1, output);
            assertFalse(output.contains("Added activity:"), output);
            assertFalse(output.contains("Edited activity:"), output);
            assertTrue(output.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [X]"), output);
            assertArrayEquals(original, Files.readAllBytes(file));
            String restarted = consoleAtPath("list\nbye\n", file);
            assertTrue(restarted.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [X]"), restarted);
            assertArrayEquals(original, Files.readAllBytes(file));
        }
    }

    /**
     * Replays the Developer Guide's date-shortcut manual test. The fixed clock makes
     * today 2026-10-14 and tomorrow 2026-10-15, whatever time the test actually runs.
     */
    @Test
    void dateWords_addEditListAndSaveRealDates(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("date-words.txt");
        String output = consoleAtPath("""
                add n/Manasi's birthday party d/today s/19:00 e/22:30 dem/low imp/4 com/4 urg/2
                add n/TCM Field Trip at Thong Chai Hospital d/tomorrow s/14:00 e/17:00 dem/high imp/5 com/2 urg/3
                list
                edit 1 d/tomorrow
                add n/Past outing d/yesterday s/09:00 e/10:00
                list
                bye
                """, file);

        // Split at the edit so each list is checked against the state it should show.
        String[] sections = output.split("Edited activity:", 2);
        String fieldTrip = "TCM Field Trip at Thong Chai Hospital (2026-10-15, 14:00-17:00) [HIGH] [ ]";
        assertTrue(sections[0].contains("1. Manasi's birthday party (2026-10-14, 19:00-22:30) [LOW] [ ]"), output);
        assertTrue(sections[0].contains("2. " + fieldTrip), output);
        assertTrue(sections[1].contains("[WARNING] Invalid date. Use a real YYYY-MM-DD date, today, or tomorrow."),
                output);
        assertTrue(sections[1].contains("1. " + fieldTrip), output);
        assertTrue(sections[1].contains("2. Manasi's birthday party (2026-10-15, 19:00-22:30) [LOW] [ ]"), output);
        assertFalse(output.contains("Past outing"), output);
        assertEquals(List.of("Manasi's birthday party|2026-10-15|19:00|22:30|LOW|false|4|2|4",
                "TCM Field Trip at Thong Chai Hospital|2026-10-15|14:00|17:00|HIGH|false|5|3|2"),
                Files.readAllLines(file));
    }
    //@@author

    //@@author Oliver-koh
    @Test
    void editWithUnicodeWhitespaceName_isRejectedAndPreservesActivityAndSavedBytes(@TempDir Path tempDir)
            throws Exception {
        String[] whitespaceNames = {"\u2003", "\u3000", " \u2003\t\u3000 "};
        for (int i = 0; i < whitespaceNames.length; i++) {
            Path activityFile = tempDir.resolve("activities-" + i + ".txt");
            String addOutput = consoleAtPath("add n/Unicode bug test d/2030-12-10 s/09:00 e/10:00\nbye\n",
                    activityFile);
            assertTrue(addOutput.contains("Added activity: Unicode bug test"), addOutput);
            byte[] original = Files.readAllBytes(activityFile);
            var originalModifiedTime = Files.getLastModifiedTime(activityFile);

            String editOutput = consoleAtPath("edit 1 n/" + whitespaceNames[i] + "\nlist\nbye\n", activityFile);

            assertTrue(editOutput.contains("[WARNING] Empty n/ parameter."), editOutput);
            assertTrue(editOutput.contains("1. Unicode bug test (2030-12-10, 09:00-10:00) [MEDIUM] [ ]"),
                    editOutput);
            assertArrayEquals(original, Files.readAllBytes(activityFile));
            assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));

            String restartOutput = consoleAtPath("list\nbye\n", activityFile);
            assertFalse(restartOutput.contains("Activity data warning:"), restartOutput);
            assertTrue(restartOutput.contains("1. Unicode bug test (2030-12-10, 09:00-10:00) [MEDIUM] [ ]"),
                    restartOutput);
            assertArrayEquals(original, Files.readAllBytes(activityFile));
        }
    }
    //@@author

    /**
     * Retains the original console cases directly in Java so fresh checkouts can run them.
     */
    private static List<String[]> consoleCases() {
        String unratedDetails = "\nImportance: -\nUrgency: -\nCommitment: -\nTotal activities: 1";
        return List.of(
                new String[]{"ADD01", "add n/Lecture d/2026-10-15 s/09:00 e/11:00",
                    "Added activity: Lecture\nDate: 2026-10-15\nTime: 09:00 - 11:00\nDemand: MEDIUM"
                        + unratedDetails, "[WARNING]"},
                new String[]{"ADD02", "add n/Exam d/2026-10-16 s/14:00 e/16:00 dem/HIGH",
                    "Added activity: Exam\nDate: 2026-10-16\nTime: 14:00 - 16:00\nDemand: HIGH"
                        + unratedDetails, "[WARNING]"},
                new String[]{"ADD03", "add n/Gym d/2026-10-17 s/18:00 e/19:00 dem/low",
                    "Added activity: Gym\nDate: 2026-10-17\nTime: 18:00 - 19:00\nDemand: LOW"
                        + unratedDetails, "[WARNING]"},
                new String[]{"ADD04", "add e/12:00 n/Tutorial s/10:00 d/2026-10-18",
                    "Added activity: Tutorial\nDate: 2026-10-18\nTime: 10:00 - 12:00\nDemand: MEDIUM"
                        + unratedDetails, "[WARNING]"},
                new String[]{"ADD05", "add n/CS2113 Meeting d/2026-10-20 s/14:00 e/16:00 dem/HIGH",
                    "Added activity: CS2113 Meeting\nDate: 2026-10-20\nTime: 14:00 - 16:00\nDemand: HIGH"
                        + unratedDetails, "[WARNING]"},
                new String[]{"ADD06", "add", "No add parameters supplied.", "Added activity:"},
                new String[]{"ADD07", "add n/Event", "Missing d/ parameter.", "Added activity:"},
                new String[]{"ADD08", "add n/Event d/2026-10-21", "Missing s/ parameter.", "Added activity:"},
                new String[]{"ADD09", "add n/Event d/2026-10-21 s/10:00", "Missing e/ parameter.", "Added activity:"},
                new String[]{"ADD10", "add n/ d/2026-10-21 s/10:00 e/11:00", "Empty n/ parameter.", "Added activity:"},
                new String[]{"ADD11", "add n/Invalid Date d/2026-02-30 s/09:00 e/10:00", "Invalid date.",
                    "Added activity:"},
                new String[]{"ADD12", "add n/Invalid Time d/2026-10-21 s/25:00 e/26:00", "Invalid time.",
                    "Added activity:"},
                new String[]{"ADD13", "add n/Invalid Range d/2026-10-21 s/15:00 e/10:00",
                    "End time must be later than start time", "Added activity:"},
                new String[]{"ADD14", "add n/Equal Time d/2026-10-21 s/10:00 e/10:00",
                    "End time must be later than start time", "Added activity:"},
                new String[]{"ADD15", "add n/Meeting n/Class d/2026-10-21 s/10:00 e/11:00",
                    "Duplicate n/ parameter.", "Added activity:"},
                new String[]{"ADD16", "add n/Test d/2026-10-21 s/10:00 e/11:00 dem/URGENT",
                    "Demand must be LOW, MEDIUM, or HIGH.", "Added activity:"},
                new String[]{"ADD17", "add n/Test d/2026-10-21 s/10:00 e/11:00 loc/NUS", "Unknown parameter 'loc/'.",
                    "Added activity:"},
                new String[]{"FAC01", "facility AS4", "[F04] AS4", "[WARNING]"},
                new String[]{"FAC02", "facility clb", "[F09] CLB", "[WARNING]"},
                new String[]{"FAC03", "facility list", "[F09] CLB", "[WARNING]"},
                new String[]{"FAC04", "facility find type/LIFT status/NO", "[F01] AS1\n[F02] AS2", "[F03] AS3"},
                new String[]{"FAC05", "facility find type/ACCESSIBLE_WASHROOM", "[F04] AS4", "[F01] AS1"},
                new String[]{"FAC06", "facility AS99", "Unknown facility 'AS99'.", "Accessibility Features:"},
                new String[]{"FAC07", "facility", "Expected exactly one facility ID or name.",
                    "Accessibility Features:"},
                new String[]{"FAC08", "facility find type/WASHROOM", "Unknown facility feature type 'WASHROOM'.",
                    "Facilities with"},
                new String[]{"FAC09", "facility AS4", "[F04] AS4", "[WARNING]"},
                new String[]{"CASE", " FaCiLiTy FiNd TyPe/lift StAtUs/unknown ", "UNKNOWN means the local dataset",
                    "[F03] AS3"},
                new String[]{"DEFAULT", "facility find type/LIFT", "Facilities with LIFT (status: YES)", "[F01] AS1"},
                new String[]{"NONE", "facility find type/AUTOMATIC_DOOR", "[WARNING] No matching facilities found.",
                    "[F01] AS1"},
                new String[]{"ID", "facility f04", "[F04] AS4", "[WARNING]"},
                new String[]{"LIST_CASE", "FACILITY LIST", "[F01] AS1", "[WARNING]"},
                new String[]{"LIST_ARGS", "facility list extra", "facility list does not accept arguments.",
                    "Known facilities"},
                new String[]{"OLD_VIEW", "facility view AS4", "Invalid facility command.", "Accessibility Features:"},
                new String[]{"VIEW_WORD", "facility view", "Invalid facility command.", "Accessibility Features:"},
                new String[]{"LOOKUP_ARGS", "facility AS4 extra", "Expected exactly one facility ID or name.",
                    "Accessibility Features:"},
                new String[]{"FIND_SHORT", "facility find", "Invalid facility find filters.", "Facilities with"},
                new String[]{"FIND_EXTRA", "facility find type/LIFT status/YES extra",
                    "Invalid facility find filters.", "Facilities with"},
                new String[]{"TYPE_PREFIX", "facility find lift", "Invalid facility find filters.", "Facilities with"},
                new String[]{"TYPE_EMPTY", "facility find type/", "Invalid facility find filters.", "Facilities with"},
                new String[]{"STATUS_PREFIX", "facility find type/LIFT NO", "Invalid facility find filters.",
                    "Facilities with"},
                new String[]{"STATUS_EMPTY", "facility find type/LIFT status/", "Invalid facility find filters.",
                    "Facilities with"},
                new String[]{"STATUS_BAD", "facility find type/LIFT status/MAYBE",
                    "Invalid facility status 'MAYBE'.", "Facilities with"},
                new String[]{"MISSPELLED", "faciliy", "Unrecognized command.", "Accessibility Features:"},
                new String[]{"BYE_ARGS", "bye extra", "bye takes no arguments. Usage: bye", "Added activity:"},
                new String[]{"BLANK", "", "Unrecognized command.", "Added activity:"},
                new String[]{"REST_POINT", "facility find type/REST_POINT", "[F08] AS8", "[F01] AS1"});
    }

    /**
     * Retains the original session cases directly in Java so fresh checkouts can run them.
     */
    private static List<String[]> sessionCases() {
        return List.of(
                new String[]{"ADD01", "add n/Lecture d/2026-10-15 s/09:00 e/11:00", "Added activity: Lecture",
                    "[WARNING]"},
                new String[]{"ADD02", "add n/Exam d/2026-10-16 s/14:00 e/16:00 dem/HIGH", "Demand: HIGH", "[WARNING]"},
                new String[]{"ADD03", "add n/Gym d/2026-10-17 s/18:00 e/19:00 dem/low", "Demand: LOW", "[WARNING]"},
                new String[]{"ADD04", "add e/12:00 n/Tutorial s/10:00 d/2026-10-18", "Added activity: Tutorial",
                    "[WARNING]"},
                new String[]{"ADD05", "add n/CS2113 Meeting d/2026-10-20 s/14:00 e/16:00 dem/HIGH",
                    "Added activity: CS2113 Meeting", "[WARNING]"},
                new String[]{"ADD06", "add", "No add parameters supplied.", "Added activity:"},
                new String[]{"ADD07", "add n/Event", "Missing d/ parameter.", "Added activity:"},
                new String[]{"ADD08", "add n/Event d/2026-10-21", "Missing s/ parameter.", "Added activity:"},
                new String[]{"ADD09", "add n/Event d/2026-10-21 s/10:00", "Missing e/ parameter.", "Added activity:"},
                new String[]{"ADD10", "add n/ d/2026-10-21 s/10:00 e/11:00", "Empty n/ parameter.", "Added activity:"},
                new String[]{"ADD11", "add n/Invalid Date d/2026-02-30 s/09:00 e/10:00", "Invalid date.",
                    "Added activity:"},
                new String[]{"ADD12", "add n/Invalid Time d/2026-10-21 s/25:00 e/26:00", "Invalid time.",
                    "Added activity:"},
                new String[]{"ADD13", "add n/Invalid Range d/2026-10-21 s/15:00 e/10:00",
                    "End time must be later than start time", "Added activity:"},
                new String[]{"ADD14", "add n/Equal Time d/2026-10-21 s/10:00 e/10:00",
                    "End time must be later than start time", "Added activity:"},
                new String[]{"ADD15", "add n/Meeting n/Class d/2026-10-21 s/10:00 e/11:00",
                    "Duplicate n/ parameter.", "Added activity:"},
                new String[]{"ADD16", "add n/Test d/2026-10-21 s/10:00 e/11:00 dem/URGENT",
                    "Demand must be LOW, MEDIUM, or HIGH.", "Added activity:"},
                new String[]{"ADD17", "add n/Test d/2026-10-21 s/10:00 e/11:00 loc/NUS", "Unknown parameter 'loc/'.",
                    "Added activity:"},
                new String[]{"FAC01", "facility AS4", "[F04] AS4", "[WARNING]"},
                new String[]{"FAC02", "facility clb", "[F09] CLB", "[WARNING]"},
                new String[]{"FAC03", "facility list", "[F09] CLB", "[WARNING]"},
                new String[]{"FAC04", "facility find type/LIFT status/NO", "[F01] AS1\n[F02] AS2", "[F03] AS3"},
                new String[]{"FAC05", "facility find type/ACCESSIBLE_WASHROOM", "[F04] AS4", "[F01] AS1"},
                new String[]{"FAC06", "facility AS99", "Unknown facility 'AS99'.", "Accessibility Features:"},
                new String[]{"FAC07", "facility", "Expected exactly one facility ID or name.",
                    "Accessibility Features:"},
                new String[]{"FAC08", "facility find type/WASHROOM", "Unknown facility feature type 'WASHROOM'.",
                    "Facilities with"},
                new String[]{"FAC09", "facility AS4", "[F04] AS4", "[WARNING]"},
                new String[]{"EXIT", "bye", "Goodbye from UniEnable!"});
    }

    /**
     * Captures an isolated console session and restores the caller's streams after execution.
     */
    private static String console(String input) throws Exception {
        Path tempDir = Files.createTempDirectory("unienable-console-test");
        Path activityFile = tempDir.resolve("activities.txt");
        try {
            return consoleAtPath(input, activityFile);
        } finally {
            Files.deleteIfExists(activityFile);
            Files.deleteIfExists(tempDir);
        }
    }

    private static String consoleAtPath(String input, Path activityFile) throws Exception {
        var originalInput = System.in;
        var originalOutput = System.out;
        var output = new ByteArrayOutputStream();
        try (var captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(captured);
            UniEnable.run(System.in, System.out, activityFile, FIXED_CLOCK);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
