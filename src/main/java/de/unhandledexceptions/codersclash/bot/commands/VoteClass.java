package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.entities.Vote;
import de.unhandledexceptions.codersclash.bot.entities.VoteCreator;
import de.unhandledexceptions.codersclash.bot.entities.VoteState;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class VoteClass extends ListenerAdapter implements ICommand {

    private Map<Guild, Vote> votes;

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args)
    {

        Guild guild = event.getGuild();
        
        if (Permissions.getPermissionLevel(member) < Permissions.getVotePermissionLevel())
        {
            channel.sendMessage("Your permission level is too low. (" + Permissions.getPermissionLevel(member) + "/" + Permissions.getVotePermissionLevel() + ")").queue();
            return;
        }

        if (votes.containsKey(guild))
        {
            if (votes.get(guild).getVoteCreator().getState() != VoteState.DEFAULT)
            {
                sendMessage(channel, Type.ERROR, "On this guild is already an vote running or in vote setup!").queue();
                return;
            }
        }

        sendMessage(channel, Type.SUCCESS, "Okay great! Let's create your vote!").queue();

        Vote vote = new Vote(guild, event.getChannel());
        VoteCreator creator = new VoteCreator(member, guild, vote, VoteState.TIME);
        vote.setVoteCreator(creator);
        
        votes.put(guild, vote);

        final String[] emojis = {Reactions.DAY, Reactions.HOUR, Reactions.MINUTE};

        final String message =
                "\u23F2 When should the vote end?\n\n" +
                        emojis[0] + " Day\n" +
                        emojis[1] + " Hour\n" +
                        emojis[2] + " Minute";

        sendMessage(event.getChannel(), Type.QUESTION, message).queue(msg -> {
            Reactions.newMenu(member.getUser(), msg, reaction -> {
                    if (reaction.equals(emojis[0])){
                        msg.delete().queue();
                        timeSet(event, TimeUnit.DAYS, msg);
                    }

                    if (reaction.equals(emojis[1]))
                    {
                        msg.delete().queue();
                        timeSet(event, TimeUnit.HOURS, msg);
                    }

                    if (reaction.equals(emojis[2]))
                    {
                        msg.delete().queue();
                        timeSet(event, TimeUnit.MINUTES, msg);
                    }

                    }, Arrays.asList(emojis));
        });
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {

        Guild guild = event.getGuild();
        TextChannel channel = event.getChannel();

        if (!votes.containsKey(guild))
            return;

        Vote vote = votes.get(guild);

        if (vote.getSetupChannel() != event.getChannel())
            return;

        if (vote.getVoteCreator().getMember() != event.getMember())
            return;

        VoteCreator creator = vote.getVoteCreator();

        if (creator.getState() == VoteState.CHANNEL)
        {

            if (event.getMessage().getMentionedChannels().isEmpty())
            {
                sendMessage(channel, Type.ERROR, "You need to mention a channel! (#channel)").queue();
                return;
            }



        }

        if (creator.getState() == VoteState.TIME)
        {

        }

        if (creator.getState() == VoteState.POSSIBILITIES)
        {

        }
    }

    private int time(TimeUnit timeUnit, int time)
    {
        if (timeUnit == TimeUnit.MINUTES)
            return time * 60000;

        if (timeUnit == TimeUnit.HOURS)
            return time * 3600000;

        if (timeUnit == TimeUnit.DAYS)
            return time * 86400000;

        return 0;
    }

    private void timeSet(GuildMessageReceivedEvent event, TimeUnit timeUnit, Message msg)
    {
        msg.delete().queue();
        event.getChannel().sendMessage(timeUnit  + " " + time(timeUnit, 1)).queue();
    }

}
