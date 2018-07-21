package de.unhandledexceptions.codersclash.bot.entities;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class Vote {

    private List<VoteAnswer> voteAnswers;
    private VoteCreator voteCreator;
    private long setupChannelId, targetChannelId, guildId, messageId;
    private long time;
    private TimeUnit timeUnit;
    private Set<String> emotes;
    private String topic;
    private final ShardManager shardManager;
    private ScheduledFuture scheduledFuture;
    private int votesPerUser;

    public Vote(Guild guild, TextChannel setupChannel, ShardManager shardManager)
    {
        this.emotes = new ListOrderedSet<>();
        this.voteAnswers = new ArrayList<>();
        this.guildId = guild.getIdLong();
        this.setupChannelId = setupChannel.getIdLong();
        this.shardManager = shardManager;
    }

    public void setTargetChannel(TextChannel targetChannel)
    {
        this.targetChannelId = targetChannel.getIdLong();
    }

    public List<VoteAnswer> getVoteAnswers()
    {
        return voteAnswers;
    }

    public long getTime()
    {
        return time;
    }

    public VoteCreator getVoteCreator()
    {
        return voteCreator;
    }

    public Guild getGuild()
    {
        return shardManager.getGuildById(guildId);
    }

    public TextChannel getSetupChannel()
    {
        return getGuild().getTextChannelById(setupChannelId);
    }

    public TextChannel getTargetChannel()
    {
        return getGuild().getTextChannelById(targetChannelId);
    }

    public void setVoteAnswers(List<VoteAnswer> voteAnswers)
    {
        this.voteAnswers = voteAnswers;
    }

    public void setVoteCreator(VoteCreator voteCreator)
    {
        this.voteCreator = voteCreator;
    }

    public void setGuild(Guild guild)
    {
        this.guildId = guild.getIdLong();
    }

    public void setSetupChannel(TextChannel setupChannel)
    {
        this.setupChannelId = setupChannel.getIdLong();
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public TimeUnit getTimeUnit()
    {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit)
    {
        this.timeUnit = timeUnit;
    }

    public Set<String> getEmotes()
    {
        return emotes;
    }

    public void setEmotes(Set<String> emotes)
    {
        this.emotes = emotes;
    }

    public String getTopic()
    {
        return topic;
    }

    public void setTopic(String topic)
    {
        this.topic = topic;
    }

    public RestAction<Message> getMessage()
    {
        return getTargetChannel().getMessageById(messageId);
    }

    public void setMessage(Message message)
    {
        this.messageId = message.getIdLong();
    }

    public long getSetupChannelId()
    {
        return setupChannelId;
    }

    public void setSetupChannelId(long setupChannelId)
    {
        this.setupChannelId = setupChannelId;
    }

    public long getTargetChannelId()
    {
        return targetChannelId;
    }

    public void setTargetChannelId(long targetChannelId)
    {
        this.targetChannelId = targetChannelId;
    }

    public long getGuildId()
    {
        return guildId;
    }

    public void setGuildId(long guildId)
    {
        this.guildId = guildId;
    }

    public long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(long messageId)
    {
        this.messageId = messageId;
    }

    public ShardManager getShardManager()
    {
        return shardManager;
    }

    public ScheduledFuture getScheduledFuture()
    {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture scheduledFuture)
    {
        this.scheduledFuture = scheduledFuture;
    }

    public int getVotesPerUser()
    {
        return votesPerUser;
    }

    public void setVotesPerUser(int votesPerUser)
    {
        this.votesPerUser = votesPerUser;
    }

    public boolean isRunning()
    {

        if (scheduledFuture == null)
            return false;

        return !scheduledFuture.isDone();
    }
}