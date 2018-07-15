package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.noPermissionsMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;
import static de.unhandledexceptions.codersclash.bot.util.Regex.MEMBER_MENTION;
import static java.lang.String.format;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class ProfileCommand implements ICommand {

    private static final Map<String, String> urls = new HashMap<>() {{
        put("online", "https://i.imgur.com/JZwNdVZ.png");
        put("idle", "https://i.imgur.com/z4Noqb7.png");
        put("dnd", "https://i.imgur.com/Er0johC.png");
        put("offline", "https://i.imgur.com/fPB7iQm.png");
    }};


    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;
        if (Permissions.getPermissionLevel(member) >= 1) {
            if (args.length <= 1 && event.getCommand().getJoinedArgs().matches("(<@!?\\d+>)?")) {
                uploadEmotes(event, channel);
                var jda = event.getJDA().asBot().getShardManager();
                Member target = member;
                if (event.getMessage().getMentionedMembers().size() == 1) {
                    target = event.getMessage().getMentionedMembers().get(0);
                }
                event.getMessage().delete().queue();
                String nickname = ((target.getNickname() != null) ? target.getNickname() : "none");
                String game = ((target.getGame() != null) ? target.getGame().getName() : "like a good boy!");
                String gametype = "Using Discord";
                String perms = Reactions.getNumber(Permissions.getPermissionLevel(target));
                String reports = String.valueOf(0);
                String roles = ((!target.getRoles().isEmpty())) ? String.join(" ", target.getRoles().stream().map(Role::getAsMention).collect(Collectors.toList())) : "none";
                String image = null;
                String status;
                var getOnlineStatus = target.getOnlineStatus();
                if (target.getGame() != null) {
                    var getGameTyp = target.getGame().getType();
                    if (getGameTyp == Game.GameType.DEFAULT) {
                        gametype = "Playing";
                    } else if (getGameTyp == Game.GameType.LISTENING) {
                        gametype = "Listening to";
                    } else if (getGameTyp == Game.GameType.STREAMING) {
                        gametype = "Streaming";
                    } else if (getGameTyp == Game.GameType.WATCHING) {
                        gametype = "Watching";
                    }

                    var isRich = target.getGame().isRich();
                    var asRichPresence = target.getGame().asRichPresence();
                    image = ((isRich) ? target.getGame().asRichPresence().getLargeImage().getUrl() : null);
                    if (isRich && getGameTyp == Game.GameType.LISTENING) {
                        game = "**" + asRichPresence.getDetails() + "** by *" + asRichPresence.getState() + "*";
                    } else if (isRich && getGameTyp == Game.GameType.STREAMING) {
                        game = "**" + asRichPresence.getName() + "** playing *" + asRichPresence.getDetails() + "*";
                    } else if (isRich) {
                        game = "**" + asRichPresence.getName() + "** :arrow_right: " + asRichPresence.getDetails();
                    }
                }
                if (getOnlineStatus == OnlineStatus.ONLINE) {
                    status = jda.getEmotesByName("online", false).get(0).getAsMention();
                } else if (getOnlineStatus == OnlineStatus.IDLE) {
                    status = jda.getEmotesByName("idle", false).get(0).getAsMention();
                } else if (getOnlineStatus == OnlineStatus.DO_NOT_DISTURB) {
                    status = jda.getEmotesByName("dnd", false).get(0).getAsMention();
                } else {
                    status = jda.getEmotesByName("offline", false).get(0).getAsMention();
                }
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy kk:mm:ss O", Locale.ENGLISH);
                EmbedBuilder embedBuilder = new EmbedBuilder();
                if (target.getUser().getAvatarUrl() == null) {
                    embedBuilder.clear().setAuthor(target.getEffectiveName(), null, target.getUser().getAvatarUrl())
                            .addField("Usertag", target.getEffectiveName() + "#" + target.getUser().getDiscriminator(), true)
                            .addField("Nickname", nickname, true)
                            .addBlankField(true)
                            .addField("ID", target.getUser().getId(), true)
                            .addField("Status", status, true)
                            .addBlankField(true)
                            .addField("Permission level", perms + " **of** :five:", true)
                            .addField("Reports on this Guild", reports, true)
                            .addBlankField(true)
                            .addField("Joined this Server", target.getJoinDate().atZoneSameInstant(ZoneId.of("Europe/Paris")).format(dateTimeFormatter), true)
                            .addField("Registered on Discord", target.getUser().getCreationTime().atZoneSameInstant(ZoneId.of("Europe/Paris")).format(dateTimeFormatter), true)
                            .addField("Roles", roles, false);
                    if (getOnlineStatus == OnlineStatus.ONLINE || getOnlineStatus == OnlineStatus.IDLE || getOnlineStatus == OnlineStatus.DO_NOT_DISTURB) {
                        embedBuilder.addField(gametype, game, false);
                    }
                    embedBuilder.setImage(image)
                            .setColor(target.getColor())
                            .setThumbnail(target.getUser().getAvatarUrl());
                } else {
                    embedBuilder.clear().setAuthor(target.getEffectiveName(), null, target.getUser().getAvatarUrl())
                            .addField("Usertag", target.getEffectiveName() + "#" + target.getUser().getDiscriminator(), true)
                            .addField("Nickname", nickname, true)
                            .addField("ID", target.getUser().getId(), true)
                            .addField("Status", status, true)
                            .addField("Permission level", perms + " **of** :five:", true)
                            .addField("Reports on this Guild", reports, true)
                            .addField("Joined this Server", target.getJoinDate().atZoneSameInstant(ZoneId.of("Europe/Paris")).format(dateTimeFormatter), true)
                            .addField("Registered on Discord", target.getUser().getCreationTime().atZoneSameInstant(ZoneId.of("Europe/Paris")).format(dateTimeFormatter), true)
                            .addField("Roles", roles, false);
                    if (getOnlineStatus == OnlineStatus.ONLINE || getOnlineStatus == OnlineStatus.IDLE || getOnlineStatus == OnlineStatus.DO_NOT_DISTURB) {
                        embedBuilder.addField(gametype, game, false);
                    }
                    embedBuilder.setImage(image)
                            .setColor(target.getColor())
                            .setThumbnail(target.getUser().getAvatarUrl());
                }
                sendMessage(channel, Messages.Type.NO_TYPE, "Information about " + target.getAsMention(), false, embedBuilder).queue();
            } else {
                sendMessage(channel, Messages.Type.INFO, "Wrong usage. Command info:\n\n" + this.info(member)).queue();
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }


    private void uploadEmotes(CommandEvent event, TextChannel channel){
        if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_EMOTES)) {
            try {
                for (String name : urls.keySet()) {
                    var emotes = event.getJDA().asBot().getShardManager().getEmotesByName(name, false);
                    if (emotes.isEmpty() || emotes.get(0).getImageUrl().equals(urls.get(name))) {
                        event.getGuild().getController().createEmote(name, Icon.from(new URL(urls.get(name)).openStream())).queue();
                    }
                }
            } catch (IOException e) {
                Logging.getLogger().error("An Exception occurred while creating/parsing emotes:", e);
                return;
            }
        } else {
            sendMessage(channel, Messages.Type.WARNING, "The Bot needs to have permission to manage custom emotes in order to display your profile!").queue();
        }
    }
        @Override
        public String info (Member member){
            String prefix = Bot.getPrefix(member.getGuild().getIdLong());
            int permLevel = Permissions.getPermissionLevel(member);
            String ret = permLevel < 1
                    ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `1`\nYour permission " +
                    "level: `" + permLevel + "`"
                    : format("**Description**: Provides you with Information about yourself or another member.\n\n" +
                    "**Usage**: `%s[profile]` to view your profile\n\t\t\t  `%s[profile] @Member` to view @Member's profile\n\n**Permission " +
                    "level**: `1`", prefix, prefix);
            return ret;
        }
    }
