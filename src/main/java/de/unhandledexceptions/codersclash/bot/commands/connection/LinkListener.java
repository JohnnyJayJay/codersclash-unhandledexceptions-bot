package de.unhandledexceptions.codersclash.bot.commands.connection;

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

    private Set<Link> links = new HashSet<>();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        if (links.stream().anyMatch((link) -> link.isLinked(event.getChannel()))) {
            links.stream().filter((link) -> link.isLinked(event.getChannel())).forEach((link) -> link.distributeMessage(event.getMessage()));
        }
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        links.stream().filter((link) -> link.isLinked(event.getChannel())).forEach((link) -> link.removeChannel(event.getChannel()));
    }

    public void addLink(Link link) {
        links.add(link);
    }

    public void removeLink(Link link) {
        links.remove(link);
    }
}
