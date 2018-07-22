package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Main;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages.*;
import de.unhandledexceptions.codersclash.bot.util.Regex;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author Johnny_JayJay
 */

public class ClearCommand implements ICommand {

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        // Wenn eine Permission fehlt
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 4) { // Benötigtes Permission level überprüfen
            if (args.length == 1 && Regex.argsMatch(args, "[1-9]\\d{0,3}")) {
                int amount = Integer.parseInt(args[0]);
                Main.otherThread(() -> event.getMessage().delete().queue((v) -> this.clear(channel, amount), defaultFailure(channel)));
            } else if (args.length == 1){
                sendMessage(channel, Type.WARNING, format("`%s` is not a valid number!", args[0])).queue((msg) -> msg.delete().queueAfter(7, TimeUnit.SECONDS));
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    private void clear(TextChannel channel, int amount) {
        long twoWeeksAgo = (System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)); // System der JDA, die Zeit zu messen
        channel.getHistory().retrievePast(amount > 100 ? 100 : amount).queue((messages) -> { // Wenn amount größer als 100 ist, retrieve 100, ansonsten amount
            // filter die messages, die man löschen kann, heraus
            var deletableMsgs = messages.stream().filter((msg) -> msg.getCreationTime().toInstant().toEpochMilli() > twoWeeksAgo).collect(Collectors.toList());
            int size = deletableMsgs.size(); // größe der liste. kann nicht mehr als 100 sein

            if (size == 100 && amount > 100) { // wenn die liste voll ist und noch mehr gelöscht werden sollen
                // die 100 retrievten messages löschen und dann die methode rekursiv aufrufen, diesmal mit 100 weniger
                channel.deleteMessages(deletableMsgs).queue((v) -> clear(channel, amount - 100), defaultFailure(channel));
            } else {
                if (size == 0) { // keine messages können mehr glöscht werden
                    // zwei möglichkeiten: es wurde KEINE message gelöscht oder 100 und dann keine mehr. Was genau passiert ist, ist schwierig zu bestimmen.
                    // Deshalb: einfach immer success
                    this.success(channel);
                } else if (size == 1) { // nur noch eine message kann gelöscht werden
                    deletableMsgs.get(0).delete().queue(
                            (v) -> this.success(channel), defaultFailure(channel));
                } else { // zwischen 1 und 100 messages können gelöscht werden
                    channel.deleteMessages(deletableMsgs).queue(
                            (v) -> this.success(channel), defaultFailure(channel));
                }
            }
        });
    }

    // Standard success callback
    private void success(TextChannel channel) {
        sendMessage(channel, Type.SUCCESS, "Successfully deleted message(s)! Note that some messages might not have been deleted because they are older than two weeks.").queue((msg) -> msg.delete().queueAfter(8, TimeUnit.SECONDS));
    }

    @Override
    public String info(Member member) {
        int permLevel = Permissions.getPermissionLevel(member);
        return permLevel < 4 ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: " +
                "`4`\nYour permission level: `" + permLevel + "`"
                : format("**Description**: Clears up to 9999 messages at a time. Though it is recommended to use it carefully. Deleting might take a while.\n\n" +
                "**Usage**: `%s[clear|clean|delete] <amount>` (amount must be a number between 1-9999)\n\n**Permission level**: `4`", Bot.getPrefix(member.getGuild().getIdLong()));
    }
}


