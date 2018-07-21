package de.unhandledexceptions.codersclash.bot.util;

import de.unhandledexceptions.codersclash.bot.core.Bot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

import java.awt.*;
import java.util.function.Consumer;

/**
 * @author Johnny_JayJay
 */

public class Roles {

    public static void getTryCatchRole(Guild guild, Consumer<Role> success, Consumer<Void> failure) {
        var tcRoles = guild.getRolesByName(Bot.getBotName() + "-perms", false);
        if (!tcRoles.isEmpty()) {
            var role = tcRoles.get(0);
            success.accept(role);
        } else if (guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            if (!guild.getSelfMember().getRoles().isEmpty()) {
                guild.getController().createRole().setName(Bot.getBotName() + "-perms").setColor(guild.getSelfMember().getColor()).queue((role) ->
                        guild.getController().modifyRolePositions().selectPosition(role).moveTo(guild.getSelfMember().getRoles().get(0).getPosition() - 1).queue((v) ->
                                success.accept(role)));
            } else {
                guild.getController().createRole().setName(Bot.getBotName() + "-perms").setColor(guild.getSelfMember().getColor()).queue(success, (t) -> failure.accept(null));
            }
        } else {
            failure.accept(null);
        }
    }

    public static void getMutedRole(Guild guild, Consumer<Role> success, Consumer<Void> failure) {
        Role muted;
        var mutedRoles = guild.getRolesByName(Bot.getBotName() + "-muted", false);
        var first = mutedRoles.stream().filter((role) -> {
            var perms = role.getPermissions();
            return !(perms.contains(Permission.VOICE_SPEAK) || perms.contains(Permission.MESSAGE_WRITE));
        }).findFirst();
        if (first.isPresent()) {
            muted = first.get();
            success.accept(muted);
        } else if (guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)){
            if(!guild.getSelfMember().getRoles().isEmpty()) {
                guild.getController().createRole().setName(Bot.getBotName() + "-muted").setHoisted(true).setMentionable(true).setColor(Color.GRAY).queue((role) -> {
                    guild.getController().modifyRolePositions().selectPosition(role).moveTo(guild.getSelfMember().getRoles().get(0).getPosition() - 2).queue();
                    role.getManager().revokePermissions(Permission.MESSAGE_WRITE, Permission.VOICE_SPEAK).queue();
                    guild.getTextChannelCache().forEach((channel) -> channel.putPermissionOverride(role).setDeny(Permission.MESSAGE_WRITE).queue());
                    guild.getVoiceChannelCache().forEach((channel) -> channel.putPermissionOverride(role).setDeny(Permission.VOICE_SPEAK).queue());
                    success.accept(role);
                }, (t) -> failure.accept(null));
            } else {
                guild.getController().createRole().setName(Bot.getBotName() + "-muted").setHoisted(true).setMentionable(true).setColor(Color.GRAY).queue((role) -> {
                    role.getManager().revokePermissions(Permission.MESSAGE_WRITE, Permission.VOICE_SPEAK).queue();
                    guild.getTextChannelCache().forEach((channel) -> channel.putPermissionOverride(role).setDeny(Permission.MESSAGE_WRITE).queue());
                    guild.getVoiceChannelCache().forEach((channel) -> channel.putPermissionOverride(role).setDeny(Permission.VOICE_SPEAK).queue());
                    success.accept(role);
                }, (t) -> failure.accept(null));
            }
        } else
            failure.accept(null);
    }
}