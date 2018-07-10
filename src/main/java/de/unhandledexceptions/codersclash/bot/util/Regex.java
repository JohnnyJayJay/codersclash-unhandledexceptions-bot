package de.unhandledexceptions.codersclash.bot.util;

import net.dv8tion.jda.core.entities.Message;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class Regex {

    public static final String ONE_TO_TEN = "(10)|[1-9]";

    public static final String MEMBER_MENTION = "<@!?\\d+>";
    public static final String USER_MENTION = "<@\\d+>";
    public static final String CHANNEL_MENTION = "<#\\d+>";
    public static final String ROLE_MENTION = "<&\\d+>";


    public static boolean argsMatch(String[] actual, String... matches) {
        return String.join(" ", actual).matches(String.join(" ", matches));
    }

    public static boolean argsMatch(String[] actual, int mentionedMembers, Message message, String... matches) {
        return String.join(" ", actual).matches(String.join(" ", matches)) && message.getMentionedMembers().size() == mentionedMembers;
    }
}
