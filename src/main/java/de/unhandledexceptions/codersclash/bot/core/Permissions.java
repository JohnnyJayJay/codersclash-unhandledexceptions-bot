package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.Color;

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
                            "try-catch-bot.").queue(), (throwable) -> sendMessage(channel, Type.WARNING, "Something went wrong while creating the role\n"
                            + throwable.getMessage()).queue());
        } else if (!member.getRoles().contains(guild.getRolesByName("try-catch", false).get(0))) {
            sendMessage(channel, Type.ERROR, "You do not have permission to manage try-catch-permissions, " + member.getAsMention()).queue();
        } else if (!String.join(" ", args).matches("set <@\\d+> [0-5]") || event.getMessage().getMentionedMembers().isEmpty()) {
            sendMessage(channel, Type.INFO, String.format("Correct usage: `%spermission set <@member> <level>` (levels: 0-5)",
                    settings.getPrefix(event.getGuild().getIdLong()))).queue();
        } else {
            var target = event.getMessage().getMentionedMembers().get(0);
            short level = Short.parseShort(args[2]);
            database.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
            database.changePermissionLevel(target, level);
            sendMessage(channel, Type.SUCCESS, String.format("Permission level of `%s` successfully set to %s.", target.getEffectiveName(), args[2])).queue();
        }
    }

    // FIXME Format Exception - why?
    @Override
    public String info(Member member) {
        return String.format("Is used to manage try-catch permissions and configure the different permission levels.\n\nLevel 0: %shelp\nLevel 2: %suserinfo\nLevel 3: " +
                "%sblock\nLevel 3: %smute and %sreport\nLevel 4: %svote and %smail\nLevel 5: %ssettings\n\nUsage: `%s[permission|perms|perm] [set] <member> " +
                "<level>`\n\nTo execute this command, the member needs to have a role named \"try-catch\".", settings.getPrefix(member.getGuild().getIdLong()));
    }

    public static int getPermissionLevel(Member member) {
        database.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return database.getPermissionLevel(member);
    }
}
