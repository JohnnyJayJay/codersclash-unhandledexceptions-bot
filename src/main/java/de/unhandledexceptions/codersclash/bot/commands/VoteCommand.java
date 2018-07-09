package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
<<<<<<< HEAD
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import net.dv8tion.jda.core.entities.Guild;
=======
>>>>>>> de67e4f073d9b30f3edcaafad808c46b858d5e7a
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class VoteCommand implements ICommand {

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {



    }

    private class VoteListener extends ListenerAdapter {
        @Override
        public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {

        }
    }

    // TODO
    @Override
    public String info(Member member) {
        return null;
    }
}
