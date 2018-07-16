package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Main;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.entities.Vote;
import de.unhandledexceptions.codersclash.bot.entities.VoteAnswer;
import de.unhandledexceptions.codersclash.bot.entities.VoteCreator;
import de.unhandledexceptions.codersclash.bot.entities.VoteState;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class VoteCommand extends ListenerAdapter implements ICommand {

    private Map<Guild, Vote> votes;
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
            System.out.println("trigger");
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
                sendMessage(channel, Type.ERROR, "On this guild is already an vote running or in setup!").queue();
                return;
            }
        }

        sendMessage(channel, Type.SUCCESS, "Okay great! Let's create your vote!").queue();
        sendMessage(channel, Type.INFO, "Your vote can not go longer then 1 week! \nYou can only submit up to 10 answer possibilities.").queue();


        Vote vote = new Vote(guild, event.getChannel());
        VoteCreator creator = new VoteCreator(member, guild, vote, VoteState.TIME);
        vote.setVoteCreator(creator);
        
        votes.put(guild, vote);

        sendTimeMessage(vote, event);
        vote.getVoteCreator().setState(VoteState.REACTON);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {

        if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_WRITE))
        {
            return;
        }

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

            long time = Long.parseLong(event.getMessage().getContentRaw());

            if (convertTime(vote.getTimeUnit(), time) > MAX_TIME)
            {
                sendMessage(channel, Type.ERROR, "Votes can't go longer then 1 week!").queue();
                return;
            }

            vote.setTime(time);
            vote.getVoteCreator().setState(VoteState.CHANNEL);
            sendChannelMessage(event);
            return;
        }

        if (creator.getState() == VoteState.CHANNEL)
        {

            if (event.getMessage().getMentionedChannels().isEmpty())
            {
                sendMessage(channel, Messages.Type.ERROR, "You need to mention a channel! (#channel)!").queue();
                return;
            }

            TextChannel targetChannel = event.getMessage().getMentionedChannels().get(0);

            if(!guild.getSelfMember().hasPermission(targetChannel, Permission.MESSAGE_WRITE)){
                sendMessage(channel, Type.ERROR, "I don't have permissions to write in this channel!").queue();
                return;
            }

            vote.setTargetChannel(targetChannel);
            sendMessage(channel, Messages.Type.SUCCESS, String.format("Successfully set <#%s> as channel!", targetChannel.getId())).queue();

            sendPossibilitieMessage(event);
            vote.getVoteCreator().setState(VoteState.POSSIBILITIES);
            return;
        }

        if (creator.getState() == VoteState.POSSIBILITIES)
        {

            if (!event.getMessage().getMentionedMembers().isEmpty())
            {
                if (event.getMessage().getMentionedMembers().get(0).getAsMention().equals(event.getGuild().getSelfMember().getAsMention()))
                {
                    vote.getVoteCreator().setState(VoteState.FINISHED);
                    sendFinishMessage(event);
                    finish(vote);
                    return;
                }
            }

            System.out.println(vote.getVoteAnswers().size());
            if (vote.getVoteAnswers().size() > 10)
            {
                vote.getVoteCreator().setState(VoteState.FINISHED);
                sendFinishMessage(event);
                finish(vote);
                return;
            }

            if (!vote.getVoteAnswers().add(event.getMessage().getContentDisplay()))
            {
                sendMessage(channel, Type.ERROR, "You already submit this possibility.").queue();
            }
            channel.sendMessage("next answer!").queue();

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

    private void sendPossibilitieMessage(GuildMessageReceivedEvent event)
    {
        event.getChannel().sendMessage("possibilities").queue();
    }

    private void sendTimeMessage(Vote vote, GuildMessageReceivedEvent event)
    {
        final String message =
                "\u23F2 When should the vote end?\n\n" +
                        emojis[0] + " Day\n" +
                        emojis[1] + " Hour\n" +
                        emojis[2] + " Minute";


        sendMessage(event.getChannel(), Type.QUESTION, message).queue(msg -> {
            Arrays.asList(emojis).forEach(emoji -> msg.addReaction(emoji).queue());
            Reactions.newMenu(event.getMember().getUser(), msg, reaction -> {

                msg.delete().queue();

                vote.getVoteCreator().setState(VoteState.TIME);

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

    private void sendChannelMessage(GuildMessageReceivedEvent event)
    {
        event.getChannel().sendMessage("channel").queue();
    }

    private void sendFinishMessage(GuildMessageReceivedEvent event)
    {
        event.getChannel().sendMessage("finish").queue();
    }

    private void finish(Vote vote)
    {

        var targetChannel = vote.getTargetChannel();
        var embedBuilder = new EmbedBuilder();

        embedBuilder.setColor(vote.getGuild().getSelfMember().getColor());
        embedBuilder.setTitle(Reactions.NEWSPAPER + " New vote!");
        embedBuilder.setAuthor(vote.getVoteCreator().getMember().getEffectiveName(), null, vote.getVoteCreator().getMember().getUser().getEffectiveAvatarUrl());

        var stringBuilder = new StringBuilder();

        System.out.println(vote.getVoteAnswers().size());
        for (int i = 0; i < vote.getVoteAnswers().size(); i++)
        {
            stringBuilder.append(Reactions.getNumber(i)).append(" \t").append(vote.getVoteAnswers().toArray()[i]).append("\n");
            vote.getEmotes().add(Reactions.getNumber(i));
        }

        embedBuilder.setDescription(stringBuilder.toString());

        targetChannel.sendMessage(embedBuilder.build()).queue(message -> {
            vote.setMessageID(message.getIdLong());
            for (String emote : vote.getEmotes())
            {
                message.addReaction(emote).queue();
            }
        });

        Runnable r = () -> voteCompleted(vote);

        Main.scheduleTask(r, vote.getTime(), TimeUnit.MILLISECONDS);
    }

    public void voteCompleted(Vote vote)
    {



    }

}
