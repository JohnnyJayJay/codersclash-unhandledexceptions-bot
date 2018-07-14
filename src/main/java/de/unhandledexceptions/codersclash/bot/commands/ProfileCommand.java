package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.time.format.DateTimeFormatter;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

public class ProfileCommand implements ICommand {

    @Override
    public void onCommand(CommandEvent commandEvent, Member member, TextChannel textChannel, String[] strings) {
        if (commandEvent.getMessage().getMentionedMembers().size()==1) {
            member = commandEvent.getMessage().getMentionedMembers().get(0);
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        var nickname = ((member.getNickname()!=null) ? member.getNickname() : "none" );
        var game = ((member.getGame().getName()!=null)? member.getGame().getName() : "none" );
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("kk:mm:ss dd.MM.yyyy");
        embedBuilder.addField("Name", member.getUser().getName(), true)
                .addField("Nickname", nickname, true)
                .addField("ID", member.getUser().getId(), true)
                .addField("Game", game, true)
                .addField("Joined DiscordServer", member.getJoinDate().format(dateTimeFormatter), true)
                .addField("Joined Discord", member.getUser().getCreationTime().format(dateTimeFormatter), true)
                .addField("Onlinestatus", member.getOnlineStatus().getKey(), true)
                .setColor(Color.CYAN)
                .setThumbnail(member.getUser().getAvatarUrl());

        sendMessage(textChannel, Messages.Type.INFO, "Here the profile from "+member.getAsMention(),
                "Profile", true, embedBuilder).queue();
    }

    @Override
    public String info(Member member) {
        return null;
    }
}
