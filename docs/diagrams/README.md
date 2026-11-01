# Diagram ownership index

PlantUML sources are grouped by the diagram contribution recorded in
the [Developer Guide](../DeveloperGuide.md#team-contributions). Each member's folder
contains `class/` and `sequence/` subfolders. Rendered files use the same owner,
diagram type, and base filename under [images](../images/README.md).
All four members' diagram folders are populated. The groups indicate diagram
responsibility; classes used across features can appear in several diagrams.

Lou Yu Luke's architecture diagram uses `luke/architecture/`. The campus connectivity
graph uses `luke/graph/`; it is separate from the class and sequence diagrams.

| Folder | Responsibility |
| --- | --- |
| [atharva](#atharva) | Atharva Rege — add sequence, calendar dashboards, help, schedule validation, and date parsing diagrams. |
| [asyraf](#asyraf) | Muhammad Asyraf Bin Abdul Rahim — chronological/demand listing, deletion, priority, overdue and next. |
| [oliver](#oliver) | Koh Li Wei Oliver — completion, storage/recovery, editing and search. |
| [luke](#luke) | Lou Yu Luke — activity-creation class diagram, name/parameter validation, architecture, optional add ratings, accessibility and exit. |

The activity-creation class diagram, name and parameter validation pairs,
architecture diagram, and shared validation helper are documented alongside the
corresponding implementation. The add sequence, calendar dashboards, help,
schedule validation pair, and date parsing pair are documented in their
respective sections. Exit has no separate diagram.

Owned PlantUML sources enclose RepoSense author markers in PlantUML block comments. Their image links and
Developer Guide embeds sit inside the corresponding Markdown author scopes.
Imported PNG files retain the original archive images. New and updated diagrams
are rendered from the corresponding PlantUML sources.

<!-- @@author Atharva005 -->

<a id="atharva"></a>

## Atharva Rege

### Class diagrams

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [ActivityDateParserClassDiagram.puml](atharva/class/ActivityDateParserClassDiagram.puml) | [PNG](../images/atharva/class/ActivityDateParserClassDiagram.png) | [Date parsing](../DeveloperGuide.md#date-parsing) |
| [ActivityScheduleValidatorClassDiagram.puml](atharva/class/ActivityScheduleValidatorClassDiagram.puml) | [PNG](../images/atharva/class/ActivityScheduleValidatorClassDiagram.png) | [Activity schedule validation](../DeveloperGuide.md#activity-schedule-validation) |
| [CalendarViewClassDiagram.puml](atharva/class/CalendarViewClassDiagram.puml) | [PNG](../images/atharva/class/CalendarViewClassDiagram.png) | [Calendar dashboards](../DeveloperGuide.md#calendar-dashboards) |
| [TopicHelpClassDiagram.puml](atharva/class/TopicHelpClassDiagram.puml) | [PNG](../images/atharva/class/TopicHelpClassDiagram.png) | [Categorized and topic help](../DeveloperGuide.md#categorized-and-topic-help) |

### Sequence diagrams

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [AddActivitySequence.puml](atharva/sequence/AddActivitySequence.puml) | [PNG](../images/atharva/sequence/AddActivitySequence.png) | [Adding activities](../DeveloperGuide.md#adding-activities) |
| [ActivityDateParsingSequence.puml](atharva/sequence/ActivityDateParsingSequence.puml) | [PNG](../images/atharva/sequence/ActivityDateParsingSequence.png) | [Date parsing](../DeveloperGuide.md#date-parsing) |
| [ActivityScheduleValidationSequence.puml](atharva/sequence/ActivityScheduleValidationSequence.puml) | [PNG](../images/atharva/sequence/ActivityScheduleValidationSequence.png) | [Activity schedule validation](../DeveloperGuide.md#activity-schedule-validation) |
| [CalendarViewSequence.puml](atharva/sequence/CalendarViewSequence.puml) | [PNG](../images/atharva/sequence/CalendarViewSequence.png) | [Calendar dashboards](../DeveloperGuide.md#calendar-dashboards) |
| [TopicHelpSequence.puml](atharva/sequence/TopicHelpSequence.puml) | [PNG](../images/atharva/sequence/TopicHelpSequence.png) | [Categorized and topic help](../DeveloperGuide.md#categorized-and-topic-help) |


### Object diagram

- [ActivityStateSnapshot.puml](atharva/object/ActivityStateSnapshot.puml) | [PNG](../images/atharva/object/ActivityStateSnapshot.png) | [Activity state snapshot](../DeveloperGuide.md#activity-state-snapshot)

The snapshot source and image use matching `atharva/object/` folders. Regenerate it with Java 25:

```text
java -jar "PATH_TO_PLANTUML_JAR" -charset UTF-8 -tpng -o ../../../images/atharva/object docs/diagrams/atharva/object/ActivityStateSnapshot.puml
```

<!-- @@author -->

<!-- @@author officialasyrafrahim -->

<a id="asyraf"></a>

## Muhammad Asyraf Bin Abdul Rahim

### Class diagrams

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [ListActivityClassDiagram.puml](asyraf/class/ListActivityClassDiagram.puml) | [PNG](../images/asyraf/class/ListActivityClassDiagram.png) | [Chronological listing](../DeveloperGuide.md#chronological-listing) |
| [ListDemandClassDiagram.puml](asyraf/class/ListDemandClassDiagram.puml) | [PNG](../images/asyraf/class/ListDemandClassDiagram.png) | [Demand-ordered listing](../DeveloperGuide.md#demand-ordered-listing) |
| [DeleteActivityClassDiagram.puml](asyraf/class/DeleteActivityClassDiagram.puml) | [PNG](../images/asyraf/class/DeleteActivityClassDiagram.png) | [Activity deletion](../DeveloperGuide.md#activity-deletion) |
| [ListPriorityClassDiagram.puml](asyraf/class/ListPriorityClassDiagram.puml) | [PNG](../images/asyraf/class/ListPriorityClassDiagram.png) | [Priority listing](../DeveloperGuide.md#priority-listing) |
| [OverdueActivityClassDiagram.puml](asyraf/class/OverdueActivityClassDiagram.puml) | [PNG](../images/asyraf/class/OverdueActivityClassDiagram.png) | [Overdue activities](../DeveloperGuide.md#overdue-activities) |
| [NextActivityClassDiagram.puml](asyraf/class/NextActivityClassDiagram.puml) | [PNG](../images/asyraf/class/NextActivityClassDiagram.png) | [Next activity](../DeveloperGuide.md#next-activity) |

### Sequence diagrams

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [ListActivitySequence.puml](asyraf/sequence/ListActivitySequence.puml) | [PNG](../images/asyraf/sequence/ListActivitySequence.png) | [Chronological listing](../DeveloperGuide.md#chronological-listing) |
| [ListDemandSequence.puml](asyraf/sequence/ListDemandSequence.puml) | [PNG](../images/asyraf/sequence/ListDemandSequence.png) | [Demand-ordered listing](../DeveloperGuide.md#demand-ordered-listing) |
| [DeleteActivitySequence.puml](asyraf/sequence/DeleteActivitySequence.puml) | [PNG](../images/asyraf/sequence/DeleteActivitySequence.png) | [Activity deletion](../DeveloperGuide.md#activity-deletion) |
| [ListPrioritySequence.puml](asyraf/sequence/ListPrioritySequence.puml) | [PNG](../images/asyraf/sequence/ListPrioritySequence.png) | [Priority listing](../DeveloperGuide.md#priority-listing) |
| [OverdueActivitySequence.puml](asyraf/sequence/OverdueActivitySequence.puml) | [PNG](../images/asyraf/sequence/OverdueActivitySequence.png) | [Overdue activities](../DeveloperGuide.md#overdue-activities) |
| [NextActivitySequence.puml](asyraf/sequence/NextActivitySequence.puml) | [PNG](../images/asyraf/sequence/NextActivitySequence.png) | [Next activity](../DeveloperGuide.md#next-activity) |

<!-- @@author -->

<!-- @@author Oliver-koh -->

<a id="oliver"></a>

## Koh Li Wei Oliver

### Class diagrams

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [MarkActivityClassDiagram.puml](oliver/class/MarkActivityClassDiagram.puml) | [PNG](../images/oliver/class/MarkActivityClassDiagram.png) | [Marking activities](../DeveloperGuide.md#marking-activities) |
| [UnmarkActivityClassDiagram.puml](oliver/class/UnmarkActivityClassDiagram.puml) | [PNG](../images/oliver/class/UnmarkActivityClassDiagram.png) | [Unmarking activities](../DeveloperGuide.md#unmarking-activities) |
| [ActivityPersistenceClassDiagram.puml](oliver/class/ActivityPersistenceClassDiagram.puml) | [PNG](../images/oliver/class/ActivityPersistenceClassDiagram.png) | [Completion and persistence](../DeveloperGuide.md#completion-and-persistence) |
| [EditActivityClassDiagram.puml](oliver/class/EditActivityClassDiagram.puml) | [PNG](../images/oliver/class/EditActivityClassDiagram.png) | [Editing activities](../DeveloperGuide.md#editing-activities) |
| [FindActivityClassDiagram.puml](oliver/class/FindActivityClassDiagram.puml) | [PNG](../images/oliver/class/FindActivityClassDiagram.png) | [Name search](../DeveloperGuide.md#name-search) |

### Sequence diagrams

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [MarkActivitySequence.puml](oliver/sequence/MarkActivitySequence.puml) | [PNG](../images/oliver/sequence/MarkActivitySequence.png) | [Marking activities](../DeveloperGuide.md#marking-activities) |
| [UnmarkActivitySequence.puml](oliver/sequence/UnmarkActivitySequence.puml) | [PNG](../images/oliver/sequence/UnmarkActivitySequence.png) | [Unmarking activities](../DeveloperGuide.md#unmarking-activities) |
| [CompletionAndSaveSequence.puml](oliver/sequence/CompletionAndSaveSequence.puml) | [PNG](../images/oliver/sequence/CompletionAndSaveSequence.png) | [Completion and persistence](../DeveloperGuide.md#completion-and-persistence) |
| [EditActivitySequence.puml](oliver/sequence/EditActivitySequence.puml) | [PNG](../images/oliver/sequence/EditActivitySequence.png) | [Editing activities](../DeveloperGuide.md#editing-activities) |
| [FindActivitySequence.puml](oliver/sequence/FindActivitySequence.puml) | [PNG](../images/oliver/sequence/FindActivitySequence.png) | [Name search](../DeveloperGuide.md#name-search) |

<!-- @@author -->

<!-- @@author lukelouyu -->

<a id="luke"></a>

## Lou Yu Luke

### Architecture

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [ArchitectureDiagram.puml](luke/architecture/ArchitectureDiagram.puml) | [PNG](../images/luke/architecture/ArchitectureDiagram.png) | [Architecture](../DeveloperGuide.md#architecture) |

### Class diagrams

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [ActivityCreationClassDiagram.puml](luke/class/ActivityCreationClassDiagram.puml) | [PNG](../images/luke/class/ActivityCreationClassDiagram.png) | [Adding activities](../DeveloperGuide.md#adding-activities) |
| [ActivityNameValidatorClassDiagram.puml](luke/class/ActivityNameValidatorClassDiagram.puml) | [PNG](../images/luke/class/ActivityNameValidatorClassDiagram.png) | [Command and name validation](../DeveloperGuide.md#ascii-command-name-validation) |
| [ActivityParameterValidatorClassDiagram.puml](luke/class/ActivityParameterValidatorClassDiagram.puml) | [PNG](../images/luke/class/ActivityParameterValidatorClassDiagram.png) | [Activity parameter validation](../DeveloperGuide.md#activity-parameter-validation) |
| [FacilityClassDiagram.puml](luke/class/FacilityClassDiagram.puml) | [PNG](../images/luke/class/FacilityClassDiagram.png) | [Facility lookup](../DeveloperGuide.md#facility-lookup) |
| [RoutingClassDiagram.puml](luke/class/RoutingClassDiagram.puml) | [PNG](../images/luke/class/RoutingClassDiagram.png) | [Accessible route search](../DeveloperGuide.md#accessible-route-search) |

### Sequence diagrams

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [ActivityNameValidationSequence.puml](luke/sequence/ActivityNameValidationSequence.puml) | [PNG](../images/luke/sequence/ActivityNameValidationSequence.png) | [Command and name validation](../DeveloperGuide.md#ascii-command-name-validation) |
| [ActivityParameterValidationSequence.puml](luke/sequence/ActivityParameterValidationSequence.puml) | [PNG](../images/luke/sequence/ActivityParameterValidationSequence.png) | [Activity parameter validation](../DeveloperGuide.md#activity-parameter-validation) |
| [FacilityLookupSequence.puml](luke/sequence/FacilityLookupSequence.puml) | [PNG](../images/luke/sequence/FacilityLookupSequence.png) | [Facility lookup](../DeveloperGuide.md#facility-lookup) |
| [RouteSearchSequence.puml](luke/sequence/RouteSearchSequence.puml) | [PNG](../images/luke/sequence/RouteSearchSequence.png) | [Accessible route search](../DeveloperGuide.md#accessible-route-search) |

### Connectivity graph

| Source | Rendered image | Developer Guide section |
| --- | --- | --- |
| [fass-accessibility-network.puml](luke/graph/fass-accessibility-network.puml) | [SVG](../images/luke/graph/fass-accessibility-network.svg) | [Connectivity graph](../DeveloperGuide.md#connectivity-graph) |

<!-- @@author -->

## Regenerating images

Use Java 25 and a PlantUML JAR. Run commands from the repository root.
PlantUML resolves `-o` relative to each source file's directory, so use
`../../../images/OWNER/TYPE`, where `OWNER` is the member's folder and `TYPE` is
`class` or `sequence`. For example:

```text
java -jar "PATH_TO_PLANTUML_JAR" -charset UTF-8 -tpng -o ../../../images/luke/class docs/diagrams/luke/class/FacilityClassDiagram.puml
```

This writes `docs/images/luke/class/FacilityClassDiagram.png`. All class
and sequence diagrams use PNG. To render a sequence into its matching folder:

```text
java -jar "PATH_TO_PLANTUML_JAR" -charset UTF-8 -tpng -o ../../../images/luke/sequence docs/diagrams/luke/sequence/FacilityLookupSequence.puml
```

The campus connectivity graph uses SVG:

```text
java -jar "PATH_TO_PLANTUML_JAR" -charset UTF-8 -tsvg -o ../../../images/luke/graph docs/diagrams/luke/graph/fass-accessibility-network.puml
```

Regenerate Luke's architecture diagram, including calendar dashboards, topic help,
editing/search, priority queries, and the current command packages, with:

```text
java -jar "PATH_TO_PLANTUML_JAR" -charset UTF-8 -tpng -o ../../../images/luke/architecture docs/diagrams/luke/architecture/ArchitectureDiagram.puml
```

This writes `docs/images/luke/architecture/ArchitectureDiagram.png`.
This source/image pair is maintained under `@@author lukelouyu`.

Regenerate the class and sequence groups separately so each image lands in the
correct category. For example, these commands regenerate all six Asyraf class/sequence pairs:

```text
java -jar "PATH_TO_PLANTUML_JAR" -charset UTF-8 -tpng -o ../../../images/asyraf/class "docs/diagrams/asyraf/class/*.puml"
java -jar "PATH_TO_PLANTUML_JAR" -charset UTF-8 -tpng -o ../../../images/asyraf/sequence "docs/diagrams/asyraf/sequence/*.puml"
```

The class diagrams select PlantUML's bundled Smetana layout engine, and the
sequence diagrams use its sequence renderer. They do not require a separate
Graphviz installation.

Use the same pattern for the `atharva` and `oliver` sources listed above,
changing both the owner and category in the input and output paths. Their class
diagrams also use Smetana; sequence diagrams use PlantUML's sequence renderer.
These diagrams do not require external Graphviz.

Update each source and its matching rendered image together when changing a
diagram. Moving an unchanged source/image pair does not require regeneration.
