package de.unhandledexceptions.codersclash.bot.entities;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Set;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class Vote {

    private Set<VoteAnswer> voteAnswers;
    private VoteCreator voteCreator;
    private Guild guild;
    private TextChannel setupChannel, targetChannel;

    public Vote(Set<VoteAnswer> voteAnswers, VoteCreator voteCreator, Guild guild, TextChannel setupChannel, TextChannel targetChannel)
    {
        this.voteAnswers = voteAnswers;
        this.voteCreator = voteCreator;
        this.guild = guild;
        this.setupChannel = setupChannel;
        this.targetChannel = targetChannel;
    }

    public Vote(VoteCreator voteCreator, Guild guild, TextChannel setupChannel, TextChannel targetChannel)
    {
        this.voteCreator = voteCreator;
        this.guild = guild;
        this.setupChannel = setupChannel;
        this.targetChannel = targetChannel;
    }

    public Vote(VoteCreator voteCreator, Guild guild, TextChannel setupChannel)
    {
        this.voteCreator = voteCreator;
        this.guild = guild;
        this.setupChannel = setupChannel;
    }

    public Vote(Guild guild, TextChannel setupChannel)
    {
        this.guild = guild;
        this.setupChannel = setupChannel;
    }



    public void setTargetChannel(TextChannel targetChannel)
    {
        this.targetChannel = targetChannel;
    }

    public Set<VoteAnswer> getVoteAnswers()
    {

        return voteAnswers;
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

    public void setVoteAnswers(Set<VoteAnswer> voteAnswers)
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


}
