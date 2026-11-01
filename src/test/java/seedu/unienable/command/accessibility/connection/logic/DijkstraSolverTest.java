//@@author lukelouyu
package seedu.unienable.command.accessibility.connection.logic;

//@@author

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;

//@@author lukelouyu
import seedu.unienable.command.accessibility.connection.logic.DijkstraSolver.Node;
//@@author
import seedu.unienable.model.Connection;
import seedu.unienable.model.enums.AccessibilityStatus;
import seedu.unienable.model.enums.ShelterStatus;
import seedu.unienable.model.enums.TraversalType;

//@@author lukelouyu
/**
 * Checks shortest distances and predecessor information produced by the internal Dijkstra solver.
 */
class DijkstraSolverTest {
    @Test
    void computeShortestPaths_shorterIndirectPath_recordsDistanceAndPredecessors() {
        var first = connection(1, "A", "B", 4);
        var second = connection(2, "B", "C", 6);
        var direct = connection(3, "A", "C", 20);
        List<List<Node>> graph = List.of(
                List.of(new Node(1, 4, first), new Node(2, 20, direct)),
                List.of(new Node(0, 4, first), new Node(2, 6, second)),
                List.of(new Node(0, 20, direct), new Node(1, 6, second)));
        var solver = new DijkstraSolver(graph.size());

        solver.computeShortestPaths(graph, 0);

        // A -> B -> C is 10 m, shorter than the direct 20 m connection.
        assertEquals(10L, solver.getDistance(2));
        assertEquals(1, solver.getPrevious(2));
        assertEquals(0, solver.getPrevious(1));
        assertSame(second, solver.getPreviousConnection(2));
        assertSame(first, solver.getPreviousConnection(1));
    }

    @Test
    void computeShortestPaths_disconnectedVertex_hasNoDistanceOrPredecessor() {
        var link = connection(1, "A", "B", 4);
        List<List<Node>> graph = List.of(
                List.of(new Node(1, 4, link)),
                List.of(new Node(0, 4, link)),
                List.of());
        var solver = new DijkstraSolver(graph.size());

        solver.computeShortestPaths(graph, 0);

        assertEquals(4L, solver.getDistance(1));
        assertEquals(Long.MAX_VALUE, solver.getDistance(2));
        assertEquals(-1, solver.getPrevious(2));
        assertNull(solver.getPreviousConnection(2));
    }

    /**
     * Creates an accessible link; graph indexes 0, 1 and 2 represent A, B and C respectively.
     */
    private static Connection connection(int id, String from, String to, int metres) {
        return new Connection(id, from, to, metres, AccessibilityStatus.YES,
                TraversalType.PATH, ShelterStatus.YES, null, null);
    }
}
//@@author
