package de.unhandledexceptions.codersclash.bot.core.connection;

import com.google.common.collect.HashBiMap;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collection;
import java.util.Map;

/**
 * @author Johnny_JayJay
 */
public class LinkManager {

    private ShardManager shardManager;

    public LinkManager(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public Link createLink() {
        return new LinkImpl();
    }

    public void addGuild(Link link, Guild guild) {
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            guild.getController().createTextChannel("connection").queue(
                    (channel) -> link.addChannel((TextChannel) channel));
        }
    }

    public void removeGuild(Link link, Guild guild, boolean deleteChannel) {
        if (deleteChannel && guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            var channel = shardManager.getTextChannelById(link.getLinkedChannel(guild));
            channel.delete().queue((v) -> link.remove(guild));
        } else {
            link.remove(guild);
        }
    }

    private class LinkImpl implements Link {

        private Map<Long, Long> channelIds; // K: Guild id, V: Channel id

        private LinkImpl() {
            this.channelIds = HashBiMap.create();
        }

        @Override
        public void distributeMessage(Message message) {
            if (message.getAuthor().isBot() || message.getAuthor().isFake())
                return;

            Long linkedChannel = channelIds.get(message.getGuild().getIdLong());
            if (linkedChannel != null && message.getChannel().getIdLong() == linkedChannel)
                send(message.getGuild(), String.format("*%#s (from %s):* %s", message.getAuthor(), message.getGuild().getName(), message.getContentDisplay()));
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
                    System.out.println(channel);
                    if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
                        channel.sendMessage(text).queue();
                }
            });
        }
    }
}