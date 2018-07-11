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

import java.awt.Color;
import java.util.*;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class MuteCommand implements ICommand {


    private static Map<Long, Set<Long>> mutedMembers = new HashMap<>();

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
                var controller = guild.getController();
                if (role == null) {
                    controller.createRole().setName("tc-muted").setColor(Color.GRAY).setHoisted(true).setMentionable(true).queue((newRole) -> {
                        newRole.getManager().revokePermissions(Permission.MESSAGE_WRITE, Permission.VOICE_SPEAK).queue((v) -> {
                            guild.getTextChannelCache().forEach((textChannel) -> textChannel.putPermissionOverride(newRole).setDeny(Permission.MESSAGE_WRITE).queue());
                            guild.getVoiceChannelCache().forEach((voiceChannel) -> voiceChannel.putPermissionOverride(newRole).setDeny(Permission.VOICE_SPEAK).queue());
                            controller.modifyRolePositions().selectPosition(newRole).moveTo(guild.getSelfMember().getRoles().get(1).getPosition()).queue();
                            this.onCommand(event, member, channel, args);
                        }, defaultFailure(channel));
                    }, defaultFailure(channel));
                } else {
                    if (!mutedMembers.containsKey(guild.getIdLong()))
                        mutedMembers.put(guild.getIdLong(), new HashSet<>());

                    if (mutedMembers.get(guild.getIdLong()).contains(target.getUser().getIdLong())) {
                        controller.removeSingleRoleFromMember(target, role).queue();
                        mutedMembers.get(guild.getIdLong()).remove(target.getUser().getIdLong());
                        sendMessage(channel, Type.SUCCESS, format("Successfully unmuted `%#s` by %s", target.getUser(), member.getAsMention()), true).queue();
                    } else {
                        controller.addSingleRoleToMember(target, role).queue();
                        mutedMembers.get(guild.getIdLong()).add(target.getUser().getIdLong());
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
            noPermissionsMessage(channel, member);
        }
    }

    public static Map<Long, Set<Long>> getMutedMembers() {
        return mutedMembers;
    }

    @Override
    public String info (Member member){
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        String ret = permLevel < 3
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `3`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Mutes a member so that he can't write in the whole guild.\n\n" +
                "**Usage**: `%s[mute|silence] @Member <reason>` to *mute*\n\n**Permission level**: `3`", prefix, prefix);
        return ret;
    }
}