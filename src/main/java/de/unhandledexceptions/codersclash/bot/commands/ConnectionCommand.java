package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.ListDisplay;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.*;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

public class ConnectionCommand implements ICommand {

    private Map<Long, Long> pendingRequests;
    private Set<Long> connected;
    private SearchCommand searchCommand;
    private MailCommand mailCommand;

    public ConnectionCommand(SearchCommand searchCommand, MailCommand mailCommand) {
        this.pendingRequests = new HashMap<>();
        this.connected = new HashSet<>();
        this.searchCommand = searchCommand;
        this.mailCommand = mailCommand;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        Guild thisGuild = event.getGuild();
        if (!thisGuild.getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 4) {
            if (connected.contains(thisGuild.getIdLong())) {
                sendMessage(channel, Type.INFO, "This guild is already connected. To abort the current connection, " +
                        "go to the connected channel and type `" + Bot.getPrefix(thisGuild.getIdLong()) + "abort connection` there.").queue();
                return;
            }
            if (args.length < 1 || !args[0].equalsIgnoreCase("request")) {
                wrongUsageMessage(channel, member, this);
                return;
            }
            if (args.length == 1) {
                if (pendingRequests.containsKey(thisGuild.getIdLong())) {
                    ShardManager shardManager = event.getJDA().asBot().getShardManager();
                    var guild = shardManager.getGuildById(pendingRequests.get(thisGuild.getIdLong()));
                    Reactions.newYesNoMenu(event.getAuthor(), channel, String.format("This guild has been requested to open a connection to " +
                            "`%s - Owner: %#s`. Would you like to accept it?", guild, guild.getOwner().getUser()), (msg) -> {
                        msg.delete().queue();
                        if (thisGuild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL) && guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                            String prefix = Bot.getPrefix(thisGuild.getIdLong());
                            String prefix2 = Bot.getPrefix(guild.getIdLong());
                            thisGuild.getController().createTextChannel("Connection to " + guild.getName()).queue((channelA) -> guild.getController().createTextChannel("Connection to " + thisGuild.getName()).queue((channelB) -> {
                                sendMessage((TextChannel) channelA, Type.SUCCESS, "Connection successfully opened! You can now chat with the people on the other side! To disconnect, please use `" + prefix + "abort connection`.").queue();
                                sendMessage((TextChannel) channelB, Type.INFO, thisGuild.getName() + " accepted your request. You can now chat with the others! To disconnect, please use `" + prefix2 + "abort connection`.").queue();
                                shardManager.addEventListener(new TransferListener(channelA.getIdLong(), channelB.getIdLong(), shardManager));
                                pendingRequests.remove(thisGuild.getIdLong());
                                connected.add(thisGuild.getIdLong());
                                connected.add(guild.getIdLong());
                            }));
                        } else {
                            sendMessage(channel, Type.ERROR, "Unfortunately, I couldn't open a connection due to a lack of permissions. " +
                                    "I need the `MANAGE_CHANNEL` permission to do that.\nThe request remains.").queue();
                        }

                    }, (msg) -> {
                        msg.delete().queue();
                        pendingRequests.remove(thisGuild.getIdLong(), guild.getIdLong());
                        sendMessage(channel, Type.SUCCESS, "Request declined.").queue();
                    });
                } else {
                    sendMessage(channel, Type.INFO, "There is currently no request for this guild. Use `"
                            + Bot.getPrefix(thisGuild.getIdLong()) + "connection request send [NOID|<ID>]` to connect with another guild.").queue();
                }
            } else if (args[1].equalsIgnoreCase("send") && args.length > 2) {
                var shardManager = event.getJDA().asBot().getShardManager();
                if (pendingRequests.containsValue(thisGuild.getIdLong())) {
                    long id = 0;
                    for (long key : pendingRequests.keySet()) {
                        if (pendingRequests.get(key) == thisGuild.getIdLong()) {
                            id = key;
                            break;
                        }
                    }
                    long finalId = id;
                    sendMessage(channel, Type.WARNING, "You have already sent a request to guild `" + shardManager.getGuildById(id).getName()
                            + "`. Would you like to delete it?").queue((msg) -> {
                                Reactions.newYesNoMenu(member.getUser(), msg, (m) -> {
                                    msg.delete().queue();
                                    pendingRequests.remove(finalId, thisGuild.getIdLong());
                                    sendMessage(channel, Type.SUCCESS, "Deleted request. You may now send a new one.").queue();
                                });
                    });
                } else if (args[2].equalsIgnoreCase("noid")) {
                    Reactions.newYesNoMenu(member.getUser(), channel, "Do you want to search the guild by name?", (msg) -> {
                        msg.delete().queue();
                        sendMessage(channel, Type.QUESTION, "Please type in the name now!").queue((m2) -> {
                            Reactions.newMessageWaiter(member.getUser(), channel, 30, (m) -> {
                                List<String> guilds = searchCommand.find(shardManager, m.getContentRaw(), false);
                                if (guilds.isEmpty()) {
                                    sendMessage(channel, Type.ERROR, "No results found. Maybe I am not on this guild?").queue();
                                } else {
                                    ListDisplay.displayListSelection(guilds, m2, member.getUser(), 10, (selected) -> {
                                        var matcher = searchCommand.FIND_ID.matcher(selected);
                                        matcher.find();
                                        var guild = shardManager.getGuildById(matcher.group().replaceAll("[\\(\\)]", ""));
                                        request(event, member, channel, args, thisGuild, guild);
                                    }, (v) -> m2.delete().queue());
                                }
                            }, (v) -> sendMessage(channel, Type.WARNING, "Your search request expired."));
                        });

                    });
                } else if (args[2].matches("\\d{1,18}")) {
                    Guild guild;
                    if ((guild = shardManager.getGuildById(args[2])) != null) {
                        request(event, member, channel, args, thisGuild, guild);
                    } else {
                        sendMessage(channel, Type.ERROR, "This guild doesn't exist (or at least I'm not on it). " +
                                "Try searching the guild with `" + Bot.getPrefix(thisGuild.getIdLong()) + "search guild <name>` " +
                                "to check if I am a member of this guild. Or try sending the request again, but use \"NOID\" instead of the id this time.").queue();
                    }
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

    private void request(CommandEvent event, Member member, TextChannel channel, String[] args, Guild thisGuild, Guild guild) {
        if (pendingRequests.containsKey(guild.getIdLong()) || connected.contains(guild.getIdLong())) {
            sendMessage(channel, Type.ERROR, "Their guild already has a connection request or is connected to a guild! You may send them a mail and tell them with `"
                    + Bot.getPrefix(thisGuild.getIdLong()) + "mail [NOID|<id>] <message>` instead.").queue();
        } else {
            pendingRequests.put(guild.getIdLong(), thisGuild.getIdLong());
            String message = "Hey, we've sent your guild a connection request " +
                    "- Use `" + Bot.getPrefix(guild.getIdLong()) + "connection request` to accept or decline it!\n\n";
            mailCommand.sendMail(member, channel, guild,
                    args.length > 3 ? message + event.getCommand().getJoinedArgs(2)
                            : "##Connection Request##" + message);
        }
    }

    private class TransferListener extends ListenerAdapter {
        private final long channelA;
        private final long channelB;
        private final ShardManager shardManager;

        private TransferListener(long channelA, long channelB, ShardManager shardManager) {
            this.channelA = channelA;
            this.channelB = channelB;
            this.shardManager = shardManager;
        }

        @Override
        public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
            if (event.getAuthor().isBot())
                return;

            Message message = event.getMessage();
            TextChannel channel = event.getChannel();
            if (channel.getIdLong() == channelA) {
                handleMessage(channel, shardManager.getTextChannelById(channelB), event.getMember(), message);
            } else if (channel.getIdLong() == channelB) {
                handleMessage(channel, shardManager.getTextChannelById(channelA), event.getMember(), message);
            }
        }

        private void handleMessage(TextChannel ownChannel, TextChannel targetChannel, Member member, Message message) {
            if (!ownChannel.getGuild().getSelfMember().hasPermission(ownChannel, Permission.MESSAGE_WRITE))
                return;

            if (targetChannel == null) {
                sendMessage(ownChannel, Type.ERROR, "It seems like the other guild deleted their channel! Connection will be closed.").queue();
                shardManager.removeEventListener(this);
                return;
            }

            if (!targetChannel.getGuild().getSelfMember().hasPermission(targetChannel, Permission.MESSAGE_WRITE))
                return;

            if (message.getContentRaw().equals(Bot.getPrefix(ownChannel.getGuild().getIdLong()) + "abort connection")
                    && Permissions.getPermissionLevel(member) >= 4) {
                shardManager.removeEventListener(this);
                connected.remove(targetChannel.getGuild().getIdLong());
                connected.remove(ownChannel.getGuild().getIdLong());
                sendMessage(targetChannel, Type.INFO, "The connection was closed by the other guild.").queue();
                sendMessage(ownChannel, Type.INFO, "Successfully closed connection.").queue();
                if (ownChannel.getGuild().getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                    Reactions.newYesNoMenu(member.getUser(), ownChannel, "Do you want to delete this channel?",
                            (msg) -> ownChannel.delete().queue(), (msg) -> msg.delete().queue());
                }
            } else {
                targetChannel.sendMessage(String.format("```\n%#s: %s```", member.getUser(), message)).queue();
            }
        }
    }

    // TODO
    @Override
    public String info(Member member) {
        return " ";
    }
}
