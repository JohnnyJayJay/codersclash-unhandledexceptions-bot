package de.unhandledexceptions.codersclash.bot.entities;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class VoteAnswer {

    private final VoteCreator creator;
    private String answer;
    private final Vote vote;
    private int position;

    public VoteAnswer(String answer, VoteCreator creator, Vote vote, int position) {
        this.creator = creator;
        this.answer = answer;
        this.position = position;
        this.vote = vote;
    }

    public VoteCreator getCreator()
    {
        return creator;
    }

    public Vote getVote()
    {
        return vote;
    }

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }
}
