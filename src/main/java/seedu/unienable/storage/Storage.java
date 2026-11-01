package seedu.unienable.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;

//@@author Oliver-koh
/**
 * Reads six-column V1.0 records and nine-column rated records; saves nine columns.
 * The saved format is {@code NAME|DATE|START|END|DEMAND|DONE|IMP|URG|COM}.
 * A stored dash means an unset rating; V1.0 records load with all ratings unset.
 *
 * <p>Malformed rows are skipped with line-numbered warnings. After malformed rows
 * or a fatal read failure, a later save preserves and verifies the original bytes
 * in a {@code .corrupt.bak} file before replacing the activity file.</p>
 */
public class Storage {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String FIELD_SEPARATOR = "|";

    private final Path activityFile;
    private List<String> warnings = List.of();
    private boolean hasLoadedCorruptData;
    /** Tracks in-memory activity changes until a complete save succeeds. */
    private boolean hasUnsavedChanges;

    /**
     * Creates storage at the default runtime location.
     */
    public Storage() {
        this(Path.of("data", "activities.txt"));
    }

    /**
     * Creates storage at a caller-supplied path, which also makes tests isolated.
     *
     * @param activityFile file containing activity records.
     */
    public Storage(Path activityFile) {
        this.activityFile = Objects.requireNonNull(activityFile);
    }

    /**
     * Loads valid activity records from disk.
     *
     * @return loaded activities, or an empty list when the file does not exist.
     * @throws UniEnableException when the file cannot be read.
     */
    public ActivityList load() throws UniEnableException {
        warnings = new ArrayList<>();
        hasLoadedCorruptData = false;
        if (!Files.exists(activityFile)) {
            return new ActivityList();
        }

        List<Activity> activities = new ArrayList<>();
        List<String> lines;
        try {
            // Split the file into rows first, so one row with invalid UTF-8
            // does not stop other rows from loading.
            lines = new String(Files.readAllBytes(activityFile), StandardCharsets.ISO_8859_1).lines().toList();
        } catch (IOException exception) {
            // A decoding or I/O failure must not bypass recovery protection on a later save.
            hasLoadedCorruptData = true;
            throw new UniEnableException("Unable to load activities: " + exception.getMessage());
        }

        for (int i = 0; i < lines.size(); i++) {
            try {
                String line = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(
                        lines.get(i).getBytes(StandardCharsets.ISO_8859_1))).toString();
                activities.add(parseLine(line));
            } catch (CharacterCodingException exception) {
                hasLoadedCorruptData = true;
                warnings.add("Line " + (i + 1) + ": invalid UTF-8 encoding; row skipped. Save the file as UTF-8.");
            } catch (IllegalArgumentException exception) {
                hasLoadedCorruptData = true;
                warnings.add("Line " + (i + 1) + ": " + exception.getMessage());
            }
        }
        return new ActivityList(activities);
    }

    /**
     * Returns warnings produced by the most recent load operation.
     *
     * @return immutable line-numbered load warnings.
     */
    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }

    /**
     * Records that the console has changed activities before attempting to save them.
     */
    public void markUnsavedChanges() {
        hasUnsavedChanges = true;
    }

    /**
     * Returns whether a changed activity list still needs a successful save.
     *
     * @return true until the current activity changes save successfully.
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * Saves all activities in insertion order using UTF-8 and the nine-column format.
     *
     * @param activities activities to persist.
     * @throws UniEnableException when the file cannot be safely written.
     */
    public void save(ActivityList activities) throws UniEnableException {
        Objects.requireNonNull(activities);
        try {
            Path parent = activityFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (hasLoadedCorruptData && Files.exists(activityFile)) {
                backupCorruptFile();
            }

            List<String> lines = activities.getActivities().stream()
                    .map(Storage::formatActivity)
                    .toList();
            Path temporaryFile = activityFile.resolveSibling(activityFile.getFileName() + ".tmp");
            String contents = String.join("\n", lines);
            if (!lines.isEmpty()) {
                contents += "\n";
            }
            Files.writeString(temporaryFile, contents, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            moveIntoPlace(temporaryFile);
            hasLoadedCorruptData = false;
            hasUnsavedChanges = false;
        } catch (IOException exception) {
            throw new UniEnableException("Unable to save activities: " + exception.getMessage());
        }
    }

    /**
     * Parses either row format without applying add-time restrictions to saved history.
     *
     * @param line saved activity record.
     * @return restored activity.
     * @throws IllegalArgumentException if the record has malformed fields.
     */
    private Activity parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 6 && fields.length != 9) {
            throw new IllegalArgumentException("expected 6 or 9 fields separated by '|'.");
        }
        if (fields[0].isBlank()) {
            throw new IllegalArgumentException("activity name must not be empty.");
        }

        Integer importance = fields.length == 9 ? parseStoredRating(fields[6]) : null;
        Integer urgency = fields.length == 9 ? parseStoredRating(fields[7]) : null;
        Integer commitment = fields.length == 9 ? parseStoredRating(fields[8]) : null;

        try {
            LocalDate date = LocalDate.parse(fields[1], DATE_FORMAT);
            LocalTime startTime = LocalTime.parse(fields[2], TIME_FORMAT);
            LocalTime endTime = LocalTime.parse(fields[3], TIME_FORMAT);
            if (!endTime.isAfter(startTime)) {
                throw new IllegalArgumentException("end time must be later than start time on the same day.");
            }
            DemandLevel demand = DemandLevel.valueOf(fields[4].toUpperCase(Locale.ROOT));
            boolean isDone = parseDone(fields[5]);
            return new Activity(fields[0], date, startTime, endTime, demand, isDone,
                    importance, urgency, commitment);
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid date, time, demand, or completion value.");
        }
    }

    /**
     * Converts the stored unset marker to null and validates recorded numeric ratings.
     *
     * @param value stored rating text.
     * @return rating from 1 to 5, or null when unset.
     * @throws IllegalArgumentException if the value is neither a valid rating nor a dash.
     */
    private Integer parseStoredRating(String value) {
        if ("-".equals(value)) {
            return null;
        }
        try {
            Integer rating = Integer.valueOf(value);
            Activity.validateRating(rating);
            return rating;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("rating must be a whole number from 1 to 5 or '-' when unset.");
        }
    }

    private boolean parseDone(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("completion value must be true or false.");
    }

    private static String formatActivity(Activity activity) {
        return String.join(FIELD_SEPARATOR,
                activity.getName(),
                activity.getDate().toString(),
                activity.getStartTime().toString(),
                activity.getEndTime().toString(),
                activity.getDemand().name(),
                Boolean.toString(activity.isDone()),
                formatStoredRating(activity.getImportance()),
                formatStoredRating(activity.getUrgency()),
                formatStoredRating(activity.getCommitment()));
    }

    /**
     * Converts an in-memory unset rating to its stored dash marker.
     */
    private static String formatStoredRating(Integer rating) {
        return rating == null ? "-" : rating.toString();
    }

    private void backupCorruptFile() throws IOException {
        Path backup = activityFile.resolveSibling(activityFile.getFileName() + ".corrupt.bak");
        Files.copy(activityFile, backup, StandardCopyOption.REPLACE_EXISTING);
        if (Files.mismatch(activityFile, backup) != -1) {
            throw new IOException("Activity recovery backup does not match the original file.");
        }
    }

    private void moveIntoPlace(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, activityFile, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (FileSystemException exception) {
            // Some Windows file systems reject atomic replacement even though a normal
            // replacement is supported. The temporary file still prevents a partial
            // write, so the fallback remains safe for the activity data.
            Files.move(temporaryFile, activityFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
//@@author
