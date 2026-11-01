# UniEnable

UniEnable is a Java 25 command-line application that helps university students, particularly those with ASD, ADHD, or mobility constraints, organize academic and daily commitments and check campus accessibility information. It combines dated activities, optional importance, urgency and commitment ratings, priority and calendar views, completion tracking, and automatic local saving with facility searches and shortest recorded accessible-route queries using sample data for nine NUS FASS campus hubs.

Read the [User Guide](https://ay2627s1-cs2113-t10-4.github.io/tp/UserGuide.html) for commands and examples, or the
[Developer Guide](https://ay2627s1-cs2113-t10-4.github.io/tp/DeveloperGuide.html) for design, requirements, diagrams,
and testing instructions. Accessibility results use bundled sample data.

## Setting up in Intellij

Prerequisites: JDK 25 (use the exact version), update Intellij to the most recent version.

1. **Ensure Intellij JDK 25 is defined as an SDK**, as described [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk) -- this step is not needed if you have used JDK 25 in a previous Intellij project.
1. **Import the project _as a Gradle project_**, as described [here](https://se-education.org/guides/tutorials/intellijImportGradleProject.html).
1. **Verify the setup**: After the importing is complete, locate the `src/main/java/seedu/unienable/UniEnable.java` file, right-click it, and choose `Run UniEnable.main()`. If the setup is correct, you should see something like the below:
   ```text
   Welcome to UniEnable! Type bye to exit.
   ```
   Type `bye` and press enter to exit.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

## Build automation using Gradle

* This project uses the included Gradle wrapper for build automation and dependency management.
* If you are new to Gradle, refer to the [Gradle Tutorial at se-education.org/guides](https://se-education.org/guides/tutorials/gradle.html).

Use JDK 25 for both Java and Gradle; confirm with `java -version` and the
wrapper's `--version` command. Run commands from the repository root:

| Purpose | Windows PowerShell | macOS / Linux |
| --- | --- | --- |
| Run the console | `.\gradlew.bat run --console=plain` | `./gradlew run --console=plain` |
| Run JUnit tests | `.\gradlew.bat test` | `./gradlew test` |
| Run all configured checks | `.\gradlew.bat check` | `./gradlew check` |
| Build the executable JAR | `.\gradlew.bat shadowJar` | `./gradlew shadowJar` |

The first build downloads Gradle and dependencies. Once built, launch with
`java -ea -jar build/libs/unienable.jar`. Use `help` for syntax and `bye` to exit.
Activity data is stored in `data/activities.txt` relative to the launch directory.
Use a separate directory for manual tests.

## Testing

* The [JUnit suite](src/test/java/seedu/unienable) covers parsers, commands, models,
  persistence/recovery, accessibility data and routing, formatting, and console integration.
* `check` runs JUnit, Checkstyle, and the configured JaCoCo coverage verification.
  Coverage thresholds apply to the accessibility classes listed in `build.gradle`.
* Test and coverage reports are written to `build/reports/tests/test/index.html`
  and `build/reports/jacoco/test/html/index.html`.
* The [manual testing instructions](docs/DeveloperGuide.md#instructions-for-manual-testing)
  cover activity commands, date-sensitive views, and restart/recovery scenarios.
* If you are new to JUnit, refer to the [JUnit Tutorial at se-education.org/guides](https://se-education.org/guides/tutorials/junit.html).

## Checkstyle

* Checkstyle enforces the project's Java style using `config/checkstyle/checkstyle.xml`.
* If you are new to Checkstyle, refer to the [Checkstyle Tutorial at se-education.org/guides](https://se-education.org/guides/tutorials/checkstyle.html).

## CI using GitHub Actions

The project uses [GitHub actions](https://github.com/features/actions) for CI. When you push a commit to this repo or PR against it, GitHub actions will run automatically to build and verify the code as updated by the commit/PR.

## Documentation

The [documentation home](docs/README.md) links the [User Guide](docs/UserGuide.md),
[Developer Guide](docs/DeveloperGuide.md), [About Us](docs/AboutUs.md), and
[project portfolios](docs/team/README.md). The [diagram index](docs/diagrams/README.md)
maps PlantUML sources to [rendered images](docs/images/README.md) and explains how to regenerate them.

Luke contributes the activity-creation class diagram, the name and parameter
validation class/sequence pairs, and the
[architecture diagram](docs/diagrams/luke/architecture/ArchitectureDiagram.puml).
Atharva contributes the add sequence, calendar dashboards, topic help, schedule
validation, and date parsing diagrams. See the diagram index for each source/image pair.

For documentation publication and link checks, see the
[publication notes](docs/README.md#publication-and-link-checks).

## Acknowledgements

This project adapts the [CS2113 team-project starter](https://github.com/NUS-CS2113-AY2627-S1/tp)
and its [SE-EDU Duke](https://github.com/se-edu/duke) foundation.
The [Developer Guide acknowledgements](docs/DeveloperGuide.md#acknowledgements)
describe the inherited infrastructure and the source of the campus reference data.
