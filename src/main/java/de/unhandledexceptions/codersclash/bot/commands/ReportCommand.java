package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages.*;
import de.unhandledexceptions.codersclash.bot.util.Regex;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.List;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author Johnny_JayJay
 */

public class ReportCommand implements ICommand {

    private static Database database;

    public ReportCommand(Database database) {
        ReportCommand.database = database;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;

        if (args.length >= 2 && event.getCommand().getJoinedArgs().matches("(?i)(<@!?\\d+> .+)|((get|remove) <@!?\\d+>( (10|[1-9]))?)") && !event.getMessage().getMentionedMembers().isEmpty()) {
            var target = event.getMessage().getMentionedMembers().get(0);
            List<String> reportList = database.getReports(target);
            if (args[0].matches(Regex.MEMBER_MENTION)) {
                String reason = String.join(" ", Arrays.asList(args).subList(1, args.length));
                if (database.addReport(target, reason)) {
                    sendMessage(channel, Type.SUCCESS, format("Successfully reported `%#s` for ```\n%s``` by %s", target.getUser(), reason, member.getAsMention()))
                            .queue(null, defaultFailure(channel));
                    if (!Bot.getBotOwners().contains(target.getUser().getIdLong()) && reportList.size() + 1 >= database.getReportsUntilBan(event.getGuild())
                            && event.getGuild().getSelfMember().canInteract(target)) {
                        event.getGuild().getController().ban(target, 0, format("User `%#s` had too many reports and was therefore banned.", target.getUser()))
                                .queue(null, defaultFailure(channel));
                    }
                } else {
                    sendMessage(channel, Type.WARNING, "This member already has 10 reports!").queue();
                }
            } else if (reportList.isEmpty()) {
                sendMessage(channel, Type.INFO, format("Member `%#s` does not have any reports!", target.getUser())).queue();
            } else {
                short index = 0;
                if (args.length == 3)
                    index = Short.parseShort(args[2]);
                if (args[0].equalsIgnoreCase("get")) {
                    showReports(channel, target, reportList, index);
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (index != 0) {
                        if (index <= reportList.size()) {
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
        } else if (args.length < 2) {
            List<String> reportList = database.getReports(member);
            if (reportList.isEmpty()) {
                sendMessage(channel, Type.INFO, "You do not have any reports!").queue();
                return;
            }
            short index = 0;
            if (args.length == 1) {
                if (args[0].matches(Regex.ONE_TO_TEN))
                    index = Short.parseShort(args[0]);
                else
                    sendMessage(channel, Type.ERROR, args[0] + " is not a valid number! You can only get report 1-10.").queue();
            }
            showReports(channel, member, reportList, index);
        } else {
            wrongUsageMessage(channel, member, this);
        }
    }

    private void showReports(TextChannel channel, Member target, List<String> reportList, short index) {
        if (index != 0) {
            if (index <= reportList.size()) {
                sendMessage(channel, Type.INFO, format("Report `%d` of Member `%#s`:\n```\n%s```", index, target.getUser(), reportList.get(index - 1)))
                        .queue(null, defaultFailure(channel));
            } else {
                sendMessage(channel, Type.WARNING, format("Member `%#s` does not have `%d` reports!\nYou may use `%sreport get` %s instead!",
                        target.getUser(), index, Bot.getPrefix(target.getGuild().getIdLong()), target.getAsMention())).queue();
            }
        } else {
            var builder = new StringBuilder();
            for (int i = 1; i <= reportList.size(); i++)
                builder.append("Report " + i + ": " + reportList.get(i -1) + "\n");
            sendMessage(channel, Type.INFO, format("Reports of Member `%#s`:\n```\n%s```", target.getUser(), builder.toString()))
                    .queue(null, defaultFailure(channel));
        }
    }

    public static int getReportCount(Member member) {
        return database.getReports(member).size();
    }

    @Override
    public String info(Member member) {
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        String retNoBan = permLevel < 3
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `3`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Reports a given member. A member will not be banned after a certain amount of report. To change this, make use of the settings command.\n\n" +
                        "**Usage**: `%s[report|rep] @Member <reason>` to *report* \n\t\t\t  `%s[rep|report] [get|remove] @Member <index>` to *manage*\n\n**Permission " +
                        "level**: `3`",
                prefix, prefix);
        String retWithBan = permLevel < 3
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `3`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Reports a given member. After `%d` reports, a member will be banned. To change this, make use of the settings command.\n\n" +
                        "**Usage**: `%s[report|rep] @Member <reason>` to *report* \n\t\t\t  `%s[rep|report] [get|remove] @Member <index>` to *manage*\n\n**Permission " +
                        "level**: `3`",
                database.getReportsUntilBan(member.getGuild()), prefix, prefix);
        return (database.getReportsUntilBan(member.getGuild()) == (11)) ? retNoBan : retWithBan;
    }
}
