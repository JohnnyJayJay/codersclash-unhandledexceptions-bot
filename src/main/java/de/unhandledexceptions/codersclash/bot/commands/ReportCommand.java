package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
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
            // TODO neue CommandAPI
            if (args.length >= 2 && String.join(" ", args).matches("(<@\\d+> .+)|((get|remove) <@\\d+>( (10|[1-9]))?)") && !event.getMessage().getMentionedMembers().isEmpty()) {
                var target = event.getMessage().getMentionedMembers().get(0);
                var reportList = database.getReports(target);
                database.createMemberIfNotExists(target.getGuild().getIdLong(), target.getUser().getIdLong());
                if (args[0].matches("<@\\d+>")) {
                    String reason = String.join(" ", Arrays.asList(args).subList(1, args.length));
                    // TODO Überprüfen, ob er gebannt werden muss
                    if (database.addReport(target, reason)) {
                        sendMessage(channel, Type.SUCCESS, String.format("Successfully reported `%#s` for ```\n%s``` by %s", target.getUser(), reason, member.getAsMention())).queue();
                        if (reportList.size() >= database.getReportsUntilBan(event.getGuild())) {
                            event.getGuild().getController().ban(target, 0, String.format("User `%#s` had too many reports and was therefore banned.", target.getUser()))
                                    .queue(null, Messages.defaultFailure(channel));
                        }
                    } else {
                        sendMessage(channel, Type.WARNING, "This member already has 10 reports!").queue();
                    }
                } else {
                    short index = -1;
                    if (args.length == 3)
                        index = Short.parseShort(args[2]);
                    if (args[0].equalsIgnoreCase("get")) {
                        if (index != -1) {
                            sendMessage(channel, Type.INFO, String.format("Report `%d` of Member `%#s`:\n```\n%s```", index, target.getUser(), reportList.get(index - 1)))
                                    .queue(null, Messages.defaultFailure(channel));
                        } else {
                            var builder = new StringBuilder();
                            for (int i = 1; i <= reportList.size(); i++)
                                builder.append("Report " + i + ": " + reportList.get(i -1) + "\n");
                            sendMessage(channel, Type.INFO, String.format("Reports of Member `%#s`:\n```\n%s```", target.getUser(), builder.toString()))
                                    .queue(null, Messages.defaultFailure(channel));
                        }
                    } else if (args[0].equalsIgnoreCase("remove")) {
                        if (index != -1) {
                            database.removeReport(target, index);
                            sendMessage(channel, Type.SUCCESS, String.format("Successfully removed report `%d` from `%#s`.", index, target.getUser())).queue();
                        } else {
                            database.removeAllReports(target);
                            sendMessage(channel, Type.SUCCESS, String.format("Successfully removed every report from member `%#s`.", target.getUser())).queue();
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

}
