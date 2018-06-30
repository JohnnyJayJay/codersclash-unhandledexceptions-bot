package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.concurrent.TimeUnit;

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
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 0) {
            if (args.length == 1 && args[0].matches("(\\d{1,2}|100)")) {
                int amount = Integer.parseInt(args[0]);
                event.getMessage().delete().queue((v) -> {
                    var messages = channel.getHistory().retrievePast(amount).complete();
                    if (amount == 1) {
                        messages.get(0).delete().queue(
                                (v2) -> sendMessage(channel, Type.SUCCESS,"Successfully deleted last message.").queue(
                                        (msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS)),
                                (throwable) -> this.failure(throwable, channel));
                    } else {
                        channel.deleteMessages(messages).queue(
                                (v2) -> sendMessage(channel, Type.SUCCESS,"Successfully deleted `" + amount + "` messages.").queue(
                                        (msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS)),
                                (throwable) -> this.failure(throwable, channel));
                    }
                }, (throwable) -> this.failure(throwable, channel));
            } else {
                sendMessage(channel, Type.WARNING, String.format("Correct usage: `%sclear <amount>` (amount must be a number between 1 and 100)",
                        settings.getPrefix(event.getGuild().getIdLong()))).queue();
            }
        } else {
            sendMessage(channel, Type.ERROR, "You do not have permission to execute this command. " + member.getAsMention()).queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
        }
    }

    private void failure(Throwable throwable, MessageChannel channel) {
        sendMessage(channel, Type.ERROR, String.format("Something went wrong:\n```\n%s```", throwable.getMessage())).queue();
    }

    @Override
    public String info(Guild guild) {
        return String.format("Description: clears up to 100 messages at a time.\nUsage: `%s[clear|clean|delete] <amount>`\nPermission level: `3`",
                settings.getPrefix(guild.getIdLong()));
    }
}
