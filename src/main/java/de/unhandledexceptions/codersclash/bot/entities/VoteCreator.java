package de.unhandledexceptions.codersclash.bot.entities;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class VoteCreator {

    private long memberId, guildId;
    private VoteState state;
    private Vote vote;
    private final ShardManager shardManager;

    public VoteCreator(Member member, Guild guild, Vote vote, VoteState state, ShardManager shardManager)
    {
        this.memberId = member.getUser().getIdLong();
        this.guildId = guild.getIdLong();
        this.vote = vote;
        this.state = state;
        this.shardManager = shardManager;
    }

    public Vote getVote() {
        return vote;
    }

    public VoteState getState()
    {
        return state;
    }

    public Guild getGuild()
    {
        return shardManager.getGuildById(guildId);
    }

    public Member getMember()
    {
        return getGuild().getMemberById(memberId);
    }

    public void setMember(Member member)
    {
        this.memberId = member.getUser().getIdLong();
    }

    public void setGuild(Guild guild)
    {
        this.guildId = guild.getIdLong();
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
