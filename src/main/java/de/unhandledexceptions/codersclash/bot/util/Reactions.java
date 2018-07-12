package de.unhandledexceptions.codersclash.bot.util;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class Reactions {

    // weitere Emotes hier hinzufügen
    public static final String YES_EMOTE = "\u2705";
    public static final String NO_EMOTE = "\u274C";
    public static final String EXCLAMATION = "❗";
    public static final String QUESTION_MARK = "❓";
    public static final String BACK_EMOTE = "↪";
    public static final String SHINY_STAR = "\uD83C\uDF1F";
    public static final String STAR = "⭐";
    public static final String BLACK_STAR = "✴";
    public static final String M = "\uD83C\uDDF2";
    public static final String Y = "\uD83C\uDDFE";
    public static final String N = "\uD83C\uDDF3";
    public static final String P = "\uD83C\uDDF5";

    public static final Consumer<Message> DO_NOTHING = msg -> {};

    public static final Consumer<Message> DELETE_MESSAGE = msg -> msg.delete().queue();

    public static String getNumber(int number) {
        String ret = " ";
        if (number == 10) {
            ret = "\uD83D\uDD1F";
        } else if (number < 10) {
            ret = number + "\u20E3";
        }
        return ret;
    }

    public static void newYesNoMenu(Message message, User user, Consumer<Message> yes, Consumer<Message> no) {
        message.addReaction(YES_EMOTE).queue();
        message.addReaction(NO_EMOTE).queue();
        user.getJDA().addEventListener(new ReactionListener(user.getIdLong(), message.getIdLong(), Map.of(YES_EMOTE, yes, NO_EMOTE, no)));
    }

    public static void newYesNoMenu(Message message, User user, Consumer<Message> yes) {
        newYesNoMenu(message, user, yes, DO_NOTHING);
    }

    public static void newYesNoMenu(String question, TextChannel channel, User user, Consumer<Message> yes) {
        Messages.sendMessage(channel, Messages.Type.QUESTION, question).queue((msg) -> newYesNoMenu(msg, user, yes, DO_NOTHING));
    }

    public static void newYesNoMenu(String question, TextChannel channel, User user, Consumer<Message> yes, Consumer<Message> no) {
        Messages.sendMessage(channel, Messages.Type.QUESTION, question).queue((msg) -> newYesNoMenu(msg, user, yes, no));
    }

    public static void newMenu(Message message, User user, Function<String, Consumer<Message>> forReaction, Set<String> emojis, boolean removeListener) {
        emojis.forEach((emoji) -> message.addReaction(emoji).queue());
        message.addReaction(NO_EMOTE).queue();
        ReactionListener listener = new ReactionListener(user.getIdLong(), message.getIdLong(), forReaction);
        if (removeListener)
            listener.setAndThen(List.of((msg) -> msg.getJDA().removeEventListener(listener)));
        user.getJDA().addEventListener(listener);
    }

    public static void newMenu(Message message, User user, Function<String, Consumer<Message>> forReaction, boolean removeListener) {
        newMenu(message, user, forReaction, Collections.EMPTY_SET, removeListener);
    }

    public static void newMenu(Message message, User user, Map<String, Consumer<Message>> forReaction) {
        forReaction.keySet().forEach((emoji) -> message.addReaction(emoji).queue());
        message.addReaction(NO_EMOTE).queue();
        user.getJDA().addEventListener(new ReactionListener(user.getIdLong(), message.getIdLong(), forReaction));
    }

    public static void newMessageWaiter(Member member, Consumer<Message> messageReceived, int waitSeconds) {
        member.getJDA().addEventListener(new MessageListener(member.getUser().getIdLong(), member.getGuild().getIdLong(), MessageListener.NO_CHANNEL, messageReceived, member.getJDA(), waitSeconds, v -> {}));
    }

    public static void newMessageWaiter(User user, MessageChannel channel, Consumer<Message> messageReceived, int waitSeconds, Consumer<Void> afterExpiration) {
        user.getJDA().addEventListener(new MessageListener(user.getIdLong(), MessageListener.NO_GUILD, channel.getIdLong(), messageReceived, user.getJDA(), waitSeconds, afterExpiration));
    }

    public static void newMessageWaiter(User user, Consumer<Message> messageReceived, int waitSeconds) {
        user.getJDA().addEventListener(new MessageListener(user.getIdLong(), MessageListener.NO_GUILD, MessageListener.NO_CHANNEL, messageReceived, user.getJDA(), waitSeconds, v -> {}));
    }

    private static class MessageListener extends ListenerAdapter {
        private static final long NO_CHANNEL = -1;
        private static final long NO_GUILD = -2;

        private static final Timer timer = new Timer();
        // TODO mit einem Consumer für mehrere Nachrichten (andThen)
        //private final int howMany;
        private final long userId;
        private final long guildId;
        private final long channelId;
        private final Consumer<Message> messageReceived;

        public MessageListener(long userId, long guildId, long channelId, Consumer<Message> messageReceived, JDA jda, int waitSeconds, Consumer<Void> afterExpiration) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    jda.removeEventListener(this);
                    afterExpiration.accept(null);
                    this.cancel();
                }
            }, waitSeconds * 1000);
            this.userId = userId;
            this.guildId = guildId;
            this.channelId = channelId;
            this.messageReceived = messageReceived;
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().getIdLong() != userId)
                return;

            if (channelId == NO_CHANNEL) {
                if (guildId == NO_GUILD) {
                    messageReceived.accept(event.getMessage());
                    event.getJDA().removeEventListener(this);
                } else if (event.getGuild() != null && event.getGuild().getIdLong() == guildId) {
                    messageReceived.accept(event.getMessage());
                    event.getJDA().removeEventListener(this);
                }
            } else if (event.getChannel().getIdLong() == channelId) {
                messageReceived.accept(event.getMessage());
                event.getJDA().removeEventListener(this);
            }
        }
    }

    private static class ReactionListener extends ListenerAdapter {

        private int reactions;
        private final int waitSeconds;
        private final boolean useFunction;
        private final long userId;
        private final long messageId;
        private final Function<String, Consumer<Message>> function;
        private final Map<String, Consumer<Message>> map;
        private Collection<Consumer<Message>> andThen;

        public ReactionListener(long authorId, long messageId, Map<String, Consumer<Message>> forReaction) {
            this(authorId, messageId, forReaction, null, 30);
        }

        public ReactionListener(long authorId, long messageId, Function<String, Consumer<Message>> forReaction) {
            this(authorId, messageId, null, forReaction, 30);
        }

        private ReactionListener(long authorId, long messageId, Map<String, Consumer<Message>> map, Function<String, Consumer<Message>> function, int waitSeconds) {
            this.reactions = 0;
            this.waitSeconds = waitSeconds;
            this.userId = authorId;
            this.messageId = messageId;
            this.map = map;
            this.function = function;
            this.useFunction = map == null;
        }

        public void setAndThen(Collection<Consumer<Message>> andThen) {
            this.andThen = andThen;
        }

        @Override
        public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
            if (reactions == 0) {
                event.getChannel().getMessageById(event.getMessageIdLong()).queueAfter(waitSeconds, TimeUnit.SECONDS,
                        (msg) -> event.getJDA().removeEventListener(this),
                        (throwable) -> event.getJDA().removeEventListener(this));
            }
            reactions++;
            var user = event.getUser();
            if (user == event.getJDA().getSelfUser() || event.getMessageIdLong() != messageId)
                return;

            if (user.getIdLong() == userId) {
                String name = event.getReactionEmote().getName();
                event.getReaction().removeReaction(user).queue();
                if (name.equals(NO_EMOTE)) {
                    event.getJDA().removeEventListener(this);
                    event.getChannel().getMessageById(event.getMessageIdLong()).queue((msg) -> msg.delete().queue());
                    return;
                }
                if (useFunction) {
                    event.getChannel().getMessageById(event.getMessageIdLong()).queue((msg) -> {
                        Consumer<Message> andThenCombined = m -> {};
                        for (Consumer<Message> c : andThen)
                            andThenCombined = andThenCombined.andThen(c);
                        function.apply(name).andThen(andThenCombined).accept(msg);
                    });
                } else if (map.containsKey(name)) {
                    Consumer<Message> andThenCombined = m -> {};
                    for (Consumer<Message> c : andThen)
                        andThenCombined = andThenCombined.andThen(c);
                    Consumer<Message> finalAndThenCombined = andThenCombined;
                    event.getChannel().getMessageById(messageId).queue((msg) -> map.get(name).andThen(finalAndThenCombined).accept(msg));
                }
            } else {
                event.getReaction().removeReaction(user).queue();
            }
        }
    }

}
