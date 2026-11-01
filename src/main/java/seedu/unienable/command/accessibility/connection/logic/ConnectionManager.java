//@@author lukelouyu
package seedu.unienable.command.accessibility.connection.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.unienable.model.Connection;
import seedu.unienable.model.enums.AccessibilityStatus;
import seedu.unienable.model.enums.ShelterStatus;
import seedu.unienable.model.enums.TraversalType;

/**
 * Provides read-only lookups, filtering and shortest accessible routes over bidirectional connection data.
 * Endpoints are short facility names such as AS1 and CLB, not stable IDs such as F01.
 */
public final class ConnectionManager {
    private final List<Connection> connections;

    /**
     * Reuses the graph built from this manager's immutable connection snapshot.
     */
    private final DijkstraShortestPath shortestPath;

    /**
     * Creates a manager using a snapshot of the supplied connections.
     *
     * @param connections non-null list of connections without null entries.
     */
    public ConnectionManager(List<Connection> connections) {
        this.connections = List.copyOf(connections);
        this.shortestPath = new DijkstraShortestPath(this.connections);
    }

    /**
     * Returns all connections in their original dataset order.
     *
     * @return immutable list of connections.
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * Finds the shortest recorded route using only connections marked YES for accessibility.
     * Each query has independent search state. Names ignore case and surrounding spaces.
     * Shelter, traversal type and recorded barriers do not add further routing restrictions.
     *
     * @param from origin facility name, such as AS2 (not a stable ID such as F02).
     * @param to destination facility name, such as CLB.
     * @return ordered stops, original connections and total meters, or empty if disconnected.
     * @throws IllegalArgumentException if either name is null, blank or absent from the connections.
     */
    public Optional<DijkstraShortestPath.Route> findRoute(String from, String to) {
        return shortestPath.findRoute(from, to);
    }

    /**
     * Finds a connection by its stable numeric ID.
     *
     * @param id connection ID.
     * @return matching connection, or empty if not found.
     */
    public Optional<Connection> findConnection(int id) {
        for (Connection connection : connections) {
            if (connection.getId() == id) {
                return Optional.of(connection);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns connections matching every provided filter. Null means the filter is omitted.
     * Endpoint filters accept facility names regardless of stored direction. When both
     * endpoints are supplied, they must match opposite endpoints of the same connection.
     * At least one filter must be supplied. String filters ignore surrounding spaces and case.
     *
     * @param from optional first endpoint facility name.
     * @param to optional second endpoint facility name.
     * @param type optional traversal type.
     * @param status optional accessibility status.
     * @param shelter optional shelter status.
     * @return immutable matching connections in their original dataset order.
     * @throws IllegalArgumentException if all filters are omitted or an endpoint is blank.
     */
    public List<Connection> findConnections(String from, String to, TraversalType type,
            AccessibilityStatus status, ShelterStatus shelter) {
        if (from == null && to == null && type == null && status == null && shelter == null) {
            throw new IllegalArgumentException("At least one connection filter is required.");
        }
        String fromName = normalizeEndpoint(from);
        String toName = normalizeEndpoint(to);
        List<Connection> matches = new ArrayList<>();
        for (Connection connection : connections) {
            if (!matchesEndpoints(connection, fromName, toName)) {
                continue;
            }
            if (type != null && connection.getType() != type) {
                continue;
            }
            if (status != null && connection.getAccessibility() != status) {
                continue;
            }
            if (shelter != null && connection.getShelter() != shelter) {
                continue;
            }
            matches.add(connection);
        }
        return List.copyOf(matches);
    }

    private String normalizeEndpoint(String endpoint) {
        if (endpoint == null) {
            return null;
        }
        String normalized = endpoint.strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Connection endpoint must not be blank.");
        }
        return normalized;
    }

    private boolean matchesEndpoints(Connection connection, String from, String to) {
        if (from != null && to != null) {
            return (matchesName(connection.getFrom(), from) && matchesName(connection.getTo(), to))
                    || (matchesName(connection.getFrom(), to) && matchesName(connection.getTo(), from));
        }
        if (from != null) {
            return matchesName(connection.getFrom(), from) || matchesName(connection.getTo(), from);
        }
        if (to != null) {
            return matchesName(connection.getFrom(), to) || matchesName(connection.getTo(), to);
        }
        return true;
    }

    private boolean matchesName(String actual, String filter) {
        return actual.equalsIgnoreCase(filter);
    }
}
//@@author
