<!-- @@author officialasyrafrahim -->

# Muhammad Asyraf Bin Abdul Rahim - Project Portfolio Page

## Project: UniEnable

### Overview

UniEnable is a Java 25 command-line application that helps university students, particularly those with ASD, ADHD, or mobility constraints, organize academic and daily commitments and check campus accessibility information. It combines dated activities, optional importance, urgency and commitment ratings, priority and calendar views, completion tracking, and automatic local saving with facility searches and shortest recorded accessible-route queries using sample data for nine NUS FASS campus hubs.

### Summary of contributions

I contributed activity listing and deletion, priority, overdue, and next-activity views. My work covers ordering and index resolution across the parser, model, commands, and formatter, alongside tests, feature documentation, and shared parser/launcher integration.

**Code contributed:** [My code and tests on the tP Code Dashboard](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs%7Efunctional-code%7Etest-code%7Eother&since=2026-09-18T00%3A00%3A00&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=officialasyrafrahim&tabRepo=AY2627S1-CS2113-T10-4%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=functional-code%7Etest-code&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false)

#### Enhancements implemented

- **Activity listing and deletion:** `list`, `list demand`, and `delete` help students inspect workload and remove the intended commitment ([PR #16](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/16)). Adding a 16:00 activity before a 09:00 activity on the same day still makes `delete 1` remove the 09:00 activity shown first by `list`. Sorted views preserve storage order, and demand-view labels retain the same chronological indexes.
- **Priority and time-based planning:** `list priority`, `overdue`, and `next` help students choose what needs attention ([PR #36](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/36)). Priority uses `URG x URG x (IMP + COM)`, puts incomplete rating sets last, and preserves chronological ties. Time queries exclude completed activities and use an injected clock: `overdue` selects activities that have ended, while `next` selects the earliest upcoming activity.
- **Defensive behavior:** Added ordering/deletion invariants and diagnostic logging ([PR #29](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/29)). Keeping listing read-only and reporting deletion as a state change allows the shared console to save only when necessary, protecting activity data during ordinary queries.

#### Testing contributions

Added tests for chronological/demand ordering, stable ties, displayed-index deletion, invalid indexes, and read-only listing in [PR #16](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/16). Extended coverage to missing priority ratings and fixed-clock query boundaries in [PR #36](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/36), including [priority command tests](https://github.com/AY2627S1-CS2113-T10-4/tp/blob/master/src/test/java/seedu/unienable/command/activity/ListPriorityCommandTest.java). These catch incorrect selection as well as incorrect presentation.

#### Documentation contributions

- **User Guide:** [Chronological/demand listing](../UserGuide.md#listing-activities-list-and-list-demand), [deletion](../UserGuide.md#deleting-an-activity-delete), [priority](../UserGuide.md#viewing-priority-list-priority), [overdue](../UserGuide.md#viewing-overdue-activities-overdue), and [next activity](../UserGuide.md#viewing-the-next-activity-next), including index and time-boundary rules.
- **Developer Guide:** [Activity views and deletion](../DeveloperGuide.md#activity-views-and-deletion), explaining comparator-based views, canonical index resolution, query boundaries, and related tests.
- **UML diagrams:** Contributed six class/sequence pairs for chronological listing, demand listing, deletion, priority listing, overdue activities, and next activity ([diagram index](../diagrams/README.md#asyraf)). The feature diagrams and accompanying guide updates were submitted in [PR #43](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/43).

#### Team-based tasks

Contributed shared parser and launcher integration, connecting activity commands to the application entry point ([PR #25](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/25)). Time-sensitive queries use a testable clock, and shared parser/import integration is checked with Java 25 builds ([integration evidence](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/25#issuecomment-6150154257)).

<!-- @@author -->
