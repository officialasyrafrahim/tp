package seedu.unienable.ui.activity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;

//@@author officialasyrafrahim
/**
 * Builds user-facing activity list messages from pre-sorted views supplied by commands.
 * Formatting is pure: it renders the given order as-is and never sorts, filters,
 * reads input, or prints to the console.
 */
public final class ActivityListFormatter {
    private static final String LIST_HEADING = "Activities (by date, start time):";

    private static final String DEMAND_LIST_HEADING = "Activities by demand (HIGH to LOW):";

    private static final String PRIORITY_LIST_HEADING = "Activities by priority (highest first):";

    private static final String OVERDUE_LIST_HEADING = "Overdue activities (ended before now):";

    private static final String NEXT_HEADING = "Next activity (earliest upcoming):";

    /**
     * Explains which chronological indexes activity-changing commands use.
     */
    private static final String CHRONOLOGICAL_INDEX_NOTICE =
            "Indexes refer to the chronological 'list' order.";

    /**
     * Reports a normal empty result when an activity view has no entries.
     */
    private static final String EMPTY_MESSAGE = ActivityList.NO_ACTIVITIES_MESSAGE;

    private static final String UNKNOWN_RATING = "-";

    private static final String PRIORITY_UNAVAILABLE = "NA";

    /**
     * Prevents construction of this utility class.
     */
    private ActivityListFormatter() {
    }

    /**
     * Formats the canonical view; numbering starts at 1 to match what
     * {@code delete INDEX} resolves against.
     *
     * @param activities activities in chronological display order.
     * @return the heading and numbered activities, or the empty-list placeholder.
     */
    public static String formatList(List<Activity> activities) {
        return format(LIST_HEADING, activities);
    }

    /**
     * Formats the demand view with a notice explaining which indexes deletion uses.
     * Empty results contain only the no-activities message.
     *
     * @param activities activities in demand display order.
     * @param canonicalView full chronological activity view.
     * @return the heading, deletion-index notice and formatted activities.
     */
    public static String formatDemandList(List<Activity> activities, List<Activity> canonicalView) {
        if (activities.isEmpty()) {
            return EMPTY_MESSAGE;
        }
        StringBuilder output = new StringBuilder(DEMAND_LIST_HEADING)
                .append("\n").append(CHRONOLOGICAL_INDEX_NOTICE);
        for (Activity activity : activities) {
            output.append("\n").append(getChronologicalIndex(activity, canonicalView)).append(". ")
                    .append(describe(activity));
        }
        return output.toString();
    }

    /**
     * Formats the priority view, using each activity's index from the full
     * chronological list and showing the score and its rating components.
     *
     * @param activities activities in priority display order.
     * @param canonicalView full chronological activity view.
     * @return the heading, index notice, and numbered activities.
     */
    public static String formatPriorityList(List<Activity> activities, List<Activity> canonicalView) {
        if (activities.isEmpty()) {
            return EMPTY_MESSAGE;
        }
        StringBuilder output = new StringBuilder(PRIORITY_LIST_HEADING)
                .append("\n").append(CHRONOLOGICAL_INDEX_NOTICE);
        for (Activity activity : activities) {
            output.append("\n").append(getChronologicalIndex(activity, canonicalView)).append(". ")
                    .append(describe(activity)).append(" | ").append(describePriority(activity));
        }
        return output.toString();
    }

    /**
     * Formats overdue activities with chronological indexes and a label that
     * distinguishes today, earlier in the current Monday-to-Sunday week, and
     * earlier dates.
     *
     * @param activities overdue activities in chronological order.
     * @param canonicalView full chronological activity view.
     * @param now local time used to classify the labels.
     * @return the heading and numbered overdue activities.
     */
    public static String formatOverdueList(List<Activity> activities, List<Activity> canonicalView,
            LocalDateTime now) {
        if (activities.isEmpty()) {
            return "Sorry, no incomplete activities have ended.";
        }
        StringBuilder output = new StringBuilder(OVERDUE_LIST_HEADING);
        for (Activity activity : activities) {
            output.append("\n").append(getChronologicalIndex(activity, canonicalView)).append(". [")
                    .append(getOverdueLabel(activity, now)).append("] ").append(describe(activity));
        }
        return output.toString();
    }

    /**
     * Formats the single next activity, or an empty-state message when none remains.
     *
     * @param next next activity, if one starts strictly after now.
     * @param canonicalView full chronological activity view.
     * @return the heading and numbered next activity, or the empty-state message.
     */
    public static String formatNext(Optional<Activity> next, List<Activity> canonicalView) {
        if (next.isEmpty()) {
            return "Sorry, no upcoming incomplete activities exist.";
        }
        Activity activity = next.get();
        return NEXT_HEADING + "\n" + getChronologicalIndex(activity, canonicalView) + ". "
                + describe(activity);
    }

    private static String describePriority(Activity activity) {
        Integer score = activity.getPriorityScore();
        String scoreText = score == null ? PRIORITY_UNAVAILABLE : score.toString();
        return "Priority " + scoreText
                + " (IMP " + formatRating(activity.getImportance())
                + ", URG " + formatRating(activity.getUrgency())
                + ", COM " + formatRating(activity.getCommitment()) + ")";
    }

    private static String formatRating(Integer rating) {
        return rating == null ? UNKNOWN_RATING : rating.toString();
    }

    private static String getOverdueLabel(Activity activity, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate activityDate = activity.getDate();
        if (activityDate.equals(today)) {
            return "OVERDUE TODAY";
        }
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (!activityDate.isBefore(monday)) {
            return "OVERDUE WEEK";
        }
        return "OVERDUE EARLIER";
    }

    private static int getChronologicalIndex(Activity activity, List<Activity> canonicalView) {
        int index = canonicalView.indexOf(activity);
        assert index >= 0 : "A formatted activity must belong to the canonical view.";
        return index + 1;
    }

    //@@author
    //@@author Oliver-koh
    /**
     * Formats name-search results using the indexes from the full chronological list.
     *
     * @param keyword searched name phrase.
     * @param matches matching activities in chronological order.
     * @param canonicalView full chronological activity view.
     * @return formatted search result.
     */
    public static String formatFindResults(String keyword, List<Activity> matches,
            List<Activity> canonicalView) {
        if (matches.isEmpty()) {
            return "Sorry, no activity names match your search.";
        }
        String activityLabel = matches.size() == 1 ? "activity" : "activities";
        StringBuilder output = new StringBuilder("Found ")
                .append(matches.size()).append(" matching ").append(activityLabel).append(":");
        for (Activity activity : matches) {
            output.append("\n").append(canonicalView.indexOf(activity) + 1)
                    .append(". ").append(describe(activity));
        }
        return output.toString();
    }
    //@@author
    //@@author officialasyrafrahim

    private static String format(String heading, List<Activity> activities) {
        if (activities.isEmpty()) {
            return EMPTY_MESSAGE;
        }
        StringBuilder output = new StringBuilder(heading);
        for (int i = 0; i < activities.size(); i++) {
            output.append("\n").append(i + 1).append(". ")
                    .append(describe(activities.get(i)));
        }
        return output.toString();
    }

    /**
     * Matches the add confirmation's field formats (ISO date, 24-hour times, enum demand).
     */
    private static String describe(Activity activity) {
        return activity.getName()
                + " (" + activity.getDate()
                + ", " + activity.getStartTime()
                + "-" + activity.getEndTime()
                + ") [" + activity.getDemand() + "] "
                + (activity.isDone() ? "[X]" : "[ ]");
    }
}
//@@author
