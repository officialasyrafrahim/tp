package seedu.unienable.command.common;

//@@author Atharva005
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import seedu.unienable.exception.ParseException;
import seedu.unienable.model.ActivityList;
import seedu.unienable.storage.Storage;

/**
 * Displays a categorized command menu or detailed help for one command without changing activities.
 */
public class HelpCommand extends Command {
    private static final String OVERVIEW = """
            UniEnable commands:
            Type help TOPIC for syntax, rules, and examples (for example, help add).

            ACTIVITY MANAGEMENT
              add                 Create an activity with optional demand and ratings
              edit INDEX          Update selected activity fields
              find KEYWORD        Search activity names
              list                List activities by date and start time
              list demand         List activities by demand, HIGH to LOW

            PLANNING AND PRIORITIES
              list priority       List activities by combined priority score
              view today          Show today's schedule, summary, and rating table
              view tomorrow       Show tomorrow's schedule, summary, and rating table
              view week           Show today and the following six days
              overdue             Show incomplete activities that have ended
              next                Show the earliest upcoming incomplete activity

            COMPLETION AND DELETION
              mark INDEX          Mark an activity as done
              unmark INDEX        Mark an activity as incomplete
              delete INDEX        Remove an activity

            FACILITIES AND ROUTES
              facility list       List recorded facilities
              facility LOCATION   Show one facility's accessibility information
              facility find       Search facilities by feature and status
              route               Find a shortest recorded accessible route

            HELP AND EXIT
              help [TOPIC]        Show this menu or detailed command help
              bye                 Exit UniEnable

            Run list to get the indexes for edit, mark, unmark, and delete.
            Activity changes are saved automatically to data/activities.txt.
            """.stripTrailing();

    private static final String EDIT_USAGE = "edit INDEX [n/NAME] [d/YYYY-MM-DD|today|tomorrow] [s/HH:mm] [e/HH:mm] "
            + "[dem/LOW|MEDIUM|HIGH] [imp/IMP] [urg/URG] [com/COM] [overlap/ALLOW]";

    /**
     * Keeps command instructions together so the parser does not duplicate help text.
     */
    private static final Map<String, String> TOPIC_HELP = Map.ofEntries(
            Map.entry("add", """
                    ADD - Create an activity

                    Usage:
                      add n/NAME d/YYYY-MM-DD|today|tomorrow s/HH:mm e/HH:mm [dem/LOW|MEDIUM|HIGH] \
                    [imp/IMP] [urg/URG] [com/COM] [overlap/ALLOW]

                    Details:
                      - n/, d/, s/, and e/ are required; prefixes may appear in any order.
                      - Names can contain spaces, but cannot be blank or contain | or line breaks.
                      - Names use printable ASCII only: English letters, digits, spaces, and punctuation.
                      - Commas and apostrophes are allowed; double quotes and backslashes are forbidden.
                      - Every / must follow a supported prefix at a field boundary; literal slashes are forbidden.
                      - Use a real YYYY-MM-DD date and 24-hour HH:mm times, such as 09:30.
                      - d/today and d/tomorrow use this computer's date and save as that real date.
                      - End must be after start on the same day and after now; ended activities cannot be added.
                      - Duplicate activities are rejected. Intentional overlaps require overlap/ALLOW.
                      - Back-to-back activities are allowed; completed activities still occupy their time.
                      - Demand defaults to MEDIUM; choose LOW, MEDIUM, or HIGH.
                      - IMP = importance; URG = urgency; COM = commitment.
                      - Ratings are optional whole numbers from 1 to 5; missing ratings display -.
                      - New activities start incomplete and save automatically.
                      - Ended activities can be added, but cannot subsequently be edited.

                    Examples:
                      add n/Study d/2030-12-01 s/10:00 e/11:00
                      add n/Study d/2030-12-01 s/10:00 e/11:00 dem/HIGH imp/5 urg/4 com/3
                      add n/Study d/tomorrow s/10:00 e/11:00
                    """.stripTrailing()),
            Map.entry("edit", """
                    EDIT - Update an existing activity

                    Usage:
                      %s

                    Details:
                      - Run list first and use the activity's current chronological index.
                      - Supply at least one field; omitted fields and completion are preserved.
                      - Names, dates, times, demand, and ratings follow the same rules as add.
                      - Replacement names use printable ASCII only; | and line breaks are rejected.
                      - Commas and apostrophes are allowed; double quotes and backslashes are forbidden.
                      - Every / must follow a supported prefix at a field boundary; literal slashes are forbidden.
                      - Date/time edits must leave an end after start on the same day and after now.
                      - Duplicate edits are rejected. Use overlap/ALLOW to confirm an intentional time clash.
                      - Activities ending at or before now cannot be edited, including name or rating changes.
                      - To change completion, use mark or unmark. Successful edits save automatically.

                    Examples:
                      edit 1 n/Project meeting dem/HIGH
                      edit 1 d/2030-12-02 s/14:00 e/15:00 imp/5 urg/4 com/3
                    """.formatted(EDIT_USAGE).stripTrailing()),
            Map.entry("find", """
                    FIND - Search activity names

                    Usage:
                      find KEYWORD

                    Details:
                      - Supply a nonblank word or phrase from the activity name.
                      - Matching ignores letter case and searches for a literal substring.
                      - Multiword keywords are literal phrases, including their internal spaces.
                      - Results keep indexes from the full chronological list.
                      - A search with no matches shows the normal no-activities message without saving.

                    Examples:
                      find study
                      find project meeting
                    """.stripTrailing()),
            Map.entry("list", """
                    LIST - Browse activities and their priorities

                    Usage:
                      list
                      list demand
                      list priority

                    Details:
                      - list shows date/start-time order; identical times keep insertion order.
                      - list demand groups HIGH, MEDIUM, then LOW and keeps chronological indexes.
                      - Run list before edit, mark, unmark, or delete to confirm the correct index.
                      - list priority sorts highest scores first and keeps chronological indexes.
                      - Priority score = URG x URG x (IMP + COM).
                      - Any missing rating makes priority unavailable (NA); those entries appear last.
                      - All three lists include completed activities and do not change saved data.

                    Examples:
                      list
                      list demand
                      list priority
                    """.stripTrailing()),
            Map.entry("view", """
                    VIEW - Display a daily or seven-day dashboard

                    Usage:
                      view today
                      view tomorrow
                      view week

                    Details:
                      - today and tomorrow select the corresponding full local calendar day.
                      - week includes today and the following six days, grouped by date.
                      - Dashboards include completed activities, summaries, and an aligned rating table.
                      - Rating tables fit 80 columns; long names are shortened to 26 columns with ...
                      - The summary counts HIGH-demand activities on the selected dates, including completed ones.
                      - IMP = importance (*), URG = urgency (!), COM = commitment (+).
                      - Priority score = URG x URG x (IMP + COM); N means a rating is missing.
                      - Missing ratings display -; demand L/M/H uses #/##/###.
                      - An empty selected period shows normal feedback without a summary or rating table.
                      - Empty days inside a populated weekly view show a plain no-activities message.
                      - Run list to get indexes before editing, marking, or deleting activities.

                    Examples:
                      view today
                      view tomorrow
                      view week
                    """.stripTrailing()),
            Map.entry("overdue", """
                    OVERDUE - Find incomplete activities that have ended

                    Usage:
                      overdue

                    Details:
                      - Shows incomplete activities with an end strictly before the current time.
                      - Excludes ongoing and completed activities, including activities ending exactly now.
                      - Results use chronological indexes and labels for today, this week, or earlier.
                      - Accepts no arguments and does not mark or modify any activity.

                    Examples:
                      overdue
                    """.stripTrailing()),
            Map.entry("next", """
                    NEXT - Find the earliest upcoming incomplete activity

                    Usage:
                      next

                    Details:
                      - Shows one incomplete activity whose start is strictly after the current time.
                      - Excludes completed activities and activities starting now or earlier.
                      - The displayed index comes from the full chronological list.
                      - If none qualifies, shows the normal no-activities message without a warning.
                      - Accepts no arguments.

                    Examples:
                      next
                    """.stripTrailing()),
            Map.entry("mark", """
                    MARK - Mark an activity as done

                    Usage:
                      mark INDEX

                    Details:
                      - Run list and use a current positive whole-number index, starting at 1.
                      - Changes only completion; activity details and ratings are preserved.
                      - A completion change saves automatically.
                      - Marking an already completed activity warns and does not save again.

                    Examples:
                      list
                      mark 1
                    """.stripTrailing()),
            Map.entry("unmark", """
                    UNMARK - Mark an activity as incomplete

                    Usage:
                      unmark INDEX

                    Details:
                      - Run list and use a current positive whole-number index, starting at 1.
                      - Clears completion while preserving activity details and ratings.
                      - A completion change saves automatically.
                      - Unmarking an already incomplete activity warns and does not save again.

                    Examples:
                      list
                      unmark 1
                    """.stripTrailing()),
            Map.entry("delete", """
                    DELETE - Remove an activity

                    Usage:
                      delete INDEX

                    Details:
                      - Run list and choose the current chronological index, starting at 1.
                      - list demand retains chronological indexes; run list to confirm the current index.
                      - Do not use numbering from the calendar dashboards.
                      - Removes that activity and saves the remaining list automatically.
                      - Remaining indexes can change; run list again before another indexed command.
                      - There is no undo command. A missing or invalid index leaves activities unchanged.

                    Examples:
                      list
                      delete 1
                    """.stripTrailing()),
            Map.entry("facility", """
                    FACILITY - Browse recorded accessibility information

                    Usage:
                      facility list
                      facility LOCATION
                      facility find type/FEATURE [status/YES|NO|UNKNOWN]

                    Details:
                      - list shows the available facilities; LOCATION is one facility ID or name.
                      - find requires type/FEATURE first; status defaults to YES.
                      - Features: LIFT, RAMP, SHELTERED_RAMP, ACCESSIBLE_WASHROOM,
                        STEP_FREE_ENTRANCE, REST_POINT, AUTOMATIC_DOOR, OTHER.
                      - YES, NO, and UNKNOWN refer to the recorded status of that feature.
                      - These queries are read-only and need loaded facility reference data.
                      - Verify recorded accessibility details against current campus information.

                    Examples:
                      facility list
                      facility AS4
                      facility find type/LIFT status/YES
                    """.stripTrailing()),
            Map.entry("route", """
                    ROUTE - Find a shortest recorded accessible route

                    Usage:
                      route from/START to/END

                    Details:
                      - Supply two different facility names (AS1-AS8 or CLB), with from/ before to/.
                      - Facility IDs F01-F09 are also accepted when facility data is available.
                      - Results show the route segments and total recorded distance in metres.
                      - Same endpoints, unknown facilities, or no confirmed accessible path produce warnings.
                      - Routes need loaded connection data and do not change activities.
                      - Verify route information against current campus conditions.

                    Examples:
                      route from/AS1 to/CLB
                      route from/AS6 to/AS8
                    """.stripTrailing()),
            Map.entry("help", """
                    HELP - Read the command menu or a detailed topic

                    Usage:
                      help
                      help TOPIC

                    Details:
                      - help displays commands grouped by purpose.
                      - Choose one command name as TOPIC; topic matching ignores letter case.
                      - help list includes demand and priority variants; help facility includes feature search.
                      - Help only displays instructions, including for topics such as delete or bye.
                      - Unknown topics or extra topic words show a warning.

                    Examples:
                      help add
                      help facility
                      help view
                    """.stripTrailing()),
            Map.entry("bye", """
                    BYE - Exit UniEnable

                    Usage:
                      bye

                    Details:
                      - Accepts no arguments and ends the current console session.
                      - Activity changes save when made; bye retries saving only if changes remain unsaved.
                      - A failed retry keeps the app running. Fix the file problem and enter bye again.
                      - Reading help bye displays these instructions and keeps the app running.

                    Examples:
                      bye
                    """.stripTrailing())
    );

    /** Activity topics whose commands can return normal empty-result feedback. */
    private static final Set<String> EMPTY_RESULT_TOPICS = Set.of("edit", "find", "list", "view",
            "overdue", "next", "mark", "unmark", "delete");

    private final String message;

    /**
     * Creates the categorized command menu shown by help without arguments.
     */
    public HelpCommand() {
        message = OVERVIEW;
    }

    /**
     * Creates detailed help for one command name, matched without regard to letter case.
     *
     * @param topic command name whose instructions should be displayed.
     * @throws ParseException if the topic is not a supported command name.
     */
    public HelpCommand(String topic) throws ParseException {
        String normalizedTopic = topic.toLowerCase(Locale.ROOT);
        String details = TOPIC_HELP.get(normalizedTopic);
        if (details == null) {
            throw new ParseException("[WARNING] Unknown help topic '" + topic
                    + "'.\nUsage: help [TOPIC]\nType help to see the available commands.");
        }
        String emptyMessage = switch (normalizedTopic) {
        case "view" -> "Sorry, no activities fall within the selected period.";
        case "find" -> "Sorry, no activity names match your search.";
        case "overdue" -> "Sorry, no incomplete activities have ended.";
        case "next" -> "Sorry, no upcoming incomplete activities exist.";
        default -> ActivityList.NO_ACTIVITIES_MESSAGE;
        };
        String emptyResultHelp = EMPTY_RESULT_TOPICS.contains(normalizedTopic)
                ? "\n\nIf no activity is available for this command, it displays this normal result:\n"
                        + emptyMessage
                : "";
        message = details + emptyResultHelp + "\n\nType help to return to the command menu.";
    }

    @Override
    public CommandResult execute(ActivityList activities, Storage storage) {
        return new CommandResult(message, false);
    }
}
//@@author
