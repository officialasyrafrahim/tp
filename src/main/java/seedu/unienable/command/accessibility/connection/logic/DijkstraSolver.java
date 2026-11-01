//@@author lukelouyu
package seedu.unienable.command.accessibility.connection.logic;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import seedu.unienable.model.Connection;

/**
 * Runs one Dijkstra search for DijkstraShortestPath.
 * Each search has its own mutable state so subsequent queries are independent.
 * Package access keeps this helper internal to the routing logic.
 */
final class DijkstraSolver {
    /**
     * Best known distance to each vertex; Long.MAX_VALUE denotes an unreachable vertex.
     */
    private final long[] distances;

    /**
     * Predecessor vertex indexes used to reconstruct the chosen route.
     */
    private final int[] previousVertices;

    /**
     * Original links joining each vertex to its predecessor.
     */
    private final Connection[] previousConnections;
    private final Set<Integer> settled = new HashSet<>();
    private final PriorityQueue<Node> priorityQueue;
    private final int vertexCount;
    private List<List<Node>> adjacency;

    /**
     * Creates fresh search state for a graph containing the specified number of vertices.
     */
    DijkstraSolver(int vertexCount) {
        this.vertexCount = vertexCount;
        distances = new long[vertexCount];
        previousVertices = new int[vertexCount];
        previousConnections = new Connection[vertexCount];
        Arrays.fill(previousVertices, -1);
        priorityQueue = new PriorityQueue<>(Math.max(1, vertexCount), Comparator.comparingLong(Node::cost));
    }

    /**
     * Calculates shortest distances and records predecessors from the source vertex.
     * Search state belongs to this solver, which is created separately for each route query.
     */
    void computeShortestPaths(List<List<Node>> adjacency, int source) {
        this.adjacency = adjacency;
        Arrays.fill(distances, Long.MAX_VALUE);
        priorityQueue.add(new Node(source, 0));
        distances[source] = 0;

        while (settled.size() != vertexCount) {
            if (priorityQueue.isEmpty()) {
                return;
            }
            int vertex = priorityQueue.remove().node;
            if (settled.contains(vertex)) {
                continue;
            }
            settled.add(vertex);
            relaxNeighbors(vertex);
        }
    }

    /**
     * Returns the calculated distance, or Long.MAX_VALUE if the vertex is unreachable.
     */
    long getDistance(int vertex) {
        return distances[vertex];
    }

    /**
     * Returns the predecessor vertex, or -1 when no predecessor was recorded.
     */
    int getPrevious(int vertex) {
        return previousVertices[vertex];
    }

    /**
     * Returns the original link to the predecessor, or null when none was recorded.
     */
    Connection getPreviousConnection(int vertex) {
        return previousConnections[vertex];
    }

    /**
     * Relaxes every not-yet-settled neighbor of the current vertex.
     */
    private void relaxNeighbors(int vertex) {
        for (Node neighbor : adjacency.get(vertex)) {
            if (settled.contains(neighbor.node)) {
                continue;
            }
            // Positive int edge weights and int vertex indexes keep simple path totals within long.
            long candidate = distances[vertex] + neighbor.cost;
            if (candidate < distances[neighbor.node]) {
                distances[neighbor.node] = candidate;
                previousVertices[neighbor.node] = vertex;
                previousConnections[neighbor.node] = neighbor.connection;
                priorityQueue.add(new Node(neighbor.node, distances[neighbor.node]));
            }
        }
    }

    /**
     * Stores a vertex and cost for an adjacency edge or priority-queue candidate.
     * Adjacency edges also retain the original connection for route reconstruction.
     */
    record Node(int node, long cost, Connection connection) {
        private Node(int node, long cost) {
            this(node, cost, null);
        }
    }
}
//@@author
