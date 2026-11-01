//@@author lukelouyu
package seedu.unienable.command.accessibility.connection.logic;

//@@author

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

//@@author lukelouyu
import seedu.unienable.command.accessibility.connection.logic.DijkstraShortestPath.Route;
//@@author
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Connection;
import seedu.unienable.model.enums.AccessibilityStatus;
import seedu.unienable.model.enums.ShelterStatus;
import seedu.unienable.model.enums.TraversalType;
import seedu.unienable.storage.ConnectionStorage;
import seedu.unienable.storage.FacilityStorage;

//@@author lukelouyu
/**
 * Checks route building and the separate DijkstraSolver through DijkstraShortestPath.findRoute().
 * Checks all 81 campus pairs against a manual distance matrix and validates the returned route segments.
 * The bundled graph has nine hubs and ten undirected links, including AS4-AS7 at 50 meters.
 * Expected values are fixed test data; no reference shortest-path algorithm runs in these tests.
 */
class DijkstraShortestPathTest {
    /**
     * Row and column order used by the manually calculated distance matrix.
     */
    private static final List<String> HUBS = List.of("AS1", "AS2", "AS3", "AS4", "AS5", "AS6", "AS7", "AS8", "CLB");

    /**
     * Shortest distances in meters for the bundled graph, with zero on the diagonal.
     */
    private static final long[][] EXPECTED_DISTANCES = {
        // AS1, AS2, AS3, AS4, AS5, AS6, AS7, AS8, CLB
        {0, 130, 165, 60, 115, 90, 110, 135, 160},
        {130, 0, 35, 190, 245, 220, 240, 265, 290},
        {165, 35, 0, 225, 280, 255, 275, 300, 325},
        {60, 190, 225, 0, 55, 150, 50, 195, 220},
        {115, 245, 280, 55, 0, 205, 65, 250, 275},
        {90, 220, 255, 150, 205, 0, 200, 45, 70},
        {110, 240, 275, 50, 65, 200, 0, 245, 270},
        {135, 265, 300, 195, 250, 45, 245, 0, 55},
        {160, 290, 325, 220, 275, 70, 270, 55, 0}
    };

    @TestFactory
    List<DynamicTest> findRoute_allCampusPairs_matchManualDistanceMatrix() throws UniEnableException {
        var shortestPath = loadCampusGraph();
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 0; i < HUBS.size(); i++) {
            for (int j = 0; j < HUBS.size(); j++) {
                String source = HUBS.get(i);
                String destination = HUBS.get(j);
                long expectedDistance = EXPECTED_DISTANCES[i][j];
                tests.add(DynamicTest.dynamicTest(source + " -> " + destination, () -> {
                    var actual = shortestPath.findRoute(source, destination).orElseThrow();
                    assertEquals(expectedDistance, actual.distanceInMetres());
                    assertValidRoute(actual, source, destination);
                }));
            }
        }
        return tests;
    }

    @Test
    void findRoute_sameSourceAndDestination_returnsZeroDistanceAndNoSegments() throws UniEnableException {
        var route = loadCampusGraph().findRoute("AS1", "AS1").orElseThrow();

        assertEquals(0L, route.distanceInMetres());
        assertEquals(List.of("AS1"), route.stops());
        assertTrue(route.segments().isEmpty());
    }

    @Test
    void findRoute_avoidsInaccessibleShortcuts() {
        var connections = List.of(
                new Connection(1, "A", "B", 4,
                        AccessibilityStatus.YES, TraversalType.PATH,
                        ShelterStatus.YES, null, null),
                new Connection(2, "B", "C", 6,
                        AccessibilityStatus.YES, TraversalType.PATH,
                        ShelterStatus.YES, null, null),
                new Connection(3, "A", "C", 1,
                        AccessibilityStatus.NO, TraversalType.PATH,
                        ShelterStatus.NO, null, null),
                new Connection(4, "A", "C", 2,
                        AccessibilityStatus.UNKNOWN, TraversalType.PATH,
                        ShelterStatus.UNKNOWN, null, null));

        var shortestPath = new DijkstraShortestPath(connections);
        var route = shortestPath.findRoute("A", "C").orElseThrow();

        assertEquals(List.of("A", "B", "C"), route.stops());
        assertEquals(10L, route.distanceInMetres());
    }

    /**
     * Loads the bundled campus graph so the expected routes also check the recorded data.
     */
    private static DijkstraShortestPath loadCampusGraph() throws UniEnableException {
        var facilities = new FacilityStorage().load().getRecords();
        var connections = new ConnectionStorage(facilities).load().getRecords();
        assertEquals(9, facilities.size());
        return new DijkstraShortestPath(connections);
    }

    /**
     * Checks that returned stops follow accessible links whose distances add up to the route total.
     *
     * @param route returned route.
     * @param source requested source hub.
     * @param destination requested destination hub.
     */
    private static void assertValidRoute(Route route, String source, String destination) {
        assertEquals(source, route.stops().getFirst());
        assertEquals(destination, route.stops().getLast());
        assertEquals(route.stops().size() - 1, route.segments().size());
        for (int i = 0; i < route.segments().size(); i++) {
            Connection connection = route.segments().get(i);
            String from = route.stops().get(i);
            String to = route.stops().get(i + 1);
            assertTrue(connection.getFrom().equals(from) && connection.getTo().equals(to)
                    || connection.getFrom().equals(to) && connection.getTo().equals(from));
            assertEquals(AccessibilityStatus.YES, connection.getAccessibility());
        }
        assertEquals(route.distanceInMetres(), route.segments().stream()
                .mapToLong(Connection::getDistanceInMetres).sum());
    }
}
//@@author
