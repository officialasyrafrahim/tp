package seedu.unienable.model;

import seedu.unienable.model.enums.AccessibilityStatus;

//@@author lukelouyu
/**
 * One immutable accessibility feature recorded at a facility.
 */
public final class FacilityFeature {
    private final Type type;
    private final AccessibilityStatus status;
    private final String notes;

    /**
     * Creates a reference-data object without parsing or normalizing the supplied values.
     *
     * @param type feature category.
     * @param status recorded accessibility status; UNKNOWN means unconfirmed.
     * @param notes optional notes, or null.
     */
    public FacilityFeature(Type type, AccessibilityStatus status, String notes) {
        this.type = type;
        this.status = status;
        this.notes = notes;
    }

    /**
     * Returns the type value recorded for this object.
     *
     * @return feature category.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the status value recorded for this object.
     *
     * @return recorded accessibility status; UNKNOWN means unconfirmed.
     */
    public AccessibilityStatus getStatus() {
        return status;
    }

    /**
     * Returns the notes value recorded for this object.
     *
     * @return optional notes, or null.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Supported categories of facility accessibility features.
     */
    public enum Type {
        /** A recorded lift. */
        LIFT,
        /** A recorded ramp. */
        RAMP,
        /** A recorded sheltered ramp. */
        SHELTERED_RAMP,
        /** A recorded accessible washroom. */
        ACCESSIBLE_WASHROOM,
        /** A recorded step-free entrance. */
        STEP_FREE_ENTRANCE,
        /** A recorded resting point. */
        REST_POINT,
        /** A recorded automatic door. */
        AUTOMATIC_DOOR,
        /** Another recorded accessibility feature. */
        OTHER
    }
}
//@@author
