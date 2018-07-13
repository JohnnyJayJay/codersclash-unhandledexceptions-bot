package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class MailCommand implements ICommand {

    private Database database;
    private EmbedBuilder builder;

    public MailCommand(Database database) {
        this.database = database;
        this.builder = new EmbedBuilder();
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length > 1) {
                var shardManager = event.getJDA().asBot().getShardManager();
                Guild guild;
                builder.clear();
                if (args[0].matches("\\d{1,18}") && (guild = shardManager.getGuildById(Long.parseLong(args[0]))) != null) {
                    Long mailChannelId;
                    if ((mailChannelId = database.getMailChannel(guild)) != null && mailChannelId != 0) {
                        var mailChannel = guild.getTextChannelById(mailChannelId);
                        if (mailChannel != null) {
                            var builder = new EmbedBuilder();
                            var author = event.getAuthor();
                            builder.setTitle("Via \"" + event.getGuild().getName() + "\" (" + event.getGuild().getId() + ")")
                                    .setAuthor(String.format("%#s", author), null, author.getEffectiveAvatarUrl())
                                    .setColor(member.getColor())
                                    .setFooter("Inbox", null)
                                    .setTimestamp(Instant.now())
                                    .addField(args.length > 4 ? String.join(" ", event.getCommand().getArgsAsList().subList(1, 4)) + "..."
                                            : "Message", event.getCommand().getJoinedArgs(1), false);
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
                    sendMessage(channel, Type.ERROR, "No valid guild id detected. Do you want to try searching the guild by name?").queue((msg) -> Reactions.newYesNoMenu(msg, event.getAuthor(), (v) -> {
                        v.delete().queue();
                        sendMessage(channel, Type.DEFAULT, "Please type in the name of the guild you are looking for!").queue();
                        Reactions.newMessageWaiter(event.getAuthor(), channel, (m) -> {
                            String name = m.getContentRaw();
                            List<Guild> guilds = new ArrayList<>();
                            shardManager.getShardCache().forEach((jda) -> guilds.addAll(jda.getGuildsByName(name, true)));
                            if (guilds.isEmpty()) {
                                sendMessage(channel, Type.ERROR, "No results found :(\nMaybe I'm not on the guild you are looking for?").queue();
                            } else {
                                var stringBuilder = new StringBuilder().append("```\n");
                                int i = 1;
                                for (var g : guilds) {
                                    stringBuilder.append(String.format("%d: %s (%d) - Owner: %#s\n", i, g.getName(), g.getIdLong(), g.getOwner().getUser()));
                                    i++;
                                }
                                stringBuilder.append("```");
                                int finalI = i - 1;
                                sendMessage(channel, Type.SUCCESS, "Results:\n" + stringBuilder.toString()).queue((message) -> {
                                    if (finalI <= 10) {
                                        for (int j = 1; j <= finalI; j++)
                                            message.addReaction(Reactions.getNumber(j)).queue();
                                        Reactions.newMenu(message, event.getAuthor(), (emoji) -> {
                                            Consumer<Message> ret = g -> {};
                                            int select;
                                            for (select = 1; select <= 11 && !emoji.equals(Reactions.getNumber(select)); select++);
                                            if (select < 11) {
                                                int finalSelect = select;
                                                args[0] = guilds.get(finalSelect - 1).getId();
                                                ret = (g) -> {
                                                    message.delete().queue();
                                                    this.onCommand(event, member, channel, args);
                                                };
                                            }
                                            return ret;
                                        }, true);
                                    } else {
                                        sendMessage(channel, Type.WARNING, "Too many results! Please refer to the id manually and try again.").queue();
                                    }
                                });
                            }
                        }, (string) -> true, 30, (t) -> {});
                    }));
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
