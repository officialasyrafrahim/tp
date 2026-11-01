package seedu.unienable.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Owns console reading and message output without closing caller-owned streams.
 */
public class Ui {
    /**
     * Length of the response borders, matching the shared UI example.
     */
    private static final int HORIZONTAL_LINE_LENGTH = 60;
    private static final String HORIZONTAL_LINE = "_".repeat(HORIZONTAL_LINE_LENGTH);

    private final Scanner input;
    private final PrintStream output;

    /**
     * Creates a console UI using streams owned by the caller.
     *
     * @param input stream supplying console commands.
     * @param output stream receiving console messages.
     */
    public Ui(InputStream input, PrintStream output) {
        this.input = new Scanner(input);
        this.output = output;
    }

    /**
     * Checks whether another line is available, waiting for input when necessary.
     *
     * @return true if another command line can be read, or false at end of input.
     */
    public boolean hasNextLine() {
        return input.hasNextLine();
    }

    /**
     * Reads the next command line without its line separator.
     *
     * @return the next input line.
     * @throws java.util.NoSuchElementException if the input has ended.
     */
    public String readLine() {
        return input.nextLine();
    }

    /**
     * Returns UniEnable's ASCII wordmark.
     *
     * @return multiline logo without a trailing line break.
     */
    public static String getLogo() {
        return " _   _       _ _____                o      o   \n"
                + "| | | |_ __ (_) ____|_ __   __ _   /|__   /|    ___\n"
                + "| | | | '_ \\| |  _| | '_ \\ / _` | / '_ \\   |   / _ \\\n"
                + "| |_| | | | | | |___| | | | (_| || |_) |   |   |  __/\n"
                + " \\___/|_| |_|_|_____|_| |_|\\__,_| \\___/   / \\  \\___|";
    }

    /**
     * Shows the welcome message followed by the logo inside one pair of borders.
     */
    public void showWelcome() {
        showMessage("Welcome to UniEnable! Type bye to exit.\n" + getLogo());
    }

    /**
     * Displays one complete response between horizontal lines.
     * Multiline responses keep their contents together inside a single pair of borders.
     *
     * @param message complete response to display.
     */
    public void showMessage(String message) {
        output.println(HORIZONTAL_LINE);
        output.println(message);
        output.println(HORIZONTAL_LINE);
    }
}
