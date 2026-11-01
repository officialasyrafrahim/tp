//@@author lukelouyu
package seedu.unienable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks printable-ASCII name input, ASCII command words, and historical UTF-8 output
 * through the real entry point and isolated storage.
 */
class UniEnableProcessTest {
    @Test
    void riddhisBirthday_acceptsApostropheAndLowercaseDemand(@TempDir Path tempDir) throws Exception {
        String output = runSession(tempDir,
                "add n/riddhi's birthday d/2027-01-01 s/20:00 e/23:30 dem/low\nlist\nbye\n");
        assertTrue(output.contains("Added activity: riddhi's birthday"), output);
        assertTrue(output.contains("Date: 2027-01-01\nTime: 20:00 - 23:30\nDemand: LOW"), output);
        assertTrue(output.contains("1. riddhi's birthday (2027-01-01, 20:00-23:30) [LOW] [ ]"), output);
        assertFalse(output.contains("[WARNING]"), output);
        assertEquals("riddhi's birthday|2027-01-01|20:00|23:30|LOW|false|-|-|-\n",
                Files.readString(tempDir.resolve("data/activities.txt")));
        String restarted = runSession(tempDir, "list\nbye\n");
        assertTrue(restarted.contains("1. riddhi's birthday (2027-01-01, 20:00-23:30) [LOW] [ ]"), restarted);
    }

    @Test
    void apostrophes_surviveAddEditSearchAndRestart(@TempDir Path tempDir) throws Exception {
        String output = runSession(tempDir, "add n/Student's meeting d/2027-12-10 s/09:00 e/10:00\n"
                + "edit 1 n/O'Brien's lecture\nfind O'Brien\nbye\n");
        assertTrue(output.contains("Added activity: Student's meeting"), output);
        assertTrue(output.contains("Edited activity: O'Brien's lecture"), output);
        assertTrue(output.contains("Found 1 matching activity:"), output);
        assertFalse(output.contains("[WARNING]"), output);
        String restarted = runSession(tempDir, "list\nbye\n");
        assertTrue(restarted.contains("1. O'Brien's lecture"), restarted);
    }

    @Test
    void emptyActivityCommands_shareNormalFeedbackAndDoNotSave(@TempDir Path tempDir) throws Exception {
        String[] commands = {"list", "list demand", "list priority", "view today", "view tomorrow", "view week",
            "overdue", "next", "find missing", "edit 1 n/Changed", "mark 1", "unmark 1", "delete 1"};
        String output = runSession(tempDir, String.join("\n", commands) + "\nbye\n");
        String message = "Sorry,";
        assertEquals(commands.length, output.split(Pattern.quote(message), -1).length - 1, output);
        for (String expected : new String[] {
            "Sorry, the activity list is empty.",
            "Sorry, no activities fall within the selected period.",
            "Sorry, no activity names match your search.",
            "Sorry, no incomplete activities have ended.",
            "Sorry, no upcoming incomplete activities exist."}) {
            assertTrue(output.contains(expected), output);
        }
        assertFalse(output.contains("[WARNING]"), output);
        assertFalse(Files.exists(tempDir.resolve("data/activities.txt")));
    }

    @Test
    void invalidActivityInput_retainsWarningsEvenWhenNoActivitiesExist(@TempDir Path tempDir) throws Exception {
        String output = runSession(tempDir, "delete 0\nedit 0 n/Changed\nmark 0\nunmark 0\n"
                + "list invalid\nfind\nview year\nbye\n");
        assertEquals(7, output.split(Pattern.quote("[WARNING]"), -1).length - 1, output);
        assertFalse(output.contains("Sorry, the activity list is empty."), output);
        assertFalse(Files.exists(tempDir.resolve("data/activities.txt")));
    }

    @Test
    void routeIds_matchNamesAndUnknownEndpointsListValidHubs(@TempDir Path tempDir) throws Exception {
        String named = runSession(tempDir, "route from/AS6 to/AS7\nbye\n");
        String ids = runSession(tempDir, "route from/f06 to/F07\nbye\n");
        assertEquals(named, ids);
        assertTrue(ids.contains("Total distance: 200 metres"), ids);
        String unknown = runSession(tempDir, "route from/ZZZ to/F07\nbye\n");
        assertTrue(unknown.contains("Valid hubs: AS1, AS2, AS3, AS4, AS5, AS6, AS7, AS8, CLB"), unknown);
    }

    @Test
    void historicalAdd_isRejectedWithoutSaving(@TempDir Path tempDir) throws Exception {
        String output = runSession(tempDir, "add n/Past tutorial d/2026-01-02 s/09:00 e/10:00\nbye\n");
        assertTrue(output.contains("[WARNING] Activities that have ended cannot be added."), output);
        assertFalse(output.contains("Added activity:"), output);
        assertFalse(Files.exists(tempDir.resolve("data/activities.txt")));
    }

    @Test
    void totalCount_includesActivitiesLoadedFromEarlierSessions(@TempDir Path tempDir) throws Exception {
        runSession(tempDir, "add n/First d/2027-12-10 s/09:00 e/10:00\nbye\n");
        String output = runSession(tempDir, "add n/Second d/2027-12-10 s/10:00 e/11:00\nbye\n");
        assertTrue(output.contains("Total activities: 2"), output);
        assertFalse(output.contains("Activities in this session"), output);
    }

    @Test
    void commas_surviveAddEditAndRestart(@TempDir Path tempDir) throws Exception {
        String output = runSession(tempDir, "add n/Lunch, then gym d/2027-12-10 s/09:00 e/10:00\n"
                + "edit 1 n/Lunch, then study\nbye\n");
        assertTrue(output.contains("Added activity: Lunch, then gym"), output);
        assertTrue(output.contains("Edited activity: Lunch, then study"), output);
        String restarted = runSession(tempDir, "list\nbye\n");
        assertTrue(restarted.contains("1. Lunch, then study"), restarted);
        assertFalse(output.contains("[WARNING]"), output);
    }

    @Test
    void doubleQuotedNames_rejectAddAndEditWithoutWritingData(@TempDir Path tempDir) throws Exception {
        String add = "add n/\"Discuss notes\" d/2030-12-10 s/09:00 e/10:00\n";
        String warning = "[WARNING] Activity names must not contain backslashes (\\)"
                + " or double quotation marks.";
        String emptyOutput = runSession(tempDir, add + "list\nbye\n");
        Path file = tempDir.resolve("data/activities.txt");
        assertTrue(emptyOutput.contains(warning), emptyOutput);
        assertFalse(emptyOutput.contains("Added activity:"), emptyOutput);
        assertFalse(Files.exists(file));
        Files.createDirectories(file.getParent());
        byte[] original = "Existing|2030-12-10|09:00|10:00|LOW|true|2|3|4\n".getBytes(StandardCharsets.UTF_8);
        Files.write(file, original);
        var modifiedTime = Files.getLastModifiedTime(file);
        String output = runSession(tempDir, add + "edit 1 n/\"Changed notes\" imp/5\nlist\nbye\n");
        assertEquals(2, output.split(Pattern.quote(warning), -1).length - 1, output);
        assertFalse(output.contains("Added activity:"), output);
        assertFalse(output.contains("Edited activity:"), output);
        assertTrue(output.contains("1. Existing"), output);
        assertArrayEquals(original, Files.readAllBytes(file));
        assertEquals(modifiedTime, Files.getLastModifiedTime(file));
    }

    @Test
    void unsupportedSlashes_rejectAddAndEditWithoutChangingSavedData(@TempDir Path tempDir) throws Exception {
        String add = "add n/Call Mom/Dad d/2027-12-10 s/09:00 e/10:00\n";
        String rejected = runSession(tempDir, add + "bye\n");
        assertTrue(rejected.contains("[WARNING] Unknown parameter 'mom/'"), rejected);
        Path file = tempDir.resolve("data/activities.txt");
        assertFalse(Files.exists(file));
        runSession(tempDir, "add n/Original d/2027-12-10 s/09:00 e/10:00\nbye\n");
        byte[] original = Files.readAllBytes(file);
        String edited = runSession(tempDir, "edit 1 n/Sem/Exam planning imp/5\nlist\nbye\n");
        assertTrue(edited.contains("[WARNING] Unknown parameter 'sem/'"), edited);
        assertFalse(edited.contains("Edited activity:"), edited);
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @Test
    void forbiddenPunctuation_preservesSavedBytesAndRestart(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("data/activities.txt");
        Files.createDirectories(file.getParent());
        byte[] original = "Existing|2030-12-10|09:00|10:00|LOW|true|2|3|4\n".getBytes(StandardCharsets.UTF_8);
        Files.write(file, original);
        var modifiedTime = Files.getLastModifiedTime(file);
        StringBuilder commands = new StringBuilder();
        String[] names = {"A\\B"};
        for (String name : names) {
            commands.append("add n/").append(name).append(" d/2030-12-11 s/09:00 e/10:00\n")
                    .append("edit 1 n/").append(name).append(" imp/5\n");
        }
        String output = runSession(tempDir, commands + "list\nbye\n");
        String warning = "[WARNING] Activity names must not contain backslashes (\\)"
                + " or double quotation marks.";
        assertEquals(names.length * 2, output.split(Pattern.quote(warning), -1).length - 1, output);
        assertFalse(output.contains("Added activity:"), output);
        assertFalse(output.contains("Edited activity:"), output);
        assertArrayEquals(original, Files.readAllBytes(file));
        assertEquals(modifiedTime, Files.getLastModifiedTime(file));
        String restarted = runSession(tempDir, "list\nbye\n");
        assertTrue(restarted.contains("1. Existing"), restarted);
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @Test
    void historicalActivities_rejectEditsWithoutChangingSavedData(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("data/activities.txt");
        Files.createDirectories(activityFile.getParent());
        byte[] original = "Historical|2026-01-01|09:00|10:00|LOW|true|2|3|4\n".getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        var originalModifiedTime = Files.getLastModifiedTime(activityFile);

        String output = runSession(tempDir, "edit 1 n/Changed dem/HIGH imp/5\n"
                + "edit 1 d/2030-01-01\nlist\nfind Historical\nbye\n");
        assertEquals(2, output.split(Pattern.quote(
                "[WARNING] Activities that have ended cannot be edited."), -1).length - 1, output);
        assertFalse(output.contains("Added activity:"), output);
        assertFalse(output.contains("Edited activity:"), output);
        assertTrue(output.contains("1. Historical (2026-01-01, 09:00-10:00) [LOW] [X]"), output);
        assertTrue(output.contains("Found 1 matching activity:"), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
        assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));

        String restarted = runSession(tempDir, "list\nbye\n");
        assertTrue(restarted.contains("1. Historical (2026-01-01, 09:00-10:00) [LOW] [X]"), restarted);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
    }

    @Test
    void digitPrefixTypos_rejectAddAndEditWithoutWritingData(@TempDir Path tempDir) throws Exception {
        String add = "add n/Class dem1/HIGH d/2030-12-10 s/10:00 e/11:00\n";
        String warning = "[WARNING] Unknown parameter 'dem1/'. Did you mean 'dem/'?";
        String emptyOutput = runSession(tempDir, add + "list\nbye\n");
        Path activityFile = tempDir.resolve("data/activities.txt");
        assertTrue(emptyOutput.contains(warning), emptyOutput);
        assertFalse(emptyOutput.contains("Added activity:"), emptyOutput);
        assertFalse(Files.exists(activityFile));

        Files.createDirectories(activityFile.getParent());
        byte[] original = "Existing|2030-12-10|09:00|10:00|LOW|true|2|3|4\n".getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        var originalModifiedTime = Files.getLastModifiedTime(activityFile);
        String output = runSession(tempDir, add + "edit 1 n/Class dem1/HIGH imp/5\nlist\nbye\n");
        assertEquals(2, output.split(Pattern.quote(warning), -1).length - 1, output);
        assertFalse(output.contains("Added activity:"), output);
        assertFalse(output.contains("Edited activity:"), output);
        assertTrue(output.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [X]"), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
        assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));
    }

    @Test
    void duplicateAndClashingCommandsPreserveCountAndStoredBytes(@TempDir Path tempDir) throws Exception {
        String setup = runSession(tempDir, "add n/CS2113 tutorial d/2030-12-01 s/10:00 e/11:00\n"
                + "add n/Later d/2030-12-01 s/12:00 e/13:00\nbye\n");
        assertTrue(setup.contains("Total activities: 2"), setup);
        Path activityFile = tempDir.resolve("data/activities.txt");
        byte[] original = Files.readAllBytes(activityFile);
        var originalModifiedTime = Files.getLastModifiedTime(activityFile);

        String output = runSession(tempDir, "add n/CS2113 tutorial d/2030-12-01 s/10:00 e/11:00\n"
                + "add n/Other d/2030-12-01 s/10:30 e/11:30\n"
                + "edit 2 n/CS2113 tutorial s/10:00 e/11:00 imp/5\n"
                + "edit 2 n/Changed s/10:30 e/11:30 imp/5\nlist\nbye\n");

        assertEquals(2, output.split(Pattern.quote("[WARNING] Duplicate activity"), -1).length - 1, output);
        assertEquals(2, output.split(Pattern.quote("[WARNING] Activity clashes with"), -1).length - 1, output);
        assertFalse(output.contains("Added activity:"), output);
        assertFalse(output.contains("Edited activity:"), output);
        assertFalse(output.contains("3. "), output);
        assertTrue(output.contains("1. CS2113 tutorial"), output);
        assertTrue(output.contains("2. Later"), output);
        assertTrue(output.contains("Goodbye from UniEnable!"), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
        assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));

        String restartedOutput = runSession(tempDir, "list\nbye\n");
        assertTrue(restartedOutput.contains("2. Later"), restartedOutput);
        assertFalse(restartedOutput.contains("3. "), restartedOutput);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
    }

    @Test
    void nonAsciiCommandWords_warnAndPreserveActivities(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("data/activities.txt");
        Files.createDirectories(activityFile.getParent());
        byte[] original = "Existing|2030-12-10|09:00|10:00|LOW|true|2|3|4\n".getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        var originalModifiedTime = Files.getLastModifiedTime(activityFile);
        String output = runSession(tempDir, "加入\nவணக்கம்\nபடி\ncafé\n🎉\nhe李lp\nbogus\nlist\nbye\n");

        String warning = "[WARNING] Please write the command in English (ASCII characters only).";
        String border = "_".repeat(60);
        assertEquals(6, output.split(Pattern.quote(warning), -1).length - 1, output);
        assertTrue(output.replace("\r\n", "\n").contains(border + "\n" + warning + "\n" + border), output);
        assertTrue(output.contains("[WARNING] Unrecognized command. Type help to see available commands."), output);
        assertTrue(output.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [X]"), output);
        assertTrue(output.contains("Goodbye from UniEnable!"), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
        assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));
    }

    @Test
    void chineseNames_rejectAddAndEditWithoutChangingStoredData(@TempDir Path tempDir) throws Exception {
        assertNameIsRejected(tempDir, "复习");
    }

    @Test
    void tamilGreeting_rejectsAddAndEditWithoutChangingStoredData(@TempDir Path tempDir) throws Exception {
        assertNameIsRejected(tempDir, "வணக்கம்");
    }

    @Test
    void tamilStudy_rejectsAddAndEditWithoutChangingStoredData(@TempDir Path tempDir) throws Exception {
        assertNameIsRejected(tempDir, "படி");
    }

    @Test
    void accentedNames_rejectAddAndEditWithoutChangingStoredData(@TempDir Path tempDir) throws Exception {
        assertNameIsRejected(tempDir, "Café");
    }

    @Test
    void emojiNames_rejectAddAndEditWithoutChangingStoredData(@TempDir Path tempDir) throws Exception {
        assertNameIsRejected(tempDir, "Study 🎉");
    }

    @Test
    void tabInName_rejectsAddAndEditWithoutChangingStoredData(@TempDir Path tempDir) throws Exception {
        assertNameIsRejected(tempDir, "Study\tGroup");
    }

    @Test
    void nulInName_rejectsAddAndEditWithoutChangingStoredData(@TempDir Path tempDir) throws Exception {
        assertNameIsRejected(tempDir, "Bad\u0000Name");
    }

    @Test
    void delInName_rejectsAddAndEditWithoutChangingStoredData(@TempDir Path tempDir) throws Exception {
        assertNameIsRejected(tempDir, "Bad\u007fName");
    }

    @Test
    void rejectedAdd_doesNotCreateActivityData(@TempDir Path tempDir) throws Exception {
        String[] names = {"复习", "வணக்கம்", "படி", "Café", "Study 🎉", "Study\tGroup",
            "Bad\u0000Name", "Bad\u007fName"};
        StringBuilder commands = new StringBuilder();
        for (String name : names) {
            commands.append("add n/").append(name).append(" d/2030-12-12 s/09:00 e/10:00\n");
        }
        String output = runSession(tempDir, commands + "list\nbye\n");

        assertEquals(names.length, output.split(Pattern.quote(invalidNameWarning()), -1).length - 1, output);
        assertFalse(output.contains("Added activity:"), output);
        assertTrue(output.contains("Sorry, the activity list is empty."), output);
        assertTrue(output.contains("Goodbye from UniEnable!"), output);
        assertFalse(Files.exists(tempDir.resolve("data/activities.txt")));
    }

    @Test
    void asciiNames_surviveAddingEditingAndRestart(@TempDir Path tempDir) throws Exception {
        String name = "CS2113 Planning review (Group 2)!";
        String updatedName = name + " revised";
        String output = runSession(tempDir, "add n/" + name
                + " d/2030-12-10 s/09:00 e/10:00\nedit 1 n/" + updatedName
                + "\nfind " + updatedName + "\nbye\n");

        assertTrue(output.contains("Added activity: " + name), output);
        assertTrue(output.contains("Edited activity: " + updatedName), output);
        assertTrue(output.contains("Found 1 matching activity:"), output);
        assertFalse(output.contains("[WARNING]"), output);
        Path activityFile = tempDir.resolve("data/activities.txt");
        byte[] saved = Files.readAllBytes(activityFile);
        assertTrue(new String(saved, StandardCharsets.UTF_8).contains(updatedName + "|2030-12-10"));

        String restartedOutput = runSession(tempDir, "list\nbye\n");
        assertTrue(restartedOutput.contains("1. " + updatedName + " (2030-12-10, 09:00-10:00)"), restartedOutput);
        assertArrayEquals(saved, Files.readAllBytes(activityFile));
    }

    @Test
    void historicalUnicodeNames_surviveLoadingAndRedirectedOutput(@TempDir Path tempDir) throws Exception {
        String name = "复习 வணக்கம் படி Café 🎉";
        Path activityFile = tempDir.resolve("data/activities.txt");
        Files.createDirectories(activityFile.getParent());
        byte[] original = (name + "|2030-12-10|09:00|10:00|LOW|false|-|-|-\n")
                .getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);

        for (int session = 0; session < 2; session++) {
            String output = runSession(tempDir, "list\nfind " + name + "\nbye\n");
            assertTrue(output.contains("1. " + name + " (2030-12-10, 09:00-10:00)"), output);
            assertTrue(output.contains("Found 1 matching activity:"), output);
            assertFalse(output.contains("Activity data warning:"), output);
            assertArrayEquals(original, Files.readAllBytes(activityFile));
        }
    }

    @Test
    void historicalUnicodeNames_editingOtherFields_preservesNameAndRestart(@TempDir Path tempDir) throws Exception {
        String name = "复习 வணக்கம் படி Café 🎉";
        Path activityFile = tempDir.resolve("data/activities.txt");
        Files.createDirectories(activityFile.getParent());
        Files.writeString(activityFile, name + "|2030-12-10|09:00|10:00|LOW|true|2|3|4\n", StandardCharsets.UTF_8);

        String output = runSession(tempDir, "edit 1 dem/HIGH imp/5\nbye\n");
        assertTrue(output.contains("Edited activity: " + name), output);
        assertFalse(output.contains("[WARNING]"), output);
        byte[] expected = (name + "|2030-12-10|09:00|10:00|HIGH|true|5|3|4\n").getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, Files.readAllBytes(activityFile));

        String restarted = runSession(tempDir, "list\nbye\n");
        assertTrue(restarted.contains("1. " + name + " (2030-12-10, 09:00-10:00) [HIGH] [X]"), restarted);
        assertArrayEquals(expected, Files.readAllBytes(activityFile));
    }

    @Test
    void rejectedEdit_doesNotLeakFieldsIntoLaterSuccessfulEdit(@TempDir Path tempDir) throws Exception {
        Path activityFile = tempDir.resolve("data/activities.txt");
        Files.createDirectories(activityFile.getParent());
        Files.writeString(activityFile, "Existing|2030-12-10|09:00|10:00|LOW|true|2|3|4\n", StandardCharsets.UTF_8);

        String output = runSession(tempDir, "edit 1 n/复习 d/2030-12-11 s/11:00 e/12:00"
                + " dem/HIGH imp/5 urg/5 com/5\nedit 1 n/Verified\nbye\n");

        assertTrue(output.contains(invalidNameWarning()), output);
        assertTrue(output.contains("Edited activity: Verified"), output);
        assertArrayEquals("Verified|2030-12-10|09:00|10:00|LOW|true|2|3|4\n".getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(activityFile));
    }

    /**
     * Checks rejected add/edit, unchanged saved bytes and modification time, and preserved restart state.
     */
    private static void assertNameIsRejected(Path tempDir, String name) throws Exception {
        Path activityFile = tempDir.resolve("data/activities.txt");
        Files.createDirectories(activityFile.getParent());
        byte[] original = "Existing|2030-12-10|09:00|10:00|LOW|true|2|3|4\n".getBytes(StandardCharsets.UTF_8);
        Files.write(activityFile, original);
        var originalModifiedTime = Files.getLastModifiedTime(activityFile);
        String output = runSession(tempDir, "add n/" + name
                + " d/2030-12-12 s/09:00 e/10:00\nedit 1 n/" + name
                + " d/2030-12-11 s/11:00 e/12:00 dem/HIGH imp/5 urg/5 com/5\nlist\nbye\n");

        assertEquals(2, output.split(Pattern.quote(invalidNameWarning()), -1).length - 1, output);
        assertFalse(output.contains("Added activity:"), output);
        assertFalse(output.contains("Edited activity:"), output);
        assertTrue(output.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [X]"), output);
        assertTrue(output.contains("Goodbye from UniEnable!"), output);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
        assertEquals(originalModifiedTime, Files.getLastModifiedTime(activityFile));

        String restarted = runSession(tempDir, "list\nbye\n");
        assertTrue(restarted.contains("1. Existing (2030-12-10, 09:00-10:00) [LOW] [X]"), restarted);
        assertArrayEquals(original, Files.readAllBytes(activityFile));
    }

    /**
     * Returns the expected user-facing warning shared by the input rejection scenarios.
     */
    private static String invalidNameWarning() {
        return "[WARNING] Please write the activity name in English (ASCII characters only).";
    }

    /**
     * Starts a separate JVM so injected UTF-8 test streams cannot hide platform encoding errors.
     * The ASCII stdout setting reproduces Windows character loss on every supported platform.
     */
    private static String runSession(Path directory, String commands) throws Exception {
        Path classes = Path.of(UniEnable.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path resources = Path.of(UniEnable.class.getResource("/facilities.txt").toURI()).getParent();
        String classpath = classes + File.pathSeparator + resources;
        Path stdout = directory.resolve("stdout.txt");
        Path stderr = directory.resolve("stderr.txt");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-ea", "-Dstdout.encoding=US-ASCII", "-cp", classpath, UniEnable.class.getName())
                .directory(directory.toFile())
                .redirectOutput(stdout.toFile())
                .redirectError(stderr.toFile())
                .start();
        try {
            try (var input = process.getOutputStream()) {
                input.write(commands.getBytes(StandardCharsets.UTF_8));
            }
            assertTrue(process.waitFor(15, TimeUnit.SECONDS), "Console process did not finish.");
            String output = Files.readString(stdout, StandardCharsets.UTF_8);
            assertEquals("", Files.readString(stderr, StandardCharsets.UTF_8));
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            process.destroyForcibly();
        }
    }
}
//@@author
