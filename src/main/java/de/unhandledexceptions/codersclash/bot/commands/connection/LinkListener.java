package de.unhandledexceptions.codersclash.bot.commands.connection;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class LinkListener extends ListenerAdapter {

    private Set<Link> links;
    private ShardManager shardManager;

    public LinkListener(ShardManager shardManager) {
        this.shardManager = shardManager;
        this.links = new HashSet<>();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        Guild guild = event.getGuild();
        links.stream().filter((link) -> link.getGuilds().contains(guild.getIdLong())).forEach((link) -> link.distributeMessage(event.getMessage()));
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        Guild guild = event.getGuild();
        links.stream().filter((link) -> link.getGuilds().contains(guild.getIdLong())).forEach((link) -> {
            if (link.remove(guild)) {
                var leftGuild = link.getGuilds().stream().findFirst();
                leftGuild.ifPresent(id -> {
                    var guild1 = shardManager.getGuildById(id);
                    guild1.getTextChannelById(link.getLinkedChannel(guild1)).sendMessage("Guild `" + guild + "` has left the link. You are the last guild in this link, so it will be closed.").queue();
                });
                links.remove(link);
            }
        });
    }

    public void addLink(Link link) {
        links.add(link);
    }

    public void removeLink(Link link) {
        links.remove(link);
    }
}
