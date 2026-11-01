//@@author lukelouyu
package seedu.unienable.command.activity;

import java.util.List;

import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.Activity;
import seedu.unienable.model.ActivityList;

/**
 * Rejects duplicate activities and unapproved overlapping times before add or edit changes the list.
 * Existing saved records remain loadable, including schedules created before this rule.
 */
final class ActivityScheduleValidator {
    /**
     * Prevents instantiation of this shared validation helper.
     */
    private ActivityScheduleValidator() {
    }

    /**
     * Checks duplicates before overlaps and reports the conflicting chronological list index.
     * A duplicate has the same name ignoring case and repeated/surrounding whitespace, date and time range.
     * Ratings, demand and completion do not make a duplicate distinct or remove a time conflict.
     * Touching endpoints are allowed; an edit excludes only the activity being replaced.
     *
     * @param candidate proposed activity, not yet inserted.
     * @param activities current activities.
     * @param replacedActivity original activity for an edit, or null for an add.
     * @throws UniEnableException if a duplicate or overlapping activity exists.
     */
    static void validate(Activity candidate, ActivityList activities, Activity replacedActivity)
            throws UniEnableException {
        validate(candidate, activities, replacedActivity, false);
    }

    /**
     * Checks duplicates unconditionally, allowing a time overlap only when explicitly requested.
     */
    static void validate(Activity candidate, ActivityList activities, Activity replacedActivity,
            boolean isOverlapAllowed) throws UniEnableException {
        List<Activity> existing = activities.getCanonicalView();
        for (int index = 0; index < existing.size(); index++) {
            Activity activity = existing.get(index);
            if (activity != replacedActivity
                    && normalizeName(activity.getName()).equalsIgnoreCase(normalizeName(candidate.getName()))
                    && activity.getDate().equals(candidate.getDate())
                    && activity.getStartTime().equals(candidate.getStartTime())
                    && activity.getEndTime().equals(candidate.getEndTime())) {
                throw new UniEnableException("[WARNING] Duplicate activity already recorded at "
                        + describe(index + 1, activity));
            }
        }
        if (isOverlapAllowed) {
            return;
        }
        for (int index = 0; index < existing.size(); index++) {
            Activity activity = existing.get(index);
            if (activity != replacedActivity && activity.getDate().equals(candidate.getDate())
                    && candidate.getStartTime().isBefore(activity.getEndTime())
                    && activity.getStartTime().isBefore(candidate.getEndTime())) {
                throw new UniEnableException("[WARNING] Activity clashes with " + describe(index + 1, activity)
                        + " Add overlap/ALLOW to keep this intentional overlap.");
            }
        }
    }

    /**
     * Collapses spacing for identity checks while preserving the user's stored/displayed name.
     */
    private static String normalizeName(String name) {
        return name.strip().replaceAll("\\s+", " ");
    }

    /**
     * Identifies the stored activity using the index and schedule shown by list.
     */
    private static String describe(int index, Activity activity) {
        return "index " + index + ": " + activity.getName() + " (" + activity.getDate() + ", "
                + activity.getStartTime() + "-" + activity.getEndTime() + ").";
    }
}
//@@author
