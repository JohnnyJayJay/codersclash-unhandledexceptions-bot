package de.unhandledexceptions.codersclash.bot.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Messages {

    private static MessageEmbed buildMessage(Type type, String content, String title, boolean timestamp, EmbedBuilder embedBuilder) {
        embedBuilder.setDescription(content);
        if (type.color != null) {
            embedBuilder.setColor(type.color);
        }
        return embedBuilder.setFooter(type.footer, type.footerUrl)
                .setTitle(title)
                .setTimestamp(timestamp ? Instant.now() : null)
                .build();
    }

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content) {
        return channel.sendMessage(buildMessage(type, content, null, false, new EmbedBuilder()));
    }

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content, String title) {
        return channel.sendMessage(buildMessage(type, content, title, false, new EmbedBuilder()));
    }

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content, boolean timestamp) {
        return channel.sendMessage(buildMessage(type, content, null, timestamp, new EmbedBuilder()));
    }

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content, String title, boolean timestamp) {
        return channel.sendMessage(buildMessage(type, content, title, timestamp, new EmbedBuilder()));
    }

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content, String title, boolean timestamp, EmbedBuilder embedBuilder) {
        return channel.sendMessage(buildMessage(type, content, title, timestamp, embedBuilder));
    }

    public static Consumer<Throwable> defaultFailure(MessageChannel channel) {
        return (throwable) -> sendMessage(channel, Type.WARNING, String.format("Something went wrong (this may not be relevant):\n```\n%s```", throwable.getMessage())).queue();
    }

    public static void noPermissionsMessage(MessageChannel channel, Member member) {
        sendMessage(channel, Type.ERROR, "You do not have permission to execute this command. " + member.getAsMention()).queue((msg) -> msg.delete().queueAfter(7, TimeUnit.SECONDS));
    }

    public enum Type {
        INFO("Information", "https://vignette.wikia.nocookie.net/dragonvale/images/f/fd/Blue_Information_Sign.png/revision/latest?cb=20120415234017", Color.CYAN),
        WARNING("Warning", "https://previews.123rf.com/images/faysalfarhan/faysalfarhan1711/faysalfarhan171142240/89589613-exclamation-mark-icon-isolated-on-yellow-round-button-abstract-illustration.jpg", Color.YELLOW),
        ERROR("Error", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/97/Dialog-error-round.svg/2000px-Dialog-error-round.svg.png", Color.RED),
        SUCCESS("Success", "https://cdn.pixabay.com/photo/2012/04/11/17/44/check-mark-29114_960_720.png", Color.GREEN),
        QUESTION("Question", "https://cdn1.iconfinder.com/data/icons/web-interface-part-2/32/circle-question-mark-512.png", Color.WHITE),
        NO_TYPE(null, null, null),
        DEFAULT("Message", null, new Color(36, 95, 233));

        private String footer, footerUrl;
        private Color color;

        Type(String footer, String footerUrl, Color color) {
            this.footer = footer;
            this.footerUrl = footerUrl;
            this.color = color;
        }
    }
}
