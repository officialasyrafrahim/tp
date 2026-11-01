package seedu.unienable.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;

//@@author Oliver-koh
/**
 * Tests legacy compatibility, rated activity persistence and protection of existing files.
 */
class StorageTest {
    @TempDir
    Path tempDir;

    @Test
    void load_missingFileReturnsAnEmptyListWithoutWarnings() throws Exception {
        Storage storage = new Storage(tempDir.resolve("activities.txt"));

        ActivityList activities = storage.load();

        assertTrue(activities.getActivities().isEmpty());
        assertTrue(storage.getWarnings().isEmpty());
    }

    @Test
    void saveAndLoad_roundTripsAllActivityFields() throws Exception {
        Path file = tempDir.resolve("nested").resolve("activities.txt");
        Storage storage = new Storage(file);
        Integer[][] ratings = {
            {null, null, null}, {5, null, null}, {null, 4, null}, {null, null, 3},
            {5, 4, null}, {5, null, 3}, {null, 4, 3}, {5, 4, 3}
        };
        List<Activity> originals = new ArrayList<>();
        for (int i = 0; i < ratings.length; i++) {
            originals.add(new Activity("Review and/or study " + i, LocalDate.of(2026, 1, 1),
                    LocalTime.of(9, 0), LocalTime.of(10, 0), DemandLevel.HIGH, i % 2 == 0,
                    ratings[i][0], ratings[i][1], ratings[i][2]));
        }

        storage.save(new ActivityList(originals));
        Storage restarted = new Storage(file);
        List<Activity> loaded = restarted.load().getActivities();

        List<String> rows = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals("Review and/or study 0|2026-01-01|09:00|10:00|HIGH|true|-|-|-", rows.getFirst());
        assertEquals("Review and/or study 1|2026-01-01|09:00|10:00|HIGH|false|5|-|-", rows.get(1));
        assertEquals("Review and/or study 7|2026-01-01|09:00|10:00|HIGH|false|5|4|3", rows.getLast());
        assertEquals(originals.size(), loaded.size());
        for (int i = 0; i < originals.size(); i++) {
            Activity expected = originals.get(i);
            Activity actual = loaded.get(i);
            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getDate(), actual.getDate());
            assertEquals(expected.getStartTime(), actual.getStartTime());
            assertEquals(expected.getEndTime(), actual.getEndTime());
            assertEquals(expected.getDemand(), actual.getDemand());
            assertEquals(expected.isDone(), actual.isDone());
            assertEquals(expected.getImportance(), actual.getImportance());
            assertEquals(expected.getUrgency(), actual.getUrgency());
            assertEquals(expected.getCommitment(), actual.getCommitment());
        }
        assertTrue(restarted.getWarnings().isEmpty());
        assertFalse(Files.exists(file.resolveSibling("activities.txt.tmp")));
    }

    @Test
    void load_legacyAndRatedRows_migratesOnlyOnSave() throws Exception {
        Path file = tempDir.resolve("activities.txt");
        String legacyDone = "Legacy done|2026-01-01|09:00|10:00|HIGH|true";
        String legacyPending = "Legacy pending|2027-01-01|10:00|11:00|LOW|false";
        String rated = "Rated|2028-01-01|11:00|12:00|MEDIUM|false|2|-|5";
        String original = String.join("\n", legacyDone, legacyPending, rated);
        Files.writeString(file, original, StandardCharsets.UTF_8);
        Storage storage = new Storage(file);

        ActivityList loaded = storage.load();

        assertEquals(List.of("Legacy done", "Legacy pending", "Rated"),
                loaded.getActivities().stream().map(Activity::getName).toList());
        for (Activity legacy : loaded.getActivities().subList(0, 2)) {
            assertNull(legacy.getImportance());
            assertNull(legacy.getUrgency());
            assertNull(legacy.getCommitment());
        }
        assertTrue(loaded.getActivities().get(0).isDone());
        assertFalse(loaded.getActivities().get(1).isDone());
        assertTrue(storage.getWarnings().isEmpty());
        assertEquals(original, Files.readString(file, StandardCharsets.UTF_8));

        storage.save(loaded);

        assertEquals(List.of(legacyDone + "|-|-|-", legacyPending + "|-|-|-", rated),
                Files.readAllLines(file, StandardCharsets.UTF_8));
        assertFalse(Files.exists(tempDir.resolve("activities.txt.corrupt.bak")));
    }

    @Test
    void load_skipsMalformedRowsAndReportsLineNumbers() throws Exception {
        Path file = tempDir.resolve("activities.txt");
        String valid = "Valid|2026-01-01|09:00|10:00|HIGH|false";
        List<String> invalid = new ArrayList<>(List.of(
                "broken row",
                "BadDate|2026-02-30|09:00|10:00|LOW|false",
                "BadTime|2026-01-01|25:00|10:00|LOW|false",
                "BadDemand|2026-01-01|09:00|10:00|URGENT|false",
                "BadDone|2026-01-01|09:00|10:00|LOW|maybe",
                valid + "|1", valid + "|1|2", valid + "|1|2|3|4"));
        for (String value : new String[] {"", "0", "6", "-1", "1.5", "text", "2147483648"}) {
            for (int field = 0; field < 3; field++) {
                String[] ratings = {"1", "2", "3"};
                ratings[field] = value;
                invalid.add(valid + "|" + String.join("|", ratings));
            }
        }
        List<String> rows = new ArrayList<>();
        rows.add(valid);
        rows.addAll(invalid);
        rows.add(valid + "|1|-|5");
        Files.writeString(file, String.join("\n", rows), StandardCharsets.UTF_8);
        Storage storage = new Storage(file);

        ActivityList loaded = storage.load();

        assertEquals(2, loaded.getActivities().size());
        assertEquals(invalid.size(), storage.getWarnings().size());
        for (int i = 0; i < invalid.size(); i++) {
            assertTrue(storage.getWarnings().get(i).startsWith("Line " + (i + 2) + ":"));
        }
    }

    @Test
    void load_invalidTimeRanges_warnsAndPreservesOriginalBeforeRecoverySave() throws Exception {
        String valid = "Historical|2026-01-01|09:00|10:00|LOW|false";
        String[] ranges = {"09:00|09:00", "10:00|09:00"};
        for (int range = 0; range < ranges.length; range++) {
            for (boolean rated : new boolean[] {false, true}) {
                Path file = tempDir.resolve("invalid-range-" + range + "-" + rated + ".txt");
                String invalid = "Invalid|2026-01-02|" + ranges[range] + "|HIGH|false"
                        + (rated ? "|5|4|3" : "");
                String original = valid + "\n" + invalid + "\n";
                Files.writeString(file, original, StandardCharsets.UTF_8);
                Storage storage = new Storage(file);

                ActivityList loaded = storage.load();

                assertEquals(List.of("Historical"), loaded.getActivities().stream().map(Activity::getName).toList(),
                        invalid);
                assertEquals(1, storage.getWarnings().size(), invalid);
                assertTrue(storage.getWarnings().getFirst().startsWith("Line 2:"), invalid);
                assertEquals(original, Files.readString(file, StandardCharsets.UTF_8));

                storage.save(loaded);

                assertEquals(valid + "|-|-|-\n", Files.readString(file, StandardCharsets.UTF_8));
                assertArrayEquals(original.getBytes(StandardCharsets.UTF_8),
                        Files.readAllBytes(file.resolveSibling(file.getFileName() + ".corrupt.bak")));
            }
        }
    }

    @Test
    void save_afterMalformedLoad_preservesOriginalInBackupBeforeRepairingFile() throws Exception {
        Path file = tempDir.resolve("activities.txt");
        String original = "Valid|2026-10-18|09:00|11:00|HIGH|false\n"
                + "BadRating|2026-10-18|09:00|11:00|HIGH|false|6|-|-\ninvalid";
        Files.writeString(file, original, StandardCharsets.UTF_8);
        Storage storage = new Storage(file);
        ActivityList loaded = storage.load();

        storage.save(loaded);

        assertEquals("Valid|2026-10-18|09:00|11:00|HIGH|false|-|-|-\n",
                Files.readString(file, StandardCharsets.UTF_8));
        assertArrayEquals(original.getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(tempDir.resolve("activities.txt.corrupt.bak")));
    }

    @Test
    void save_afterInvalidUtf8Row_preservesOriginalBytesInBackup() throws Exception {
        Path file = tempDir.resolve("activities.txt");
        byte[] original = {0x4F, 0x6C, 0x64, (byte) 0xFF, 0x0A};
        Files.write(file, original);
        Storage storage = new Storage(file);
        assertTrue(storage.load().getActivities().isEmpty());
        assertTrue(storage.getWarnings().getFirst().contains("invalid UTF-8 encoding"));
        Activity added = new Activity("New meeting", LocalDate.of(2026, 10, 20), LocalTime.of(9, 0),
                LocalTime.of(10, 0), DemandLevel.MEDIUM, false);

        storage.save(new ActivityList(List.of(added)));

        assertArrayEquals(original, Files.readAllBytes(tempDir.resolve("activities.txt.corrupt.bak")));
        assertEquals("New meeting|2026-10-20|09:00|10:00|MEDIUM|false|-|-|-\n", Files.readString(file));
    }

    @Test
    void load_invalidUtf8BetweenValidRows_recoversReadableRecordsWithoutReplacingBytes() throws Exception {
        Path file = tempDir.resolve("activities.txt");
        byte[] original = ("Before|2027-12-01|09:00|10:00|LOW|false\r\n"
                + "Bad\u00ff|2027-12-01|10:00|11:00|LOW|false\r\n"
                + "After|2027-12-01|11:00|12:00|LOW|false\r\n").getBytes(StandardCharsets.ISO_8859_1);
        Files.write(file, original);
        Storage storage = new Storage(file);
        ActivityList loaded = storage.load();
        assertEquals(List.of("Before", "After"), loaded.getActivities().stream().map(Activity::getName).toList());
        assertEquals(List.of("Line 2: invalid UTF-8 encoding; row skipped. Save the file as UTF-8."),
                storage.getWarnings());
        assertArrayEquals(original, Files.readAllBytes(file));
        storage.save(loaded);
        assertArrayEquals(original, Files.readAllBytes(tempDir.resolve("activities.txt.corrupt.bak")));
        assertEquals(2, new Storage(file).load().getActivities().size());
    }

    @Test
    void save_whenRecoveryBackupCannotBeCreated_preservesOriginalFile() throws Exception {
        Path file = tempDir.resolve("activities.txt");
        byte[] original = "BadRating|2026-01-01|09:00|10:00|HIGH|false|6|-|-\n"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(file, original);
        Storage storage = new Storage(file);
        ActivityList loaded = storage.load();
        Path blockedBackup = Files.createDirectory(tempDir.resolve("activities.txt.corrupt.bak"));
        Files.writeString(blockedBackup.resolve("keep.txt"), "Backup destination is occupied.");

        assertTrue(loaded.getActivities().isEmpty());
        assertEquals(1, storage.getWarnings().size());
        assertThrows(UniEnableException.class, () -> storage.save(loaded));

        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @Test
    void save_whenTemporaryWriteFails_preservesOriginalFile() throws Exception {
        Path file = tempDir.resolve("activities.txt");
        String original = "Valid|2026-01-01|09:00|10:00|HIGH|true\n";
        Files.writeString(file, original, StandardCharsets.UTF_8);
        Storage storage = new Storage(file);
        ActivityList loaded = storage.load();
        Files.createDirectory(tempDir.resolve("activities.txt.tmp"));

        assertThrows(UniEnableException.class, () -> storage.save(loaded));

        assertEquals(original, Files.readString(file, StandardCharsets.UTF_8));
    }
}
//@@author
