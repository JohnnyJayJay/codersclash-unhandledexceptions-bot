package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import de.unhandledexceptions.codersclash.bot.util.Roles;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.Arrays;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

public class Permissions implements ICommand {

    private CommandSettings settings;
    private static Database database;

    public Permissions(CommandSettings settings, Database database) {
        this.settings = settings;
        this.database = database;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        var guild = event.getGuild();
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES, Permission.MESSAGE_WRITE))
            return;

        var role = Roles.getTryCatchRole(event.getGuild());
        if (role == null) {
            guild.getController().createRole().setName("try-catch").setColor(Color.GREEN).queue(
                    (newRole) -> sendMessage(channel, Type.INFO, "A role \"try-catch\" has been created. Only members of this role can manage permissions concerning commands of " +
                            "try-catch-bot. Be careful, members with this role have full control about try-catch-permissions!").queue(), Messages.defaultFailure(channel));
        } else if (!member.getRoles().contains(role)) {
            sendMessage(channel, Type.ERROR, "You do not have permission to manage try-catch-permissions. Request help for more. " + member.getAsMention()).queue();
        } else if (!event.getCommand().getJoinedArgs().matches("((<@!?\\d+>)|(<@&\\d+>)) [0-5]") ||
                event.getMessage().getMentions(Message.MentionType.ROLE, Message.MentionType.USER).isEmpty()) {
            sendMessage(channel, Type.INFO, "Wrong usage. Command info: \n\n" + info(member)).queue();
        } else {
            short level = Short.parseShort(args[1]);
            if (event.getMessage().getMentionedMembers().isEmpty()) {
                var targetRole = event.getMessage().getMentionedRoles().get(0);
                guild.getMemberCache().stream().filter((m) -> m.getRoles().contains(targetRole)).forEach((m) -> database.changePermissionLevel(m, level));
                sendMessage(channel, Type.SUCCESS, String.format("Permission level of role `%s` successfully set to `%d`.", targetRole.getName(), level)).queue();
            } else {
                var targetMember = guild.getMember(event.getMessage().getMentionedUsers().get(0));
                database.changePermissionLevel(targetMember, level);
                sendMessage(channel, Type.SUCCESS, String.format("Permission level of member `%#s` successfully set to `%d`.", targetMember.getUser(), level)).queue();
            }
        }
    }
    
    @Override
    public String info(Member member) {
        String prefix = settings.getPrefix(member.getGuild().getIdLong());
        String[] prefixArr = new String[10];
        Arrays.fill(prefixArr, prefix);
        String ret = member.getRoles().stream().map(Role::getName).anyMatch((role) -> role.equals("try-catch"))
                ? String.format("Manage try-catch permissions and configure the different permission levels.\n```\nLevel 0: %shelp\nLevel 1: %suserinfo\nLevel 2: " +
                "%sblock\nLevel 3: %smute and %sreport\nLevel 4: %svote and %smail\nLevel 5: %ssettings and %srole```\n\nUsage: `%s[permission|perms|perm] [<@Member>|<@Role>] " +
                "<level>` (level may be 0-5)\n\nTo execute this command, the member needs to have a role named \"try-catch\".", prefixArr)
                : "This command is not available for you.\n **Permissions needed**: `try-catch` role.";
        return ret;
    }

    public static int getPermissionLevel(Member member) {
        return database.getPermissionLevel(member);
    }

    public static int getVotePermissionLevel()
    {
        return 4;
    }
}
