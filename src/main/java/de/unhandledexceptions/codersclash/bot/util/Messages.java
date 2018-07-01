package de.unhandledexceptions.codersclash.bot.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import java.awt.Color;
import java.time.Instant;

public class Messages {

    private static MessageEmbed buildMessage(Type type, String content, String title, boolean timestamp) {
        return new EmbedBuilder()
                .setDescription(content)
                .setColor(type.color)
                .setFooter(type.footer, type.footerUrl)
                .setTitle(title)
                .setTimestamp(timestamp ? Instant.now() : null)
                .build();
    }

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content) {
        return channel.sendMessage(buildMessage(type, content, null, false));
    }

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content, String title) {
        return channel.sendMessage(buildMessage(type, content, title, false));
    }

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content, boolean timestamp) {
        return channel.sendMessage(buildMessage(type, content, null, timestamp));
    }

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content, String title, boolean timestamp) {
        return channel.sendMessage(buildMessage(type, content, title, timestamp));
    }

    public enum Type {
        INFO("Information", "https://vignette.wikia.nocookie.net/dragonvale/images/f/fd/Blue_Information_Sign.png/revision/latest?cb=20120415234017", Color.CYAN),
        WARNING("Warning", "https://previews.123rf.com/images/faysalfarhan/faysalfarhan1711/faysalfarhan171142240/89589613-exclamation-mark-icon-isolated-on-yellow-round-button-abstract-illustration.jpg", Color.YELLOW),
        ERROR("Error", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/97/Dialog-error-round.svg/2000px-Dialog-error-round.svg.png", Color.RED),
        SUCCESS("Success", "https://cdn.pixabay.com/photo/2012/04/11/17/44/check-mark-29114_960_720.png", Color.GREEN),
        DEFAULT("Message", null, Color.WHITE);

        private String footer, footerUrl;
        private Color color;

        Type(String footer, String footerUrl, Color color) {
            this.footer = footer;
            this.footerUrl = footerUrl;
            this.color = color;
        }
    }

}
