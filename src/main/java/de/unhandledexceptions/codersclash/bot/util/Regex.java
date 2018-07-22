package de.unhandledexceptions.codersclash.bot.util;

/**
 * @author Johnny_JayJay
 */

public class Regex {

    public static final String ONE_TO_TEN = "(10)|[1-9]";

    public static final String MEMBER_MENTION = "<@!?\\d+>";
    public static final String CHANNEL_MENTION = "<#\\d+>";


    public static boolean argsMatch(String[] actual, String... matches) {
        return String.join(" ", actual).matches(String.join(" ", matches));
    }

}
