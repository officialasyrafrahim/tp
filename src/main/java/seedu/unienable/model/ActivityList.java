package seedu.unienable.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import seedu.unienable.exception.InvalidIndexException;

/**
 * Owns activity collection state without exposing its mutable backing list.
 * The backing list stays in insertion (add) order; the display orderings used by
 * {@code list}, {@code list demand}, {@code list priority}, {@code overdue}, and
 * {@code next} are applied on demand by the view methods, so adding an activity
 * never reorders the stored list.
 */
public class ActivityList {
    /** Shared feedback when an activity command has no records or matching results. */
    public static final String NO_ACTIVITIES_MESSAGE = "Sorry, the activity list is empty.";

    //@@author officialasyrafrahim
    /**
     * Canonical {@code list} order: date, then start time; stable ties keep add order.
     */
    private static final Comparator<Activity> CANONICAL_ORDER =
            Comparator.comparing(Activity::getDate).thenComparing(Activity::getStartTime);

    /**
     * HIGH to LOW, reversed because the enum declares LOW, MEDIUM, HIGH; ties keep canonical order.
     */
    private static final Comparator<Activity> DEMAND_ORDER =
            Comparator.comparing(Activity::getDemand).reversed().thenComparing(CANONICAL_ORDER);

    /**
     * Highest available score first; activities with missing ratings follow,
     * and ties keep canonical order. Completed activities remain included.
     */
    private static final Comparator<Activity> PRIORITY_ORDER =
            Comparator.comparing(Activity::getPriorityScore,
                    Comparator.nullsLast(Comparator.<Integer>reverseOrder()))
                    .thenComparing(CANONICAL_ORDER);
    //@@author

    private final List<Activity> activities;

    /**
     * Creates an empty activity collection.
     */
    public ActivityList() {
        this(List.of());
    }

    /**
     * Creates an activity collection from a copy of the supplied insertion order.
     *
     * @param activities initial activities in insertion order.
     */
    public ActivityList(List<Activity> activities) {
        this.activities = new ArrayList<>(activities);
    }

    //@@author Atharva005
    /**
     * Appends without ordering; the view methods apply display order on demand.
     *
     * @param activity non-null activity to append.
     * @throws NullPointerException if the activity is null.
     */
    public void addActivity(Activity activity) {
        activities.add(Objects.requireNonNull(activity));
    }
    //@@author

    //@@author officialasyrafrahim
    /**
     * Returns an immutable snapshot sorted by date, then start time, the order {@code list}
     * displays and {@code delete INDEX} resolves its index against.
     *
     * @return an immutable snapshot in chronological order.
     */
    public List<Activity> getCanonicalView() {
        List<Activity> view = activities.stream().sorted(CANONICAL_ORDER).toList();
        assert view.size() == activities.size() : "Canonical view must contain every activity.";
        assert isSorted(view, CANONICAL_ORDER) : "Canonical view must be in date and start-time order.";
        return view;
    }
    //@@author

    //@@author Atharva005
    /**
     * Returns activities scheduled on the inclusive date range in chronological order.
     *
     * @param start first included date.
     * @param end last included date.
     * @return activities within the inclusive date range in chronological order.
     */
    public List<Activity> getViewBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        return getCanonicalView().stream()
                .filter(activity -> !activity.getDate().isBefore(start) && !activity.getDate().isAfter(end))
                .toList();
    }
    //@@author

    //@@author Oliver-koh
    /**
     * Returns activities whose names contain the supplied phrase, ignoring case.
     * Results retain chronological order so their indexes can be mapped to the
     * full list shown by {@code list}.
     *
     * @param keyword case-insensitive name substring.
     * @return matching activities in chronological order.
     */
    public List<Activity> findByName(String keyword) {
        String normalizedKeyword = Objects.requireNonNull(keyword).strip().toLowerCase(Locale.ROOT);
        return getCanonicalView().stream()
                .filter(activity -> activity.getName().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .toList();
    }
    //@@author

    //@@author officialasyrafrahim
    /**
     * Returns an immutable snapshot grouped by demand (HIGH to LOW); ties keep canonical order.
     *
     * @return an immutable snapshot ordered by demand and then chronology.
     */
    public List<Activity> getDemandView() {
        List<Activity> view = activities.stream().sorted(DEMAND_ORDER).toList();
        assert view.size() == activities.size() : "Demand view must contain every activity.";
        assert isSorted(view, DEMAND_ORDER) : "Demand view must be in demand and canonical order.";
        return view;
    }
    //@@author

    //@@author officialasyrafrahim
    /**
     * Returns every activity ordered by descending priority score, with activities
     * whose ratings are incomplete last. Ties keep canonical order, and completed
     * activities remain included as specified by the revised V2.0 guide.
     *
     * @return immutable priority view.
     */
    public List<Activity> getPriorityView() {
        List<Activity> view = activities.stream().sorted(PRIORITY_ORDER).toList();
        assert view.size() == activities.size() : "Priority view must contain every activity.";
        assert isSorted(view, PRIORITY_ORDER) : "Priority view must be in priority and canonical order.";
        return view;
    }
    //@@author

    //@@author officialasyrafrahim
    /**
     * Returns incomplete activities whose end is strictly before the supplied
     * local time. The result keeps canonical order for stable chronological ties.
     *
     * @param now local date and time used for the strict end boundary.
     * @return immutable overdue view.
     */
    public List<Activity> getOverdueView(LocalDateTime now) {
        Objects.requireNonNull(now);
        List<Activity> view = activities.stream()
                .filter(activity -> !activity.isDone())
                .filter(activity -> LocalDateTime.of(activity.getDate(), activity.getEndTime()).isBefore(now))
                .sorted(CANONICAL_ORDER)
                .toList();
        assert isSorted(view, CANONICAL_ORDER) : "Overdue view must stay in canonical order.";
        return view;
    }
    //@@author

    //@@author officialasyrafrahim
    /**
     * Returns the earliest incomplete activity whose start is strictly after the
     * supplied local time. Canonical order decides ties between equal starts.
     *
     * @param now local date and time used for the strict start boundary.
     * @return the next activity, or empty when none starts later.
     */
    public Optional<Activity> getNext(LocalDateTime now) {
        Objects.requireNonNull(now);
        return activities.stream()
                .filter(activity -> !activity.isDone())
                .filter(activity -> LocalDateTime.of(activity.getDate(), activity.getStartTime()).isAfter(now))
                .sorted(CANONICAL_ORDER)
                .findFirst();
    }
    //@@author

    //@@author officialasyrafrahim
    /**
     * Resolves a 1-based displayed index against the canonical view, removes that
     * activity, and returns it. Removal is by object rather than by raw backing-list
     * position, so the displayed index always refers to the order shown by
     * {@code list}, never to insertion order.
     *
     * @param displayedIndex 1-based index shown by the chronological {@code list} command.
     * @return the removed activity.
     * @throws InvalidIndexException when there are no activities, or when the index
     *         is outside the range 1 to (number of activities).
     */
    public Activity deleteByDisplayedIndex(int displayedIndex) throws InvalidIndexException {
        Activity removed = getByDisplayedIndex(displayedIndex);
        int expectedSize = activities.size() - 1;
        boolean wasRemoved = activities.remove(removed);
        assert wasRemoved : "The selected displayed activity must belong to the backing list.";
        assert activities.size() == expectedSize : "Deleting one activity must reduce the list size by one.";
        return removed;
    }
    //@@author

    //@@author officialasyrafrahim
    /**
     * Resolves a user-visible one-based index against the canonical chronological view.
     *
     * @param displayedIndex index shown by the chronological list command.
     * @return the activity at that index.
     * @throws InvalidIndexException when the index is outside the current canonical view.
     */
    public Activity getByDisplayedIndex(int displayedIndex) throws InvalidIndexException {
        List<Activity> canonical = getCanonicalView();
        if (canonical.isEmpty()) {
            if (displayedIndex < 1) {
                throw new InvalidIndexException("[WARNING] Index must be a positive whole number.");
            }
            throw new InvalidIndexException(NO_ACTIVITIES_MESSAGE);
        }
        if (displayedIndex < 1 || displayedIndex > canonical.size()) {
            throw new InvalidIndexException("[WARNING] No activity at index " + displayedIndex
                    + ". " + describeValidIndexes(canonical.size()));
        }
        return canonical.get(displayedIndex - 1);
    }
    //@@author

    //@@author lukelouyu
    /**
     * Describes which indexes the user may enter, replacing the awkward range
     * "1 to 1" with a plain sentence when only one activity exists.
     *
     * @param activityCount number of activities in the list; at least one.
     * @return sentence appended to the invalid-index warning.
     */
    private static String describeValidIndexes(int activityCount) {
        assert activityCount >= 1 : "An empty list has its own warning and no valid indexes.";
        if (activityCount == 1) {
            return "There is only one item.";
        }
        return "Valid range: 1 to " + activityCount + ".";
    }
    //@@author

    //@@author Oliver-koh
    /**
     * Replaces an activity selected by its chronological index while preserving
     * its insertion position for stable tie ordering.
     *
     * @param displayedIndex index shown by the chronological list command.
     * @param replacement replacement activity.
     * @throws InvalidIndexException when the index is outside the current view.
     */
    public void replaceByDisplayedIndex(int displayedIndex, Activity replacement)
            throws InvalidIndexException {
        Objects.requireNonNull(replacement);
        Activity current = getByDisplayedIndex(displayedIndex);
        int backingIndex = activities.indexOf(current);
        activities.set(backingIndex, replacement);
    }
    //@@author

    //@@author Oliver-koh
    /**
     * Updates completion state using the same canonical displayed index as list and delete.
     *
     * @param displayedIndex index shown by the chronological list command.
     * @param isDone desired completion state.
     * @return true when the activity state changed, false when it already had that state.
     * @throws InvalidIndexException when the index is outside the current canonical view.
     */
    public boolean setDoneAtDisplayedIndex(int displayedIndex, boolean isDone)
            throws InvalidIndexException {
        Activity activity = getByDisplayedIndex(displayedIndex);
        if (activity.isDone() == isDone) {
            return false;
        }
        activity.setDone(isDone);
        return true;
    }
    //@@author

    /**
     * Returns an immutable snapshot in insertion (add) order, the storage order rather than display order;
     * use {@link #getCanonicalView()} when the ordering that the user sees is needed.
     *
     * @return an immutable snapshot retaining insertion order.
     */
    public List<Activity> getActivities() {
        return List.copyOf(activities);
    }

    //@@author officialasyrafrahim
    /**
     * Checks the ordering invariant for a sorted view when assertions are enabled.
     *
     * @param view activities to check.
     * @param order comparator defining the required order.
     * @return true if every adjacent pair is in comparator order.
     */
    private static boolean isSorted(List<Activity> view, Comparator<Activity> order) {
        for (int index = 1; index < view.size(); index++) {
            if (order.compare(view.get(index - 1), view.get(index)) > 0) {
                return false;
            }
        }
        return true;
    }
    //@@author
}
