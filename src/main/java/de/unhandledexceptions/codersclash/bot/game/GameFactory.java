package de.unhandledexceptions.codersclash.bot.game;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class GameFactory {

    public static boolean createSideGame(TextChannel channel) {
        return true;
    }

    public static class GameListener extends ListenerAdapter {
        private Set<Game> games;

        public GameListener() {
            games = new HashSet<>();
        }

        public void addGame(Game game) {
            games.add(game);
        }

        @Override
        public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
            if (games.stream().anyMatch((game) -> game.getMessageID() == event.getMessageIdLong())) {
                games.stream().filter((game) -> game.getMessageID() == event.getMessageIdLong()).forEach((game) -> game.onGameEvent(event));
            }
        }
    }
}
