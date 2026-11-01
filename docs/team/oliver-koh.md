<!-- @@author Oliver-koh -->

# Koh Li Wei Oliver - Project Portfolio Page

## Project: UniEnable

### Overview

UniEnable is a Java 25 command-line application that helps university students, particularly those with ASD, ADHD, or mobility constraints, organize academic and daily commitments and check campus accessibility information. It combines dated activities, optional importance, urgency and commitment ratings, priority and calendar views, completion tracking, and automatic local saving with facility searches and shortest recorded accessible-route queries using sample data for nine NUS FASS campus hubs.

### Summary of contributions

I implemented activity completion, file persistence and malformed-record recovery, partial editing, and name search. I connected activity changes to the console's save flow and contributed restart/command tests, storage and editing documentation, and UML diagrams.

**Code contributed:** [My code and tests on the tP Code Dashboard](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs%7Efunctional-code%7Etest-code%7Eother&since=2026-09-18T00%3A00%3A00&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=Oliver-koh&tabRepo=AY2627S1-CS2113-T10-4%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=functional-code%7Etest-code&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false)

#### Enhancements implemented

- **Completion tracking:** Implemented `mark` and `unmark` so students can track progress across sessions ([PR #20](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/20)). Commands resolve chronological indexes and signal whether state actually changed. Repeating an already satisfied request warns without requesting another save, avoiding unnecessary file writes.
- **Persistence and recovery:** UTF-8 activity storage and console integration preserve commitments across restart ([PR #20](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/20)). Valid records load alongside line-numbered warnings for malformed or invalidly encoded rows. Saves retain optional ratings, read legacy records, and verify an original-file backup before recovery. Temporary-file replacement avoids partial writes; failed saves keep changes in memory and `bye` retries before exiting.
- **Editing and name search:** Partial `edit` and literal, case-insensitive `find` let students revise and locate commitments without recreating them ([PR #31](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/31)). For an activity that has not ended, a name-only edit preserves its schedule and completion state; invalid replacements leave it unchanged. Ended activities cannot be edited or rescheduled. Search retains full-list chronological indexes, and supplied names reject Unicode whitespace to keep saved records readable ([PR #42](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/42)).

#### Testing contributions

Contributed storage, mark/unmark, and parser tests for restart persistence, malformed-row recovery, invalid indexes, and repeated completion requests ([PR #20](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/20)). The [edit tests](https://github.com/AY2627S1-CS2113-T10-4/tp/blob/master/src/test/java/seedu/unienable/command/activity/EditCommandTest.java) and search tests cover partial updates, rejected changes, and chronological results ([PR #31](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/31)).

#### Documentation contributions

- **User Guide:** [Data storage](../UserGuide.md#data-storage), [completion commands](../UserGuide.md#changing-completion-mark-and-unmark), [editing](../UserGuide.md#editing-an-activity-edit), [name search](../UserGuide.md#searching-activity-names-find), and persistence/recovery guidance in [error handling](../UserGuide.md#error-handling).
- **Developer Guide:** [Completion, storage, editing, and search](../DeveloperGuide.md#completion-storage-editing-and-search), including partial edits, chronological indexes, conditional saves, record formats, and backup/retry behavior.
- **UML diagrams:** Contributed five class/sequence pairs for editing, name search, marking, unmarking, and persistence ([diagram index](../diagrams/README.md#oliver)). They show how commands use the activity list and how the console decides whether saving is needed.

#### Team-based tasks

Connected add, delete, and completion commands to the command-result flag that tells the console when to save ([PR #20](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/20)). Integrated edit/search with the shared ratings model ([PR #31](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/31)). Maintain the corresponding diagram assets ([PR #59](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/59), [PR #70](https://github.com/AY2627S1-CS2113-T10-4/tp/pull/70)).

<!-- @@author -->
