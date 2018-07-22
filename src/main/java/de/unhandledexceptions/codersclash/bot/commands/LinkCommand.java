package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Main;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.connection.Link;
import de.unhandledexceptions.codersclash.bot.core.connection.LinkListener;
import de.unhandledexceptions.codersclash.bot.core.connection.LinkManager;
import de.unhandledexceptions.codersclash.bot.core.reactions.ListDisplay;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author Johnny_JayJay
 */

public class LinkCommand implements ICommand {

    private final MessageEmbed expired = new EmbedBuilder().setDescription("Your request expired.")
            .setColor(Type.WARNING.getColor())
            .setFooter(Type.WARNING.getFooter(), Type.WARNING.getFooterUrl()).build();

    private final String DEFAULT_MESSAGE = "Hey, we've sent your guild a connection request - Use the link command to accept or decline it!\n\n";

    private LinkManager manager;
    private LinkListener listener;
    private SearchCommand searchCommand;
    private MailCommand mailCommand;
    private Database database;
    private Map<Long, Link> requests; // K: Guild id, V: link
    private Map<Long, Link> running;

    public LinkCommand(LinkManager manager, LinkListener listener, SearchCommand searchCommand, MailCommand mailCommand, Database database) {
        this.manager = manager;
        this.listener = listener;
        this.searchCommand = searchCommand;
        this.mailCommand = mailCommand;
        this.database = database;
        this.requests = new HashMap<>();
        this.running = new HashMap<>();
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE))
            return;

        if (Permissions.getPermissionLevel(member) < 4) {
            noPermissionsMessage(channel, member);
            return;
        }
        if (args.length < 1) {
            wrongUsageMessage(channel, member, this);
            return;
        }

        var guild = event.getGuild();
        ShardManager shardManager = event.getJDA().asBot().getShardManager();

        if (args[0].equalsIgnoreCase("request")) {
            if (running.containsKey(guild.getIdLong())) {
                sendMessage(channel, Type.WARNING, "There is an active link to this guild. To request a new one, you must close it first.\n" +
                        "To invite new guilds to this link, you may use `" + Bot.getPrefix(guild.getIdLong()) + "link invite <name|id>`. To disconnect from this link, " +
                        "use `" + Bot.getPrefix(guild.getIdLong()) + "link disconnect`.").queue();
            } else {
                Reactions.newYesNoMenu(member.getUser(), channel, "There are no active links to this guild. Would you like to request one?", (msg) -> Main.otherThread(() -> {
                    Set<Guild> guildSet = new HashSet<>();
                    if (args.length > 1) {
                        if (args.length == 2 && args[1].matches("\\d{16,18}")) {
                            addPossiblyNullGuild(guildSet, channel, member, shardManager.getGuildById(args[1]));
                        } else {
                            searchGuild(guildSet, channel, member, String.join(" ", Arrays.copyOfRange(args, 1, args.length)), shardManager);
                        }
                    } else {
                        creationDialogue(guildSet, channel, member);
                    }
                }), true);
            }
        } else if (args[0].equalsIgnoreCase("accept")) {
            if (requests.containsKey(guild.getIdLong())) {
                var link = requests.get(guild.getIdLong());
                if (link.getGuilds().size() < 10 || link.getGuilds().size() > 0) {
                    sendMessage(channel, Type.INFO, "There is a request for this guild. Guilds that are in there: ```\n" +
                            String.join("\n", link.getGuilds().stream().map((id) -> shardManager.getGuildById(id).toString()).collect(Collectors.toList())) +
                            "```\nWould you like to accept it?").queue((msg) -> Reactions.newYesNoMenu(member.getUser(), msg, (yes) -> {
                        msg.delete().queue();
                        requests.remove(guild.getIdLong());
                        manager.addGuild(link, guild);
                        if (!listener.containsLink(link)) {
                            listener.addLink(link);
                        }
                        running.put(guild.getIdLong(), link);
                    }));
                } else {
                    requests.remove(guild.getIdLong());
                    sendMessage(channel, Type.ERROR, "There was a request for this guild, but it is no longer available. Reason: unexpected amount of guilds in this link.").queue();
                }
            } else {
                sendMessage(channel, Type.INFO, "There is no request for this guild. To create one, use `"
                        + Bot.getPrefix(guild.getIdLong()) + "link request <name|id>`.").queue();
            }
        } else if (args[0].equalsIgnoreCase("disconnect")) {
            if (running.containsKey(guild.getIdLong())) {
                var link = running.get(guild.getIdLong());
                Reactions.newYesNoMenu(member.getUser(), channel, "Would you like to delete the channel when you disconnect?", (msg) -> {
                    running.remove(guild.getIdLong());
                    manager.removeGuild(link, guild, true);
                }, (msg) -> {
                    running.remove(guild.getIdLong());
                    manager.removeGuild(link, guild, false);
                });
            } else {
                sendMessage(channel, Type.INFO, "There are no active links to this guild. You may create one with " +
                        "`" + Bot.getPrefix(guild.getIdLong()) + "link request <name|id>`.").queue();
            }
        } else if (args[0].equalsIgnoreCase("invite")) {
            if (running.containsKey(guild.getIdLong())) {
                Main.otherThread(() -> {
                    var link = running.get(guild.getIdLong());
                    if (args.length == 2 && args[0].matches("\\d{1,18}")) {
                        Guild selectedGuild = shardManager.getGuildById(args[1]);
                        if (selectedGuild == null) {
                            sendMessage(channel, Type.ERROR, "I couldn't find a guild with that id! Do you want to try searching by name?").queue((msg) -> {
                                Reactions.newYesNoMenu(member.getUser(), msg, (yes) -> {
                                    msg.delete().queue();
                                    Reactions.newMessageWaiter(member.getUser(), channel, 40, (msgReceived) -> {
                                        String[] split = msgReceived.getContentRaw().split("\\s+");
                                        String[] newArgs = new String[split.length + 1];
                                        newArgs[0] = "invite";
                                        System.arraycopy(split, 0, newArgs, 1, split.length);
                                        this.onCommand(event, member, channel, newArgs);
                                    }, (v) -> channel.sendMessage(expired).queue());
                                });
                            });
                        } else if (selectedGuild == guild) {
                            sendMessage(channel, Type.ERROR, "That's literally your own guild!").queue();
                        } else {
                            Reactions.newYesNoMenu(member.getUser(), channel, format("Do you want to invite `%s - Owner: %#s` to this link?", selectedGuild, selectedGuild.getOwner().getUser()), (msg) -> {
                                addCustomMessage(member, channel, guild, link, msg, selectedGuild);
                            }, true);
                        }
                    } else {
                        List<String> found = searchCommand.find(shardManager, args[1], false);
                        if (!found.isEmpty()) {
                            channel.sendMessage(found.size() + " guilds found for this name. Please select one or abort:").queue((msg2) ->
                                    ListDisplay.displayListSelection(found, msg2, member.getUser(), 10, (selected) -> {
                                        msg2.delete().queue();
                                        Matcher matcher = SearchCommand.FIND_ID.matcher(selected);
                                        matcher.find();
                                        Guild selectedGuild = shardManager.getGuildById(matcher.group().replaceAll("[\\(\\)]", ""));
                                        if (!selectedGuild.equals(guild)) {
                                            addCustomMessage(member, channel, guild, link, msg2, selectedGuild);
                                        } else {
                                            sendMessage(channel, Type.ERROR, "That's literally your own guild!").queue();
                                        }
                                    }, (v) -> msg2.delete().queue()));
                        } else {
                            sendMessage(channel, Type.ERROR, "No matches found for this search.").queue();
                        }
                    }
                });
            } else {
                sendMessage(channel, Type.INFO, "There are no active links to this guild. You may create one with " +
                        "`" + Bot.getPrefix(guild.getIdLong()) + "link request <name|id>`.").queue();
            }
        } else {
            wrongUsageMessage(channel, member, this);
        }
    }

    private void addCustomMessage(Member member, TextChannel channel, Guild guild, Link link, Message msg2, Guild selectedGuild) {
        Reactions.newYesNoMenu(member.getUser(), channel, "Do you want to add a custom message to your invite?",
                (msg) -> sendMessage(channel, Type.QUESTION, "Please type in your custom message now.").queue(
                        (msg3) -> Reactions.newMessageWaiter(member.getUser(), channel, 180, (msgReceived) -> {
                            msg3.delete().queue();
                            requests.put(selectedGuild.getIdLong(), running.get(guild.getIdLong()));
                            mailCommand.sendMail(member, channel, selectedGuild, DEFAULT_MESSAGE + msgReceived.getContentDisplay());
                        }, (v) -> msg2.editMessage(expired).queue())), (msg) -> {
                    requests.put(selectedGuild.getIdLong(), link);
                    mailCommand.sendMail(member, channel, selectedGuild, "##Link Request## " + DEFAULT_MESSAGE);
                }, true);
    }

    private void creationDialogue(Set<Guild> guilds, TextChannel channel, Member member) {
        sendMessage(channel, Type.QUESTION, "Please enter the id or the name of the guild you want to connect to!").queue((msg) ->
                Reactions.newMessageWaiter(member.getUser(), channel, 30, (msgReceived) -> {
                    msg.delete().queue();
                    String raw = msgReceived.getContentRaw();
                    ShardManager shardManager = channel.getJDA().asBot().getShardManager();
                    if (raw.matches("\\d{1,18}")) {
                        var guild = shardManager.getGuildById(raw);
                        addPossiblyNullGuild(guilds, channel, member, guild);
                    } else {
                        searchGuild(guilds, channel, member, raw, shardManager);
                    }
                }, (v) -> msg.editMessage(expired).queue()));
    }

    private void addPossiblyNullGuild(Set<Guild> guilds, TextChannel channel, Member member, Guild guild) {
        if (guild == null) {
            sendMessage(channel, Type.ERROR, "No guild found with this id!").queue(Messages::deleteAfterFiveSec);
            addAnotherGuild(guilds, channel, member);
        } else if (guild == member.getGuild()) {
            sendMessage(channel, Type.ERROR, "That's literally your own guild!").queue(Messages::deleteAfterFiveSec);
            addAnotherGuild(guilds, channel, member);
        } else {
            Long mailChannel = database.getMailChannel(guild);
            if (mailChannel == null || mailChannel == 0) {
                sendMessage(channel, Type.ERROR, "The given guild hasn't set a mail channel! Please contact their administrators.").queue(Messages::deleteAfterFiveSec);
                addAnotherGuild(guilds, channel, member);
            } else {
                addThisGuild(guild, guilds, channel, member);
            }
        }
    }

    private void searchGuild(Set<Guild> guilds, TextChannel channel, Member member, String raw, ShardManager shardManager) {
        List<String> found = searchCommand.find(shardManager, raw, false);
        if (found.isEmpty()) {
            sendMessage(channel, Type.ERROR, "No guild found with this name!").queue(Messages::deleteAfterFiveSec);
            addAnotherGuild(guilds, channel, member);
        } else {
            channel.sendMessage(found.size() + " guilds found for this name. Please select one or abort:").queue((msg2) ->
                    ListDisplay.displayListSelection(found, msg2, member.getUser(), 10, (selected) -> {
                        msg2.delete().queue();
                        Matcher matcher = SearchCommand.FIND_ID.matcher(selected);
                        matcher.find();
                        Guild guild = shardManager.getGuildById(matcher.group().replaceAll("[\\(\\)]", ""));
                        Long mailChannel = database.getMailChannel(guild);
                        if (guild == member.getGuild()) {
                            sendMessage(channel, Type.ERROR, "That's literally your own guild!").queue(Messages::deleteAfterFiveSec);
                            addAnotherGuild(guilds, channel, member);
                        } else if (mailChannel == null || mailChannel == 0) {
                            sendMessage(channel, Type.ERROR, "The given guild hasn't set a mail channel! Please contact their administrators.").queue(Messages::deleteAfterFiveSec);
                            addAnotherGuild(guilds, channel, member);
                        } else if (guild.getTextChannelById(mailChannel) == null){
                            sendMessage(channel, Type.ERROR, "This guild apparently deleted their mail channel, so I can't send a request. Contact their administrators.").queue(Messages::deleteAfterFiveSec);
                            addAnotherGuild(guilds, channel, member);
                        } else {
                            addThisGuild(guild, guilds, channel, member);
                        }
                    }, (v) -> {
                        msg2.delete().queue();
                        addAnotherGuild(guilds, channel, member);
                    }));
        }
    }

    private void addThisGuild(Guild guild, Set<Guild> guilds, TextChannel channel, Member member) {
        Reactions.newYesNoMenu(member.getUser(), channel, format("Do you want to add `%s - Owner: %#s` to your request?", guild, guild.getOwner().getUser()), (msg2) -> {
            guilds.add(guild);
            if (guilds.size() == 10) {
                sendMessage(channel, Type.WARNING, "You've added 10 guilds to your request, that's the maximum for a link. " +
                        "Though you may send more requests with the `link invite` command. Would you like to finish the request for now?").queue((msg) -> {
                    Reactions.newYesNoMenu(member.getUser(), msg, (yes) -> finishRequest(guilds, channel, member));
                });
            } else {
                addAnotherGuild(guilds, channel, member);
            }
        }, (msg3) -> addAnotherGuild(guilds, channel, member), true);
    }

    private void addAnotherGuild(Set<Guild> guilds, TextChannel channel, Member member) {
        Reactions.newYesNoMenu(member.getUser(), channel, "Do you want to add another guild?",
                (msg3) -> creationDialogue(guilds, channel, member), (msg3) -> finishRequest(guilds, channel, member), true);
    }

    private void finishRequest(Set<Guild> guilds, TextChannel channel, Member member) {
        if (guilds.isEmpty()) {
            sendMessage(channel, Type.ERROR, "No guilds added!").queue();
            return;
        }
        final String message = "Hey, we've sent your guild a connection request - Use `link accept` to accept or decline it!\n\n";
        Reactions.newYesNoMenu(member.getUser(), channel, "Do you want to add a custom message to your invite?",
                (msg) -> sendMessage(channel, Type.QUESTION, "Please type in your custom message now.").queue(
                        (msg2) -> Reactions.newMessageWaiter(member.getUser(), channel, 180, (msgReceived) -> {
                            msg2.delete().queue();
                            sendInvite(member, guilds, channel, message + msgReceived.getContentDisplay());
                        }, (v) -> msg2.editMessage(expired).queue())), (msg) -> sendInvite(member, guilds, channel, "##Link Request## " + message), true);
    }

    private void sendInvite(Member requester, Set<Guild> guilds, TextChannel channel, String message) {
        Link link = manager.createLink();
        requester.getGuild().getController().createTextChannel("connection").queue((newChannel) -> {
            running.put(requester.getGuild().getIdLong(), link);
            sendMessage((MessageChannel) newChannel, Type.INFO, "The link was opened, the request was sent. Currently, no request was accepted. You will be informed if so.").queue();
            link.addChannel((TextChannel) newChannel);
            for (Guild guild : guilds) {
                requests.put(guild.getIdLong(), link);
                mailCommand.sendMail(requester, channel, guild, message);
            }
            sendMessage(channel, Type.SUCCESS, "Successfully sent a message to the following guilds: ```\n" + String.join("\n", guilds.stream().map(Guild::toString).collect(Collectors.toList())) + "```").queue();
        });
    }

    @Override
    public String info(Member member) {
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLvl = Permissions.getPermissionLevel(member);
        return permLvl < 4 ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: " +
                "`4`\nYour permission level: `" + permLvl + "`"
                : format("**Description:** Links your guild with up to 9 other guilds with a channel.\n\n**Usage**: `%slink [request|accept|disconnect]`\n`%slink invite <guildname>`Note that " +
                "you may only use `invite` and `disconnect` if you're currently connected.\n\n A guild may only have one link or one request " +
                "at a time. As soon as you request a link, your guild is linked.\nIn order to send a new request, you need to " +
                "disconnect first.\n\n**Permission level:** `4`", prefix, prefix);
    }
}