package de.unhandledexceptions.codersclash.bot.core.connection;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
            guild.getController().createTextChannel("connection").setTopic("Connection to " + link.getGuilds()).queue(
                    (channel) -> link.addChannel((TextChannel) channel));
        }
    }

    public void removeGuild(Link link, Guild guild, boolean deleteChannel) {
        link.remove(guild);
        if (deleteChannel && guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            var channel = shardManager.getTextChannelById(link.getLinkedChannel(guild));
            channel.delete().queue();
        }
    }

    private class LinkImpl implements Link {

        private MessageBuilder builder;
        private BiMap<Long, Long> channelIds; // K: Guild id, V: Channel id
        private Set<Long> guilds;

        private LinkImpl() {
            this.builder = new MessageBuilder();
            this.guilds = new HashSet<>();
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
            return guilds;
        }

        @Override
        public long getLinkedChannel(Guild guild) {
            return channelIds.get(guild.getIdLong());
        }

        @Override
        public void addChannel(TextChannel channel) {
            guilds.add(channel.getGuild().getIdLong());
            channelIds.put(channel.getGuild().getIdLong(), channel.getIdLong());
        }

        @Override
        public boolean remove(Guild guild) {
            guilds.remove(guild.getIdLong());
            channelIds.remove(guild.getIdLong(), this.getLinkedChannel(guild));
            return channelIds.size() < 2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getGuilds());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            boolean equals = false;
            if (obj instanceof Link) {
                Link other = (Link) obj;
                equals =  Objects.equals(other.getGuilds(), this.getGuilds());
            }

            return equals;
        }

        private void send(Guild source, String text) {
            channelIds.forEach((guildId, channelId) -> {
                if (source.getIdLong() != guildId) {
                    var channel = shardManager.getTextChannelById(channelId);
                    if (source.getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
                        shardManager.getTextChannelById(channelId).sendMessage(text).queue();
                }
            });
        }
    }
}