//@@author lukelouyu
package seedu.unienable.command.accessibility.connection.logic;

//@@author

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Connection;
import seedu.unienable.model.enums.AccessibilityStatus;
import seedu.unienable.model.enums.ShelterStatus;
import seedu.unienable.model.enums.TraversalType;
import seedu.unienable.storage.ConnectionStorage;
import seedu.unienable.storage.FacilityStorage;

//@@author lukelouyu
/**
 * Protects endpoint identity and combined filters for accessible route graph construction.
 */
class ConnectionManagerTest {
    private final Connection first = new Connection(1, "AS1", "AS2", 10, AccessibilityStatus.YES,
            TraversalType.PATH, ShelterStatus.YES, null, null);
    private final Connection second = new Connection(2, "AS2", "AS3", 20, AccessibilityStatus.UNKNOWN,
            TraversalType.RAMP, ShelterStatus.NO, "Step", "Note");
    private final ConnectionManager connections = new ConnectionManager(List.of(first, second));

    @TestFactory
    Stream<DynamicTest> filters() throws Exception {
        return filterCases().stream().map(row -> DynamicTest.dynamicTest(row[0], () -> {
            var matches = connections.findConnections(text(row[1]), text(row[2]),
                    row[3].isEmpty() ? null : TraversalType.valueOf(row[3]),
                    row[4].isEmpty() ? null : AccessibilityStatus.valueOf(row[4]),
                    row[5].isEmpty() ? null : ShelterStatus.valueOf(row[5]));
            assertEquals(row[6], String.join(",", matches.stream().map(c -> "" + c.getId()).toList()));
        }));
    }

    /**
     * Converts an omitted endpoint filter to null.
     */
    private String text(String value) {
        return value.isEmpty() ? null : value;
    }

    @Test
    void connectionBoundaries_rejectEmptyFiltersAndPreserveSnapshots() {
        assertThrows(IllegalArgumentException.class, () -> connections.findConnections(null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> connections.findConnections(" ", null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> connections.findConnections(null, " ", null, null, null));
        assertSame(first, connections.findConnection(1).orElseThrow());
        assertTrue(connections.findConnection(99).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> connections.getConnections().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> connections.findConnections("AS1", null, null, null, null).clear());
        var source = new ArrayList<>(List.of(first));
        var snapshot = new ConnectionManager(source);
        source.clear();
        assertEquals(List.of(first), snapshot.getConnections());
        assertTrue(new ConnectionManager(List.of()).findConnections("AS1", null, null, null, null).isEmpty());
    }

    @Test
    void findRoute_usesLoadedCampusConnectionsInBothDirections() throws UniEnableException {
        var facilities = new FacilityStorage().load().getRecords();
        var loaded = new ConnectionStorage(facilities).load();
        assertTrue(loaded.getWarnings().isEmpty());
        var manager = new ConnectionManager(loaded.getRecords());
        var route = manager.findRoute(" as2 ", "clb").orElseThrow();
        assertEquals(List.of("AS2", "AS1", "AS6", "CLB"), route.stops());
        assertEquals(List.of(7, 3, 1), route.segments().stream().map(Connection::getId).toList());
        assertEquals(290, route.distanceInMetres());
        assertEquals(List.of("CLB", "AS6", "AS1", "AS2"),
                manager.findRoute("CLB", "AS2").orElseThrow().stops());
        assertEquals(290, manager.findRoute("AS2", "CLB").orElseThrow().distanceInMetres());
    }

    @Test
    void findRoute_preservesSnapshotAndRejectsUnknownEndpoints() {
        var source = new ArrayList<>(List.of(first, second));
        var manager = new ConnectionManager(source);
        source.clear();
        var route = manager.findRoute("AS2", "AS1").orElseThrow();
        assertEquals(List.of("AS2", "AS1"), route.stops());
        assertSame(first, route.segments().getFirst());
        assertEquals(10, route.distanceInMetres());
        assertTrue(manager.findRoute("AS1", "AS3").isEmpty());
        assertEquals(0, manager.findRoute("AS3", "AS3").orElseThrow().distanceInMetres());
        assertThrows(IllegalArgumentException.class, () -> manager.findRoute("F01", "AS1"));
        assertThrows(IllegalArgumentException.class, () -> manager.findRoute("AS1", " "));
        assertThrows(IllegalArgumentException.class, () -> manager.findRoute(null, "AS1"));
        assertThrows(IllegalArgumentException.class,
                () -> new ConnectionManager(List.of()).findRoute("AS1", "AS2"));
    }

    /**
     * Retains the original filters cases directly in Java so fresh checkouts can run them.
     */
    private static List<String[]> filterCases() {
        return List.of(
                new String[]{"FROM", "AS1", "", "", "", "", "1"},
                new String[]{"FROM_OTHER", "AS2", "", "", "", "", "1,2"},
                new String[]{"TO", "", "AS1", "", "", "", "1"},
                new String[]{"TO_OTHER", "", "AS2", "", "", "", "1,2"},
                new String[]{"FORWARD", "AS1", "AS2", "", "", "", "1"},
                new String[]{"REVERSE", "AS2", "AS1", "", "", "", "1"},
                new String[]{"CROSS", "AS1", "AS3", "", "", "", ""},
                new String[]{"ABSENT", "AS9", "", "", "", "", ""},
                new String[]{"TO_ABSENT", "", "AS9", "", "", "", ""},
                new String[]{"CASE", " as1 ", "aS2", "", "", "", "1"},
                new String[]{"TYPE", "", "", "PATH", "", "", "1"},
                new String[]{"TYPE_OTHER", "", "", "RAMP", "", "", "2"},
                new String[]{"YES", "", "", "", "YES", "", "1"},
                new String[]{"NO", "", "", "", "NO", "", ""},
                new String[]{"UNKNOWN", "", "", "", "UNKNOWN", "", "2"},
                new String[]{"SHELTER_YES", "", "", "", "", "YES", "1"},
                new String[]{"SHELTER_NO", "", "", "", "", "NO", "2"},
                new String[]{"SHELTER_UNKNOWN", "", "", "", "", "UNKNOWN", ""},
                new String[]{"ALL", "AS2", "AS1", "PATH", "YES", "YES", "1"},
                new String[]{"TYPE_CONFLICT", "AS1", "AS2", "RAMP", "YES", "YES", ""},
                new String[]{"STATUS_CONFLICT", "AS1", "AS2", "PATH", "NO", "YES", ""},
                new String[]{"SHELTER_CONFLICT", "AS1", "AS2", "PATH", "YES", "NO", ""},
                new String[]{"SAME", "AS2", "AS2", "", "", "", ""},
                new String[]{"SECOND", "AS3", "AS2", "RAMP", "UNKNOWN", "NO", "2"});
    }
}
//@@author
