package seedu.unienable.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Connection;
import seedu.unienable.model.Facility;

//@@author lukelouyu
/**
 * Checks connection reference loading, malformed-record recovery, and I/O ownership.
 */
class ConnectionStorageTest {
    private final ConnectionStorage connections = new ConnectionStorage(List.of(
            new Facility("F01", "AS1", null, List.of()), new Facility("F02", "AS2", null, List.of())));

    @TestFactory
    Stream<DynamicTest> records() throws Exception {
        var rows = recordCases().stream();
        return rows.map(row -> DynamicTest.dynamicTest(row[0], () -> {
            LoadResult<Connection> result = connections.load(new StringReader(row[2]));
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
     * Serializes all routing fields, including restrictions and optional notes.
     */
    private String describe(Connection c) {
        return c.getId() + "|" + c.getFrom() + "|" + c.getTo() + "|" + c.getDistanceInMetres() + "|"
                + c.getAccessibility() + "|" + c.getType() + "|" + c.getShelter() + "|"
                + c.getKnownBarrier() + "|" + c.getNotes();
    }

    @Test
    void bundledGraph_preservesAllEndpointsDistancesAndRestrictions() throws Exception {
        var source = new ArrayList<>(List.of(new Facility("F01", "AS1", null, List.of()),
                new Facility("F02", "AS2", null, List.of())));
        var snapshot = new ConnectionStorage(source);
        source.clear();
        assertEquals(1, snapshot.load(new StringReader("CONNECTION|1|AS1|AS2|10|YES|PATH|YES")).getRecords().size());
        var hubs = new FacilityStorage().load();
        var edges = new ConnectionStorage(hubs.getRecords()).load();
        assertFalse(hubs.hasWarnings());
        assertFalse(edges.hasWarnings());
        assertEquals(List.of(70, 45, 90, 60, 50, 55, 130, 35, 55, 65),
                edges.getRecords().stream().map(Connection::getDistanceInMetres).toList());
        List<String> names = hubs.getRecords().stream().map(Facility::getName).toList();
        for (Connection edge : edges.getRecords()) {
            assertTrue(names.contains(edge.getFrom()) && names.contains(edge.getTo()));
        }
        assertEquals("Narrow passageway (2F link via LT9/LT10)", edges.getRecords().get(3).getKnownBarrier());
        assertEquals("Via The Deck", edges.getRecords().get(3).getNotes());
    }

    @Test
    void failures_reportCausesAndMissingResources() {
        assertThrows(UniEnableException.class, () -> connections.loadResource("/missing-connections"));
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
        var error = assertThrows(UniEnableException.class, () -> connections.load(broken));
        assertEquals(cause, error.getCause());
        assertTrue(error.getMessage().contains("read failed"));
        Reader closeFailure = new FilterReader(new StringReader("")) {
            @Override
            public void close() throws IOException {
                throw cause;
            }
        };
        var closeError = assertThrows(UniEnableException.class, () -> connections.loadOwnedReader(closeFailure));
        assertEquals(cause, closeError.getCause());
    }

    /**
     * Retains the original storage cases directly in Java so fresh checkouts can run them.
     */
    private static List<String[]> recordCases() {
        return List.of(
                new String[]{"C_EMPTY", "C", "", "0", "0", "", ""},
                new String[]{"C_COMMENTS", "C", "# Header\n  # Indented\n\n  \nCONNECTION|9|AS1|AS2|10|YES|PATH|YES",
                    "1", "0", "9|AS1|AS2|10|YES|PATH|YES|null|null", ""},
                new String[]{"C_VALID", "C", "CONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "0",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", ""},
                new String[]{"C_REVERSE", "C", "CONNECTION|9|AS2|AS1|10|YES|PATH|YES", "1", "0",
                    "9|AS2|AS1|10|YES|PATH|YES|null|null", ""},
                new String[]{"C_NINE", "C", "CONNECTION|9|AS1|AS2|10|YES|PATH|YES|Barrier", "1", "0",
                    "9|AS1|AS2|10|YES|PATH|YES|Barrier|null", ""},
                new String[]{"C_TEN", "C", "CONNECTION|9|AS1|AS2|10|YES|PATH|YES|Barrier|  Café 学生  ", "1", "0",
                    "9|AS1|AS2|10|YES|PATH|YES|Barrier|  Café 学生  ", ""},
                new String[]{"C_EMPTY_OPTIONALS", "C", "CONNECTION|9|AS1|AS2|10|YES|PATH|YES||", "1", "0",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", ""},
                new String[]{"C_DUP", "C",
                    "CONNECTION|9|AS1|AS2|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|20|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Line 2: Duplicate connection ID"},
                new String[]{"C_TAG", "C", "OTHER|1|AS1|AS2|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES",
                    "1", "1", "9|AS1|AS2|10|YES|PATH|YES|null|null", "8 to 10 fields"},
                new String[]{"C_SHORT", "C",
                    "CONNECTION|1|AS1|AS2|10|YES|PATH\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "8 to 10 fields"},
                new String[]{"C_LONG", "C",
                    "CONNECTION|9|AS1|AS2|10|YES|PATH|YES|||extra\nCONNECTION|9|AS1|AS2|10|YES|PA" + "TH|YES", "1",
                    "1", "9|AS1|AS2|10|YES|PATH|YES|null|null", "8 to 10 fields"},
                new String[]{"C_FROM", "C",
                    "CONNECTION|1|AS9|AS2|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Unknown facility endpoint"},
                new String[]{"C_TO", "C",
                    "CONNECTION|1|AS1|AS9|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Unknown facility endpoint"},
                new String[]{"C_IDS", "C",
                    "CONNECTION|1|F01|F02|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Unknown facility endpoint"},
                new String[]{"C_SELF", "C",
                    "CONNECTION|1|AS1|AS1|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Self-connection"},
                new String[]{"C_STATUS", "C",
                    "CONNECTION|1|AS1|AS2|10|MAYBE|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Invalid accessibility status"},
                new String[]{"C_TYPE", "C",
                    "CONNECTION|1|AS1|AS2|10|YES|STAIRS|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Invalid traversal type"},
                new String[]{"C_SHELTER", "C",
                    "CONNECTION|1|AS1|AS2|10|YES|PATH|MAYBE\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Invalid shelter status"},
                new String[]{"C_EMPTY_1", "C",
                    "CONNECTION||AS1|AS2|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Mandatory connection field"},
                new String[]{"C_EMPTY_2", "C",
                    "CONNECTION|9||AS2|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Mandatory connection field"},
                new String[]{"C_EMPTY_3", "C",
                    "CONNECTION|9|AS1||10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Mandatory connection field"},
                new String[]{"C_EMPTY_4", "C",
                    "CONNECTION|9|AS1|AS2||YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Mandatory connection field"},
                new String[]{"C_EMPTY_5", "C",
                    "CONNECTION|9|AS1|AS2|10||PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Mandatory connection field"},
                new String[]{"C_EMPTY_6", "C",
                    "CONNECTION|9|AS1|AS2|10|YES||YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Mandatory connection field"},
                new String[]{"C_EMPTY_7", "C",
                    "CONNECTION|9|AS1|AS2|10|YES|PATH|\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Mandatory connection field"},
                new String[]{"C_ID_abc", "C",
                    "CONNECTION|abc|AS1|AS2|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Invalid connection ID"},
                new String[]{"C_DISTANCE_abc", "C",
                    "CONNECTION|9|AS1|AS2|abc|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Invalid distance"},
                new String[]{"C_ID_1.5", "C",
                    "CONNECTION|1.5|AS1|AS2|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Invalid connection ID"},
                new String[]{"C_DISTANCE_1.5", "C",
                    "CONNECTION|9|AS1|AS2|1.5|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "Invalid distance"},
                new String[]{"C_ID_2147483648", "C",
                    "CONNECTION|2147483648|AS1|AS2|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|P" + "ATH|YES", "1",
                    "1", "9|AS1|AS2|10|YES|PATH|YES|null|null", "Invalid connection ID"},
                new String[]{"C_DISTANCE_2147483648", "C",
                    "CONNECTION|9|AS1|AS2|2147483648|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PA" + "TH|YES", "1",
                    "1", "9|AS1|AS2|10|YES|PATH|YES|null|null", "Invalid distance"},
                new String[]{"C_DISTANCE_0", "C",
                    "CONNECTION|9|AS1|AS2|0|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "distance must be positive"},
                new String[]{"C_DISTANCE_-1", "C",
                    "CONNECTION|9|AS1|AS2|-1|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "1",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", "distance must be positive"},
                new String[]{"C_ENUM_YES_PATH_YES", "C", "CONNECTION|9|AS1|AS2|10|YES|PATH|YES", "1", "0",
                    "9|AS1|AS2|10|YES|PATH|YES|null|null", ""},
                new String[]{"C_ENUM_YES_PATH_NO", "C", "CONNECTION|9|AS1|AS2|10|YES|PATH|NO", "1", "0",
                    "9|AS1|AS2|10|YES|PATH|NO|null|null", ""},
                new String[]{"C_ENUM_YES_PATH_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|YES|PATH|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|YES|PATH|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_YES_RAMP_YES", "C", "CONNECTION|9|AS1|AS2|10|YES|RAMP|YES", "1", "0",
                    "9|AS1|AS2|10|YES|RAMP|YES|null|null", ""},
                new String[]{"C_ENUM_YES_RAMP_NO", "C", "CONNECTION|9|AS1|AS2|10|YES|RAMP|NO", "1", "0",
                    "9|AS1|AS2|10|YES|RAMP|NO|null|null", ""},
                new String[]{"C_ENUM_YES_RAMP_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|YES|RAMP|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|YES|RAMP|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_YES_LIFT_YES", "C", "CONNECTION|9|AS1|AS2|10|YES|LIFT|YES", "1", "0",
                    "9|AS1|AS2|10|YES|LIFT|YES|null|null", ""},
                new String[]{"C_ENUM_YES_LIFT_NO", "C", "CONNECTION|9|AS1|AS2|10|YES|LIFT|NO", "1", "0",
                    "9|AS1|AS2|10|YES|LIFT|NO|null|null", ""},
                new String[]{"C_ENUM_YES_LIFT_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|YES|LIFT|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|YES|LIFT|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_NO_PATH_YES", "C", "CONNECTION|9|AS1|AS2|10|NO|PATH|YES", "1", "0",
                    "9|AS1|AS2|10|NO|PATH|YES|null|null", ""},
                new String[]{"C_ENUM_NO_PATH_NO", "C", "CONNECTION|9|AS1|AS2|10|NO|PATH|NO", "1", "0",
                    "9|AS1|AS2|10|NO|PATH|NO|null|null", ""},
                new String[]{"C_ENUM_NO_PATH_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|NO|PATH|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|NO|PATH|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_NO_RAMP_YES", "C", "CONNECTION|9|AS1|AS2|10|NO|RAMP|YES", "1", "0",
                    "9|AS1|AS2|10|NO|RAMP|YES|null|null", ""},
                new String[]{"C_ENUM_NO_RAMP_NO", "C", "CONNECTION|9|AS1|AS2|10|NO|RAMP|NO", "1", "0",
                    "9|AS1|AS2|10|NO|RAMP|NO|null|null", ""},
                new String[]{"C_ENUM_NO_RAMP_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|NO|RAMP|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|NO|RAMP|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_NO_LIFT_YES", "C", "CONNECTION|9|AS1|AS2|10|NO|LIFT|YES", "1", "0",
                    "9|AS1|AS2|10|NO|LIFT|YES|null|null", ""},
                new String[]{"C_ENUM_NO_LIFT_NO", "C", "CONNECTION|9|AS1|AS2|10|NO|LIFT|NO", "1", "0",
                    "9|AS1|AS2|10|NO|LIFT|NO|null|null", ""},
                new String[]{"C_ENUM_NO_LIFT_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|NO|LIFT|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|NO|LIFT|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_PATH_YES", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|PATH|YES", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|PATH|YES|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_PATH_NO", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|PATH|NO", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|PATH|NO|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_PATH_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|PATH|UNKNOWN", "1",
                    "0", "9|AS1|AS2|10|UNKNOWN|PATH|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_RAMP_YES", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|RAMP|YES", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|RAMP|YES|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_RAMP_NO", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|RAMP|NO", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|RAMP|NO|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_RAMP_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|RAMP|UNKNOWN", "1",
                    "0", "9|AS1|AS2|10|UNKNOWN|RAMP|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_LIFT_YES", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|LIFT|YES", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|LIFT|YES|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_LIFT_NO", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|LIFT|NO", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|LIFT|NO|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_LIFT_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|LIFT|UNKNOWN", "1",
                    "0", "9|AS1|AS2|10|UNKNOWN|LIFT|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_YES_SHELTERED_RAMP_YES", "C", "CONNECTION|9|AS1|AS2|10|YES|SHELTERED_RAMP|YES",
                    "1", "0", "9|AS1|AS2|10|YES|SHELTERED_RAMP|YES|null|null", ""},
                new String[]{"C_ENUM_YES_SHELTERED_RAMP_NO", "C", "CONNECTION|9|AS1|AS2|10|YES|SHELTERED_RAMP|NO",
                    "1", "0", "9|AS1|AS2|10|YES|SHELTERED_RAMP|NO|null|null", ""},
                new String[]{"C_ENUM_YES_SHELTERED_RAMP_UNKNOWN", "C",
                    "CONNECTION|9|AS1|AS2|10|YES|SHELTERED_RAMP|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|YES|SHELTERED_RAMP|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_YES_OTHER_YES", "C", "CONNECTION|9|AS1|AS2|10|YES|OTHER|YES", "1", "0",
                    "9|AS1|AS2|10|YES|OTHER|YES|null|null", ""},
                new String[]{"C_ENUM_YES_OTHER_NO", "C", "CONNECTION|9|AS1|AS2|10|YES|OTHER|NO", "1", "0",
                    "9|AS1|AS2|10|YES|OTHER|NO|null|null", ""},
                new String[]{"C_ENUM_YES_OTHER_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|YES|OTHER|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|YES|OTHER|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_NO_SHELTERED_RAMP_YES", "C", "CONNECTION|9|AS1|AS2|10|NO|SHELTERED_RAMP|YES",
                    "1", "0", "9|AS1|AS2|10|NO|SHELTERED_RAMP|YES|null|null", ""},
                new String[]{"C_ENUM_NO_SHELTERED_RAMP_NO", "C", "CONNECTION|9|AS1|AS2|10|NO|SHELTERED_RAMP|NO", "1",
                    "0", "9|AS1|AS2|10|NO|SHELTERED_RAMP|NO|null|null", ""},
                new String[]{"C_ENUM_NO_SHELTERED_RAMP_UNKNOWN", "C",
                    "CONNECTION|9|AS1|AS2|10|NO|SHELTERED_RAMP|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|NO|SHELTERED_RAMP|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_NO_OTHER_YES", "C", "CONNECTION|9|AS1|AS2|10|NO|OTHER|YES", "1", "0",
                    "9|AS1|AS2|10|NO|OTHER|YES|null|null", ""},
                new String[]{"C_ENUM_NO_OTHER_NO", "C", "CONNECTION|9|AS1|AS2|10|NO|OTHER|NO", "1", "0",
                    "9|AS1|AS2|10|NO|OTHER|NO|null|null", ""},
                new String[]{"C_ENUM_NO_OTHER_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|NO|OTHER|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|NO|OTHER|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_SHELTERED_RAMP_YES", "C",
                    "CONNECTION|9|AS1|AS2|10|UNKNOWN|SHELTERED_RAMP|YES", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|SHELTERED_RAMP|YES|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_SHELTERED_RAMP_NO", "C",
                    "CONNECTION|9|AS1|AS2|10|UNKNOWN|SHELTERED_RAMP|NO", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|SHELTERED_RAMP|NO|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_SHELTERED_RAMP_UNKNOWN", "C",
                    "CONNECTION|9|AS1|AS2|10|UNKNOWN|SHELTERED_RAMP|UNKNOWN", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|SHELTERED_RAMP|UNKNOWN|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_OTHER_YES", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|OTHER|YES", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|OTHER|YES|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_OTHER_NO", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|OTHER|NO", "1", "0",
                    "9|AS1|AS2|10|UNKNOWN|OTHER|NO|null|null", ""},
                new String[]{"C_ENUM_UNKNOWN_OTHER_UNKNOWN", "C", "CONNECTION|9|AS1|AS2|10|UNKNOWN|OTHER|UNKNOWN",
                    "1", "0", "9|AS1|AS2|10|UNKNOWN|OTHER|UNKNOWN|null|null", ""},
                new String[]{"C_NEIGHBOURS", "C",
                    "# Header\n\nCONNECTION|9|AS1|AS9|10|YES|PATH|YES\nCONNECTION|9|AS1|AS2|10|YES|"
                        + "PATH|YES\nBROKEN", "1", "2", "9|AS1|AS2|10|YES|PATH|YES|null|null",
                    "Line 3: Unknown facility endpoint: AS1 / AS9\nLine 5: CONNECTION requires 8 " + "to 10 fields."});
    }
}
//@@author
