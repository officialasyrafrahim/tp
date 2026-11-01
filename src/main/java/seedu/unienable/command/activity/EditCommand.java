package seedu.unienable.command.activity;

//@@author Oliver-koh
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import seedu.unienable.command.common.Command;
import seedu.unienable.command.common.CommandResult;
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.enums.DemandLevel;
import seedu.unienable.storage.Storage;

/**
 * Edits the supplied fields of one activity selected by its chronological index.
 */
public class EditCommand extends Command {
    private final int displayedIndex;
    private final Fields fields;
    private final Clock clock;
    private final boolean isOverlapAllowed;

    /**
     * Groups optional edit values; null means omitted and preserves the existing value.
     * Rating fields cannot be explicitly cleared by the edit command.
     *
     * @param name replacement name, or null when omitted.
     * @param date replacement date, or null when omitted.
     * @param startTime replacement start time, or null when omitted.
     * @param endTime replacement end time, or null when omitted.
     * @param demand replacement demand, or null when omitted.
     * @param importance replacement importance, or null when omitted.
     * @param urgency replacement urgency, or null when omitted.
     * @param commitment replacement commitment, or null when omitted.
     */
    public record Fields(String name, LocalDate date, LocalTime startTime, LocalTime endTime,
            DemandLevel demand, Integer importance, Integer urgency, Integer commitment) {
    }

    /**
     * Creates an edit command from parsed fields and an injectable time source.
     *
     * @param displayedIndex chronological index of the activity to edit.
     * @param fields supplied replacements; null components preserve their existing values.
     * @param clock time source used to reject ended activities and validate replacement times.
     * @param isOverlapAllowed whether this edit explicitly permits an intentional overlap.
     */
    public EditCommand(int displayedIndex, Fields fields, Clock clock, boolean isOverlapAllowed) {
        this.isOverlapAllowed = isOverlapAllowed;
        this.displayedIndex = displayedIndex;
        this.fields = Objects.requireNonNull(fields);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Validates the resulting activity before replacing any field, keeping invalid
     * edits atomic.
     */
    @Override
    public CommandResult execute(ActivityList activities, Storage storage) throws UniEnableException {
        Activity activity = activities.getByDisplayedIndex(displayedIndex);
        LocalDateTime now = LocalDateTime.now(clock);
        if (!LocalDateTime.of(activity.getDate(), activity.getEndTime()).isAfter(now)) {
            throw createWarning("Activities that have ended cannot be edited.");
        }
        String updatedName = fields.name() == null ? activity.getName() : fields.name();
        LocalDate updatedDate = fields.date() == null ? activity.getDate() : fields.date();
        LocalTime updatedStart = fields.startTime() == null ? activity.getStartTime() : fields.startTime();
        LocalTime updatedEnd = fields.endTime() == null ? activity.getEndTime() : fields.endTime();
        DemandLevel updatedDemand = fields.demand() == null ? activity.getDemand() : fields.demand();
        Integer updatedImportance = fields.importance() == null ? activity.getImportance() : fields.importance();
        Integer updatedUrgency = fields.urgency() == null ? activity.getUrgency() : fields.urgency();
        Integer updatedCommitment = fields.commitment() == null ? activity.getCommitment() : fields.commitment();

        if (fields.date() != null || fields.startTime() != null || fields.endTime() != null) {
            if (!updatedEnd.isAfter(updatedStart)) {
                throw createWarning("End time must be later than start time on the same day.");
            }
            LocalDateTime endDateTime = LocalDateTime.of(updatedDate, updatedEnd);
            if (!endDateTime.isAfter(now)) {
                throw createWarning("Edited activity must end after the current local time.");
            }
        }

        Activity replacement = new Activity(updatedName, updatedDate, updatedStart, updatedEnd,
                updatedDemand, activity.isDone(), updatedImportance, updatedUrgency, updatedCommitment);
        boolean isScheduleChanged = !updatedDate.equals(activity.getDate())
                || !updatedStart.equals(activity.getStartTime()) || !updatedEnd.equals(activity.getEndTime());
        // Renaming does not introduce a time clash; duplicate identity still needs checking.
        ActivityScheduleValidator.validate(replacement, activities, activity,
                isOverlapAllowed || !isScheduleChanged);
        activities.replaceByDisplayedIndex(displayedIndex, replacement);
        String message = "Edited activity: " + replacement.getName() + "\n"
                + "Date: " + replacement.getDate() + "\n"
                + "Time: " + replacement.getStartTime() + " - " + replacement.getEndTime() + "\n"
                + "Demand: " + replacement.getDemand() + "\n"
                + "Importance: " + formatRating(replacement.getImportance()) + "\n"
                + "Urgency: " + formatRating(replacement.getUrgency()) + "\n"
                + "Commitment: " + formatRating(replacement.getCommitment());
        return new CommandResult(message, false, true);
    }

    private String formatRating(Integer rating) {
        return rating == null ? "-" : rating.toString();
    }

    private UniEnableException createWarning(String message) {
        return new UniEnableException("[WARNING] " + message);
    }
}
//@@author
