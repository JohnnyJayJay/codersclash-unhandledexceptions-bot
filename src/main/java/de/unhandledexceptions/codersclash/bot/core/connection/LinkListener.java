package de.unhandledexceptions.codersclash.bot.core.connection;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashSet;
import java.util.Set;

import static de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

/**
 * @author Johnny_JayJay
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
            String addition = "";
            if (link.remove(guild)) {
                links.remove(link);
                addition = "You are the last guild in this link, so it will be closed.";
            }
            Guild rest;
            for (long id : link.getGuilds()) {
                rest = shardManager.getGuildById(id);
                sendMessage(rest.getTextChannelById(link.getLinkedChannel(rest)), Type.INFO, "Guild `" + guild + "` has left the link. " + addition).queue();
            }
        });
    }

    public void addLink(Link link) {
        links.add(link);
    }

    public void removeLink(Link link) {
        links.remove(link);
    }

    public boolean containsLink(Link link) {
        return links.contains(link);
    }
}