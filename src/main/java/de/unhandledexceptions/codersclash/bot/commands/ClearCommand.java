package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class ClearCommand implements ICommand {

    private CommandSettings settings;

    public ClearCommand(CommandSettings settings) {
        this.settings = settings;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        // Wenn eine Permission fehlt
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 0) { // Benötigtes Permission level überprüfen
            if (args.length == 1 && args[0].matches("[1-9]{1,5}")) {
                int amount = Integer.parseInt(args[0]);
                event.getMessage().delete().queue((v) -> this.clear(channel, amount), (throwable) -> this.failure(throwable, channel));
            } else if (args.length == 1){
                sendMessage(channel, Type.WARNING, String.format("`%s` is not a valid number!", args[0])).queue((msg) -> msg.delete().queueAfter(7, TimeUnit.SECONDS));
            } else {
                sendMessage(channel, Type.INFO, String.format("Correct usage: `%s[clear|clean|delete] <amount>`", settings.getPrefix(event.getGuild().getIdLong()))).queue();
            }
        } else {
            sendMessage(channel, Type.ERROR, "You do not have permission to execute this command. " + member.getAsMention()).queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
        }
    }

    private void clear(TextChannel channel, int amount) {
        // FIXME Buffer einbauen
        long twoWeeksAgo = (System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)); // System der JDA, die Zeit zu messen
        channel.getHistory().retrievePast(amount > 100 ? 100 : amount).queue((messages) -> { // Wenn amount größer als 100 ist, retrieve 100, ansonsten amount
            // filter die messages, die man löschen kann, heraus
            var deletableMsgs = messages.stream().filter((msg) -> msg.getCreationTime().toInstant().toEpochMilli() > twoWeeksAgo).collect(Collectors.toList());
            int size = deletableMsgs.size(); // größe der liste. kann nicht mehr als 100 sein

            if (size == 100 && amount > 100) { // wenn die liste voll ist und noch mehr gelöscht werden sollen
                // die 100 retrievten messages löschen und dann die methode rekursiv aufrufen, diesmal mit 100 weniger
                channel.deleteMessages(deletableMsgs).queue((v) -> clear(channel, amount - 100), (throwable) -> this.failure(throwable, channel));
            } else {
                if (size == 0) { // keine messages können mehr glöscht werden
                    // zwei möglichkeiten: es wurde KEINE message gelöscht oder 100 und dann keine mehr. Was genau passiert ist, ist schwierig zu bestimmen.
                    // Deshalb: einfach immer success
                    this.success(channel);
                } else if (size == 1) { // nur noch eine message kann gelöscht werden
                    deletableMsgs.get(0).delete().queue(
                            (v) -> this.success(channel),
                            (throwable) -> this.failure(throwable, channel));
                } else if (size > 1) { // zwischen 1 und 100 messages können gelöscht werden
                    channel.deleteMessages(deletableMsgs).queue(
                            (v) -> this.success(channel),
                            (throwable) -> this.failure(throwable, channel));
                }
                // In allen Fällen: Warnung, dass manche messages evtl. nicht gelöscht wurden
                sendMessage(channel, Type.WARNING, "Note that some messages might not have been deleted because they are older than two weeks.").queue(
                        (msg) -> msg.delete().queueAfter(6, TimeUnit.SECONDS));
            }
        });
    }

    // Standard failure callback
    private void failure(Throwable throwable, MessageChannel channel) {
        sendMessage(channel, Type.WARNING, String.format("Something went wrong (this may not be relevant):\n```\n%s```", throwable.getMessage())).queue();
    }

    // Standard success callback
    private void success(TextChannel channel) {
        sendMessage(channel, Type.SUCCESS, "Successfully deleted message(s).").queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
    }

    @Override
    public String info(Member member) {
        return String.format("**Description**: clears up to 100 messages at a time.\n\n**Usage**: `%s[clear|clean|delete] <amount>`\n\n**Permission level**: `3`",
                settings.getPrefix(member.getGuild().getIdLong()));
    }
}


