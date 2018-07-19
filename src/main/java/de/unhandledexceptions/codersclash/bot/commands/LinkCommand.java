package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.unhandledexceptions.codersclash.bot.commands.connection.Link;
import de.unhandledexceptions.codersclash.bot.commands.connection.LinkListener;
import de.unhandledexceptions.codersclash.bot.commands.connection.LinkManager;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.ListDisplay;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import static de.unhandledexceptions.codersclash.bot.util.Messages.noPermissionsMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class LinkCommand implements ICommand {

    private final MessageEmbed expired = new EmbedBuilder().setDescription("Your request expired.")
            .setColor(Type.WARNING.getColor())
            .setFooter(Type.WARNING.getFooter(), Type.WARNING.getFooterUrl()).build();

    private final MessageEmbed commands = new EmbedBuilder().setDescription("Command Help").build();

    private LinkManager manager;
    private LinkListener listener;
    private SearchCommand searchCommand;
    private MailCommand mailCommand;
    private Database database;
    private BiMap<Long, Link> requesters; // K: Guild id, V: Channel id
    private Map<Long, Link> requests; // K: Guild id, V: link
    private Map<Long, Link> running;

    public LinkCommand(LinkManager manager, LinkListener listener, SearchCommand searchCommand, MailCommand mailCommand, Database database) {
        this.manager = manager;
        this.listener = listener;
        this.searchCommand = searchCommand;
        this.mailCommand = mailCommand;
        this.database = database;
        this.requesters = HashBiMap.create();
        this.requests = new HashMap<>();
        this.running = new HashMap<>();
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (Permissions.getPermissionLevel(member) < 4) {
            noPermissionsMessage(channel, member);
            return;
        }

        var guild = event.getGuild();

        if (requesters.containsKey(guild.getIdLong())) {
            Reactions.newYesNoMenu(member.getUser(), channel, "You have already requested a connection. Would you like to delete it?", (msg) -> {
                var link = requesters.get(guild.getIdLong());
                TextChannel linkedChannel;
                if ((linkedChannel = event.getJDA().getTextChannelById(link.getLinkedChannel(guild))) != null) {
                    linkedChannel.delete().queue();
                }
                link.getGuilds().forEach(requests::remove);
                requesters.remove(guild.getIdLong());
                sendMessage(channel, Type.SUCCESS, "Successfully cancelled request. You may now send a new one.").queue();
            }, true);

        } else if (running.containsKey(guild.getIdLong())) {
            var shardManager = event.getJDA().asBot().getShardManager();
            if (args.length > 1 && args[0].equalsIgnoreCase("invite")) {
                final String message = "Hey, we've sent your guild a connection request - Use the link command to accept or decline it!\n\n";
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
                            }, (no) -> msg.delete().queue());
                        });
                    } else {
                        Reactions.newYesNoMenu(member.getUser(), channel, String.format("Do you want to invite `%s - Owner: %#s` to this link?", selectedGuild, selectedGuild.getOwner().getUser()), (msg) -> {
                            Reactions.newYesNoMenu(member.getUser(), channel, "Do you want to add a custom message to your invite?",
                                    (msg2) -> sendMessage(channel, Type.QUESTION, "Please type in your custom message now.").queue(
                                            (msg3) -> Reactions.newMessageWaiter(member.getUser(), channel, 180, (msgReceived) -> {
                                                msg3.delete().queue();
                                                requests.put(selectedGuild.getIdLong(), running.get(guild.getIdLong()));
                                                mailCommand.sendMail(member, channel, selectedGuild, message + msgReceived.getContentDisplay());
                                            }, (v) -> msg3.editMessage(expired).queue())), (msg4) -> mailCommand.sendMail(member, channel, selectedGuild, "##Link Request## " + message), true);
                        }, true);
                    }
                } else {
                    List<String> found = searchCommand.find(shardManager, args[1], false);
                    channel.sendMessage(found.size() + " guilds found for this name. Please select one or abort:").queue((msg2) ->
                            ListDisplay.displayListSelection(found, msg2, member.getUser(), 10, (selected) -> {
                                msg2.delete().queue();
                                Matcher matcher = searchCommand.FIND_ID.matcher(selected);
                                matcher.find();
                                Guild selectedGuild = shardManager.getGuildById(matcher.group().replaceAll("[\\(\\)]", ""));
                                Reactions.newYesNoMenu(member.getUser(), channel, "Do you want to add a custom message to your invite?",
                                        (msg) -> sendMessage(channel, Type.QUESTION, "Please type in your custom message now.").queue(
                                                (msg3) -> Reactions.newMessageWaiter(member.getUser(), channel, 180, (msgReceived) -> {
                                                    msg3.delete().queue();
                                                    requests.put(selectedGuild.getIdLong(), running.get(guild.getIdLong()));
                                                    mailCommand.sendMail(member, channel, selectedGuild, message + msgReceived.getContentDisplay());
                                                }, (v) -> msg2.editMessage(expired).queue())), (msg) -> mailCommand.sendMail(member, channel, selectedGuild, "##Link Request## " + message), true);
                            }, (v) -> msg2.delete().queue()));
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("disconnect")) {
                Reactions.newYesNoMenu(member.getUser(), channel, "Do you want to disconnect from your current link?", (msg) -> {
                    var link = running.get(guild.getIdLong());
                    if (link.remove(guild)) {
                        link.getGuilds().stream().findFirst().ifPresent((id) -> {
                            Guild last = shardManager.getGuildById(id);
                            last.getTextChannelById(link.getLinkedChannel(last)).sendMessage("You are now the only guild left in this link, so it will be deactivated.").queue();
                        });
                        listener.removeLink(link);
                    } else {
                        sendMessage(channel, Type.SUCCESS, "Successfully disconnected from this. link.").queue();
                        sendMessage(guild.getTextChannelById(link.getLinkedChannel(guild)), Type.INFO, "This guild disconnected from this link.").queue();
                    }
                }, true);
            }
        } else if (requests.containsKey(guild.getIdLong())) {
            sendMessage(channel, Type.INFO, "There is a request for this guild. Would you like to accept it?").queue((msg) -> Reactions.newYesNoMenu(member.getUser(), msg, (yes) -> {
                msg.delete().queue();
                var link = requests.get(guild.getIdLong());
                requests.remove(guild.getIdLong());
                if (link.getGuilds().isEmpty()) {
                    sendMessage(channel, Type.ERROR, "It seems like no guild is in this link anymore. You may create an own request instead.").queue();
                } else {
                    manager.addGuild(link, guild);
                    listener.addLink(link);
                    if (requesters.inverse().get(link) != null)
                        running.put(requesters.inverse().remove(link), link);
                    running.put(guild.getIdLong(), link);
                }
            }, (no) -> msg.delete().queue()));
        } else {
            Reactions.newYesNoMenu(member.getUser(), channel, "There are no requests and no active links to this guild. Would you like to create one?", (msg) -> creationDialogue(new HashSet<>(), channel, member), true);
        }
    }

    private void creationDialogue(Set<Guild> guilds, TextChannel channel, Member member) {
        sendMessage(channel, Type.QUESTION, "Please enter the id or the name of the guild you want to connect to!").queue((msg) ->
                Reactions.newMessageWaiter(member.getUser(), channel, 30, (msgReceived) -> {
                    msg.delete().queue();
                    String raw = msgReceived.getContentRaw();
                    ShardManager shardManager = channel.getJDA().asBot().getShardManager();
                    if (raw.matches("\\d{1,18}")) {
                        var guild = shardManager.getGuildById(raw);
                        if (guild == null) {
                            sendMessage(channel, Type.ERROR, "No guild found with this id!").queue(Messages::deleteAfterFiveSec);
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
                    } else {
                        List<String> found = searchCommand.find(shardManager, raw, false);
                        if (found.isEmpty()) {
                            sendMessage(channel, Type.ERROR, "No guild found with this name!").queue(Messages::deleteAfterFiveSec);
                            addAnotherGuild(guilds, channel, member);
                        } else {
                            channel.sendMessage(found.size() + " guilds found for this name. Please select one or abort:").queue((msg2) ->
                                    ListDisplay.displayListSelection(found, msg2, member.getUser(), 10, (selected) -> {
                                        msg2.delete().queue();
                                        Matcher matcher = searchCommand.FIND_ID.matcher(selected);
                                        matcher.find();
                                        Guild guild = shardManager.getGuildById(matcher.group().replaceAll("[\\(\\)]", ""));
                                        Long mailChannel = database.getMailChannel(guild);
                                        if (mailChannel == null || mailChannel == 0) {
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
                }, (v) -> msg.editMessage(expired).queue()));
    }

    private void addThisGuild(Guild guild, Set<Guild> guilds, TextChannel channel, Member member) {
        Reactions.newYesNoMenu(member.getUser(), channel, String.format("Do you want to add `%s - Owner: %#s` to your request?", guild, guild.getOwner().getUser()), (msg2) -> {
            guilds.add(guild);
            if (guilds.size() == 10) {
                sendMessage(channel, Type.WARNING, "You've added 10 guilds to your request, that's the maximum. Would you like to finish it?").queue((msg) -> {
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
        final String message = "Hey, we've sent your guild a connection request - Use the link command to accept or decline it!\n\n";
        Reactions.newYesNoMenu(member.getUser(), channel, "Do you want to add a custom message to your invite?",
                (msg) -> sendMessage(channel, Type.QUESTION, "Please type in your custom message now.").queue(
                        (msg2) -> Reactions.newMessageWaiter(member.getUser(), channel, 180, (msgReceived) -> {
                            msg2.delete().queue();
                            sendInvite(member, guilds, channel, message + msgReceived.getContentDisplay());
                        }, (v) -> msg2.editMessage(expired).queue())), (msg) -> sendInvite(member, guilds, channel, "##Link Request## " + message), true);
    }

    private void sendInvite(Member requester, Set<Guild> guilds, TextChannel channel, String message) {
        Link link = manager.createLink(guilds.stream().map(Guild::getIdLong).collect(Collectors.toSet()));
        requester.getGuild().getController().createTextChannel("connection").setTopic("Connection to " + guilds).queue((newChannel) -> {
            requesters.put(requester.getGuild().getIdLong(), link);
            sendMessage((MessageChannel) newChannel, Type.INFO, "This is the channel to chat with the others. Currently, no request was accepted. You will be informed if so.").queue();
            ((MessageChannel) newChannel).sendMessage(commands).queue();
            link.addChannel((TextChannel) newChannel);
        });
        for (Guild guild : guilds) {
            requests.put(guild.getIdLong(), link);
            mailCommand.sendMail(requester, channel, guild, message);
        }
        sendMessage(channel, Type.SUCCESS, "Successfully sent a message to the following guilds: ```\n" + String.join("\n", guilds.stream().map(Guild::getName).collect(Collectors.toList())) + "```").queue();
    }
}
