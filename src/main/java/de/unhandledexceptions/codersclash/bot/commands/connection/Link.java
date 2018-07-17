package de.unhandledexceptions.codersclash.bot.commands.connection;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public interface Link {

    void distributeMessage(Message message);

    boolean isLinked(TextChannel channel);

    void addChannel(TextChannel channel);

    boolean removeChannel(TextChannel channel);
}
