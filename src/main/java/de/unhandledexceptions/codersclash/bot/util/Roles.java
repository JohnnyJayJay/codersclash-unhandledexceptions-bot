package de.unhandledexceptions.codersclash.bot.util;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class Roles {

    public static Role getTryCatchRole(Guild guild) {
        Role tc = null;
        var tcRoles = guild.getRolesByName("try-catch", false);
        if (!tcRoles.isEmpty())
            tc = tcRoles.get(0);
        return tc;
    }

    public static Role getMutedRole(Guild guild) {
        Role muted = null;
        var mutedRoles = guild.getRolesByName("tc-muted", false);
        if (!mutedRoles.isEmpty()) {
            var first = mutedRoles.stream().filter((role) -> {
                var perms = role.getPermissions();
                return !(perms.contains(Permission.VOICE_SPEAK) || perms.contains(Permission.MESSAGE_WRITE));
            }).findFirst();
            if (first.isPresent()) {
                muted = first.get();
            }
        }
        return muted;
    }

}
