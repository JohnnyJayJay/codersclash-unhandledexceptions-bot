package de.unhandledexceptions.codersclash.bot.util;

import com.github.johnnyjayjay.discord.commandapi.ICommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Messages {

    private static MessageEmbed buildMessage(Type type, String content, String title, boolean timestamp, EmbedBuilder embedBuilder) {
        embedBuilder.appendDescription(content);
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

    public static MessageAction sendMessage(MessageChannel channel, Type type, String content, boolean timestamp, EmbedBuilder embedBuilder) {
        return channel.sendMessage(buildMessage(type, content, null, timestamp, embedBuilder));
    }

    public static Consumer<Throwable> defaultFailure(MessageChannel channel) {
        return (throwable) -> sendMessage(channel, Type.WARNING, String.format("Something went wrong (this may not be relevant):\n```\n%s```", throwable.getMessage())).queue();
    }

    public static void noPermissionsMessage(MessageChannel channel, Member member) {
        sendMessage(channel, Type.ERROR, "You do not have permission to execute this command. " + member.getAsMention()).queue((msg) -> msg.delete().queueAfter(7, TimeUnit.SECONDS));
    }

    public static void wrongUsageMessage(MessageChannel channel, Member member, ICommand command) {
        sendMessage(channel, Type.WARNING, "Wrong usage. Command Info:\n\n" + command.info(member)).queue();
    }

    public static void deleteAfterFiveSec(Message message) {
        message.delete().queueAfter(5, TimeUnit.SECONDS);
    }

    public enum Type {
        SHUTDOWN("Shutdown", "https://cdn.pixabay.com/photo/2014/03/25/15/22/power-296626_960_720.png", Color.RED),
        INFO("Information", "https://vignette.wikia.nocookie.net/dragonvale/images/f/fd/Blue_Information_Sign.png/revision/latest?cb=20120415234017", Color.CYAN),
        WARNING("Warning", "https://i.imgur.com/ozSen6U.png", Color.YELLOW),
        ERROR("Error", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/97/Dialog-error-round.svg/2000px-Dialog-error-round.svg.png", Color.RED),
        SUCCESS("Success", "https://cdn.pixabay.com/photo/2012/04/11/17/44/check-mark-29114_960_720.png", Color.GREEN),
        QUESTION("Question", "https://cdn1.iconfinder.com/data/icons/web-interface-part-2/32/circle-question-mark-512.png", new Color(60,132,167)), //Hell: 70, 159, 204 Dunkel: 60,132,167
        DEFAULT(null, null, Color.WHITE),
        NO_TYPE(null, null, null);

        private final String footer, footerUrl;
        private final Color color;

        Type(String footer, String footerUrl, Color color) {
            this.footer = footer;
            this.footerUrl = footerUrl;
            this.color = color;
        }

        public String getFooter() {
            return footer;
        }

        public String getFooterUrl() {
            return footerUrl;
        }

        public Color getColor() {
            return color;
        }
    }
}
