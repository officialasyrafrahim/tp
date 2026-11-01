package seedu.unienable.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.unienable.command.activity.DeleteCommand;
import seedu.unienable.command.activity.ListCommand;
import seedu.unienable.command.activity.ListDemandCommand;
import seedu.unienable.command.activity.ListPriorityCommand;
//@@author lukelouyu
import seedu.unienable.command.common.Command;
//@@author
import seedu.unienable.exception.ParseException;
import seedu.unienable.model.ActivityList;
import seedu.unienable.storage.Storage;

//@@author officialasyrafrahim
/**
 * Checks list, list demand, and delete grammar through the real Parser:
 * accepted variants construct the right command; rejected inputs produce one
 * [WARNING] message containing usage.
 */
class ListDeleteCommandParserTest {
    private final Parser parser = new Parser();
    private final Storage unusedByListCommands = new Storage();

    @Test
    void parse_list_buildsListCommandAndExecutesReadOnly() throws Exception {
        Command command = parser.parse("list");
        assertInstanceOf(ListCommand.class, command);
        assertTrue(command.execute(new ActivityList(), unusedByListCommands).getMessage()
                .endsWith("Sorry, the activity list is empty."));
    }

    @Test
    void parse_listIsCaseAndWhitespaceTolerant() throws Exception {
        assertInstanceOf(ListCommand.class, parser.parse("  LIST  "));
        assertInstanceOf(ListDemandCommand.class, parser.parse("list demand"));
        assertInstanceOf(ListDemandCommand.class, parser.parse("List DEMAND"));
        assertInstanceOf(ListPriorityCommand.class, parser.parse("list priority"));
        assertInstanceOf(ListPriorityCommand.class, parser.parse("LIST PRIORITY"));
    }

    @Test
    void parse_listRejectsUnknownVariantAndExtraArguments() {
        ParseException unknown = assertThrows(ParseException.class, () -> parser.parse("list foo"));
        assertTrue(unknown.getMessage().startsWith("[WARNING]"), "warnings must carry the [WARNING] prefix");
        assertTrue(unknown.getMessage().contains("Usage: list"), "warnings must show usage");
        assertThrows(ParseException.class, () -> parser.parse("list demand extra"));
        assertThrows(ParseException.class, () -> parser.parse("list priority extra"));
    }

    @Test
    void parse_delete_buildsDeleteCommandWithGivenIndex() throws Exception {
        assertInstanceOf(DeleteCommand.class, parser.parse("delete 2"));
        // "delete 0" is syntactically fine: range checking is the model's job (decided Q4).
        assertInstanceOf(DeleteCommand.class, parser.parse("delete 0"));
    }

    @Test
    void parse_deleteRejectsMissingExtraAndNonIntegerIndexes() {
        assertThrows(ParseException.class, () -> parser.parse("delete"));
        ParseException extra = assertThrows(ParseException.class, () -> parser.parse("delete 1 extra"));
        assertTrue(extra.getMessage().contains("exactly one index"));
        ParseException notInteger = assertThrows(ParseException.class, () -> parser.parse("delete abc"));
        assertTrue(notInteger.getMessage().startsWith("[WARNING]"));
        assertThrows(ParseException.class, () -> parser.parse("delete 2.5"));
        // Integer.parseInt alone would accept "+3"; the grammar does not.
        assertThrows(ParseException.class, () -> parser.parse("delete +3"));
    }

    @Test
    void parse_deleteRejectsIndexesTooLargeForInt() {
        assertThrows(ParseException.class, () -> parser.parse("delete 99999999999999"));
    }
}
//@@author
