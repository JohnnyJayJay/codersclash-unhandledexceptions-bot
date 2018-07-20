package de.unhandledexceptions.codersclash.bot.core.mute;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Roles;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.*;
import java.util.stream.Collectors;

import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

public class MuteManager {

    private Map<Long, MuteState> muteStates;
    private CommandSettings settings;
    private ShardManager shardManager;

    public MuteManager(ShardManager shardManager, CommandSettings settings) {
        this.shardManager = shardManager;
        this.settings = settings;
        shardManager.addEventListener(new MuteListener());
        this.muteStates = new HashMap<>();
    }

    public MuteState getMuteState(Guild guild) {
        var muteState = muteStates.get(guild.getIdLong());
        if (muteState == null) {
            muteState = new MuteStateImpl(guild.getIdLong());
            muteStates.put(guild.getIdLong(), muteState);
        }
        return muteState;
    }

    private class MuteListener extends ListenerAdapter {
        @Override
        public void onGuildMemberJoin(GuildMemberJoinEvent event) {
            var guild = event.getGuild();
            var muteState = muteStates.get(guild.getIdLong());

            if (muteState != null && muteState.isGuildMuted()) {
                Roles.getMutedRole(guild,
                        (role) -> guild.getController().addSingleRoleToMember(event.getMember(), role).queue(),
                        (v) -> {});
            }
        }

        @Override
        public void onTextChannelCreate(TextChannelCreateEvent event) {
            var muteState = muteStates.get(event.getGuild().getIdLong());
            if (muteState != null && muteState.isGuildMuted())
                settings.addChannelToBlacklist(event.getChannel().getIdLong());
        }
    }

    private class MuteStateImpl implements MuteState {
        private boolean guildMuted;
        private Set<Long> mutes;
        private long guildId;

        MuteStateImpl(long guildId) {
            this.guildId = guildId;
            mutes = new HashSet<>();
        }

        @Override
        public void muteGuild(Member member) {
            var guild = getGuild();
            Roles.getMutedRole(guild, (mutedRole) -> {
                GuildController controller = guild.getController();
                controller.createTextChannel("guild-mute")
                        .addPermissionOverride(mutedRole, Collections.emptyList(), List.of(Permission.MESSAGE_READ))
                        .addPermissionOverride(member, List.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION), Collections.emptyList())
                        .addPermissionOverride(member, List.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION), Collections.emptyList())
                        .addPermissionOverride(guild.getSelfMember(), List.of(Permission.MESSAGE_WRITE), Collections.emptyList()).queue((textChannel) -> {
                    sendMessage((TextChannel) textChannel, Messages.Type.SUCCESS, String.format("This guild has been muted. To unmute the guild, please type `%s[guildmute|muteguild|lockdown]` " +
                            "again.", Bot.getPrefix(guild.getIdLong())), true).queue();
                    ((TextChannel) textChannel).sendMessage(member.getAsMention()).queue();
                    settings.addChannelsToBlacklist(guild.getTextChannelCache().stream().map(TextChannel::getIdLong).filter((TextChannel) -> TextChannel != textChannel.getIdLong()).collect(Collectors.toList()));
                    guildMuted = true;
                });
            }, (v) -> {});
        }

        @Override
        public void mute(User user) {
            var guild = getGuild();
            Roles.getMutedRole(guild,
                    (role) -> guild.getController().addSingleRoleToMember(guild.getMember(user), role).queue((v) -> mutes.add(user.getIdLong())),
                    (v) -> {});
        }

        private Guild getGuild() {
            return shardManager.getGuildById(guildId);
        }

        @Override
        public void unMuteGuild() {
            var guild = getGuild();
            Roles.getMutedRole(guild, (role) -> {
                var controller = guild.getController();
                guild.getMemberCache().stream().filter((member) -> !mutes.contains(member.getUser().getIdLong()))
                        .forEach((member) -> controller.removeSingleRoleFromMember(member, role).queue());
                settings.removeChannelsFromBlackList(guild.getTextChannelCache().stream().map(TextChannel::getIdLong).collect(Collectors.toList()));
            }, (v) -> {});
        }

        @Override
        public void unMuteUser(User user) {
            var guild = getGuild();
            Roles.getMutedRole(guild,
                    (role) -> guild.getController().removeSingleRoleFromMember(guild.getMember(user), role).queue((v) -> mutes.remove(user.getIdLong())),
                    (v) -> {});
        }

        @Override
        public boolean isGuildMuted() {
            return guildMuted;
        }

        @Override
        public boolean isUserMuted(User user) {
            return mutes.contains(user.getIdLong());
        }
    }
}
