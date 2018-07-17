package de.unhandledexceptions.codersclash.bot.commands.connection;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class LinkManager {

    public Link createLink(ShardManager shardManager, Guild... guilds) {
        Set<Long> channelIds = new HashSet<>();
        for (Guild guild : guilds) {
            guild.getController().createTextChannel("connection")
                    .setTopic("The following guilds are connected: " + String.join(", ", Arrays.stream(guilds).map(Guild::getName).collect(Collectors.toList())))
                    .queue((channel) -> channelIds.add(channel.getIdLong()));
        }
        return new LinkImpl(shardManager, channelIds);
    }

    private class LinkImpl implements Link {

        private Set<Long> channelIds;
        private ShardManager shardManager;

        private LinkImpl(ShardManager shardManager, Collection<Long> channels) {
            this.shardManager = shardManager;
            this.channelIds = new HashSet<>();
            channelIds.addAll(channels);
        }

        @Override
        public void distributeMessage(Message message) {
            channelIds.stream().filter((id) -> message.getChannel().getIdLong() != id).map(shardManager::getTextChannelById).forEach(
                    (channel) -> channel.sendMessage(message).queue());
        }

        @Override
        public boolean isLinked(TextChannel channel) {
            return channelIds.contains(channel.getIdLong());
        }

        @Override
        public void addChannel(TextChannel channel) {
            channelIds.add(channel.getIdLong());
        }

        @Override
        public boolean removeChannel(TextChannel channel) {
            channelIds.remove(channel.getIdLong());
            return !channelIds.isEmpty();
        }
    }

}
