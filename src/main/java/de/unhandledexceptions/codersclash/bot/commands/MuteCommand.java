package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Roles;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.Arrays;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class MuteCommand implements ICommand {


    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;
        if (Permissions.getPermissionLevel(member) >= 3) {
            if (args.length >= 1 && event.getCommand().getJoinedArgs().matches("<@!?\\d+>( .+)?") && !event.getMessage().getMentionedMembers().isEmpty()) {
                var role = Roles.getMutedRole(event.getGuild());
                var target = event.getMessage().getMentionedMembers().get(0);
                var reason = String.join(" ", Arrays.asList(args).subList(1, args.length));
                var guild = event.getGuild();
                if (role == null) {
                    event.getGuild().getController().createRole().setName("tc-muted").setColor(Color.GRAY).queue(
                            (newRole) -> {
                                newRole.getManager().revokePermissions(Permission.MESSAGE_WRITE, Permission.VOICE_SPEAK).queue();
                                guild.getTextChannelCache().forEach((textChannel) -> textChannel.putPermissionOverride(newRole).setDeny(Permission.MESSAGE_WRITE).queue());
                                guild.getVoiceChannelCache().forEach((voiceChannel) -> voiceChannel.putPermissionOverride(newRole).setDeny(Permission.VOICE_SPEAK).queue());
                                event.getGuild().getController().modifyRolePositions().selectPosition(newRole).moveTo(guild.getSelfMember().getRoles().get(0).getPosition()).queue();
                            });
                    this.onCommand(event, member, channel, args);
                } else {
                    if (target.getRoles().contains(role)) {
                        event.getGuild().getController().removeSingleRoleFromMember(target, role).queue();
                        sendMessage(channel, Type.SUCCESS, format("Successfully unmuted `%#s` by %s", target.getUser(), member.getAsMention()), true).queue();
                    } else {
                        event.getGuild().getController().addSingleRoleToMember(target, role).queue();
                        if (args.length == 1) {
                            sendMessage(channel, Type.SUCCESS, format("Successfully muted `%#s` by %s", target.getUser(), member.getAsMention()), true).queue();
                        } else {
                            sendMessage(channel,Type.SUCCESS, format("Successfully muted `%#s` for ```\n%s``` by %s", target.getUser(), reason, member.getAsMention()), true).queue();
                        }
                    }
                }
            } else {
                sendMessage(channel, Type.INFO, "Wrong usage. Command info:\n\n" + this.info(member)).queue();
            }
        } else {
            Messages.noPermissionsMessage(channel, member);
        }
    }

    @Override
    public String info (Member member){
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        String[] prefixArr = new String[2];
        Arrays.fill(prefixArr, prefix);
        String ret = permLevel < 3
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `5`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Mutes a member so that he can't write in the whole guild.\n\n" +
                "**Usage**: `%s[mute|silence] @Member <reason>` to *mute*\n\n**Permission level**: `3`", prefixArr);
        return ret;
    }
}