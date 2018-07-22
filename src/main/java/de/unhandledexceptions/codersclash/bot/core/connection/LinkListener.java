package de.unhandledexceptions.codersclash.bot.core.connection;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Johnny_JayJay
 */
public class LinkListener extends ListenerAdapter {

    private Set<Link> links;
    private LinkManager linkManager;

    public LinkListener() {
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
        List<Link> linksToRemoveFrom = new ArrayList<>();
        links.stream().filter((link) -> link.getGuilds().contains(guild.getIdLong())).forEach((link) -> {
            if (link.getLinkedChannel(guild) == event.getChannel().getIdLong())
                linksToRemoveFrom.add(link);
        });
        linksToRemoveFrom.forEach((link) -> linkManager.removeGuild(link, guild, false));
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

    public void setLinkManager(LinkManager linkManager) {
        this.linkManager = linkManager;
    }
}