<!-- @@author Atharva005 -->

# Atharva Rege - Project Portfolio Page

## Project: UniEnable

### Overview

UniEnable is a Java 25 command-line application that helps university students, particularly those with ASD, ADHD, or mobility constraints, organize academic and daily commitments and check campus accessibility information. It combines dated activities, optional importance, urgency and commitment ratings, priority and calendar views, completion tracking, and automatic local saving with facility searches and shortest recorded accessible-route queries using sample data for nine NUS FASS campus hubs.

### Summary of contributions

I contributed activity creation, daily/weekly calendar dashboards, and command help. My work covers input parsing, activity insertion, schedule summaries, related tests, and feature documentation and diagrams.

**Code contributed:** [My code and tests on the tP Code Dashboard](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs%7Efunctional-code%7Etest-code%7Eother&since=2026-09-18T00%3A00%3A00&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=Atharva005&tabRepo=AY2627S1-CS2113-T10-4%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=functional-code%7Etest-code&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false)

#### Enhancements implemented

- **Activity creation:** `add` accepts named fields in any order and multiword activity names. It validates required fields, calendar dates, and same-day time ranges before insertion. Students can record ongoing or future activities whose end is after the current time, with optional demand and ratings. Duplicate and overlap checks protect the schedule, and rejected input leaves activities unchanged ([PR #11](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/11), [PR #23](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/23)).
- **Date shortcuts:** `add` and `edit` accept `d/today` and `d/tomorrow`. The shared `ActivityDateParser` turns each word into a real date using the injectable clock, so saved activities stay on that day and the existing schedule checks still apply ([PR #100](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/100)).
- **Calendar dashboards:** `view today`, `view tomorrow`, and `view week` combine date filtering, daily schedules, completion summaries, demand counts, and rating charts without changing stored activities. Students can inspect commitments and workload together; an injectable clock makes date selection reproducible ([PR #47](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/47)).
- **Command discovery:** `help` displays a categorized command menu, while `help TOPIC` provides syntax, rules, and examples. Both are read-only, allowing users to discover commands without executing them ([PR #21](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/21)).

#### Testing contributions

Contributed JUnit tests for activity creation, parser field handling, invalid inputs, and help output ([PR #21](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/21)). The [add tests](https://github.com/AY2627S1-CS2113-T10-4/tp/blob/master/src/test/java/seedu/unienable/command/activity/AddCommandTest.java) exercise insertion and confirmation output; parser tests check accepted fields and rejection before mutation.

#### Documentation contributions

- **User Guide:** [Adding activities](../UserGuide.md#adding-an-activity-add), [calendar dashboards](../UserGuide.md#viewing-calendar-dashboards-view), and [command help](../UserGuide.md#viewing-command-help-help), including formats, examples, validation rules, and empty-period behavior.
- **Developer Guide:** [Adding activities](../DeveloperGuide.md#adding-activities), [date parsing](../DeveloperGuide.md#date-parsing), [calendar dashboards](../DeveloperGuide.md#calendar-dashboards), and [categorized/topic help](../DeveloperGuide.md#categorized-and-topic-help), covering the parser-command-model flow and read-only views.
- **UML diagrams:** Contributed the add sequence, four class/sequence pairs for calendar dashboards, topic help, activity-schedule validation, and date parsing, and an illustrative activity-state object diagram. These ten sources and rendered images are listed in [my diagram index](../diagrams/README.md#atharva). The activity-creation class diagram and name/parameter validation pairs are credited to Luke.

#### Team-based tasks

Connected `add` parsing to activity insertion and registered calendar commands in the shared dispatcher ([PR #11](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/11), [PR #47](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/47)). Updated my diagram assets in [PR #58](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/58) and corrected them in [PR #71](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/71), keeping feature explanations aligned with the integrated application.

<!-- @@author -->
