package de.unhandledexceptions.codersclash.bot.core.connection;

import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

/**
 * @author Johnny_JayJay
 */
public class LinkManager {

    private ShardManager shardManager;
    private LinkListener linkListener;

    public LinkManager(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public Link createLink() {
        return new LinkImpl();
    }

    public void addGuild(Link link, Guild guild) {
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            guild.getController().createTextChannel("connection").queue(
                    (channel) -> {
                        link.addChannel((TextChannel) channel);
                        Guild linkedGuild;
                        for (var guildId : link.getGuilds()) {
                            linkedGuild = shardManager.getGuildById(guildId);
                            sendMessage(linkedGuild.getTextChannelById(link.getLinkedChannel(linkedGuild)), Type.INFO, "Guild `" + guild + "` joined the link!").queue();
                        }
                    });
        }
    }

    public void removeGuild(Link link, Guild guild, boolean deleteChannel) {
        if (deleteChannel && guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            var channel = shardManager.getTextChannelById(link.getLinkedChannel(guild));
            channel.delete().queue();
        } else {
            Guild linkedGuild;
            for (var guildId : link.getGuilds()) {
                if (guildId != guild.getIdLong()) {
                    linkedGuild = shardManager.getGuildById(guildId);
                    sendMessage(linkedGuild.getTextChannelById(link.getLinkedChannel(linkedGuild)), Messages.Type.INFO, "Guild `" + guild + "` left the link!").queue();
                }
            }
            if (link.remove(guild)) {
                link.getGuilds().stream().findFirst().ifPresent((id) -> {
                    var left = shardManager.getGuildById(id);
                    sendMessage(left.getTextChannelById(link.getLinkedChannel(left)), Type.INFO, "You are the last guild in this link, so it will be closed").queue();
                });
                linkListener.removeLink(link);
            }
        }
    }

    public void setLinkListener(LinkListener linkListener) {
        this.linkListener = linkListener;
    }

    private class LinkImpl implements Link {

        private Map<Long, Long> channelIds; // K: Guild id, V: Channel id

        private LinkImpl() {
            this.channelIds = new HashMap<>();
        }

        @Override
        public void distributeMessage(Message message) {
            if (message.getAuthor().isBot() || message.getAuthor().isFake())
                return;

            Long linkedChannel = channelIds.get(message.getGuild().getIdLong());
            if (linkedChannel != null && message.getChannel().getIdLong() == linkedChannel)
                send(message.getGuild(), String.format("`%#s` :satellite: **%s:** %s", message.getAuthor(), message.getGuild().getName(), message.getContentDisplay()));
        }

        @Override
        public Collection<Long> getGuilds() {
            return channelIds.keySet();
        }

        @Override
        public long getLinkedChannel(Guild guild) {
            return channelIds.get(guild.getIdLong());
        }

        @Override
        public void addChannel(TextChannel channel) {
            channelIds.put(channel.getGuild().getIdLong(), channel.getIdLong());
        }

        @Override
        public boolean remove(Guild guild) {
            channelIds.remove(guild.getIdLong(), this.getLinkedChannel(guild));
            return channelIds.size() < 2;
        }

        private void send(Guild source, String text) {
            channelIds.forEach((guildId, channelId) -> {
                if (source.getIdLong() != guildId) {
                    var channel = shardManager.getGuildById(guildId).getTextChannelById(channelId);
                    if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
                        channel.sendMessage(text).queue();
                }
            });
        }
    }
}