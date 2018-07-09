package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;

import java.util.Arrays;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static java.lang.String.format;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class ReportCommand implements ICommand {

    private static Logger logger = Logging.getLogger();
    private Database database;

    public ReportCommand(Database database) {
        this.database = database;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;
        if (Permissions.getPermissionLevel(member) >= 3) {
            if (args.length >= 2 && event.getCommand().getJoinedArgs().matches("(<@\\d+> .+)|((get|remove) <@\\d+>( (10|[1-9]))?)") && !event.getMessage().getMentionedMembers().isEmpty()) {
                var target = event.getMessage().getMentionedMembers().get(0);
                var reportList = database.getReports(target);
                if (args[0].matches("<@\\d+>")) {
                    String reason = String.join(" ", Arrays.asList(args).subList(1, args.length));
                    if (database.addReport(target, reason)) {
                        sendMessage(channel, Type.SUCCESS, format("Successfully reported `%#s` for ```\n%s``` by %s", target.getUser(), reason, member.getAsMention())).queue();
                        if (reportList.size() >= database.getReportsUntilBan(event.getGuild()) && event.getGuild().getSelfMember().canInteract(target)) {
                            event.getGuild().getController().ban(target, 0, format("User `%#s` had too many reports and was therefore banned.", target.getUser()))
                                    .queue(null, Messages.defaultFailure(channel));
                        }
                    } else {
                        sendMessage(channel, Type.WARNING, "This member already has 10 reports!").queue();
                    }
                } else {
                    short index = 0;
                    if (args.length == 3)
                        index = Short.parseShort(args[2]);
                    if (args[0].equalsIgnoreCase("get")) {
                        if (index != 0) {
                            if (index < reportList.size()) {
                                sendMessage(channel, Type.INFO, format("Report `%d` of Member `%#s`:\n```\n%s```", index, target.getUser(), reportList.get(index - 1)))
                                        .queue(null, Messages.defaultFailure(channel));
                            } else {
                                sendMessage(channel, Type.WARNING, format("Member `%#s` does not have `%d` reports!\nYou may use `%sreport get` %s instead!",
                                        target.getUser(), index, Bot.getPrefix(event.getGuild().getIdLong()), target.getAsMention())).queue();
                            }
                        } else if (reportList.isEmpty()) {
                            sendMessage(channel, Type.INFO, format("Member `%#s` does not have any reports!", target.getUser())).queue();
                        } else {
                            var builder = new StringBuilder();
                            for (int i = 1; i <= reportList.size(); i++)
                                builder.append("Report " + i + ": " + reportList.get(i -1) + "\n");
                            sendMessage(channel, Type.INFO, format("Reports of Member `%#s`:\n```\n%s```", target.getUser(), builder.toString()))
                                    .queue(null, Messages.defaultFailure(channel));
                        }
                    } else if (args[0].equalsIgnoreCase("remove")) {
                        if (index != 0) {
                            if (index < reportList.size()) {
                                database.removeReport(target, index);
                                sendMessage(channel, Type.SUCCESS, format("Successfully removed report `%d` from `%#s`.", index, target.getUser())).queue();
                            } else {
                                sendMessage(channel, Type.WARNING, format("Member `%#s` does not have `%d` reports!\nYou may use `%sreport remove` %s to remove all reports or " +
                                        "get a list of reports with `%sreport get` %s.", target.getUser(), index, Bot.getPrefix(event.getGuild().getIdLong()),
                                        target.getAsMention(), Bot.getPrefix(event.getGuild().getIdLong()), target.getAsMention())).queue();
                            }
                        } else {
                            database.removeAllReports(target);
                            sendMessage(channel, Type.SUCCESS, format("Successfully removed every report from member `%#s`.", target.getUser())).queue();
                        }
                    }
                }
            } else {
                sendMessage(channel, Type.INFO, "Wrong usage. Command info:\n\n" + this.info(member)).queue();
            }
        } else {
            Messages.noPermissionsMessage(channel, member);
        }
    }

    @Override
    public String info(Member member) {
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        String ret = permLevel < 3
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `3`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Reports a given member. After `%d` reports, a member will be banned. To change this, make use of the settings command.\n\n" +
                "**Usage**: `%s[report|rep] @Member <reason>` to *report* \n\t\t\t\t`%s[rep|report] [get|remove] @Member <index>` to *manage*\n\n**Permission " +
                        "level**: `3`",
                database.getReportsUntilBan(member.getGuild()), prefix, prefix);
        return ret;
    }
}
