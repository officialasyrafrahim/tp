package seedu.unienable.ui.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.unienable.model.Activity;
import seedu.unienable.model.enums.DemandLevel;

//@@author officialasyrafrahim
/**
 * Checks pure formatting of activity views: preserved order, 1-based numbering,
 * and the empty-list placeholder.
 */
class ActivityListFormatterTest {
    private final Activity mondayGym = new Activity("Gym session", LocalDate.parse("2026-10-12"),
            LocalTime.parse("07:00"), LocalTime.parse("09:00"), DemandLevel.HIGH, false);
    private final Activity tuesdayLab = new Activity("Lab", LocalDate.parse("2026-10-13"),
            LocalTime.parse("14:00"), LocalTime.parse("16:00"), DemandLevel.LOW, false);

    @Test
    void formatList_empty_showsOnlySorryMessage() {
        String message = ActivityListFormatter.formatList(List.of());
        assertEquals("Sorry, the activity list is empty.", message);
        assertTrue(message.endsWith("Sorry, the activity list is empty."),
                "empty-list message missing");
    }

    @Test
    void formatList_preservesGivenOrderWithOneBasedIndexes() {
        // Deliberately not in canonical order: the formatter must render what it receives.
        String message = ActivityListFormatter.formatList(List.of(tuesdayLab, mondayGym));
        assertTrue(message.contains("1. Lab (2026-10-13, 14:00-16:00) [LOW]"));
        assertTrue(message.contains("2. Gym session (2026-10-12, 07:00-09:00) [HIGH]"));
        assertTrue(message.indexOf("Lab") < message.indexOf("Gym session"),
                "formatter must not reorder the given view");
    }

    @Test
    void formatDemandList_usesDemandHeadingAndSameNumbering() {
        String headingAndNotice = "Activities by demand (HIGH to LOW):\n"
                + "Indexes refer to the chronological 'list' order.\n";
        assertEquals(headingAndNotice + "1. Gym session (2026-10-12, 07:00-09:00) [HIGH] [ ]",
                ActivityListFormatter.formatDemandList(List.of(mondayGym), List.of(mondayGym)));
        assertEquals("Sorry, the activity list is empty.",
                ActivityListFormatter.formatDemandList(List.of(), List.of()));
    }

    @Test
    void formatDemandList_preservesChronologicalIndexesWhenDemandOrderDiffers() {
        Activity laterHigh = new Activity("Later high", LocalDate.parse("2026-10-14"),
                LocalTime.parse("09:00"), LocalTime.parse("10:00"), DemandLevel.HIGH, true);
        String message = ActivityListFormatter.formatDemandList(List.of(laterHigh, tuesdayLab),
                List.of(tuesdayLab, laterHigh));

        assertEquals("Activities by demand (HIGH to LOW):\n"
                + "Indexes refer to the chronological 'list' order.\n"
                + "2. Later high (2026-10-14, 09:00-10:00) [HIGH] [X]\n"
                + "1. Lab (2026-10-13, 14:00-16:00) [LOW] [ ]", message);
    }

    @Test
    void formatPriorityList_usesChronologicalIndexesAndShowsScoreComponents() {
        Activity scored = new Activity("Scored", LocalDate.parse("2026-10-11"), LocalTime.parse("10:00"),
                LocalTime.parse("11:00"), DemandLevel.LOW, true, 5, 4, 3);
        String message = ActivityListFormatter.formatPriorityList(List.of(scored, tuesdayLab),
                List.of(scored, mondayGym, tuesdayLab));

        assertEquals("Activities by priority (highest first):\n"
                + "Indexes refer to the chronological 'list' order.\n"
                + "1. Scored (2026-10-11, 10:00-11:00) [LOW] [X] | Priority 128 (IMP 5, URG 4, COM 3)\n"
                + "3. Lab (2026-10-13, 14:00-16:00) [LOW] [ ] | Priority NA (IMP -, URG -, COM -)",
                message);
    }

    @Test
    void formatOverdueList_labelsTodayWeekAndEarlierAndKeepsFullIndexes() {
        Activity earlier = new Activity("Earlier", LocalDate.parse("2026-10-05"), LocalTime.parse("09:00"),
                LocalTime.parse("10:00"), DemandLevel.LOW, false);
        Activity today = new Activity("Today", LocalDate.parse("2026-10-13"), LocalTime.parse("09:00"),
                LocalTime.parse("10:00"), DemandLevel.LOW, false);
        List<Activity> canonicalView = List.of(earlier, mondayGym, today);
        String message = ActivityListFormatter.formatOverdueList(List.of(earlier, mondayGym, today),
                canonicalView, LocalDateTime.parse("2026-10-13T12:00:00"));

        assertEquals("Overdue activities (ended before now):\n"
                + "1. [OVERDUE EARLIER] Earlier (2026-10-05, 09:00-10:00) [LOW] [ ]\n"
                + "2. [OVERDUE WEEK] Gym session (2026-10-12, 07:00-09:00) [HIGH] [ ]\n"
                + "3. [OVERDUE TODAY] Today (2026-10-13, 09:00-10:00) [LOW] [ ]",
                message);
        assertEquals("Sorry, no incomplete activities have ended.",
                ActivityListFormatter.formatOverdueList(List.of(), canonicalView,
                        LocalDateTime.parse("2026-10-13T12:00:00")));
    }

    @Test
    void formatNext_showsOneIndexedActivityOrEmptyMessage() {
        assertEquals("Next activity (earliest upcoming):\n2. Lab (2026-10-13, 14:00-16:00) [LOW] [ ]",
                ActivityListFormatter.formatNext(Optional.of(tuesdayLab), List.of(mondayGym, tuesdayLab)));
        assertEquals("Sorry, no upcoming incomplete activities exist.",
                ActivityListFormatter.formatNext(Optional.empty(), List.of(mondayGym, tuesdayLab)));
    }
}
//@@author
