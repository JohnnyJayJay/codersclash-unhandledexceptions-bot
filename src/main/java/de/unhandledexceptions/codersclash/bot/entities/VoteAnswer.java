package de.unhandledexceptions.codersclash.bot.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 15.07.2018
 */

public class VoteAnswer {

    private final VoteCreator creator;
    private Set<String> answers;
    private final Vote vote;

    public VoteAnswer(VoteCreator creator, Vote vote)
    {
        this.vote = vote;
        this.creator = creator;
        answers = new HashSet<>();
    }

    public VoteCreator getCreator()
    {
        return creator;
    }

    public Set<String> getAnswers()
    {
        return answers;
    }

    public Vote getVote()
    {
        return vote;
    }

    public boolean add(String answer)
    {
        return answers.add(answer);
    }
}
