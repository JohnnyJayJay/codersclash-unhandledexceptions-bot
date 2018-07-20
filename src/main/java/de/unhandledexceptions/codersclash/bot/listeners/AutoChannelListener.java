package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Database;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author TheRealYann
 * 20.07.2018
 */

public class AutoChannelListener extends ListenerAdapter {

    private Database database;
    private Set<VoiceChannel> channels = new HashSet<>();

    public AutoChannelListener(Database database) {
        this.database = database;
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        var joined = event.getChannelJoined();
        if (joined.getIdLong() == database.getAutoChannel(event.getGuild()))
            createChannel(joined, event.getGuild(), event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        var left = event.getChannelLeft();
        if (channels.contains(left) && left.getMembers().isEmpty())
            left.delete().queue((v) -> channels.remove(left));
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        var left = event.getChannelLeft();
        var joined = event.getChannelJoined();
        if (channels.contains(left) && left.getMembers().isEmpty())
            left.delete().queue((v) -> channels.remove(left));
        if (joined.getIdLong() == database.getAutoChannel(event.getGuild()))
            createChannel(joined, event.getGuild(), event.getMember());
    }

    private void createChannel(VoiceChannel channelJoined, Guild guild, Member member) {
        guild.getController().createVoiceChannel("Channel by " + member.getEffectiveName())
                .setUserlimit(channelJoined.getUserLimit())
                .setParent(channelJoined.getParent())
                .queue((channel) -> {
                    channels.add((VoiceChannel) channel);
                    guild.getController().moveVoiceMember(member, (VoiceChannel) channel).queue();
                    channel.createPermissionOverride(member).setAllow(Permission.ALL_CHANNEL_PERMISSIONS).queue();
                });
    }
}
