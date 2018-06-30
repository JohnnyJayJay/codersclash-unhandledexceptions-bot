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

    public Permissions(CommandSettings settings) {
        this.settings = settings;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        var guild = event.getGuild();
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))
            return;

        if (guild.getRolesByName("try-catch", false).isEmpty()) {
            guild.getController().createRole().setName("try-catch").setColor(Color.GREEN).queue(
                    (role) -> sendMessage(channel, Type.INFO, "A role \"try-catch\" has been created. Only members of this role can manage permissions concerning commands of " +
                            "try-catch-bot.").queue(), (throwable) -> sendMessage(channel, Type.WARNING, "Something went wrong while creating the role\n"
                            + throwable.getMessage()).queue());
        } else if (!member.getRoles().contains(guild.getRolesByName("try-catch", false).get(0))) {
            sendMessage(channel, Type.ERROR, "You do not have permission to manage try-catch-permissions, " + member.getAsMention()).queue();
        } else if (!String.join(" ", args).matches("set <@\\d+> \\d$") || event.getMessage().getMentionedMembers().isEmpty()) {
            sendMessage(channel, Type.INFO, String.format("Correct usage: `%spermission set <@member> <level>`", settings.getPrefix() /*Auch hier: custom prefixes beachten*/)).queue();
        } else {
            var target = event.getMessage().getMentionedMembers().get(0);
            int level = Integer.parseInt(args[2]);
            // TODO Datenbankanbindung schreiben: Werte werden aktualisiert
            sendMessage(channel, Type.SUCCESS, String.format("Permission level of `%s` successfully set to %s.", target.getEffectiveName(), args[2])).queue();
        }
    }

    @Override
    public String info(Guild guild) {
        return String.format("Is used to manage try-catch permissions and configure the different permission levels.\nUsage: `%spermission [set] <member> <level>`\nTo execute " +
                "this command, the member needs to have a role named \"try-catch\".", settings.getPrefix(guild.getIdLong()));
    }

    // TODO Datenbankanbindung hinzufügen: Werte werden hier ausgelesen
    public static int getPermissionLevel(Member member) {
        return 0;
    }
}
