package seedu.unienable.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

//@@author lukelouyu
import seedu.unienable.command.accessibility.connection.logic.FacilityManager;
//@@author
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Facility;
import seedu.unienable.model.FacilityFeature;
import seedu.unienable.model.enums.AccessibilityStatus;

//@@author lukelouyu
/**
 * Checks facility reference loading, malformed-record recovery, and I/O ownership.
 */
class FacilityStorageTest {
    private final FacilityStorage facilities = new FacilityStorage();

    @TestFactory
    Stream<DynamicTest> records() throws Exception {
        var rows = recordCases().stream();
        return rows.map(row -> DynamicTest.dynamicTest(row[0], () -> {
            LoadResult<Facility> result = facilities.load(new StringReader(row[2]));
            assertEquals(Integer.parseInt(row[3]), result.getRecords().size());
            assertEquals(Integer.parseInt(row[4]), result.getWarnings().size());
            assertEquals(!row[4].equals("0"), result.hasWarnings());
            assertEquals(row[5], String.join(",", result.getRecords().stream().map(this::describe).toList()));
            if (!row[6].isEmpty()) {
                assertTrue(String.join("\n", result.getWarnings()).contains(row[6]), result.getWarnings().toString());
            }
        }));
    }

    /**
     * Serializes each facility field and ordered features for exact fixture comparisons.
     */
    private String describe(Facility f) {
        return f.getId() + "|" + f.getName() + "|" + f.getDescription() + "|" + String.join(";",
                f.getFeatures().stream().map(x -> x.getType() + "|" + x.getStatus() + "|" + x.getNotes()).toList());
    }

    @Test
    void bundledDataset_preservesFacilitiesWithoutWarnings() throws Exception {
        var hubs = facilities.load();
        assertFalse(hubs.hasWarnings());
        assertEquals(List.of("AS1", "AS2", "AS3", "AS4", "AS5", "AS6", "AS7", "AS8", "CLB"),
                hubs.getRecords().stream().map(Facility::getName).toList());
    }

    /**
     * Reproduces contradictory feature searches for every ordered pair of distinct statuses.
     */
    @TestFactory
    Stream<DynamicTest> conflictingFeatureStatuses_keepFirstStatusAndWarn() {
        return Arrays.stream(AccessibilityStatus.values()).flatMap(firstStatus ->
                Arrays.stream(AccessibilityStatus.values()).filter(laterStatus -> laterStatus != firstStatus)
                        .map(laterStatus -> DynamicTest.dynamicTest(firstStatus + " then " + laterStatus, () -> {
                            String data = "FACILITY|F01|AS1|Sample building\n"
                                    + "FEATURE|F01|LIFT|" + firstStatus + "\n"
                                    + "FEATURE|F01|LIFT|" + laterStatus;
                            LoadResult<Facility> result = facilities.load(new StringReader(data));
                            assertEquals(List.of("Line 3: Conflicting accessibility status for LIFT at facility F01: "
                                    + "already recorded as " + firstStatus + ", rejected " + laterStatus + "."),
                                    result.getWarnings());
                            Facility facility = result.getRecords().getFirst();
                            assertEquals(1, facility.getFeatures().size());
                            assertEquals(firstStatus, facility.getFeatures().getFirst().getStatus());
                            FacilityManager manager = new FacilityManager(result.getRecords());
                            assertEquals(List.of(facility),
                                    manager.findByFeature(FacilityFeature.Type.LIFT, firstStatus));
                            assertTrue(manager.findByFeature(FacilityFeature.Type.LIFT, laterStatus).isEmpty());
                        })));
    }

    @Test
    void featuresBeforeFacility_reportConflictsAtPhysicalLineNumbers() throws Exception {
        String data = "FEATURE|F01|LIFT|YES|First note\n"
                + "# Comment\n\n"
                + "FEATURE|F01|LIFT|NO|Conflicting note\n"
                + "FACILITY|F01|AS1|\n"
                + "FEATURE|F01|LIFT|UNKNOWN";
        LoadResult<Facility> result = facilities.load(new StringReader(data));
        assertEquals(List.of(
                "Line 4: Conflicting accessibility status for LIFT at facility F01: "
                        + "already recorded as YES, rejected NO.",
                "Line 6: Conflicting accessibility status for LIFT at facility F01: "
                        + "already recorded as YES, rejected UNKNOWN."), result.getWarnings());
        assertEquals("F01|AS1|null|LIFT|YES|First note", describe(result.getRecords().getFirst()));
    }

    @Test
    void matchingStatuses_preserveNotesAndIndependentFeaturesAndFacilities() throws Exception {
        String data = "FACILITY|F01|AS1|\n"
                + "FACILITY|F02|AS2|\n"
                + "FEATURE|F01|LIFT|YES|First note\n"
                + "FEATURE|F01|RAMP|NO\n"
                + "FEATURE|F02|LIFT|NO\n"
                + "FEATURE|F01|LIFT|YES|Second note";
        LoadResult<Facility> result = facilities.load(new StringReader(data));
        assertFalse(result.hasWarnings());
        assertEquals(List.of("F01|AS1|null|LIFT|YES|First note;RAMP|NO|null;LIFT|YES|Second note",
                "F02|AS2|null|LIFT|NO|null"), result.getRecords().stream().map(this::describe).toList());
    }

    @Test
    void failures_reportCausesAndMissingResources() {
        assertThrows(UniEnableException.class, () -> facilities.loadResource("/missing-facilities"));
        IOException cause = new IOException("read failed");
        Reader broken = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw cause;
            }

            @Override
            public void close() {
                throw new AssertionError("Caller owns this reader");
            }
        };
        var error = assertThrows(UniEnableException.class, () -> facilities.load(broken));
        assertEquals(cause, error.getCause());
        assertTrue(error.getMessage().contains("read failed"));
        Reader closeFailure = new FilterReader(new StringReader("")) {
            @Override
            public void close() throws IOException {
                throw cause;
            }
        };
        var closeError = assertThrows(UniEnableException.class, () -> facilities.loadOwnedReader(closeFailure));
        assertEquals(cause, closeError.getCause());
    }

    /**
     * Retains the original storage cases directly in Java so fresh checkouts can run them.
     */
    private static List<String[]> recordCases() {
        return List.of(
                new String[]{"F_EMPTY", "F", "", "0", "0", "", ""},
                new String[]{"F_COMMENTS", "F", "# Header\n  # Indented\n\n  ", "0", "0", "", ""},
                new String[]{"F_VALID", "F", "FACILITY|F01|AS1|", "1", "0", "F01|AS1|null|", ""},
                new String[]{"F_BEFORE", "F", "FEATURE|F01|LIFT|YES|Note\nFACILITY|F01|AS1|", "1", "0",
                    "F01|AS1|null|LIFT|YES|Note", ""},
                new String[]{"F_OPTIONALS", "F",
                    "FACILITY|F01|AS1|  Café  \nFEATURE|F01|LIFT|NO\nFEATURE|F01|RAMP|UNKNOWN|", "1", "0",
                    "F01|AS1|  Café  |LIFT|NO|null;RAMP|UNKNOWN|null", ""},
                new String[]{"F_TAG", "F", "OTHER|F01|AS1|\nFACILITY|F01|AS1|", "1", "1", "F01|AS1|null|",
                    "Unknown record type"},
                new String[]{"F_SHORT", "F", "FACILITY|F01|AS1\nFACILITY|F01|AS1|", "1", "1", "F01|AS1|null|",
                    "exactly 4 fields"},
                new String[]{"F_LONG", "F", "FACILITY|F01|AS1||extra\nFACILITY|F01|AS1|", "1", "1", "F01|AS1|null|",
                    "exactly 4 fields"},
                new String[]{"F_ID_EMPTY", "F", "FACILITY||AS1|\nFACILITY|F01|AS1|", "1", "1", "F01|AS1|null|",
                    "must not be empty"},
                new String[]{"F_NAME_EMPTY", "F", "FACILITY|F01| |\nFACILITY|F01|AS1|", "1", "1", "F01|AS1|null|",
                    "must not be empty"},
                new String[]{"F_ID_SPACE", "F", "FACILITY| F01|AS1|\nFACILITY|F01|AS1|", "1", "1", "F01|AS1|null|",
                    "surrounding whitespace"},
                new String[]{"F_NAME_SPACE", "F", "FACILITY|F01|AS1 |\nFACILITY|F01|AS1|", "1", "1", "F01|AS1|null|",
                    "surrounding whitespace"},
                new String[]{"F_FEAT_SHORT", "F", "FEATURE|F01|LIFT\nFACILITY|F01|AS1|", "1", "1", "F01|AS1|null|",
                    "4 or 5 fields"},
                new String[]{"F_FEAT_LONG", "F", "FEATURE|F01|LIFT|YES|x|x\nFACILITY|F01|AS1|", "1", "1",
                    "F01|AS1|null|", "4 or 5 fields"},
                new String[]{"F_FEAT_UNKNOWN", "F", "FEATURE|F99|LIFT|YES\nFACILITY|F01|AS1|", "1", "1",
                    "F01|AS1|null|", "Unknown facility ID"},
                new String[]{"F_FEAT_TYPE", "F", "FEATURE|F01|BAD|YES\nFACILITY|F01|AS1|", "1", "1", "F01|AS1|null|",
                    "Invalid feature type"},
                new String[]{"F_FEAT_STATUS", "F", "FEATURE|F01|LIFT|MAYBE\nFACILITY|F01|AS1|", "1", "1",
                    "F01|AS1|null|", "Invalid accessibility status"},
                new String[]{"F_DUP_ID", "F", "FACILITY|F01|AS1|\nFACILITY|f01|AS2|\nFACILITY|F02|AS2|", "2", "1",
                    "F01|AS1|null|,F02|AS2|null|", "Line 2: Duplicate facility ID"},
                new String[]{"F_DUP_NAME", "F", "FACILITY|F01|AS1|\nFACILITY|F02|as1|\nFACILITY|F02|AS2|", "2", "1",
                    "F01|AS1|null|,F02|AS2|null|", "Line 2: Duplicate facility name"},
                new String[]{"F_UNICODE_ID", "F", "FACILITY|Fİ1|AS1|\nFACILITY|Fi1|AS2|\nFEATURE|Fİ1|LIFT|YES", "1",
                    "1", "Fİ1|AS1|null|LIFT|YES|null", "Duplicate facility ID"},
                new String[]{"F_UNICODE_REVERSE", "F", "FACILITY|Fi1|AS1|\nFACILITY|Fİ1|AS2|", "1", "1",
                    "Fi1|AS1|null|", "Duplicate facility ID"},
                new String[]{"F_UNICODE_NAME", "F", "FACILITY|F01|ASİ|\nFACILITY|F02|ASi|\nFACILITY|F02|AS2|", "2",
                    "1", "F01|ASİ|null|,F02|AS2|null|", "Duplicate facility name"},
                new String[]{"F_DISTINCT", "F", "FACILITY|Fß1|AS1|\nFACILITY|FSS1|AS2|", "2", "0",
                    "Fß1|AS1|null|,FSS1|AS2|null|", ""},
                new String[]{"F_FEATURE_LIFT_YES", "F", "FACILITY|F01|AS1|\nFEATURE|F01|LIFT|YES|Note", "1", "0",
                    "F01|AS1|null|LIFT|YES|Note", ""},
                new String[]{"F_FEATURE_LIFT_NO", "F", "FACILITY|F01|AS1|\nFEATURE|F01|LIFT|NO|Note", "1", "0",
                    "F01|AS1|null|LIFT|NO|Note", ""},
                new String[]{"F_FEATURE_LIFT_UNKNOWN", "F", "FACILITY|F01|AS1|\nFEATURE|F01|LIFT|UNKNOWN|Note", "1",
                    "0", "F01|AS1|null|LIFT|UNKNOWN|Note", ""},
                new String[]{"F_FEATURE_RAMP_YES", "F", "FACILITY|F01|AS1|\nFEATURE|F01|RAMP|YES|Note", "1", "0",
                    "F01|AS1|null|RAMP|YES|Note", ""},
                new String[]{"F_FEATURE_RAMP_NO", "F", "FACILITY|F01|AS1|\nFEATURE|F01|RAMP|NO|Note", "1", "0",
                    "F01|AS1|null|RAMP|NO|Note", ""},
                new String[]{"F_FEATURE_RAMP_UNKNOWN", "F", "FACILITY|F01|AS1|\nFEATURE|F01|RAMP|UNKNOWN|Note", "1",
                    "0", "F01|AS1|null|RAMP|UNKNOWN|Note", ""},
                new String[]{"F_FEATURE_SHELTERED_RAMP_YES", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|SHELTERED_RAMP|YES|Note", "1", "0",
                    "F01|AS1|null|SHELTERED_RAMP|YES|Note", ""},
                new String[]{"F_FEATURE_SHELTERED_RAMP_NO", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|SHELTERED_RAMP|NO|Note", "1", "0",
                    "F01|AS1|null|SHELTERED_RAMP|NO|Note", ""},
                new String[]{"F_FEATURE_SHELTERED_RAMP_UNKNOWN", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|SHELTERED_RAMP|UNKNOWN|Note", "1", "0",
                    "F01|AS1|null|SHELTERED_RAMP|UNKNOWN|Note", ""},
                new String[]{"F_FEATURE_ACCESSIBLE_WASHROOM_YES", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|ACCESSIBLE_WASHROOM|YES|Note", "1", "0",
                    "F01|AS1|null|ACCESSIBLE_WASHROOM|YES|Note", ""},
                new String[]{"F_FEATURE_ACCESSIBLE_WASHROOM_NO", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|ACCESSIBLE_WASHROOM|NO|Note", "1", "0",
                    "F01|AS1|null|ACCESSIBLE_WASHROOM|NO|Note", ""},
                new String[]{"F_FEATURE_ACCESSIBLE_WASHROOM_UNKNOWN", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|ACCESSIBLE_WASHROOM|UNKNOWN|Note", "1", "0",
                    "F01|AS1|null|ACCESSIBLE_WASHROOM|UNKNOWN|Note", ""},
                new String[]{"F_FEATURE_STEP_FREE_ENTRANCE_YES", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|STEP_FREE_ENTRANCE|YES|Note", "1", "0",
                    "F01|AS1|null|STEP_FREE_ENTRANCE|YES|Note", ""},
                new String[]{"F_FEATURE_STEP_FREE_ENTRANCE_NO", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|STEP_FREE_ENTRANCE|NO|Note", "1", "0",
                    "F01|AS1|null|STEP_FREE_ENTRANCE|NO|Note", ""},
                new String[]{"F_FEATURE_STEP_FREE_ENTRANCE_UNKNOWN", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|STEP_FREE_ENTRANCE|UNKNOWN|Note", "1", "0",
                    "F01|AS1|null|STEP_FREE_ENTRANCE|UNKNOWN|Note", ""},
                new String[]{"F_FEATURE_REST_POINT_YES", "F", "FACILITY|F01|AS1|\nFEATURE|F01|REST_POINT|YES|Note",
                    "1", "0", "F01|AS1|null|REST_POINT|YES|Note", ""},
                new String[]{"F_FEATURE_REST_POINT_NO", "F", "FACILITY|F01|AS1|\nFEATURE|F01|REST_POINT|NO|Note",
                    "1", "0", "F01|AS1|null|REST_POINT|NO|Note", ""},
                new String[]{"F_FEATURE_REST_POINT_UNKNOWN", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|REST_POINT|UNKNOWN|Note", "1", "0",
                    "F01|AS1|null|REST_POINT|UNKNOWN|Note", ""},
                new String[]{"F_FEATURE_AUTOMATIC_DOOR_YES", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|AUTOMATIC_DOOR|YES|Note", "1", "0",
                    "F01|AS1|null|AUTOMATIC_DOOR|YES|Note", ""},
                new String[]{"F_FEATURE_AUTOMATIC_DOOR_NO", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|AUTOMATIC_DOOR|NO|Note", "1", "0",
                    "F01|AS1|null|AUTOMATIC_DOOR|NO|Note", ""},
                new String[]{"F_FEATURE_AUTOMATIC_DOOR_UNKNOWN", "F",
                    "FACILITY|F01|AS1|\nFEATURE|F01|AUTOMATIC_DOOR|UNKNOWN|Note", "1", "0",
                    "F01|AS1|null|AUTOMATIC_DOOR|UNKNOWN|Note", ""},
                new String[]{"F_FEATURE_OTHER_YES", "F", "FACILITY|F01|AS1|\nFEATURE|F01|OTHER|YES|Note", "1", "0",
                    "F01|AS1|null|OTHER|YES|Note", ""},
                new String[]{"F_FEATURE_OTHER_NO", "F", "FACILITY|F01|AS1|\nFEATURE|F01|OTHER|NO|Note", "1", "0",
                    "F01|AS1|null|OTHER|NO|Note", ""},
                new String[]{"F_FEATURE_OTHER_UNKNOWN", "F", "FACILITY|F01|AS1|\nFEATURE|F01|OTHER|UNKNOWN|Note",
                    "1", "0", "F01|AS1|null|OTHER|UNKNOWN|Note", ""});
    }
}
//@@author
