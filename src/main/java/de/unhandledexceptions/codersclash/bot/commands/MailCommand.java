package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.ListDisplay;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

/**
 * @author Johnny_JayJay
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
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION))
            return;

        if (Permissions.getPermissionLevel(member) >= 4) {
            if (args.length > 1) {
                var shardManager = event.getJDA().asBot().getShardManager();
                Guild guild;
                if (args[0].matches("\\d{1,18}")) {
                    if ((guild = shardManager.getGuildById(Long.parseLong(args[0]))) != null) {
                        sendMail(member, channel, guild, event.getCommand().getJoinedArgs(1));
                    } else {
                        search(event, member, channel, args);
                    }
                } else if (args[0].equalsIgnoreCase("noid")) {
                    search(event, member, channel, args);
                } else {
                    wrongUsageMessage(channel, member, this);
                }
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    public void sendMail(Member member, TextChannel channel, Guild guild, String content) {
        Long mailChannelId;
        if ((mailChannelId = database.getMailChannel(guild)) != null && mailChannelId != 0) {
            var mailChannel = guild.getTextChannelById(mailChannelId);
            if (mailChannel != null) {
                var builder = new EmbedBuilder();
                var author = member.getUser();
                String topic = "No Topic";
                Matcher matcher = topicPattern.matcher(content);
                if (matcher.find()) {
                    String found = matcher.group();
                    content = content.replaceFirst(found, "");
                    topic = found.replaceAll("##", "");
                }
                builder.setTitle("Via \"" + member.getGuild().getName() + "\" (" + member.getGuild().getId() + ")")
                        .setAuthor(String.format("%#s", author), null, author.getEffectiveAvatarUrl())
                        .setColor(member.getColor())
                        .setFooter("Inbox", null)
                        .setTimestamp(Instant.now())
                        .addField(topic, content, false);
                if (guild.getSelfMember().hasPermission(mailChannel, Permission.MESSAGE_WRITE)) {
                    mailChannel.sendMessage(builder.build()).queue(
                            (msg) -> sendMessage(channel, Type.SUCCESS, "Mail sent!").queue(),
                            defaultFailure(channel));
                } else {
                    sendMessage(channel, Type.ERROR, "I can't send a mail to guild `" + guild + "`, because I have no permissions to do that!").queue();
                }
            } else {
                sendMessage(channel, Type.ERROR, "I can't send a mail to `" + guild + "`, it seems like they deleted their mail channel!").queue();
            }
        } else {
            sendMessage(channel, Type.ERROR, "The guild `" + guild + "` hasn't set a mail channel! Contact their administrators.").queue();
        }
    }

    private void search(CommandEvent event, Member member, TextChannel channel, String[] args) {
        ShardManager shardManager = event.getJDA().asBot().getShardManager();
        Reactions.newYesNoMenu(member.getUser(), channel, "Do you want to search the guild by name?", (msg) -> {
            msg.delete().queue();
            sendMessage(channel, Type.QUESTION, "Please type in the name of the guild you are looking for!").queue((msg2) -> {
                Reactions.newMessageWaiter(member.getUser(), channel, 30, (m) -> {
                    List<String> guilds = searchCommand.find(shardManager, m.getContentRaw(), false);
                    if (guilds.isEmpty()) {
                        sendMessage(channel, Type.ERROR, "Sorry, no guilds were found for this search :(\nMaybe I'm not on the guild you're looking for?").queue(Messages::deleteAfterFiveSec);
                    } else {
                        ListDisplay.displayListSelection(guilds, msg2, event.getAuthor(), 10, (selected) -> {
                            msg2.delete().queue();
                            var matcher = searchCommand.FIND_ID.matcher(selected);
                            matcher.find();
                            args[0] = matcher.group().replaceAll("\\(|\\)", "");
                            this.onCommand(event, member, channel, args);
                        }, (v) -> msg2.delete().queue());
                    }
                });
            });
        });


    }

    @Override
    public String info(Member member) {
        int permLevel = Permissions.getPermissionLevel(member);
        return permLevel < 4 ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: " +
                "`4`\nYour permission level: `" + permLevel + "`"
                : "**Description:** Send a \"mail\" to a guild the bot is also on!\n\n"
                + "**Usage:** `" + Bot.getPrefix(member.getGuild().getIdLong()) + "[mail|contact] <Guild-ID> <Message>`\nIf you don't have an id, replace it with \"NOID\". "
                + "You may then search a guild by name.\nTo add a topic to your mail, put `##your-topic##` somewhere "
                + "(replace \"your-topic\" with the topic you want).\nThis *only* works if the other guild has set a mail channel.\n\n"
                + "**Permission Level:** `4`";
    }
}
