package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.concurrent.TimeUnit;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class RoleCommand implements ICommand {

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;

        if(Permissions.getPermissionLevel(member) >= 5) {
            if (args.length >= 2 && event.getCommand().getJoinedArgs().matches("(?i)(add|remove) <@!?\\d+>( .+)?") && !event.getMessage().getMentionedMembers().isEmpty()) {
                var target = event.getMessage().getMentionedMembers().get(0);
                String role = event.getCommand().getJoinedArgs(2);
                if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                    sendMessage(channel, Type.ERROR, String.format("%s doesn't have permissions to manage roles!", event.getGuild().getSelfMember().getEffectiveName())).queue((msg) ->
                            msg.delete().queueAfter(7, TimeUnit.SECONDS));
                } else if (event.getGuild().getRolesByName(role, false).isEmpty()){
                    sendMessage(channel, Type.ERROR, String.format("Role `%s` doesn't exist!", role)).queue((msg) -> msg.delete().queueAfter(7, TimeUnit.SECONDS));
                    sendMessage(channel, Type.QUESTION, "Do you wish to create the Role `" + role + "`?").queue(
                            (msg) -> Reactions.newYesNoMenu(member.getUser(), msg,
                                    (reaction) -> {
                                        msg.delete().queue();
                                        var controller = event.getGuild().getController();
                                        controller.createRole().setName(role).queue((newRole) -> {
                                            sendMessage(channel, Type.SUCCESS, "Role created! Do you want to add the role?").queue(
                                                    (message) -> Reactions.newYesNoMenu(event.getAuthor(), message, (emoji) -> controller.addSingleRoleToMember(event.getMessage().getMentionedMembers().get(0), newRole).queue()));
                                        }, defaultFailure(channel));
                                    }));
                } else if (args[0].equalsIgnoreCase("add")) {
                        event.getGuild().getController().addSingleRoleToMember(event.getMessage().getMentionedMembers().get(0), event.getGuild().getRolesByName(role, true).get(0)).queue();
                        sendMessage(channel, Type.SUCCESS, String.format("Successfully granted Role `%s` to `%#s` by %s",  role, target.getUser(), member.getAsMention()), true).queue();
                } else if (args[0].equalsIgnoreCase("remove")) {
                        event.getGuild().getController().removeSingleRoleFromMember(event.getMessage().getMentionedMembers().get(0), event.getGuild().getRolesByName(role, true).get(0)).queue();
                        sendMessage(channel, Type.SUCCESS, String.format("Successfully removed Role `%s` from `%#s` by %s",  role, target.getUser(), member.getAsMention()), true).queue();
                }
            } else {
                sendMessage(channel, Type.INFO, "Wrong usage. Command info:\n\n" + this.info(member)).queue();
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    @Override
    public String info(Member member) {
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        String ret = permLevel < 2
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `5`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Grants or removes a specific role from a member.\n\n**Usage**: `%s[role|manage] [add|remove] @Member <role>`\n\n**Permission level**: `5`", prefix);
        return ret;
    }
}
