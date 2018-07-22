package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import de.unhandledexceptions.codersclash.bot.util.Roles;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static java.lang.String.format;

public class Permissions implements ICommand {

    private CommandSettings settings;
    private static Database database;

    public Permissions(CommandSettings settings, Database database) {
        this.settings = settings;
        Permissions.database = database;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        var guild = event.getGuild();
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES, Permission.MESSAGE_WRITE))
            return;

        Roles.getTryCatchRole(event.getGuild(), (role) -> {
            if (member.getRoles().contains(role)) {
                if (!event.getCommand().getJoinedArgs().matches("((<@!?\\d+>)|(<@&\\d+>)) [0-5]") ||
                        event.getMessage().getMentions(Message.MentionType.ROLE, Message.MentionType.USER).isEmpty()) {
                    sendMessage(channel, Type.INFO, "Wrong usage. Command info: \n\n" + info(member)).queue();
                } else {
                    short level = Short.parseShort(args[1]);
                    if (event.getMessage().getMentionedMembers().isEmpty()) {
                        var targetRole = event.getMessage().getMentionedRoles().get(0);
                        guild.getMemberCache().stream().filter((m) -> m.getRoles().contains(targetRole)).forEach((m) -> database.changePermissionLevel(m, level));
                        sendMessage(channel, Type.SUCCESS, format("Permission level of role `%s` successfully set to `%d`.", targetRole.getName(), level)).queue();
                    } else {
                        var targetMember = guild.getMember(event.getMessage().getMentionedUsers().get(0));
                        database.changePermissionLevel(targetMember, level);
                        sendMessage(channel, Type.SUCCESS, format("Permission level of member `%#s` successfully set to `%d`.", targetMember.getUser(), level)).queue();
                    }
                }
            } else {
                sendMessage(channel, Type.ERROR, format("You do not have permission to manage %s-permissions. Request help for more. " + member.getAsMention(), Bot.getBotName())).queue();
            }
        }, (v) -> {});
    }
    
    @Override
    public String info(Member member) {
        String prefix = settings.getPrefix(member.getGuild().getIdLong());
        String ret = member.getRoles().stream().map(Role::getName).anyMatch((role) -> role.equals(Bot.getBotName() + "-perms"))
                ? format("Manage %s-permissions and configure the different permission levels.\n```\nLevel 0: %shelp and %sttt\nLevel 1: %sprofile, %ssearch, %sinvite, %sxp, %sscoreboard\nLevel 2: " +
                "%sblock\nLevel 3: %smute and %sreport\nLevel 4: %svote, %smail, %slink, %sclear\nLevel 5: %ssettings, %srole, %smuteguild```\n\nUsage: `%s[permission|perms|perm] [<@Member>|<@Role>] " +
                "<level>` (level may be 0-5)\n\nTo execute this command, you need to have a role named `%s-perms`.", Bot.getBotName(),
                prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, prefix, Bot.getBotName())
                : format("This command is not available for you.\n **Role needed**: `%s-perms`", Bot.getBotName());
        return ret;
    }

    public static int getPermissionLevel(Member member) {
        return database.getPermissionLevel(member);
    }
}
