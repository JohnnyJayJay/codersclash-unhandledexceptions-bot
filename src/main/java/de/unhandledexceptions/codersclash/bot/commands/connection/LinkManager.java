package de.unhandledexceptions.codersclash.bot.commands.connection;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collection;
import java.util.Set;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class LinkManager {

    private ShardManager shardManager;

    public LinkManager(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public Link createLink(Set<Long> guilds) {
        return new LinkImpl(guilds);
    }

    public void addGuild(Link link, Guild guild) {
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            guild.getController().createTextChannel("connection").setTopic("Connection to " + link.getGuilds()).queue(
                    (channel) -> link.addChannel((TextChannel) channel));
        }
    }

    private class LinkImpl implements Link {

        private MessageBuilder builder;
        private BiMap<Long, Long> channelIds; // K: Guild id, V: Channel id
        private Set<Long> guilds;

        private LinkImpl(Set<Long> guilds) {
            this.builder = new MessageBuilder();
            this.guilds = guilds;
            this.channelIds = HashBiMap.create();
        }

        @Override
        public void distributeMessage(Message message) {
            if (message.getAuthor().isBot())
                return;

            TextChannel channel;
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
        public long getLinkedGuild(TextChannel channel) {
            return channelIds.inverse().get(channel.getIdLong());
        }

        @Override
        public void addChannel(TextChannel channel) {
            guilds.add(channel.getGuild().getIdLong());
            channelIds.put(channel.getGuild().getIdLong(), channel.getIdLong());
        }

        @Override
        public boolean remove(Guild guild) {
            send(guild, "Guild `" + guild + "` has left the link.");
            guilds.remove(guild.getIdLong());
            channelIds.remove(guild.getIdLong(), this.getLinkedChannel(guild));
            return channelIds.size() < 2;
        }

        private void send(Guild source, String text) {
            channelIds.forEach((guildId, channelId) -> {
                if (!channelIds.get(guildId).equals(channelId)) {
                    shardManager.getTextChannelById(channelIds.get(guildId)).sendMessage(text).queue();
                }
            });
        }
    }

}
