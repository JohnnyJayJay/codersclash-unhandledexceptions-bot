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

import java.util.HashSet;
import java.util.Set;

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
    private static Logger logger = Logging.getLogger();

    public VoteCommand(Database database)
    {
        this.database = database;
        this.answers = new HashSet<>();
        this.members = new HashSet<>();
        this.textChannels = new HashSet<>();
        logger.debug("reset");
    }


    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel textChannel, String[] args)
    {

        if (Permissions.getPermissionLevel(member) < Permissions.getVotePermissionLevel())
        {
            textChannel.sendMessage("Your permission level is too low. (" + Permissions.getPermissionLevel(member) + "/" + Permissions.getVotePermissionLevel() + ")").queue();
            return;
        }

        event.getChannel().sendMessage("Let's create your vote! Write your answers").queue();

        textChannels.add(textChannel);
        members.add(member);

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

        if (!event.getMessage().getMentionedMembers().isEmpty())
        {
            if (event.getMessage().getMentionedMembers().get(0).getAsMention().equals(event.getGuild().getSelfMember().getAsMention()))
            {
                members.remove(event.getMember());
                textChannels.remove(event.getChannel());
                return;
            }
        }

        answers.add(new Answer(event.getGuild().getId(), event.getMessage().getContentRaw()));
        event.getChannel().sendMessage("Next Answer").queue();
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
}
