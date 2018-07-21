package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.ListDisplay;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author TheRealYann
 */

public class RoleCommand implements ICommand {
    //TODO if role above bot
    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length >= 2 && event.getCommand().getJoinedArgs().matches("(?i)(add|remove) <@!?\\d+>( .+)?") && !event.getMessage().getMentionedMembers().isEmpty()) {
                var target = event.getMessage().getMentionedMembers().get(0);
                String role = event.getCommand().getJoinedArgs(2);
                if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                    sendMessage(channel, Type.ERROR, format("%s doesn't have permissions to manage roles!", event.getGuild().getSelfMember().getEffectiveName())).queue((msg) ->
                            msg.delete().queueAfter(7, TimeUnit.SECONDS));
                } else if (event.getGuild().getRolesByName(role, false).isEmpty()) {
                    sendMessage(channel, Type.ERROR, format("Role `%s` doesn't exist!", role)).queue((msg) -> msg.delete().queueAfter(7, TimeUnit.SECONDS));
                    Reactions.newYesNoMenu(member.getUser(), channel, "Do you wish to create the Role `" + role + "`?",
                            (msg) -> {
                                msg.delete().queue();
                                var controller = event.getGuild().getController();
                                controller.createRole().setName(role).queue((newRole) -> {
                                    sendMessage(channel, Type.SUCCESS, "Role created! Do you want to add the role?").queue(
                                            (message) -> Reactions.newYesNoMenu(event.getAuthor(), message, (emoji) -> controller.addSingleRoleToMember(event.getMessage().getMentionedMembers().get(0), newRole).queue()));
                                }, defaultFailure(channel));
                            });
                } else if (args[0].equalsIgnoreCase("add")) {
                    List<Role> roles = event.getGuild().getRolesByName(role, true);
                    if (roles.size() > 1) {
                        Reactions.newYesNoMenu(member.getUser(), channel, "Multiple roles with this name detected. Do you want to add all of them?\n"
                                + Reactions.YES_EMOTE + " Yes, add all of them.\n"
                                + Reactions.NO_EMOTE + " No, let me select one.", (msg) -> {
                            msg.delete().queue();
                            event.getGuild().getController().addRolesToMember(target, roles).queue(
                                    (v) -> sendMessage(channel, Type.SUCCESS, format("Successfully granted `%#s` provided roles. Executor: %s", target.getUser(), member), true).queue(), Messages.defaultFailure(channel));
                        }, (msg) -> {
                            msg.delete().queue();
                            sendMessage(channel, Type.DEFAULT, "Loading roles...").queue((msg2) -> {
                                ListDisplay.displayListSelection(roles.stream().map((role1) -> format("%d: %s (%d)", roles.indexOf(role1) + 1, role1.getAsMention(), role1.getIdLong())).collect(Collectors.toList()),
                                        msg2, member.getUser(), 5, (selected) -> {
                                            msg2.delete().queue();
                                            event.getGuild().getController().addSingleRoleToMember(target, event.getGuild().getRoleById(selected.replaceAll("((\\d+: )|([\\(\\)])|(<@&\\d+> ))", ""))).queue(
                                                    (v) -> sendMessage(channel, Type.SUCCESS, format("Successfully gave Role `%s` to `%#s`. Executor: %s", role, target.getUser(), member), true).queue(), Messages.defaultFailure(channel));
                                        }, (v) -> {
                                            msg.delete().queue();
                                            msg2.delete().queue();
                                        });
                            });
                        });
                    } else {
                        event.getGuild().getController().addSingleRoleToMember(target, roles.get(0)).queue(
                                (v) -> sendMessage(channel, Type.SUCCESS, format("Successfully gave Role `%s` to `%#s`. Executor: %s", role, target.getUser(), member), true).queue(), Messages.defaultFailure(channel));
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    List<Role> roles = event.getGuild().getRolesByName(role, true);
                    if (roles.size() > 1) {
                        Reactions.newYesNoMenu(member.getUser(), channel, "Multiple roles with this name detected. Do you want to remove all of them?\n"
                                + Reactions.YES_EMOTE + " Yes, remove all of them.\n"
                                + Reactions.NO_EMOTE + " No, let me select one.", (msg) -> {
                            msg.delete().queue();
                            event.getGuild().getController().removeRolesFromMember(target, roles).queue(
                                    (v) -> sendMessage(channel, Type.SUCCESS, format("Successfully removed `%#s` provided roles. Executor: %s", target.getUser(), member), true).queue(), Messages.defaultFailure(channel));
                        }, (msg) -> {
                            msg.delete().queue();
                            sendMessage(channel, Type.DEFAULT, "Loading roles...").queue((msg2) -> {
                                ListDisplay.displayListSelection(roles.stream().map((role1) -> format("%d: %s (%d)", roles.indexOf(role1) + 1, role1.getAsMention(), role1.getIdLong())).collect(Collectors.toList()),
                                        msg2, member.getUser(), 5, (selected) -> {
                                            msg2.delete().queue();
                                            event.getGuild().getController().removeSingleRoleFromMember(target, event.getGuild().getRoleById(selected.replaceAll("((\\d+: )|([\\(\\)])|(<@&\\d+> ))", ""))).queue(
                                                    (v) -> sendMessage(channel, Type.SUCCESS, format("Successfully removed Role `%s` to `%#s`. Executor: %s", role, target.getUser(), member), true).queue(), Messages.defaultFailure(channel));
                                        }, (v) -> {
                                            msg.delete().queue();
                                            msg2.delete().queue();
                                        });
                            });
                        });
                    } else {
                        event.getGuild().getController().removeSingleRoleFromMember(target, event.getGuild().getRolesByName(role, true).get(0)).queue(
                                (v) -> sendMessage(channel, Type.SUCCESS, format("Successfully removed Role `%s` to `%#s`. Executor: %s", role, target.getUser(), member), true).queue(), Messages.defaultFailure(channel));
                    }
                } else {
                    wrongUsageMessage(channel, member, this);
                }
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
                : format("**Description**: Grants or removes a specific role from a member.\n\n**Usage**: `%srole [add|remove] @Member <role>`\n\n**Permission level**: `5`", prefix);
        return ret;
    }
}
