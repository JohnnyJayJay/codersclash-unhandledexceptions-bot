package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.entities.Vote;
import de.unhandledexceptions.codersclash.bot.entities.VoteCreator;
import de.unhandledexceptions.codersclash.bot.entities.VoteState;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class VoteCommand extends ListenerAdapter implements ICommand {

    private Map<Guild, Vote> votes;
    private GuildMessageReceivedEvent event;
    private final String[] emojis = {Reactions.DAY, Reactions.HOUR, Reactions.MINUTE};
    private final long MAX_TIME = 604800000;

    public VoteCommand()
    {
        votes = new HashMap<>();
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args)
    {

        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
        {
            return;
        }

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

        sendTimeMessage(vote);
        vote.getVoteCreator().setState(VoteState.TIME);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {

        if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_WRITE))
        {
            return;
        }

        this.event = event;
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

        if (creator.getState() == VoteState.TIME)
        {

            if (!event.getMessage().getContentRaw().matches("\\d{1,6}"))
            {
                sendMessage(channel, Type.ERROR, "Just insert digits please or your number is too long. (Max is 6 digits)!").queue();
                return;
            }

            long time = Long.getLong(event.getMessage().getContentRaw());

            if (convertTime(vote.getTimeUnit(), time) > MAX_TIME)
            {
                sendMessage(channel, Type.ERROR, "Votes can't go longer then 1 week!").queue();
                return;
            }

            vote.setTime(time);
            vote.getVoteCreator().setState(VoteState.CHANNEL);
            return;
        }

        if (creator.getState() == VoteState.CHANNEL)
        {

            if (event.getMessage().getMentionedChannels().isEmpty())
            {
                sendMessage(channel, Messages.Type.ERROR, "You need to mention a channel! (#channel)").queue();
                return;
            }

            TextChannel targetChannel = event.getMessage().getMentionedChannels().get(0);

            if(guild.getSelfMember().hasPermission(targetChannel, Permission.MESSAGE_WRITE)){
                sendMessage(channel, Type.ERROR, "I don't have permissions to write in this channel!").queue();
                return;
            }

            vote.setTargetChannel(targetChannel);
            sendMessage(channel, Messages.Type.SUCCESS, String.format("Successfully set <#%s> as channel!", targetChannel.getId())).queue();

            sendPossibilitieMessage();
            vote.getVoteCreator().setState(VoteState.POSSIBILITIES);
            return;
        }

        if (creator.getState() == VoteState.POSSIBILITIES)
        {

            vote.getVoteAnswers().add(event.getMessage().getContentDisplay());
            if (vote.getVoteAnswers().size() <= 10)
            {
                sendMessage(channel, Type.SUCCESS, "You finished your vote setup!").queue();
            }
        }
    }

    private long convertTime(TimeUnit timeUnit, long time)
    {

        if (timeUnit == TimeUnit.MINUTES)
            return time * 60000;

        if (timeUnit == TimeUnit.HOURS)
            return time * 3600000;

        if (timeUnit == TimeUnit.DAYS)
            return time * 86400000;

        return 0;
    }

    private void sendPossibilitieMessage()
    {

    }

    private void sendTimeMessage(Vote vote)
    {
        final String message =
                "\u23F2 When should the vote end?\n\n" +
                        emojis[0] + " Day\n" +
                        emojis[1] + " Hour\n" +
                        emojis[2] + " Minute";


        sendMessage(event.getChannel(), Type.QUESTION, message).queue(msg -> {
            Reactions.newMenu(event.getMember().getUser(), msg, reaction -> {

                msg.delete().queue();

                switch (reaction)
                {
                    case Reactions.DAY:
                        vote.setTimeUnit(TimeUnit.DAYS);
                        break;

                    case Reactions.HOUR:
                        vote.setTimeUnit(TimeUnit.HOURS);
                        break;

                    case Reactions.MINUTE:
                        vote.setTimeUnit(TimeUnit.MINUTES);
                        break;
                }
            }, Arrays.asList(emojis));
        });
    }

    private void sendChannelMessage()
    {

    }

    private void sendFinishMessage()
    {

    }

}
