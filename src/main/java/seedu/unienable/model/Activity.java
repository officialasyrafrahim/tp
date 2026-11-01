package seedu.unienable.model;

import java.time.LocalDate;
import java.time.LocalTime;

import seedu.unienable.model.enums.DemandLevel;

/**
 * Stores activity details, completion state and independently optional ratings.
 * A null rating means unset; a recorded rating is an integer from 1 to 5.
 * Input formatting and current-time validation belong to the command parser.
 */
public class Activity {
    //@@author lukelouyu
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    //@@author

    //@@author Atharva005
    private final String name;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final DemandLevel demand;
    //@@author

    //@@author lukelouyu
    /**
     * Recorded importance from 1 to 5, or null when unset.
     */
    private final Integer importance;

    /**
     * Recorded urgency from 1 to 5, or null when unset.
     */
    private final Integer urgency;

    /**
     * Recorded commitment from 1 to 5, or null when unset.
     */
    private final Integer commitment;
    //@@author
    //@@author Oliver-koh
    private boolean isDone;
    //@@author

    /**
     * Creates an activity with all ratings unset, preserving the V1.0 constructor contract.
     *
     * @param name activity name.
     * @param date activity date.
     * @param startTime start time.
     * @param endTime end time.
     * @param demand estimated demand level.
     * @param isDone whether the activity is completed.
     */
    public Activity(String name, LocalDate date, LocalTime startTime, LocalTime endTime,
            DemandLevel demand, boolean isDone) {
        this(name, date, startTime, endTime, demand, isDone, null, null, null);
    }

    /**
     * Creates an activity with independently optional ratings.
     * No current-time check is made so saved historical activities can be restored.
     *
     * @param name activity name.
     * @param date activity date.
     * @param startTime start time.
     * @param endTime end time.
     * @param demand estimated demand level, separate from the ratings.
     * @param isDone whether the activity is completed.
     * @param importance importance from 1 to 5, or null when unset.
     * @param urgency urgency from 1 to 5, or null when unset.
     * @param commitment commitment from 1 to 5, or null when unset.
     * @throws IllegalArgumentException if a supplied rating is outside 1 to 5.
     */
    public Activity(String name, LocalDate date, LocalTime startTime, LocalTime endTime,
            DemandLevel demand, boolean isDone, Integer importance, Integer urgency, Integer commitment) {
        //@@author lukelouyu
        validateRating(importance);
        validateRating(urgency);
        validateRating(commitment);
        //@@author
        //@@author Atharva005
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.demand = demand;
        //@@author
        //@@author Oliver-koh
        this.isDone = isDone;
        //@@author
        //@@author lukelouyu
        this.importance = importance;
        this.urgency = urgency;
        this.commitment = commitment;
        //@@author
    }

    //@@author lukelouyu
    /**
     * Checks the shared numeric rating rule for activity constructors and other feature owners.
     * Text parsing remains the caller's responsibility.
     *
     * @param rating rating to check, or null when unset.
     * @throws IllegalArgumentException if the rating is outside 1 to 5.
     */
    public static void validateRating(Integer rating) {
        if (rating != null && (rating < MIN_RATING || rating > MAX_RATING)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5, or null when unset.");
        }
    }
    //@@author

    //@@author Atharva005
    /**
     * Returns the activity name as recorded.
     *
     * @return the full activity name.
     */
    public String getName() {
        return name;
    }
    //@@author

    //@@author Atharva005
    /**
     * Returns the date on which the activity is scheduled.
     *
     * @return the scheduled date.
     */
    public LocalDate getDate() {
        return date;
    }
    //@@author

    //@@author Atharva005
    /**
     * Returns the activity start time.
     *
     * @return the scheduled local start time.
     */
    public LocalTime getStartTime() {
        return startTime;
    }
    //@@author

    //@@author Atharva005
    /**
     * Returns the activity end time.
     *
     * @return the scheduled local end time.
     */
    public LocalTime getEndTime() {
        return endTime;
    }
    //@@author

    //@@author Atharva005
    /**
     * Returns the activity demand independently of its numeric ratings.
     *
     * @return the recorded demand level.
     */
    public DemandLevel getDemand() {
        return demand;
    }
    //@@author

    //@@author lukelouyu
    /**
     * Returns the recorded importance; callers must check for null before numeric use.
     *
     * @return importance from 1 to 5, or null when unset.
     */
    public Integer getImportance() {
        return importance;
    }
    //@@author

    //@@author lukelouyu
    /**
     * Returns the recorded urgency; callers must check for null before numeric use.
     *
     * @return urgency from 1 to 5, or null when unset.
     */
    public Integer getUrgency() {
        return urgency;
    }
    //@@author

    //@@author lukelouyu
    /**
     * Returns the recorded commitment; callers must check for null before numeric use.
     *
     * @return commitment from 1 to 5, or null when unset.
     */
    public Integer getCommitment() {
        return commitment;
    }
    //@@author

    //@@author officialasyrafrahim
    /**
     * Calculates the combined priority score used by {@code list priority}.
     * The score is {@code urgency * urgency * (importance + commitment)}.
     * Completion state does not affect the score, but all three ratings must
     * be present to calculate it.
     *
     * @return priority score from 2 to 250, or null when any rating is unset.
     */
    public Integer getPriorityScore() {
        if (importance == null || urgency == null || commitment == null) {
            return null;
        }
        return urgency * urgency * (importance + commitment);
    }
    //@@author

    //@@author Oliver-koh
    /**
     * Sets whether this activity is completed.
     *
     * @param isDone true when the activity has been completed.
     */
    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }
    //@@author

    //@@author Oliver-koh
    /**
     * Returns the completion state used by list display and persistence.
     *
     * @return true when this activity is completed.
     */
    public boolean isDone() {
        return isDone;
    }
    //@@author
}
