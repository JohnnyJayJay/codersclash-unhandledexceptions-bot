package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.time.Instant;

public class MentionListener extends ListenerAdapter {

    Database database;

    public MentionListener(Database database) {
        this.database = database;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getMessage().getContentRaw().contains(" ")&&event.getMessage().getMentionedMembers().size()==1
                &&event.getMessage().getMentionedMembers().get(0).getUser().getId().equals(event.getJDA().getSelfUser().getId()))
        {
            String prefix = Bot.getPrefix(event.getGuild().getIdLong());
            Messages.sendMessage(event.getChannel(), Messages.Type.INFO,
                    "Hello! I'am the try-catch Bot!\nMy Prefix: "+prefix+
                            "\nTo see help do: "+prefix+"help"+
                            "\n\nHave fun with me ^^"
            , "Hi!", true).queue(
                    this::reactionsAdd
            );
        }
    }

    private void reactionsAdd(Message msg) {
        msg.addReaction("\uD83C\uDDF9").queue();
        msg.addReaction("\uD83C\uDDF7").queue();
        msg.addReaction("\uD83C\uDDFE").queue();
        msg.addReaction("\uD83C\uDDE8").queue();
        msg.addReaction("\uD83C\uDDE6").queue();
        msg.addReaction("\uD83C\uDDED").queue();
    }
}
