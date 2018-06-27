package de.unhandledexceptions.codersclash.bot.listener;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class VoiceChannelListener extends ListenerAdapter {

    private final String logchannel = "logchannel"; // muss noch durch fertigen Logchannel (aus Config) ersetzt werden

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {

        if(event.getMember().getUser().isBot())return;

        if (event.getGuild().getTextChannelsByName(logchannel, false).isEmpty())
            return;

       // entsprechende Message in einen Channel senden oder ähnliches
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {

        if(event.getMember().getUser().isBot())return;

        if (event.getGuild().getTextChannelsByName(logchannel, false).isEmpty())
            return;

        // entsprechende Message in einen Channel senden oder ähnliches
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {

        if(event.getMember().getUser().isBot())return;

        if (event.getGuild().getTextChannelsByName(logchannel, false).isEmpty())
            return;

        // entsprechende Message in einen Channel senden oder ähnliches
    }
}
