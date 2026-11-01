//@@author lukelouyu
package seedu.unienable.command.accessibility.connection.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import seedu.unienable.command.accessibility.connection.logic.DijkstraSolver.Node;
//@@author
import seedu.unienable.model.Connection;
import seedu.unienable.model.enums.AccessibilityStatus;

/**
 * Finds the shortest recorded accessible route between campus facilities.
 * Connections are bidirectional; only links marked YES are traversable.
 * The route minimizes recorded distance, not time or number of connections.
 */
public final class DijkstraShortestPath {
    private final Map<String, Integer> indexByName = new LinkedHashMap<>();
    private final List<String> names = new ArrayList<>();
    private final List<List<Node>> adjacency;

    /**
     * Builds an adjacency list from the existing connection model.
     * Endpoints from inaccessible or unconfirmed links remain known vertices.
     *
     * @param connections reference data supplied by ConnectionManager/ConnectionStorage.
     */
    public DijkstraShortestPath(List<Connection> connections) {
        Objects.requireNonNull(connections, "connections");
        for (Connection connection : connections) {
            Objects.requireNonNull(connection, "connection");
            if (connection.getDistanceInMetres() <= 0) {
                throw new IllegalArgumentException("Connection distance must be positive");
            }
            addEndpoint(connection.getFrom());
            addEndpoint(connection.getTo());
        }

        adjacency = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            adjacency.add(new ArrayList<>());
        }
        for (Connection connection : connections) {
            if (connection.getAccessibility() != AccessibilityStatus.YES) {
                continue;
            }
            int from = indexByName.get(normalizeEndpoint(connection.getFrom()));
            int to = indexByName.get(normalizeEndpoint(connection.getTo()));
            int distance = connection.getDistanceInMetres();
            adjacency.get(from).add(new Node(to, distance, connection));
            adjacency.get(to).add(new Node(from, distance, connection));
        }
    }

    /**
     * Makes endpoint lookup independent of case, whitespace and the system locale.
     */
    private static String normalizeEndpoint(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Connection endpoint must not be blank");
        }
        return name.strip().toUpperCase(Locale.ROOT);
    }

    /**
     * Finds a shortest distance route, ignoring case and surrounding spaces.
     *
     * @param from starting facility name, ignoring case and surrounding spaces.
     * @param to destination facility name, ignoring case and surrounding spaces.
     * @return the route, or empty if the facilities cannot be connected with YES links.
     * @throws IllegalArgumentException if either endpoint is unknown or blank.
     */
    public Optional<Route> findRoute(String from, String to) {
        int source = lookup(from, "startpoint");
        int destination = lookup(to, "endpoint");
        DijkstraSolver solver = new DijkstraSolver(names.size());
        solver.computeShortestPaths(adjacency, source);
        if (solver.getDistance(destination) == Long.MAX_VALUE) {
            return Optional.empty();
        }

        List<String> stops = new ArrayList<>();
        List<Connection> segments = new ArrayList<>();
        int current = destination;
        stops.add(names.get(current));
        while (current != source) {
            segments.add(solver.getPreviousConnection(current));
            current = solver.getPrevious(current);
            stops.add(names.get(current));
        }
        Collections.reverse(stops);
        Collections.reverse(segments);
        return Optional.of(new Route(stops, segments, solver.getDistance(destination)));
    }

    /**
     * Assigns one vertex index to each normalized facility name.
     */
    private void addEndpoint(String name) {
        String normalized = normalizeEndpoint(name);
        if (!indexByName.containsKey(normalized)) {
            indexByName.put(normalized, names.size());
            names.add(name.strip());
        }
    }

    /**
     * Resolves a facility name and identifies its role when reporting an unknown start or end.
     */
    private int lookup(String name, String role) {
        Integer index = indexByName.get(normalizeEndpoint(name));
        if (index == null) {
            throw new IllegalArgumentException("Unknown connection " + role + ": " + name);
        }
        return index;
    }

    /**
     * A path with its ordered facility names, original connections and total meters.
     * Each segment joins the corresponding adjacent stops in either stored direction.
     * A long total accommodates routes whose combined distance exceeds the int range.
     *
     * @param stops facility names in travel order.
     * @param segments recorded connections joining adjacent stops.
     * @param distanceInMetres total recorded distance along the route.
     */
    public record Route(List<String> stops, List<Connection> segments, long distanceInMetres) {
        /**
         * Copies the path lists so callers cannot alter a completed route.
         */
        public Route {
            stops = List.copyOf(stops);
            segments = List.copyOf(segments);
        }
    }
}
