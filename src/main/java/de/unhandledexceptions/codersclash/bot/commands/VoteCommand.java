package de.unhandledexceptions.codersclash.bot.commands;

import ch.qos.logback.core.util.TimeUtil;
import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
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

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {



                        }
                    }, Date.from(Instant.now()), time);
                }
            }
        }

        if (state.equals(State.TIME))
        {

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
        POSSIBILITIES()
    }

    private int time(String timeUnit, int time)
    {
        timeUnit = timeUnit.toLowerCase();

        if (timeUnit.matches("^sec"))
            return time * 1000;

        if (timeUnit.matches("^min"))
            return time * 60000;

        if (timeUnit.matches("^hour"))
            return time * 3600000;

        if (timeUnit.matches("^day"))
            return time * 86400000;

        return 0;
    }

    private void timeSet(GuildMessageReceivedEvent event, String timeUnit, Message msg)
    {
        msg.delete().queue();
        event.getChannel().sendMessage(timeUnit  + " " + time(timeUnit, 1)).queue();
    }
}
