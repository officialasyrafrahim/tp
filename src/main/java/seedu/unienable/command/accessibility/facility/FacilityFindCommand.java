package seedu.unienable.command.accessibility.facility;

import java.util.List;

import seedu.unienable.command.accessibility.connection.logic.FacilityManager;
import seedu.unienable.command.common.Command;
import seedu.unienable.command.common.CommandResult;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.Facility;
import seedu.unienable.model.FacilityFeature;
import seedu.unienable.model.enums.AccessibilityStatus;
import seedu.unienable.storage.Storage;
import seedu.unienable.ui.accessibility.FacilityOutputFormatter;

//@@author lukelouyu
/**
 * Lists facilities with a recorded accessibility feature and a specified status.
 */
public class FacilityFindCommand extends Command {
    private final FacilityManager facilityManager;
    private final FacilityFeature.Type type;
    private final AccessibilityStatus status;

    /**
     * Creates a read-only facility feature search.
     *
     * @param facilityManager manager containing the loaded facility reference data.
     * @param type feature to look for.
     * @param status exact recorded status to match.
     */
    public FacilityFindCommand(FacilityManager facilityManager, FacilityFeature.Type type,
            AccessibilityStatus status) {
        this.facilityManager = facilityManager;
        this.type = type;
        this.status = status;
    }

    /**
     * Lists matching facilities without changing activities or stored data.
     *
     * @param activities existing activities, not modified.
     * @param storage activity storage, not used.
     * @return non-exiting command result containing matching facilities and disclaimer.
     */
    @Override
    public CommandResult execute(ActivityList activities, Storage storage) {
        List<Facility> matches = facilityManager.findByFeature(type, status);
        String message = FacilityOutputFormatter.formatFindResults(matches, type, status);
        return new CommandResult(message, false);
    }
}
//@@author
