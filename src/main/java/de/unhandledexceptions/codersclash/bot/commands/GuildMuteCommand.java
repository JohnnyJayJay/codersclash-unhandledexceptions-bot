package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Roles;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.noPermissionsMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.wrongUsageMessage;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class GuildMuteCommand implements ICommand {

    private CommandSettings settings;
    private Set<Long> guildIds;

    public GuildMuteCommand(CommandSettings settings) {
        this.settings = settings;
        this.guildIds = new HashSet<>();
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_WRITE, Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL))
            return;

        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length == 0) {
                if (guildIds.contains(event.getGuild().getIdLong())) {
                    guildIds.remove(event.getGuild().getIdLong());
                    channel.delete().queue((v) -> {
                        var guild = event.getGuild();
                        var mutedRole = Roles.getMutedRole(guild);
                        Set<Long> mutedMembers = MuteCommand.getMutedMembers().get(guild.getIdLong());
                        settings.removeChannelsFromBlackList(guild.getTextChannelCache().stream().map(TextChannel::getIdLong).collect(Collectors.toList()));
                        if (mutedRole != null) {
                            var controller = guild.getController();
                            if (mutedMembers != null) {
                                guild.getMemberCache().stream()
                                        .filter((m) -> !mutedMembers.contains(m.getUser().getIdLong()))
                                        .forEach((m) -> controller.removeSingleRoleFromMember(m, mutedRole).queue());
                            } else {
                                guild.getMemberCache().forEach((m) -> controller.removeSingleRoleFromMember(m, mutedRole).queue());
                            }
                        }
                    });
                } else {
                    sendMessage(channel, Type.WARNING, "This will have immediate effect and will result in a completely muted guild.\nAre you sure?").queue((msg) -> {
                        Reactions.newYesNoMenu(member.getUser(), msg, (reaction) -> {
                            msg.delete().queue();
                            settings.addChannelsToBlacklist(event.getGuild().getTextChannels().stream().map(TextChannel::getIdLong).collect(Collectors.toList()));
                            sendMessage(channel, Type.WARNING, "Muting guild...").queue();
                            muteGuild(member);
                        });
                    });
                }
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    private void muteGuild(Member member) {
        var guild = member.getGuild();
        var mutedRole = Roles.getMutedRole(guild);
        var controller = guild.getController();
        if (mutedRole == null) {
            controller.createRole().setName("tc-muted").queue((role) -> {
                role.getManager().revokePermissions(Permission.MESSAGE_WRITE, Permission.VOICE_SPEAK).queue((v) -> {
                    guild.getTextChannelCache().forEach((channel) -> channel.putPermissionOverride(role).setDeny(Permission.MESSAGE_WRITE).queue());
                    guild.getVoiceChannelCache().forEach((channel) -> channel.putPermissionOverride(role).setDeny(Permission.VOICE_SPEAK).queue());
                    controller.modifyRolePositions().selectPosition(role).moveTo(guild.getSelfMember().getRoles().get(1).getPosition()).queue();
                    this.muteGuild(member);
                });
            });
        } else {
            controller.createTextChannel("guild-mute").queue((textChannel) -> {
                guild.getMemberCache().forEach((m) -> controller.addSingleRoleToMember(m, mutedRole).queue());
                textChannel.putPermissionOverride(member).setAllow(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION).queue();
                sendMessage((TextChannel) textChannel, Type.SUCCESS, String.format("This guild has been muted. To unmute the guild, please type `%s[guildmute|muteguild|lockdown]` " +
                        "again.", Bot.getPrefix(guild.getIdLong())), true).queue();
                ((TextChannel) textChannel).sendMessage(member.getAsMention()).queue();
                guildIds.add(guild.getIdLong());
            });
        }
    }

    @Override
    public String info(Member member) {
        // TODO
        return " ";
    }
}
