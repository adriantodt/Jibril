package pw.aru.utils;

import java.util.regex.Pattern;

public class BotUtils {
    private static final Pattern regexPattern = Pattern.compile("[\\-\\[\\]/{}()*+?.\\\\^$|]");

    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String escapeRegex(String input) {
        return regexPattern.matcher(input).replaceAll("\\$&");
    }
}