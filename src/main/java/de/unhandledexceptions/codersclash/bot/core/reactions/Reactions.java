package de.unhandledexceptions.codersclash.bot.core.reactions;

import de.unhandledexceptions.codersclash.bot.core.Main;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Johnny_JayJay
 */

public class Reactions {
    // weitere Emotes hier hinzufügen
    public static final String YES_EMOTE = "\u2705";
    public static final String NO_EMOTE = "\u274C";
    public static final String EXCLAMATION_MARK = "❗";
    public static final String QUESTION_MARK = "❓";
    public static final String SPEECH_BUBBLE = "\uD83D\uDCAC";
    public static final String FLOPPY = "\uD83D\uDCBE";
    public static final String BACK = "↪";
    public static final String SHINY_STAR = "\uD83C\uDF1F";
    public static final String STAR = "⭐";
    public static final String BLACK_STAR = "✴";
    public static final String M = "\uD83C\uDDF2";
    public static final String Y = "\uD83C\uDDFE";
    public static final String N = "\uD83C\uDDF3";
    public static final String P = "\uD83C\uDDF5";
    public static final String MAIL = "\uD83D\uDCE9";
    public static final String REPEAT = "\uD83D\uDD01";
    public static final String CONTROLLER = "\uD83C\uDFAE";
    public static final String NEW = "\uD83C\uDD95";
    public static final String CLOSED_INBOX = "\uD83D\uDCEA";
    public static final String BOT = "\uD83E\uDD16";
    public static final String SATTELITE = "\uD83D\uDCE1";
    public static final String ARROW_UP = "\uD83D\uDD3C";
    public static final String ARROW_DOWN = "\uD83D\uDD3D";
    public static final String DOUBLE_ARROW_DOWN = "⏬";
    public static final String DOUBLE_ARROW_UP = "⏫";
    public static final String ARROW_LEFT = "⬅";
    public static final String ARROW_RIGHT = "➡";
    public static final String CLIPBOARD = "\uD83D\uDCCB";
    public static final String PENCIL = "✏";
    public static final String DAY = "\uD83D\uDCC5";
    public static final String HOUR = "\uD83D\uDD5B";
    public static final String MINUTE = "\u231A";
    public static final String NEWSPAPER = "\uD83D\uDCF0";

    public static String getNumber(int number) {
        String ret = " ";
        if (number == 10) {
            ret = "\uD83D\uDD1F";
        } else if (number < 10) {
            ret = number + "\u20E3";
        }
        return ret;
    }

    public static void newYesNoMenu(User user, Message message, Consumer<Message> yes, Consumer<Message> no) {
        message.addReaction(YES_EMOTE).queue();
        message.addReaction(NO_EMOTE).queue();
        user.getJDA().addEventListener(new ReactionListener(Set.of(YES_EMOTE, NO_EMOTE), (emoji) -> {
            if (emoji.equals(YES_EMOTE))
                yes.accept(message);
            else {
                message.delete().queue();
                no.accept(message);
            }
        }, user.getIdLong(), message.getIdLong(), 20, true));
    }

    public static void newYesNoMenu(User user, Message message, Consumer<Message> yes) {
        newYesNoMenu(user, message, yes, (s) -> {});
    }

    public static void newYesNoMenu(User user, TextChannel channel, String question, Consumer<Message> yes, Consumer<Message> no) {
        Messages.sendMessage(channel, Messages.Type.QUESTION, question).queue((msg) -> newYesNoMenu(user, msg, yes, no));
    }

    public static void newYesNoMenu(User user, TextChannel channel, String question, Consumer<Message> yes) {
        Messages.sendMessage(channel, Messages.Type.QUESTION, question).queue((msg) -> newYesNoMenu(user, msg, yes));
    }

    public static void newMenu(User user, Message message, Consumer<String> reacted, Collection<String> emojis, int waitSeconds, boolean removeListener) {
        user.getJDA().addEventListener(new ReactionListener(emojis, reacted, user.getIdLong(), message.getIdLong(), waitSeconds, removeListener));
    }

    public static void newMenu(User user, Message message, Consumer<String> reacted, Collection<String> emojis, int waitSeconds) {
        newMenu(user, message, reacted, emojis, waitSeconds, true);
    }

    public static void newMenu(User user, Message message, Consumer<String> reacted, Collection<String> emojis, boolean removeListener) {
        newMenu(user, message, reacted, emojis, 30, removeListener);
    }

    public static void newMenu(User user, Message message, Consumer<String> reacted, Collection<String> emojis) {
        newMenu(user, message, reacted, emojis, 30, true);
    }

    public static void newMessageWaiter(User user, MessageChannel channel, int waitSeconds, Predicate<Message> condition, Consumer<Message> messageReceived, Consumer<Void> afterExpiration) {
        var listener = new MessageListener(user.getJDA(), user.getIdLong(), channel.getIdLong(), waitSeconds, messageReceived, afterExpiration);
        listener.setCondition(condition);
        user.getJDA().addEventListener(listener);
    }

    public static void newMessageWaiter(User user, MessageChannel channel, int waitSeconds, Consumer<Message> messageReceived, Consumer<Void> afterExpiration) {
        newMessageWaiter(user, channel, waitSeconds, (s) -> true, messageReceived, afterExpiration);
    }

    public static void newMessageWaiter(User user, MessageChannel channel, int waitSeconds, Consumer<Message> messageReceived) {
        newMessageWaiter(user, channel, waitSeconds, (s) -> true, messageReceived, (v) -> {});
    }

    private static class MessageListener extends ListenerAdapter {
        private static final long NO_CHANNEL = -1;
        private static final long NO_GUILD = -2;

        // TODO mit einem Consumer für mehrere Nachrichten (andThen)
        //private final int howMany;
        private final long userId;
        private final long channelId;
        private final Consumer<Message> messageReceived;
        private Predicate<Message> condition;
        private boolean receivedMessage;

        public MessageListener(JDA jda, long userId, long channelId, int waitSeconds, Consumer<Message> messageReceived, Consumer<Void> afterExpiration) {
            Main.scheduleTask(() -> {
                if (!receivedMessage) {
                    jda.removeEventListener(this);
                    afterExpiration.accept(null);
                }
            }, waitSeconds, TimeUnit.SECONDS);
            this.userId = userId;
            this.channelId = channelId;
            this.messageReceived = messageReceived;
            this.receivedMessage = false;
            this.condition = (s) -> true;
        }

        public void setCondition(Predicate<Message> condition) {
            this.condition = condition;
        }

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().getIdLong() != userId || channelId != event.getChannel().getIdLong())
                return;

            Message message = event.getMessage();
            if (condition.test(message)) {
                event.getJDA().removeEventListener(this);
                this.receivedMessage = true;
                messageReceived.accept(message);
            }
        }
    }

    private static class ReactionListener extends ListenerAdapter {

        private Collection<String> emojis;
        private Consumer<String> reacted;
        private long userId;
        private long messageId;
        private boolean firstReaction;
        private boolean removeListener;
        private int waitSeconds;

        ReactionListener(Collection<String> emojis, Consumer<String> reacted, long userId, long messageId, int waitSeconds, boolean removeListener) {
            this.emojis = emojis;
            this.reacted = reacted;
            this.userId = userId;
            this.messageId = messageId;
            this.waitSeconds = waitSeconds;
            this.removeListener = removeListener;
            this.firstReaction = true;
        }

        @Override
        public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
            if (firstReaction) {
                event.getChannel().getMessageById(event.getMessageIdLong()).queueAfter(waitSeconds, TimeUnit.SECONDS,
                        (msg) -> event.getJDA().removeEventListener(this), (t) -> event.getJDA().removeEventListener(this));
                firstReaction = false;
            }

            User user = event.getUser();

            if (user == event.getJDA().getSelfUser() || event.getMessageIdLong() != messageId)
                return;

            event.getReaction().removeReaction(user).queue();

            String emoji = event.getReactionEmote().getName();
            if (user.getIdLong() == userId) {
                if (emoji.equals(NO_EMOTE)) {
                    event.getJDA().removeEventListener(this);
                }
                if (emojis.contains(emoji)) {
                    reacted.accept(emoji);
                    if (removeListener)
                        event.getJDA().removeEventListener(this);
                }
            }
        }
    }
}