package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import net.dv8tion.jda.core.entities.Guild;
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

    @Override
    public String info(Member member) {
        return null;
    }
}