package de.unhandledexceptions.codersclash.bot.util;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class Reactions {

    // weitere Emotes hier hinzufügen
    public static final String YES_EMOTE = "\u2705";
    public static final String NO_EMOTE = "\u274C";

    public static final Consumer<Void> NOTHING = aVoid -> {};

    public static void newYesNoMenu(Message message, User user, Consumer<Void> yes, Consumer<Void> no) {
        message.addReaction(YES_EMOTE).queue();
        message.addReaction(NO_EMOTE).queue();
        user.getJDA().addEventListener(new ReactionListener(user.getIdLong(), message.getIdLong(), Map.of(YES_EMOTE, yes, NO_EMOTE, no)));
    }

    public static void newMenu(Message message, User user, Map<String, Consumer<Void>> forReaction) {
        message.addReaction(NO_EMOTE).queue();
        forReaction.keySet().forEach((emoji) -> message.addReaction(emoji).queue());
        user.getJDA().addEventListener(new ReactionListener(user.getIdLong(), message.getIdLong(), forReaction));
    }

    private static class ReactionListener extends ListenerAdapter {

        private final long authorId;
        private final long messageId;
        private final Map<String, Consumer<Void>> forReaction;

        public ReactionListener(long authorId, long messageId, Map<String, Consumer<Void>> forReaction) {
            this.authorId = authorId;
            this.messageId = messageId;
            this.forReaction = forReaction;
        }

        @Override
        public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
            var user = event.getUser();
            if (user == event.getJDA().getSelfUser() || event.getMessageIdLong() != messageId)
                return;

            if (user.getIdLong() == authorId) {
                String name = event.getReactionEmote().getName();
                event.getReaction().removeReaction(user).queue();
                if (name.equals(NO_EMOTE)) {
                    event.getJDA().removeEventListener(this);
                    event.getChannel().getMessageById(event.getMessageIdLong()).queue((msg) -> msg.delete().queue());
                    return;
                }
                if (forReaction.containsKey(name)) {
                    forReaction.get(name).accept(null);
                    event.getChannel().getMessageById(messageId).queueAfter(3, TimeUnit.SECONDS, null, (throwable) -> event.getJDA().removeEventListener(this));
                }
            } else {
                event.getReaction().removeReaction(user).queue();
            }
        }
    }

}
