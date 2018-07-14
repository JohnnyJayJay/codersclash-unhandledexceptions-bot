package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import de.unhandledexceptions.codersclash.bot.util.Messages;
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

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

public class ProfileCommand implements ICommand {

    private static final Map<String, String> urls = new HashMap<>() {{
        put("online", "https://i.imgur.com/JZwNdVZ.png");
        put("idle", "https://i.imgur.com/z4Noqb7.png");
        put("dnd", "https://i.imgur.com/Er0johC.png");
        put("offline", "https://i.imgur.com/fPB7iQm.png");
    }};


    @Override
    public void onCommand(CommandEvent commandEvent, Member member, TextChannel textChannel, String[] strings) {
        uploadEmotes(commandEvent, textChannel);
        var jda = commandEvent.getJDA().asBot().getShardManager();
        Member target = member;
        if (commandEvent.getMessage().getMentionedMembers().size() == 1) {
            target = commandEvent.getMessage().getMentionedMembers().get(0);
        }
        String nickname = ((target.getNickname() != null) ? target.getNickname() : "none");
        String image = ((target.getGame().isRich()) ? target.getGame().asRichPresence().getLargeImage().getUrl() : null);
        String game = ((target.getGame() != null) ? target.getGame().getName() : "like a good boy!");
        String gametype = "Using Discord";
        String status = null;
        if (target.getGame().isRich() && target.getGame().getType() == Game.GameType.LISTENING) {
            game = "**" + target.getGame().asRichPresence().getDetails() + "** by *" + target.getGame().asRichPresence().getState() + "*";
        } else if (target.getGame().isRich() && target.getGame().getType() == Game.GameType.STREAMING) {
            game = "**" + target.getGame().asRichPresence().getName() + "** playing *" + target.getGame().asRichPresence().getDetails() + "*";
        } else if (target.getGame().isRich()) {
            game = target.getGame().asRichPresence().getDetails();
        }
        if (target.getGame() != null) {
            if (target.getGame().getType() == Game.GameType.DEFAULT) {
                gametype = "Playing";
            } else if (target.getGame().getType() == Game.GameType.LISTENING) {
                gametype = "Listening to";
            } else if (target.getGame().getType() == Game.GameType.STREAMING) {
                gametype = "Streaming";
            } else if (target.getGame().getType() == Game.GameType.WATCHING) {
                gametype = "Watching";
            }
        }

        if (target.getOnlineStatus() == OnlineStatus.ONLINE) {
            status = jda.getEmotesByName("online", false).get(0).getAsMention();
        } else if (target.getOnlineStatus() == OnlineStatus.IDLE) {
            status = jda.getEmotesByName("idle", false).get(0).getAsMention();
        } else if (target.getOnlineStatus() == OnlineStatus.DO_NOT_DISTURB) {
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
                    .addField("Joined this Server", target.getJoinDate().atZoneSameInstant(ZoneId.of("Europe/Paris")).format(dateTimeFormatter), true)
                    .addField("Registered on Discord", target.getUser().getCreationTime().atZoneSameInstant(ZoneId.of("Europe/Paris")).format(dateTimeFormatter), true)
                    .addField("Roles", String.join(" ", target.getRoles().stream().map(Role::getAsMention).collect(Collectors.toList())), false)
                    .addField(gametype, game, false)
                    .setImage(image)
                    .setColor(target.getColor());
        } else {
            embedBuilder.clear().setAuthor(target.getEffectiveName(), null, target.getUser().getAvatarUrl())
                    .addField("Usertag", target.getEffectiveName() + "#" + target.getUser().getDiscriminator(), true)
                    .addField("Nickname", nickname, true)
                    .addField("ID", target.getUser().getId(), true)
                    .addField("Status", status, true)
                    .addField("Joined this Server", target.getJoinDate().atZoneSameInstant(ZoneId.of("Europe/Paris")).format(dateTimeFormatter), true)
                    .addField("Registered on Discord", target.getUser().getCreationTime().atZoneSameInstant(ZoneId.of("Europe/Paris")).format(dateTimeFormatter), true)
                    .addField("Roles", String.join(" ", target.getRoles().stream().map(Role::getAsMention).collect(Collectors.toList())), false)
                    .addField(gametype, game, false)
                    .setImage(image)
                    .setColor(target.getColor())
                    .setThumbnail(target.getUser().getAvatarUrl());
        }
            sendMessage(textChannel, Messages.Type.NO_TYPE, "Information about " + target.getAsMention(),false, embedBuilder).queue();
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
            return null;
        }
    }
