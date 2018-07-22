package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
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

import static java.lang.String.format;

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
        if (channels.contains(left) && left.getMembers().isEmpty()) {
            left.delete().queue((v) -> channels.remove(left));
            if (!event.getGuild().getTextChannelsByName("channel-by-" + event.getMember().getUser().getName().toLowerCase(), false).isEmpty()) {
                System.out.print("hier");
                event.getGuild().getTextChannelsByName("channel-by-" + event.getMember().getUser().getName().toLowerCase(), false).get(0).delete().queue();
            }
        }
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
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            guild.getController().createVoiceChannel("Channel by " + member.getUser().getName())
                    .setUserlimit(channelJoined.getUserLimit())
                    .setParent(channelJoined.getParent())
                    .queue((channel) -> {
                        channels.add((VoiceChannel) channel);
                        guild.getController().moveVoiceMember(member, (VoiceChannel) channel).queue();
                        channel.createPermissionOverride(member).setAllow(Permission.ALL_CHANNEL_PERMISSIONS).queue();
                        guild.getController().createTextChannel("channel-by-" + member.getUser().getName())
                                .setTopic(format("This Channel is linked to the same-named Voice Channel %s %s (%s) by %s. Only people that joined the Voice Channel have access to this one.", Reactions.SPEAKER,
                                        channel.getName(), channel.getId(), member.getUser())).queue((textChannel) -> {
                                    textChannel.createPermissionOverride(guild.getPublicRole()).setDeny(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE).queue();
                                    textChannel.createPermissionOverride(member).setAllow(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE).queue();
                                }

                        );

                    });
        }
    }
}
