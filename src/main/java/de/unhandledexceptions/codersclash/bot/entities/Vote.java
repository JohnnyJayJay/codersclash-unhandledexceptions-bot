package de.unhandledexceptions.codersclash.bot.entities;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class Vote {

    private Set<String> voteAnswers;
    private VoteCreator voteCreator;
    private Guild guild;
    private TextChannel setupChannel, targetChannel;
    private long time;
    private TimeUnit timeUnit;
    private long messageID;
    private Set<String> emotes;

    public Vote(Set<String> voteAnswers, VoteCreator voteCreator, Guild guild, TextChannel setupChannel, TextChannel targetChannel, long time, TimeUnit timeUnit, long messageID, Set<String> emotes)
    {
        this.voteAnswers = voteAnswers;
        this.voteCreator = voteCreator;
        this.guild = guild;
        this.setupChannel = setupChannel;
        this.targetChannel = targetChannel;
        this.time = time;
        this.timeUnit = timeUnit;
        this.messageID = messageID;
        this.emotes = emotes;
    }

    public Vote(Set<String> voteAnswers, VoteCreator voteCreator, Guild guild, TextChannel setupChannel, TextChannel targetChannel, long time)
    {
        this.emotes = new HashSet<>();
        this.voteAnswers = voteAnswers;
        this.voteCreator = voteCreator;
        this.guild = guild;
        this.setupChannel = setupChannel;
        this.targetChannel = targetChannel;
        this.time = time;
    }

    public Vote(Set<String> voteAnswers, VoteCreator voteCreator, Guild guild, TextChannel setupChannel, TextChannel targetChannel)
    {
        this.emotes = new HashSet<>();
        this.voteAnswers = voteAnswers;
        this.voteCreator = voteCreator;
        this.guild = guild;
        this.setupChannel = setupChannel;
        this.targetChannel = targetChannel;
    }

    public Vote(VoteCreator voteCreator, Guild guild, TextChannel setupChannel, TextChannel targetChannel)
    {
        this.emotes = new HashSet<>();
        this.voteAnswers = new HashSet<>();
        this.voteCreator = voteCreator;
        this.guild = guild;
        this.setupChannel = setupChannel;
        this.targetChannel = targetChannel;
    }

    public Vote(VoteCreator voteCreator, Guild guild, TextChannel setupChannel)
    {
        this.emotes = new HashSet<>();
        this.voteAnswers = new HashSet<>();
        this.voteCreator = voteCreator;
        this.guild = guild;
        this.setupChannel = setupChannel;
    }

    public Vote(Guild guild, TextChannel setupChannel)
    {
        this.emotes = new ListOrderedSet<>();
        this.voteAnswers = new HashSet<>();
        this.guild = guild;
        this.setupChannel = setupChannel;
    }



    public void setTargetChannel(TextChannel targetChannel)
    {
        this.targetChannel = targetChannel;
    }

    public Set<String> getVoteAnswers()
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
        return guild;
    }

    public TextChannel getSetupChannel()
    {
        return setupChannel;
    }

    public TextChannel getTargetChannel()
    {
        return targetChannel;
    }

    public void setVoteAnswers(Set<String> voteAnswers)
    {
        this.voteAnswers = voteAnswers;
    }

    public void setVoteCreator(VoteCreator voteCreator)
    {
        this.voteCreator = voteCreator;
    }

    public void setGuild(Guild guild)
    {
        this.guild = guild;
    }

    public void setSetupChannel(TextChannel setupChannel)
    {
        this.setupChannel = setupChannel;
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

    public long getMessageID()
    {
        return messageID;
    }

    public void setMessageID(long messageID)
    {
        messageID = messageID;
    }

    public Set<String> getEmotes()
    {
        return emotes;
    }

    public void setEmotes(Set<String> emotes)
    {
        this.emotes = emotes;
    }
}