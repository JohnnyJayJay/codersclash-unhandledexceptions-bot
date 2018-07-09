package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
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

        if (guild.getRolesByName("try-catch", false).isEmpty()) {
            guild.getController().createRole().setName("try-catch").setColor(Color.GREEN).queue(
                    (role) -> sendMessage(channel, Type.INFO, "A role \"try-catch\" has been created. Only members of this role can manage permissions concerning commands of " +
                            "try-catch-bot. Be careful, members with this role have full control about try-catch-permissions!").queue(), Messages.defaultFailure(channel));
        } else if (!member.getRoles().contains(guild.getRolesByName("try-catch", false).get(0))) {
            sendMessage(channel, Type.ERROR, "You do not have permission to manage try-catch-permissions. Request help for more. " + member.getAsMention()).queue();
        } else if (!event.getCommand().getJoinedArgs().matches("<@\\d+> [0-5]") || event.getMessage().getMentionedMembers().isEmpty()) {
            sendMessage(channel, Type.INFO, "Wrong usage. Command info: \n\n" + info(member)).queue();
        } else {
            var target = event.getMessage().getMentionedMembers().get(0);
            short level = Short.parseShort(args[1]);
            database.createMemberIfNotExists(target.getGuild().getIdLong(), target.getUser().getIdLong());
            database.changePermissionLevel(target, level);
            sendMessage(channel, Type.SUCCESS, String.format("Permission level of `%s` successfully set to %s.", target.getEffectiveName(), args[1])).queue();
        }
    }
    
    @Override
    public String info(Member member) {
        String prefix = settings.getPrefix(member.getGuild().getIdLong());
        String[] prefixArr = new String[9];
        Arrays.fill(prefixArr, prefix);
        String ret = member.getRoles().stream().map(Role::getName).anyMatch((role) -> role.equals("try-catch"))
                ? String.format("Manage try-catch permissions and configure the different permission levels.\n```\nLevel 0: %shelp\nLevel 1: %suserinfo\nLevel 2: " +
                "%sblock\nLevel 3: %smute and %sreport\nLevel 4: %svote and %smail\nLevel 5: %ssettings```\n\nUsage: `%s[permission|perms|perm] <member> " +
                "<level>` (level may be 0-5)\n\nTo execute this command, the member needs to have a role named \"try-catch\".", prefixArr)
                : "This command is not available for you.\n **Permissions needed**: `try-catch` role.";
        return ret;
    }

    public static int getPermissionLevel(Member member) {
        database.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return database.getPermissionLevel(member);
    }
}
