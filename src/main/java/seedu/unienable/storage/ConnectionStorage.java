package seedu.unienable.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Connection;
import seedu.unienable.model.Facility;
import seedu.unienable.model.enums.AccessibilityStatus;
import seedu.unienable.model.enums.ShelterStatus;
import seedu.unienable.model.enums.TraversalType;

//@@author lukelouyu
/**
 * Loads bidirectional connection reference data against an explicitly supplied facility collection.
 * Stored endpoints are facility names, not facility IDs; their order is preserved without implying direction.
 */
public class ConnectionStorage {
    private final Set<String> facilityNames;

    /**
     * Takes a snapshot of known facility names without loading the facility dataset again.
     *
     * @param facilities already loaded facilities against which endpoints are validated.
     */
    public ConnectionStorage(List<Facility> facilities) {
        Set<String> names = new HashSet<>();
        for (Facility facility : facilities) {
            names.add(facility.getName());
        }
        this.facilityNames = Set.copyOf(names);
    }

    /**
     * Loads connections.txt from the UTF-8 classpath, including inside a packaged JAR.
     *
     * @return valid connections in source order with warnings for skipped lines.
     * @throws UniEnableException if the bundled resource is missing or unreadable.
     */
    public LoadResult<Connection> load() throws UniEnableException {
        return loadResource("/connections.txt");
    }

    /**
     * Parses a caller-owned reader without closing it. Empty optional fields become null.
     * Tags, enum values, and endpoint names are matched exactly; endpoint text is not rewritten.
     *
     * @param source reader containing connection records.
     * @return valid records and warnings carrying physical line numbers.
     * @throws UniEnableException if reading fails; partial data is not returned as a successful load.
     */
    public LoadResult<Connection> load(Reader source) throws UniEnableException {
        List<Connection> records = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        BufferedReader reader = new BufferedReader(source);
        try {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.stripLeading().startsWith("#")) {
                    continue;
                }
                try {
                    Connection connection = parseConnection(line.split("\\|", -1));
                    if (!ids.add(connection.getId())) {
                        throw new IllegalArgumentException("Duplicate connection ID: " + connection.getId());
                    }
                    records.add(connection);
                } catch (IllegalArgumentException exception) {
                    warnings.add("Line " + lineNumber + ": " + exception.getMessage());
                }
            }
        } catch (IOException exception) {
            throw createReadFailure(exception);
        }
        return new LoadResult<>(records, warnings);
    }

    /**
     * Opens and closes a classpath reader; package access permits missing-resource tests.
     */
    LoadResult<Connection> loadResource(String resourcePath) throws UniEnableException {
        InputStream stream = ConnectionStorage.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new UniEnableException("Missing connection dataset resource: " + resourcePath);
        }
        return loadOwnedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * Closes an owned reader and reports close failures through the same checked error contract.
     * Package access permits deterministic I/O failure tests without changing the public API.
     */
    LoadResult<Connection> loadOwnedReader(Reader source) throws UniEnableException {
        try (Reader reader = source) {
            return load(reader);
        } catch (IOException exception) {
            throw createReadFailure(exception);
        }
    }

    /**
     * Validates field counts, references, numbers, and enums before constructing a connection.
     */
    private Connection parseConnection(String[] fields) {
        if (fields.length < 8 || fields.length > 10 || !"CONNECTION".equals(fields[0])) {
            throw new IllegalArgumentException("CONNECTION requires 8 to 10 fields.");
        }
        for (int index = 1; index < 8; index++) {
            if (fields[index].isBlank()) {
                throw new IllegalArgumentException("Mandatory connection field " + (index + 1) + " is empty.");
            }
        }
        int id = parseInteger(fields[1], "connection ID");
        int distance = parseInteger(fields[4], "distance");
        if (distance <= 0) {
            throw new IllegalArgumentException("Connection distance must be positive: " + fields[4]);
        }
        if (!facilityNames.contains(fields[2]) || !facilityNames.contains(fields[3])) {
            throw new IllegalArgumentException("Unknown facility endpoint: " + fields[2] + " / " + fields[3]);
        }
        if (fields[2].equals(fields[3])) {
            throw new IllegalArgumentException("Self-connection is not allowed: " + fields[2]);
        }
        AccessibilityStatus accessibility = parseEnum(AccessibilityStatus.class, fields[5], "accessibility status");
        TraversalType type = parseEnum(TraversalType.class, fields[6], "traversal type");
        ShelterStatus shelter = parseEnum(ShelterStatus.class, fields[7], "shelter status");
        String barrier = fields.length >= 9 ? toOptionalText(fields[8]) : null;
        String notes = fields.length == 10 ? toOptionalText(fields[9]) : null;
        return new Connection(id, fields[2], fields[3], distance, accessibility, type, shelter, barrier, notes);
    }

    private int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
        }
    }

    /**
     * Keeps enum validation messages consistent without conflating distinct status enums.
     */
    private <T extends Enum<T>> T parseEnum(Class<T> enumType, String value, String fieldName) {
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
        }
    }

    private String toOptionalText(String value) {
        return value.isEmpty() ? null : value;
    }

    /**
     * Reports a concise I/O failure and retains its cause for diagnostics.
     */
    private UniEnableException createReadFailure(IOException cause) {
        UniEnableException exception = new UniEnableException(
                "Unable to read connection dataset: " + cause.getMessage());
        exception.initCause(cause);
        return exception;
    }
}
//@@author
