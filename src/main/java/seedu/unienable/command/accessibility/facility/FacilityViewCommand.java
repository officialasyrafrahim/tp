package seedu.unienable.command.accessibility.facility;

import java.util.Optional;

import seedu.unienable.command.accessibility.connection.logic.FacilityManager;
import seedu.unienable.command.common.Command;
import seedu.unienable.command.common.CommandResult;
import seedu.unienable.exception.UniEnableException;
import seedu.unienable.model.ActivityList;
import seedu.unienable.model.Facility;
import seedu.unienable.storage.Storage;
import seedu.unienable.ui.accessibility.FacilityOutputFormatter;

//@@author lukelouyu
/**
 * Shows the recorded accessibility features of a single facility.
 */
public class FacilityViewCommand extends Command {
    private final FacilityManager facilityManager;
    private final String identifier;

    /**
     * Creates a command to view a facility by its name or stable ID.
     *
     * @param facilityManager manager containing the local facility reference data.
     * @param identifier facility ID or name entered by the user.
     */
    public FacilityViewCommand(FacilityManager facilityManager, String identifier) {
        this.facilityManager = facilityManager;
        this.identifier = identifier;
    }

    /**
     * Displays facility details or reports an unknown facility through the checked error contract.
     *
     * @param activities activity collection, not modified by this command.
     * @param storage activity storage, not used by this command.
     * @return non-exiting result containing facility details.
     * @throws UniEnableException if the facility is unknown.
     */
    @Override
    public CommandResult execute(ActivityList activities, Storage storage) throws UniEnableException {
        Optional<Facility> matchingFacility = facilityManager.findFacility(identifier);
        if (matchingFacility.isEmpty()) {
            String warning = FacilityOutputFormatter.formatUnknownFacility(
                    identifier, facilityManager.getFacilities());
            throw new UniEnableException(warning);
        }
        String message = FacilityOutputFormatter.formatFacility(matchingFacility.get());
        return new CommandResult(message, false);
    }
}
//@@author
