package de.unhandledexceptions.codersclash.bot.commands;

import ch.qos.logback.core.util.TimeUtil;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 09.07.2018
 */

public class VoteCommand extends ListenerAdapter implements ICommand {

    private HashMap<Member, Vote> votes;
    private Set<Answer> answers;
    private HashMap<Member, State> currentState;
    private static Logger logger = Logging.getLogger();


    public VoteCommand()
    {
        this.votes = new HashMap<>();
        this.answers = new HashSet<>();
        this.currentState = new HashMap<>();
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args)
    {
        if (Permissions.getPermissionLevel(member) < Permissions.getVotePermissionLevel())
        {
            channel.sendMessage("Your permission level is too low. (" + Permissions.getPermissionLevel(member) + "/" + Permissions.getVotePermissionLevel() + ")").queue();
            return;
        }

        if (votes.containsKey(member))
        {
            sendMessage(channel, Type.ERROR, "You are already in the voting setup. If you want to cancel the vote setup type: 'cancel'.").queue();
            return;
        }

        answers.forEach(answer -> {
            if (answer.getGuildID().equals(event.getGuild().getId()))
            {
                sendMessage(event.getChannel(), Type.ERROR, "There is already a vote running on this guild").queue();
                return;
            }

    });

        votes.put(member, new Vote(member, event.getGuild(), channel));
        sendMessage(channel, Type.SUCCESS, "Okay great! Let's create your vote!").queue();

        final String[] emojis = {"\uD83D\uDCC5", "\uD83D\uDD5B", "\u231A"};

        final String message =
                "\u23F2 When should the vote end?\n\n" +
                        emojis[0] + " Day\n" +
                        emojis[1] + " Hour\n" +
                        emojis[2] + " Minute";

        sendMessage(event.getChannel(), Type.QUESTION, message).queue(msg -> {
            Reactions.newMenu(msg, event.getAuthor(), Map.of(
                    emojis[0], v -> {
                        timeSet(event, "day", msg);
                    },
                    emojis[1], v -> {
                        timeSet(event, "hour", msg);
                    },
                    emojis[2], v -> {
                        timeSet(event, "minute", msg);
                    }
            ));
        });

        currentState.put(member, State.TIME);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {

        event.getChannel().sendMessage("456").queue();

        if (!votes.containsKey(event.getMember()))
        {
            event.getChannel().sendMessage("member").queue();
            return;
        }

        if (votes.get(event.getMember()).getSetupChan() != event.getChannel())
        {
            event.getChannel().sendMessage("channel").queue();
            return;
        }

        event.getChannel().sendMessage("123").queue();
        State state = currentState.get(event.getMember());

        if (event.getMessage().getContentRaw().equals("cancel"))
        {
            clear(event.getMember());
            sendMessage(event.getChannel(), Type.SUCCESS, "Successfully canceled vote setup!").queue();
            return;
        }

        int time = 0;

        if (!event.getMessage().getMentionedMembers().isEmpty())
        {
            if (event.getMessage().getMentionedMembers().get(0).getAsMention().equals(event.getGuild().getSelfMember().getAsMention()))
            {
                if (state.equals(State.POSSIBILITIES))
                {
                    int i = (int) answers.stream().filter(answer -> answer.getGuildID().equals(event.getGuild().getId())).count();

                    if (i == 0)
                    {
                        sendMessage(event.getChannel(), Type.WARNING, "You did't submit any possibilities. If you want to cancel the vote setup type: 'cancel'.").queue();
                        return;
                    }
                    sendMessage(event.getChannel(), Type.SUCCESS, "Successfully completed vote setup with " + i + " vote possibilities!").queue();

                    //TODO TIMER
                }
            }
        }

        if (state.equals(State.TIME))
        {
            event.getChannel().sendMessage("time").queue();

        }


            final String[] emoji = {"\u23F2", "\uD83D\uDCC5", "\uD83D\uDD5B", "\u231A"};

            final String message =
                    emoji[0] + " When should the vote end?\n\n" +
                    emoji[1] + " Day\n" +
                    emoji[2] + " Hour\n" +
                    emoji[3] + " Minute";

					
					
            sendMessage(event.getChannel(), Type.QUESTION, message).queue(msg -> { 
                // Reactions.newMenu(msg, event.getAuthor(), ;
            }); 
 
        }

        if (state.equals(State.POSSIBILITIES))
        {
            answers.add(new Answer(event.getGuild().getId(), event.getMessage().getContentRaw()));
            event.getChannel().sendMessage("Next VoteAnswer").queue();
        }
    }

    private class Answer {
        private String guildID, answer;

        private Answer(String guildID, String answer)
        {
            this.guildID = guildID;
            this.answer = answer;
        }

        private String getAnswer()
        {
            return answer;
        }

        private String getGuildID()
        {
            return guildID;
        }
    }

    private enum State{
        DEFAULT(),
        TIME(),


        CHANNEL(),
        POSSIBILITIES(
        )
    }

    private int time(String timeUnit, int time)
    {
        timeUnit = timeUnit.toLowerCase();

        if (timeUnit.equals("minute"))
            return time * 60000;

        if (timeUnit.equals("hour"))
            return time * 3600000;

        if (timeUnit.equals("day"))
            return time * 86400000;

        long bla = 1;

        return 0;
    }

    private void timeSet(GuildMessageReceivedEvent event, String timeUnit, Message msg)
    {
        msg.delete().queue();
        event.getChannel().sendMessage(timeUnit  + " " + time(timeUnit, 1)).queue();
    }

    private class Vote
    {

        private Member member;
        private Guild guild;
        private TextChannel setupChan, targetChan;

        private Vote(Member member, Guild guild, TextChannel setupChan, TextChannel targetChan)
        {
            this.member = member;
            this.guild = guild;
            this.setupChan = setupChan;
            this.targetChan = targetChan;

        }

        private Vote(Member member, Guild guild, TextChannel setupChan)
        {
            this.member = member;
            this.guild = guild;
            this.setupChan = setupChan;
        }

        public void clear(Vote vote)
        {
            vote.setGuild(null);
            vote.setMember(null);
            vote.setTargetChan(null);
            vote.setSetupChan(null);
        }

        public void setMember(Member member)
        {
            this.member = member;
        }

        public void setGuild(Guild guild)
        {
            this.guild = guild;
        }

        public void setSetupChan(TextChannel setupChan)
        {
            this.setupChan = setupChan;
        }

        public void setTargetChan(TextChannel targetChan)
        {
            this.targetChan = targetChan;
        }

        public Guild getGuild()
        {

            return guild;
        }

        public TextChannel getSetupChan()
        {
            return setupChan;
        }

        public TextChannel getTargetChan()
        {
            return targetChan;
        }

        public Member getMember()
        {

            return member;
        }


    }

    public void clear(Member m)
    {
        currentState.put(m, State.DEFAULT);
        votes.remove(m);
        answers.forEach(answer -> {
            if (answer.getGuildID().equals(m.getGuild().getId()))
                answers.remove(answer);
        });

    }
}
