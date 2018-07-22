package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Main;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.entities.*;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.jfree.chart.ChartUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class VoteCommand extends ListenerAdapter implements ICommand {

    private Map<Long, Vote> votes;
    private final String[] emojis = {Reactions.DAY, Reactions.HOUR, Reactions.MINUTE};
    private final long MAX_TIME = 604800000;
    private final ShardManager shardManager;
    private final int WIDTH = 640;
    private final int HEIGHT = 480;

    public VoteCommand(ShardManager shardManager)
    {
        this.shardManager = shardManager;
        votes = new HashMap<>();
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args)
    {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION))
            return;

        if (Permissions.getPermissionLevel(member) < 4) {
            noPermissionsMessage(channel, member);
            return;
        }

        if (args.length > 0) {
            if (args[0].equals("close")) {
                if (votes.containsKey(event.getGuild().getIdLong())) {
                    Vote vote = votes.get(event.getGuild().getIdLong());
                    if (vote.getVoteCreator().getMember() == member) {
                        if (vote.isRunning()) {
                            vote.getScheduledFuture().cancel(true);
                            sendMessage(channel, Type.SUCCESS, "Vote got closed!").queue();
                            if (vote.isRunning()) {
                                sendMessage(channel, Type.ERROR, "Uhm something went wrong!").queue();
                                return;
                            }
                            voteCompleted(vote);
                            return;
                        } else {
                            sendMessage(channel, Type.ERROR, "There is no active vote!").queue();
                            return;
                        }
                    } else {
                        sendMessage(channel, Type.ERROR, format("Only the creator %s can close this vote!", vote.getVoteCreator().getMember().getUser().getId()));
                        return;
                    }
                } else {
                    sendMessage(channel, Type.ERROR, "There is no active vote!").queue();
                    return;
                }
            }
        }

        Guild guild = event.getGuild();



        if (votes.containsKey(guild.getIdLong())) {
            if (votes.get(guild.getIdLong()).getVoteCreator().getState() != VoteState.DEFAULT)
            {
                sendMessage(channel, Type.ERROR, "There is already a vote in setup or one active!").queue();
                return;
            }
        }


        Main.otherThread(() -> {
            sendMessage(channel, Type.SUCCESS, "Okay great! Let's create your vote!").queue();
            sendStartInfoMessage(event);

            Vote vote = new Vote(guild, event.getChannel(), shardManager);
            VoteCreator creator = new VoteCreator(member, guild, vote, VoteState.TIME, shardManager);
            vote.setVoteCreator(creator);
            votes.put(guild.getIdLong(), vote);

            sendTimeReactionMessage(vote, event);
            vote.getVoteCreator().setState(VoteState.REACTION);
        });
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_WRITE))
            return;

        Guild guild = event.getGuild();
        TextChannel channel = event.getChannel();

        if (!votes.containsKey(guild.getIdLong()))
            return;

        Vote vote = votes.get(guild.getIdLong());

        if (vote.getSetupChannel() != event.getChannel())
            return;

        if (vote.getVoteCreator().getMember() != event.getMember())
            return;

        VoteCreator creator = vote.getVoteCreator();

        if (event.getMessage().getContentRaw().equals("cancel")) {
            votes.remove(vote.getGuildId());
            sendMessage(channel, Type.SUCCESS, "Successfully canceled your vote!").queue();
            return;
        }

        if (creator.getState() == VoteState.TIME) {
            if (!event.getMessage().getContentRaw().matches("\\d{1,6}")) {
                sendMessage(channel, Type.ERROR, "Just insert digits please or your number is too long. (Max is `6` digits)!").queue();
                return;
            }

            long time = Long.parseLong(event.getMessage().getContentRaw());

            if (convertTime(vote.getTimeUnit(), time) > MAX_TIME) {
                sendMessage(channel, Type.ERROR, "Votes can't go longer then `1` week!").queue();
                return;
            }

            vote.setTime(time);
            vote.getVoteCreator().setState(VoteState.CHANNEL);
            sendChannelMessage(event);
            return;
        }

        if (creator.getState() == VoteState.CHANNEL)
        {

            if (event.getMessage().getMentionedChannels().isEmpty()) {
                sendMessage(channel, Messages.Type.ERROR, "You need to mention a channel! Use #channel.").queue();
                return;
            }

            TextChannel targetChannel = event.getMessage().getMentionedChannels().get(0);

            if (!guild.getSelfMember().hasPermission(targetChannel, Permission.MESSAGE_WRITE)) {
                return;
            }

            vote.setTargetChannel(targetChannel);
            sendMessage(channel, Messages.Type.SUCCESS, format("Successfully set <#%s> as channel!", targetChannel.getId())).queue();

            sendTopicMessage(event);
            creator.setState(VoteState.TOPIC);
            return;
        }

        if (creator.getState() == VoteState.TOPIC) {
            vote.setTopic(event.getMessage().getContentRaw());
            vote.getVoteCreator().setState(VoteState.POSSIBILITIES);
            sendPossibilitiesMessage(event);
            return;
        }

        if (creator.getState() == VoteState.POSSIBILITIES) {
            if (event.getMessage().getContentRaw().equals("finished")) {
                if (vote.getVoteAnswers().size() < 2) {
                    sendMessage(channel, Type.ERROR, "You need to submit at least `2` answers").queue();
                    return;
                }

                vote.getVoteCreator().setState(VoteState.VOTES);
                sendVotesMessage(event);
                return;
            }


            if (vote.getVoteAnswers().size() == 9) {
                vote.getVoteCreator().setState(VoteState.VOTES);
                sendVotesMessage(event);
                return;
            }


            for (VoteAnswer answer : vote.getVoteAnswers()) {
                if (answer.getAnswer().equals(event.getMessage().getContentRaw()))
                {
                    sendMessage(channel, Type.ERROR, "You already submit this possibility. Send a new one or finish the setup by typing 'finished'.").queue();
                    return;
                }
            }

            vote.getVoteAnswers().add(new VoteAnswer(event.getMessage().getContentRaw(), vote.getVoteCreator(), vote, vote.getVoteAnswers().size() + 1));

            sendMessage(event.getChannel(), Type.SUCCESS, format("Received answer. Answer count: %s.", vote.getVoteAnswers().size())).queue();
        }

        if (creator.getState() == VoteState.VOTES) {
            if (event.getMessage().getContentRaw().matches("\\d{1,2}")) {
                int i = Integer.parseInt(event.getMessage().getContentRaw());

                if (vote.getVoteAnswers().size() < i) {
                    sendMessage(channel, Type.ERROR, "Users can't have more votes then possibilities!").queue();
                    return;
                }

                vote.setVotesPerUser(i);
                sendMessage(channel, Type.SUCCESS, "Successfully set votes per user!").queue();

                sendSetupFinishMessage(event);
                creator.setState(VoteState.FINISHED);
                finish(vote);
                return;
            } else {
                sendMessage(channel, Type.ERROR, "Just insert digits please or your number is too long. (Max is `2` digits)!").queue();
            }
        }
    }

    private long convertTime(TimeUnit timeUnit, long time) {
        if (timeUnit == TimeUnit.MINUTES)
            return time * 60000;

        if (timeUnit == TimeUnit.HOURS)
            return time * 3600000;

        if (timeUnit == TimeUnit.DAYS)
            return time * 86400000;

        return 0;
    }

    private void sendTopicMessage(GuildMessageReceivedEvent event) {
        sendMessage(event.getChannel(), Type.QUESTION, "What is the vote topic?").queue();
    }

    private void sendPossibilitiesMessage(GuildMessageReceivedEvent event) {
        sendMessage(event.getChannel(), Type.QUESTION, "What are the possibilities? If you finished type `finished`.").queue();
    }

    private void sendVotesMessage(GuildMessageReceivedEvent event) {
        sendMessage(event.getChannel(), Type.QUESTION, "How many votes should each user have?").queue();
    }

    private void sendTimeReactionMessage(Vote vote, GuildMessageReceivedEvent event) {
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

                switch (reaction) {
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
                sendTimeMessage(event, vote);
            }, Arrays.asList(emojis));
        });
    }

    private void sendTimeMessage(GuildMessageReceivedEvent event, Vote vote) {
        String time = vote.getTimeUnit().name().toLowerCase();
        sendMessage(event.getChannel(), Type.SUCCESS, "Successfully set time to " + time + ".").queue();
        sendMessage(event.getChannel(), Type.INFO, "How many " + time + " should the vote last?").queue();
    }

    private void sendChannelMessage(GuildMessageReceivedEvent event) {
        sendMessage(event.getChannel(), Type.INFO, "In which channel should the vote take part? Use #channel.").queue();
    }

    private void sendSetupFinishMessage(GuildMessageReceivedEvent event) {

        String[] reactions = {emojis[1], Reactions.SATTELITE, Reactions.MAIL};

        Vote vote = votes.get(event.getGuild().getIdLong());

        String voteStats = format(
                "**Your vote information**\n\n" +
                        "%s Time:\t\t\t%s %s\n" +
                        "%s Channel:\t\t<#%s>\n" +
                        "%s Votes per user:\t%s\n" +
                        "%s Answer count:\t%s", reactions[0], vote.getTime(), vote.getTimeUnit().name().toLowerCase(), reactions[1], vote.getTargetChannelId(), reactions[2], vote.getVoteAnswers().size(), Reactions.USER, vote.getVotesPerUser());

        sendMessage(event.getChannel(), Type.INFO, voteStats, false).queue();
    }

    private void sendStartInfoMessage(GuildMessageReceivedEvent event) {
        sendMessage(event.getChannel(), Type.INFO, "Your vote can not go longer then `1` week! \nYou can only submit up to `10` answer possibilities.\nYou can cancel the vote any time by typing `cancel`").queue();

    }

    private void finish(Vote vote) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy kk:mm:ss O", Locale.ENGLISH).withZone(ZoneId.of("Europe/Paris"));
        var targetChannel = vote.getTargetChannel();
        var embedBuilder = new EmbedBuilder()
                .setColor(vote.getGuild().getSelfMember().getColor())
                .setTitle(Reactions.NEWSPAPER + " New vote!")
                .setAuthor(vote.getVoteCreator().getMember().getEffectiveName(), null, vote.getVoteCreator().getMember().getUser().getEffectiveAvatarUrl())
                .setFooter(format("Duration: %s %s -> End: %s", vote.getTime(), vote.getTimeUnit().name().toLowerCase(),
                        dateTimeFormatter.format(Instant.now().plus(vote.getTime(), vote.getTimeUnit().toChronoUnit()))), null)
                .setTimestamp(Instant.now());

        var stringBuilder = new StringBuilder();

        stringBuilder.append(vote.getTopic()).append("\n\n**Answers:**\n");

        for (int i = 1; i < vote.getVoteAnswers().size() + 1; i++)
        {
            stringBuilder.append(Reactions.getNumber(i)).append(" \t").append(vote.getVoteAnswers().get(i - 1).getAnswer()).append("\n");
            vote.getEmotes().add(Reactions.getNumber(i));
        }

        embedBuilder.setDescription(stringBuilder.toString());

        targetChannel.sendMessage(embedBuilder.build()).queue(message -> {
            vote.setMessage(message);
            for (String emote : vote.getEmotes())
            {
                message.addReaction(emote).queue();
            }
        });

        vote.setScheduledFuture(Main.scheduleTask(() -> voteCompleted(vote), vote.getTime(), vote.getTimeUnit()));
    }

    public void voteCompleted(Vote vote) {

        if (vote.getMessage() == null) {
            sendMessage(vote.getTargetChannel(), Type.ERROR, "Uhm, something went wrong!").queue();
            return;
        }

        var chart = new PieChart(vote.getTopic());

        var tiles = chart.getTiles();

        var reactionCount = new HashMap<String, Integer>();

        vote.getMessage().queue(message -> {
            List<MessageReaction> reactions = message.getReactions();

            for (MessageReaction reaction : reactions) {
                if (vote.getEmotes().stream().anyMatch(reaction.getReactionEmote().getName()::equals))
                {
                    reactionCount.put(reaction.getReactionEmote().getName(), reaction.getCount() - 1);
                }
            }

            for (VoteAnswer answer : vote.getVoteAnswers()) {
                if (!(reactionCount.get(Reactions.getNumber(answer.getPosition())) == 0))
                    tiles.add(new PieTile(answer.getAnswer(), reactionCount.get(Reactions.getNumber(answer.getPosition()))));
            }

            if (tiles.size() == 0) {
                sendMessage(vote.getTargetChannel(), Type.ERROR, "Nobody voted so no result can be created!").queue();
                votes.remove(vote.getGuildId());
                return;
            }

            chart.create();

            File chartFile = new File(vote.getGuild().getName() + "_chart.jpeg");

            try
            {
                ChartUtils.saveChartAsJPEG(chartFile, chart.getChart(), WIDTH, HEIGHT);
            } catch (IOException e)
            {
                sendMessage(vote.getTargetChannel(), Type.ERROR, "Something went wrong while creating your file!").queue();
            }

            sendMessage(vote.getTargetChannel(), Type.SUCCESS, "Your result has been created and will be posted within the next 10 seconds!").queue(Messages::deleteAfterFiveSec);

            vote.getTargetChannel().sendFile(chartFile).queueAfter(10, TimeUnit.SECONDS);

            votes.remove(vote.getGuildId());
            chartFile.delete();


            int total = 0;

            for (int i : reactionCount.values()) {
                total = total + i;
            }

            StringBuilder stringBuilder = new StringBuilder();

            for (VoteAnswer voteAnswer : vote.getVoteAnswers()) {
                String emoteName = Reactions.getNumber(voteAnswer.getPosition());
                stringBuilder.append(emoteName + " " + reactionCount.get(emoteName) / total * 100 + "%\n");
            }

            String msg = "Vote results:\n\n" + stringBuilder.toString();

            sendMessage(vote.getTargetChannel(), Type.INFO, msg).queue();
            votes.remove(vote.getGuildId());
        });
    }


    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event)
    {
        if (!votes.containsKey(event.getGuild().getIdLong()))
            return;

        if (event.getMessageIdLong() == votes.get(event.getGuild().getIdLong()).getMessageId()) {
            sendMessage(event.getChannel(), Type.ERROR, "Vote message got deleted and the vote removed!").queue();
            votes.remove(event.getGuild().getIdLong());
        }
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event)
    {
        if (!votes.containsKey(event.getGuild().getIdLong()))
            return;

        if (event.getChannel() == votes.get(event.getGuild().getIdLong()).getTargetChannel()) {
            sendMessage(event.getChannel(), Type.ERROR, "Vote channel got deleted and the vote removed!").queue();
            votes.remove(event.getGuild().getIdLong());
        }
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event)
    {

        if (!votes.containsKey(event.getGuild().getIdLong()))
            return;

        String name = event.getReactionEmote().getName();
        if (event.getMessageIdLong() == votes.get(event.getGuild().getIdLong()).getMessageId()) {

            Vote vote = votes.get(event.getGuild().getIdLong());

            if (vote.getEmotes().stream().noneMatch(name::equals)) {
                event.getReaction().removeReaction(event.getUser()).queue();
                return;
            } else {
                vote.getMessage().queue(message -> {

                    List<MessageReaction> reactions = message.getReactions();

                    for (MessageReaction reaction : reactions) {
                        reaction.getUsers().queue(users -> {

                            int userVoted = 0;

                            if (users.contains(event.getUser()))
                                userVoted++;

                            if (userVoted > vote.getVotesPerUser())
                                event.getReaction().removeReaction().queue();
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        votes.remove(event.getGuild().getIdLong());
    }

    public Map<Long, Vote> getVotes() {
        return votes;
    }

    @Override
    public String info(Member member) {
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        String ret = permLevel < 4
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `4`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Creates a vote and evaluates it\ninto a percentage value and a piechart.\nIf you desire you can close the vote early.\n\n" +
                "**Usage**: `%s[vote|poll]`\n\t\t\t  `%s[vote|poll] close`\n\n**Permission level**: `4`", prefix, prefix);
        return ret;
    }
}