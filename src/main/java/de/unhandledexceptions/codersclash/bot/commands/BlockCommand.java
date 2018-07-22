package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.managers.ChannelManager;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author TheRealYann
 */

public class BlockCommand implements ICommand {

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE) || !event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_PERMISSIONS))
            return;

        if (Permissions.getPermissionLevel(member) >= 2) {
            if (args.length >= 2 && event.getCommand().getJoinedArgs().matches("<@.\\d+> <#\\d+>( .+)?") && !event.getMessage().getMentionedMembers().isEmpty() && !event.getMessage().getMentionedChannels().isEmpty()) {
                Channel targetChannel = event.getMessage().getMentionedChannels().get(0);
                var reason = event.getCommand().getJoinedArgs(2);
                var targetMember = event.getMessage().getMentionedMembers().get(0);
                ChannelManager channelManager = new ChannelManager(targetChannel);
                if (targetMember.hasPermission(targetChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) {
                    targetChannel.putPermissionOverride(targetMember).setDeny(Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ).queue();
                    if (args.length == 2) {
                        sendMessage(channel, Type.SUCCESS, format("Successfully blocked `%#s` in %s by %s", targetMember.getUser(), ((TextChannel) targetChannel).getAsMention(), member.getAsMention()), true).queue();
                    } else {
                        sendMessage(channel, Type.SUCCESS, format("Successfully blocked `%#s` in %s for ```\n%s``` by %s", targetMember.getUser(), ((TextChannel) targetChannel).getAsMention(), reason, member.getAsMention()), true).queue();
                    }
                } else {
                    channelManager.removePermissionOverride(targetMember).queue();
                    sendMessage(channel, Type.SUCCESS, format("Successfully unblocked `%#s` in %s by %s", targetMember.getUser(), ((TextChannel) targetChannel).getAsMention(), member.getAsMention()), true).queue();
                }
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    @Override
    public String info(Member member) {
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        return permLevel < 2
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `2`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Denies permission for a specific user in a specific channel.\n\n**Usage**: `%s[block|deny] @Member #Channel <reason>`\n\n**Permission level**: `2`", prefix);
    }
}
