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
            if (mutedRoles.get(0).hasPermission(Permission.MESSAGE_WRITE) && guild.getSelfMember().canInteract(mutedRoles.get(0))) {
                mutedRoles.get(0).getManager().revokePermissions(Permission.MESSAGE_WRITE).queue();
                muted = mutedRoles.get(0);
            }
        }
        return muted;
    }

}
