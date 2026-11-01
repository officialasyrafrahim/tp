<!-- @@author lukelouyu -->

# Lou Yu - Project Portfolio Page

## Project: UniEnable

### Overview

UniEnable is a Java 25 command-line application that helps university students, particularly those with ASD, ADHD, or mobility constraints, organize academic and daily commitments and check campus accessibility information. It combines dated activities, optional importance, urgency and commitment ratings, priority and calendar views, completion tracking, and automatic local saving with facility searches and shortest recorded accessible-route queries using sample data for nine NUS FASS campus hubs.

### Summary of contributions

I implemented facility queries and accessible-route search, added optional activity ratings, and strengthened shared input validation, record recovery, and saving. I also contributed routing/regression tests, architecture and feature documentation, UML diagrams, and reviews of teammates' changes.

**Code contributed:** [My code and tests on the tP Code Dashboard](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs%7Efunctional-code%7Etest-code%7Eother&since=2026-09-18T00%3A00%3A00&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=lukelouyu&tabRepo=AY2627S1-CS2113-T10-4%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=functional-code%7Etest-code&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false)

#### Enhancements implemented

- **Facilities and accessible routes:** Implemented facility lookup, feature/status filtering, data loading, and route queries to help students inspect recorded accessibility information ([PR #6](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/6), [PR #17](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/17)). Dijkstra's algorithm considers accessible bidirectional connections. Separating graph search, loading, and formatting keeps route queries independently testable and lets the console report recoverable data errors.
- **Optional activity ratings:** Importance, urgency, and commitment are independently optional scores from 1 to 5. Shared validation distinguishes an unset rating from a score. Storage preserves these values while reading legacy six-field records; priority and calendar views use the same rating model ([PR #30](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/30)).
- **Shared validation and recovery:** Add/edit require supported slash prefixes and allow commas and apostrophes. Duplicate checks ignore case and repeated spacing; renaming preserves approved overlaps ([PR #52](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/52), [PR #54](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/54)). Storage loads readable rows around invalid UTF-8 records and preserves original backup bytes. Failed saves retain changes in memory, and `bye` retries saving before exit ([PR #55](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/55)).

#### Testing contributions

Routing tests use a fixed matrix for all 81 campus pairs to independently check distances and valid links ([PR #45](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/45), [tests](https://github.com/AY2627S1-CS2113-T10-4/tp/blob/master/src/test/java/seedu/unienable/command/accessibility/connection/logic/DijkstraShortestPathTest.java)). Regression coverage includes rating/legacy-storage compatibility ([PR #30](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/30)), save retries ([PR #55](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/55)), and [console checks](https://github.com/AY2627S1-CS2113-T10-4/tp/blob/master/src/test/java/seedu/unienable/UniEnableProcessTest.java) for punctuation, slash rejection, route ID/name equivalence, and counts across restart. Date shortcut coverage includes `ActivityDateParserTest` and a full `UniEnableTest` session for `d/today` and `d/tomorrow` ([PR #100](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/100)).

#### Documentation contributions

- **User Guide:** [Optional add ratings](../UserGuide.md#trying-optional-add-ratings), [date shortcuts](../UserGuide.md#adding-an-activity-add), [facility queries](../UserGuide.md#listing-and-viewing-facilities-facility), [feature search](../UserGuide.md#finding-facilities-by-feature-facility-find), [routes](../UserGuide.md#finding-an-accessible-route-route), and [exit](../UserGuide.md#exiting-the-application-bye).
- **Developer Guide:** [Architecture](../DeveloperGuide.md#architecture), shared validation, [ratings/accessibility](../DeveloperGuide.md#ratings-and-accessibility), data-loading contracts, and routing design. Consolidated guide sections and accessibility diagrams in [PR #35](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/35).
- **Diagrams:** Contributed the activity-creation class diagram, name and parameter validation class/sequence pairs, architecture diagram, facility and routing class/sequence pairs, and campus connectivity graph - eleven diagrams in total ([index](../diagrams/README.md#luke)). The add sequence, schedule validation pair, and date parsing pair remain credited to Atharva.

#### Team-based tasks and reviews

Gave specific feedback on add parsing, model insertion, package placement, and tests in [PR #11](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/11#issuecomment-6075041154). Reviewed and approved the [planning views](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/36#pullrequestreview-5601662187) and [calendar command](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/47#pullrequestreview-5643188730). Reported a reproducible Unicode-whitespace edit/restart defect with regression suggestions in [course tracker issue #41](https://github.com/NUS-CS2113-AY2627-S1/tp/issues/41).

**Credits:** Campus data references the NUS FASS accessibility map; inherited tooling comes from the course starter ([acknowledgements](../DeveloperGuide.md#acknowledgements)).

<!-- @@author -->
