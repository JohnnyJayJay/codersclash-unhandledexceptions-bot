package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 09.07.2018
 */

public class VoteCommand extends ListenerAdapter implements ICommand {

    private final Database database;
    private Set<Answer> answers;
    private Set<Member> members;
    private Set<TextChannel> textChannels;
    private HashMap<Member, State> currentState;
    private static Logger logger = Logging.getLogger();

    public VoteCommand(Database database)
    {
        this.database = database;
        this.answers = new HashSet<>();
        this.members = new HashSet<>();
        this.textChannels = new HashSet<>();
        this.currentState = new HashMap();

    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args)
    {
        if (Permissions.getPermissionLevel(member) < Permissions.getVotePermissionLevel())
        {
            channel.sendMessage("Your permission level is too low. (" + Permissions.getPermissionLevel(member) + "/" + Permissions.getVotePermissionLevel() + ")").queue();
            return;
        }

        if (!members.add(member))
        {
            sendMessage(channel, Type.ERROR, "You are already in the voting setup. If you want to cancel the vote setup type: 'cancel'.").queue();
            return;
        }
        textChannels.add(channel);
        sendMessage(channel, Type.SUCCESS, "Okay great! Let's create your vote!\nWhen should the vote end?").queue();
        currentState.put(member, State.TIME);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        if (!textChannels.contains(event.getChannel()))
        {
            return;
        }

        if (!members.contains(event.getMember()))
        {
            return;
        }

        State state = currentState.get(event.getMember());

        if (event.getMessage().getContentRaw().equals("cancel"))
        {
            members.remove(event.getMember());
            textChannels.remove(event.getChannel());
            answers.forEach(answer -> {
                if (answer.getGuildID().equals(event.getGuild().getId()))
                    answers.remove(answer);
            });
            sendMessage(event.getChannel(), Type.SUCCESS, "Successfully canceled vote setup!").queue();
            currentState.put(event.getMember(), State.DEFAULT);
            return;
        }

        if (!event.getMessage().getMentionedMembers().isEmpty())
        {
            if (event.getMessage().getMentionedMembers().get(0).getAsMention().equals(event.getGuild().getSelfMember().getAsMention()))
            {
                if (state.equals(State.POSSIBILLITIES))
                {
                    int i = (int) answers.stream().filter(answer -> answer.getGuildID().equals(event.getGuild().getId())).count();

                    if (i == 0)
                    {
                        sendMessage(event.getChannel(), Type.WARNING, "You did't submit any possibilities. If you want to cancel the vote setup type: 'cancel'.").queue();
                        return;
                    }

                    sendMessage(event.getChannel(), Type.SUCCESS, "Successfully completed vote setup with " + i + " vote possibilities!").queue();
                }
            }
        }

        if (state.equals(State.TIME))
        {
            
        }

        if (state.equals(State.POSSIBILLITIES))
        {
            answers.add(new Answer(event.getGuild().getId(), event.getMessage().getContentRaw()));
            event.getChannel().sendMessage("Next Answer").queue();
        }
    }

    private class Answer {
        private String guildID, answer;

        private Answer(String guildID, String answer)
        {
            this.guildID = guildID;
            this.answer = answer;
        }

        public String getAnswer()
        {
            return answer;
        }

        public String getGuildID()
        {
            return guildID;
        }
    }

    private enum State{
        DEFAULT(),
        TIME(),
        POSSIBILLITIES()
    }

    private TimeUnit time(String timeUnit)
    {
        timeUnit = timeUnit.toLowerCase();

        if (timeUnit.matches("^sec"))
            return TimeUnit.SECONDS;

        if (timeUnit.matches("^min"))
            return TimeUnit.MINUTES;

        if (timeUnit.matches("^hour"))
            return TimeUnit.HOURS;

        if (timeUnit.matches("^day"))
            return TimeUnit.DAYS;

        return null;
    }
}
