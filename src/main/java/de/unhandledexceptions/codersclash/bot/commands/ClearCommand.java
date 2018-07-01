package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.List;
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
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 0) {
            if (args.length == 1 && args[0].matches("\\d{1,6}")) {
                int amount = Integer.parseInt(args[0]);
                event.getMessage().delete().queue((v) -> this.clear(channel, amount), (throwable) -> this.failure(throwable, channel));
            } else {
                sendMessage(channel, Type.WARNING, String.format("`%s` is not a valid number!", args[0])).queue((msg) -> msg.delete().queueAfter(7, TimeUnit.SECONDS));
            }
        } else {
            sendMessage(channel, Type.ERROR, "You do not have permission to execute this command. " + member.getAsMention()).queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
        }
    }

    private void failure(Throwable throwable, MessageChannel channel) {
        sendMessage(channel, Type.ERROR, String.format("Something went wrong:\n```\n%s```", throwable.getMessage())).queue();
    }

    private void clear(TextChannel channel, int amount) {
        long twoWeeksAgo = ((System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)) - MiscUtil.DISCORD_EPOCH) << MiscUtil.TIMESTAMP_OFFSET;
        channel.getHistory().retrievePast(amount > 100 ? 100 : amount).queue((messages) -> {
            var deletableMsgs = messages.stream().filter((msg) -> MiscUtil.parseSnowflake(msg.getId()) < twoWeeksAgo).collect(Collectors.toList());
            int size = deletableMsgs.size();
            if (size > 100) {
                channel.deleteMessages(deletableMsgs).queue((v) -> clear(channel, amount - 100));
            } else if (size == 1) {
                deletableMsgs.get(0).delete().queue(
                        (v) -> sendMessage(channel, Type.SUCCESS, "Successfully deleted message(s).").queue(
                                (msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS)),
                        (throwable) -> this.failure(throwable, channel));
                sendMessage(channel, Type.WARNING, "Some messages may not have been deleted because they are older than two weeks.").queue(
                        (msg) -> msg.delete().queueAfter(6, TimeUnit.SECONDS));
            } else if (size == 0) {
                sendMessage(channel, Type.WARNING, "Some messages may not have been deleted because they are older than two weeks.").queue(
                        (msg) -> msg.delete().queueAfter(6, TimeUnit.SECONDS));
            } else {
                channel.deleteMessages(deletableMsgs).queue(
                        (v) -> sendMessage(channel, Type.SUCCESS, "Successfully deleted messages!").queue(
                                (msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS)),
                        (throwable) -> this.failure(throwable, channel));
                sendMessage(channel, Type.WARNING, "Some messages may not have been deleted because they are older than two weeks.").queue(
                        (msg) -> msg.delete().queueAfter(6, TimeUnit.SECONDS));
            }
        });
    }

    @Override
    public String info(Guild guild) {
        return String.format("**Description**: clears up to 100 messages at a time.\n\n**Usage**: `%s[clear|clean|delete] <amount>`\n\n**Permission level**: `3`",
                settings.getPrefix(guild.getIdLong()));
    }
}
