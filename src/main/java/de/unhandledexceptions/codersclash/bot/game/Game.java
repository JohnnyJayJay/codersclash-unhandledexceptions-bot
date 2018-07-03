package de.unhandledexceptions.codersclash.bot.game;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.*;


public abstract class Game {

    protected long guildId;
    // channel, in dem das Spiel stattfindet.
    protected long channelId;
    // Die Message, "auf" der das Spiel gespielt wird
    protected long messageId;
    // gibt an, ob das Spiel läuft
    protected boolean inGame;
    // ein Set, in das alle Spieler kommen, die mitspielen
    protected Set<Member> players;

    public Game(TextChannel channel, Guild guild, Message message) {
        this.channelId = channel.getIdLong();
        this.guildId = guild.getIdLong();
        this.messageId = message.getIdLong();
        this.players = new HashSet<>();
    }

    // Was passieren soll, wenn reacted wird und das Spiel noch nicht gestartet wurde
    protected abstract void idle(GuildMessageReactionAddEvent event);
    // Was passieren soll, wenn reacted wird und das Spiel gestartet wurde (bei Aktivierung ist klar, dass derjenige mitspielt und am Zug ist)
    protected abstract void inGame(GuildMessageReactionAddEvent event);
    // Was passieren soll, wenn das Spiel beendet wurde. Muss selbst aufgerufen werden.
    public abstract void reset();


    /**
     * Wird bei Reaction aktiviert und prüft, ob es an idle() oder inGame() weitergegeben werden soll
     * Prüft automatisch, ob der ReactionAdder kein Bot ist und dass auf gui reagiert wird.
     * Wenn das Spiel läuft, wird außerdem gesprüft, ob der ReactionAdder mitspielt und am Zug ist.
     * */
    public final void onGameEvent(GuildMessageReactionAddEvent event) {
        if (inGame) {
            if (this.players.contains(event.getMember())) {
                this.inGame(event);
            } else {
                event.getReaction().removeReaction(event.getUser()).queue();
            }
        } else {
            this.idle(event);
        }
    }

    public final long getMessageID() {
        return messageId;
    }
}
