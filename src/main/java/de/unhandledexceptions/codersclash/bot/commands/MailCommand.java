package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class MailCommand implements ICommand {

    private Database database;
    private SearchCommand searchCommand;
    private Pattern topicPattern;

    public MailCommand(Database database, SearchCommand searchCommand) {
        this.database = database;
        this.searchCommand = searchCommand;
        this.topicPattern = Pattern.compile("##.+##");
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length > 1) {
                var shardManager = event.getJDA().asBot().getShardManager();
                Guild guild;
                if (args[0].matches("\\d{1,18}") && (guild = shardManager.getGuildById(Long.parseLong(args[0]))) != null) {
                    Long mailChannelId;
                    if ((mailChannelId = database.getMailChannel(guild)) != null && mailChannelId != 0) {
                        var mailChannel = guild.getTextChannelById(mailChannelId);
                        if (mailChannel != null) {
                            var builder = new EmbedBuilder();
                            var author = event.getAuthor();
                            String message = event.getCommand().getJoinedArgs(1);
                            String topic = "No Topic";
                            Matcher matcher = topicPattern.matcher(event.getCommand().getJoinedArgs());
                            if (matcher.find()) {
                                String found = matcher.group();
                                message = message.replaceFirst(found, "");
                                topic = found.replaceAll("##", "");
                            }
                            builder.setTitle("Via \"" + event.getGuild().getName() + "\" (" + event.getGuild().getId() + ")")
                                    .setAuthor(String.format("%#s", author), null, author.getEffectiveAvatarUrl())
                                    .setColor(member.getColor())
                                    .setFooter("Inbox", null)
                                    .setTimestamp(Instant.now())
                                    .addField(topic, message, false);
                            mailChannel.sendMessage(builder.build()).queue(
                                    (msg) -> sendMessage(channel, Type.SUCCESS, "Mail sent!").queue(Messages::deleteAfterFiveSec),
                                    Messages.defaultFailure(channel));
                        } else {
                            sendMessage(channel, Type.ERROR, "I can't send a mail to `" + guild.getName() + "`, it seems like they deleted their mail channel!").queue();
                        }
                    } else {
                        sendMessage(channel, Type.ERROR, "The guild `" + guild.getName() + "` hasn't set a mail channel! Contact their administrators.").queue();
                    }
                } else {
                    Reactions.newYesNoMenu(event.getAuthor(), channel, "No valid id detected. Do you want to try searching the guild by name?", (msg) -> {
                        msg.delete().queue();
                        sendMessage(channel, Type.DEFAULT, "Please type in the name of the guild you are looking for!"
                                + " If you find the guild you are looking for in the results, copy their id and try sending the mail again like this:\n"
                                + "`" + Bot.getPrefix(event.getGuild().getIdLong()) + "mail <id> <message>`").queue();
                        Reactions.newMessageWaiter(event.getAuthor(), channel, 30, (m) -> {
                            List<String> preparedArgs = new ArrayList<>();
                            preparedArgs.add("guild");
                            preparedArgs.addAll(Arrays.asList(m.getContentRaw().split("\\s+")));
                            searchCommand.onCommand(
                                    new CommandEvent(event.getJDA(), event.getResponseNumber(), event.getMessage(), event.getCommand()),
                                    member, channel, preparedArgs.toArray(new String[preparedArgs.size()])
                            );
                        });
                    });
                }
            } else {
                sendMessage(channel, Type.INFO, "Wrong usage. Command info\n\n" + this.info(member)).queue();
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    // TODO
    @Override
    public String info(Member member) {
        return " ";
    }
}
