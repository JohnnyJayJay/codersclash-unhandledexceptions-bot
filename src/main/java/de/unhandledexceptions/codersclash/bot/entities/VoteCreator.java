package de.unhandledexceptions.codersclash.bot.entities;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class VoteCreator {

    private Member member;
    private Guild guild;
    private Vote vote;
    private VoteState state;

    public VoteCreator(Member member, Guild guild, Vote vote, VoteState state)
    {
        this.member = member;
        this.guild = guild;
        this.vote = vote;
        this.state = state;
    }
    public VoteCreator(Member member, Guild guild, VoteState state)
    {
        this.member = member;
        this.guild = guild;
        this.state = state;
    }

    public Vote getVote()
    {

        return vote;
    }

    public VoteState getState()
    {
        return state;
    }

    public Guild getGuild()
    {
        return guild;
    }

    public Member getMember()
    {
        return member;
    }

    public void setMember(Member member)
    {
        this.member = member;
    }

    public void setGuild(Guild guild)
    {
        this.guild = guild;
    }

    public void setVote(Vote vote)
    {
        this.vote = vote;
    }

    public void setState(VoteState state)
    {
        this.state = state;
    }
}
