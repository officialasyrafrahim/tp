//@@author lukelouyu
package seedu.unienable.command.accessibility.connection.logic;

//@@author

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.unienable.model.Facility;
import seedu.unienable.model.FacilityFeature;
import seedu.unienable.model.enums.AccessibilityStatus;

//@@author lukelouyu
/**
 * Checks facility identity, exact statuses, deduplication, and immutable manager snapshots.
 */
class FacilityManagerTest {
    @Test
    void facilityIdentityAndStatuses_preserveDistinctMeaning() {
        var lift = new FacilityFeature(FacilityFeature.Type.LIFT, AccessibilityStatus.YES, null);
        Facility firstHub = new Facility("F01", "AS1", null, List.of(lift, lift));
        Facility secondHub = new Facility("F02", "F01", null, List.of(
                new FacilityFeature(FacilityFeature.Type.LIFT, AccessibilityStatus.UNKNOWN, null)));
        var source = new ArrayList<>(List.of(firstHub, secondHub));
        var manager = new FacilityManager(source);
        source.clear();
        for (String query : List.of("f01", " F01 ", "as1")) {
            assertSame(firstHub, manager.findFacility(query).orElseThrow());
        }
        assertSame(secondHub, manager.findFacility("f02").orElseThrow());
        assertTrue(manager.findFacility("missing").isEmpty());
        assertTrue(manager.findFacility(" ").isEmpty());
        assertEquals(List.of(firstHub), manager.findByFeature(FacilityFeature.Type.LIFT));
        assertEquals(List.of(secondHub),
                manager.findByFeature(FacilityFeature.Type.LIFT, AccessibilityStatus.UNKNOWN));
        assertTrue(manager.findByFeature(FacilityFeature.Type.RAMP).isEmpty());
        assertTrue(manager.findByFeature(FacilityFeature.Type.RAMP, AccessibilityStatus.UNKNOWN).isEmpty());
        assertThrows(NullPointerException.class, () -> manager.findFacility(null));
        assertThrows(NullPointerException.class, () -> manager.findByFeature(null));
        assertThrows(NullPointerException.class, () -> manager.findByFeature(FacilityFeature.Type.LIFT, null));
        assertThrows(UnsupportedOperationException.class, () -> manager.getFacilities().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> manager.findByFeature(FacilityFeature.Type.LIFT).clear());
        assertTrue(new FacilityManager(List.of()).findFacility("AS1").isEmpty());
    }
}
//@@author
