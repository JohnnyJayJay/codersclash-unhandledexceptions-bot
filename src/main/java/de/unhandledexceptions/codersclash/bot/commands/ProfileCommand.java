package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author TheRealYann
 */

public class ProfileCommand implements ICommand {

    private ReportCommand reportCommand;

    public ProfileCommand(ReportCommand reportCommand){
        this.reportCommand = reportCommand;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
            return;
        if (Permissions.getPermissionLevel(member) >= 1) {
            if (args.length <= 1 && event.getCommand().getJoinedArgs().matches("(<@!?\\d+>)?")) {
                var jda = event.getJDA().asBot().getShardManager();
                Member target = member;
                if (event.getMessage().getMentionedMembers().size() == 1) {
                    target = event.getMessage().getMentionedMembers().get(0);
                }
                String nickname = ((target.getNickname() != null) ? target.getNickname() : "none");
                String game = ((target.getGame() != null) ? target.getGame().getName() : "like a good boy!");
                String gametype = "Using Discord";
                String perms = Reactions.getNumber(Permissions.getPermissionLevel(target));
                String reports = ((Reactions.getNumber(reportCommand.getReportCount(target)).equals(Reactions.getNumber(0))) ? ":zero: aka. **clean af**" : Reactions.getNumber(reportCommand.getReportCount(target)));
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
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy kk:mm:ss O", Locale.ENGLISH).withZone(ZoneId.of("Europe/Paris"));
                EmbedBuilder embedBuilder = new EmbedBuilder();
                if (target.getUser().getAvatarUrl() == null) {
                    embedBuilder.clear()
                            .addField("Usertag", format("%#s", target.getUser()), true)
                            .addField("Nickname", nickname, true)
                            .addBlankField(true)
                            .addField("ID", target.getUser().getId(), true)
                            .addField("Status", status, true)
                            .addBlankField(true)
                            .addField("Permission level", perms + " **of** :five:", true)
                            .addField("Reports on this Server", reports, true)
                            .addBlankField(true)
                            .addField("Joined this Server", target.getJoinDate().format(dateTimeFormatter), true)
                            .addField("Registered on Discord", target.getUser().getCreationTime().format(dateTimeFormatter), true)
                            .addField("Roles", roles, false);
                    if (getOnlineStatus == OnlineStatus.ONLINE || getOnlineStatus == OnlineStatus.IDLE || getOnlineStatus == OnlineStatus.DO_NOT_DISTURB) {
                        embedBuilder.addField(gametype, game, false);
                    }
                    embedBuilder.setImage(image)
                            .setColor(target.getColor())
                            .setThumbnail(target.getUser().getAvatarUrl());
                } else {
                    embedBuilder.clear()
                            .addField("Usertag", format("%#s", target.getUser()), true)
                            .addField("Nickname", nickname, true)
                            .addField("ID", target.getUser().getId(), true)
                            .addField("Status", status, true)
                            .addField("Permission level", perms + " **of** :five:", true)
                            .addField("Reports", reports, true)
                            .addField("Joined this Server", target.getJoinDate().format(dateTimeFormatter), true)
                            .addField("Registered on Discord", target.getUser().getCreationTime().format(dateTimeFormatter), true)
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
                wrongUsageMessage(channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

        @Override
        public String info (Member member){
            String prefix = Bot.getPrefix(member.getGuild().getIdLong());
            int permLevel = Permissions.getPermissionLevel(member);
            return permLevel < 1
                    ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `1`\nYour permission " +
                    "level: `" + permLevel + "`"
                    : format("**Description**: Provides you with Information about yourself or another member.\n\n" +
                    "**Usage**: `%s[profile|userinfo]` to view your profile\n\t\t\t  `%s[profile|userinfo] @Member` to view @Member's profile\n\n**Permission " +
                    "level**: `1`", prefix, prefix);
        }
    }
