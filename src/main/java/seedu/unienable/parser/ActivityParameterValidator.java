//@@author lukelouyu
package seedu.unienable.parser;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.unienable.exception.ParseException;

/**
 * Requires every slash token to be a supported activity field and suggests likely spelling corrections.
 */
final class ActivityParameterValidator {
    private static final Pattern PARAMETER = Pattern.compile("([^\\s/]*)/");
    private static final List<String> PREFIXES = List.of("n", "d", "s", "e", "dem", "imp", "urg", "com", "overlap");
    private static final List<String> UNSUPPORTED_PREFIXES = List.of("urgency", "importance", "commitment", "priority");

    /**
     * Prevents instantiation of this shared parser helper.
     */
    private ActivityParameterValidator() {
    }

    /**
     * Checks every slash, including nested slash text inside a name. Supported prefixes
     * must start at a whitespace boundary. Literal slashes and escaping are unsupported.
     *
     * @param arguments unparsed activity fields.
     * @throws ParseException when a token is likely a misspelled parameter.
     */
    static void validate(String arguments) throws ParseException {
        Matcher matcher = PARAMETER.matcher(arguments);
        while (matcher.find()) {
            String token = matcher.group(1).toLowerCase(Locale.ROOT);
            if (PREFIXES.contains(token)) {
                if (matcher.start() > 0 && !Character.isWhitespace(arguments.charAt(matcher.start() - 1))) {
                    throw new ParseException("[WARNING] Parameter '" + token + "/' must be preceded by whitespace.");
                }
                continue;
            }
            if (UNSUPPORTED_PREFIXES.contains(token)) {
                String replacement = switch (token) {
                case "urgency" -> "urg";
                case "importance" -> "imp";
                case "commitment" -> "com";
                default -> null;
                };
                String suggestion = replacement == null ? "" : " Did you mean '" + replacement + "/'?";
                throw new ParseException("[WARNING] Unknown parameter '" + token + "/'." + suggestion);
            }
            for (String prefix : PREFIXES) {
                if (prefix.length() > 1 && token.length() > 1 && isOneEditAway(token, prefix)) {
                    throw new ParseException("[WARNING] Unknown parameter '" + token
                            + "/'. Did you mean '" + prefix + "/'?");
                }
            }
            throw new ParseException("[WARNING] Unknown parameter '" + token + "/'. "
                    + "Valid prefixes: n/, d/, s/, e/, dem/, imp/, urg/, com/, overlap/. "
                    + "Use spaces or hyphens instead of literal slashes in names.");
        }
    }

    /**
     * Parses the explicit, per-command permission to keep an overlapping activity.
     */
    static boolean allowsOverlap(String value) throws ParseException {
        if (value == null) {
            return false;
        }
        if (!value.equalsIgnoreCase("ALLOW")) {
            throw new ParseException("[WARNING] The overlap/ parameter must be ALLOW.");
        }
        return true;
    }

    /**
     * Returns whether a token is one edit from a prefix, counting an adjacent swap as one edit.
     */
    private static boolean isOneEditAway(String token, String prefix) {
        if (Math.abs(token.length() - prefix.length()) > 1) {
            return false;
        }
        int[][] distance = new int[token.length() + 1][prefix.length() + 1];
        for (int i = 0; i <= token.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= prefix.length(); j++) {
            distance[0][j] = j;
        }
        for (int i = 1; i <= token.length(); i++) {
            for (int j = 1; j <= prefix.length(); j++) {
                int substitution = token.charAt(i - 1) == prefix.charAt(j - 1) ? 0 : 1;
                distance[i][j] = Math.min(distance[i - 1][j] + 1,
                        Math.min(distance[i][j - 1] + 1, distance[i - 1][j - 1] + substitution));
                if (i > 1 && j > 1 && token.charAt(i - 1) == prefix.charAt(j - 2)
                        && token.charAt(i - 2) == prefix.charAt(j - 1)) {
                    distance[i][j] = Math.min(distance[i][j], distance[i - 2][j - 2] + 1);
                }
            }
        }
        return distance[token.length()][prefix.length()] == 1;
    }
}
//@@author
