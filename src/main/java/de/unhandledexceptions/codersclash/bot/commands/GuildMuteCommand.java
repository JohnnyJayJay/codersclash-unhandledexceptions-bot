package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.listeners.ReadyListener;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class GuildMuteCommand implements ICommand {

    private CommandSettings settings;

    private final String YES_EMOTE = "\u2705";
    private final String NO_EMOTE = "\u274C";
    private Set<Long> guildIds;

    public GuildMuteCommand(CommandSettings settings) {
        this.settings = settings;
        this.guildIds = new HashSet<>();
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_WRITE, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL))
            return;

        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length == 0) {
                if (guildIds.contains(event.getGuild().getIdLong())) {
                    guildIds.remove(event.getGuild().getIdLong());
                    channel.delete().queue((v) -> {
                        var guild = event.getGuild();
                        PermissionOverride override;
                        for (var m : guild.getMemberCache()) {
                            for (var tChannel : guild.getTextChannelCache()) {
                                if ((override = tChannel.getPermissionOverride(m)) != null)
                                    override.delete().queue();
                            }
                            for (var vChannel : guild.getVoiceChannelCache()) {
                                if ((override = vChannel.getPermissionOverride(m)) != null)
                                    override.delete().queue();
                            }
                        }
                    });
                } else {
                    Messages.sendMessage(channel, Type.WARNING, "This will have immediate effect and will result in a completely muted guild.\nAre you sure?").queue((msg) -> {
                        msg.addReaction(YES_EMOTE).queue();
                        msg.addReaction(NO_EMOTE).queue();
                        event.getJDA().addEventListener(new ReactionListener(msg.getIdLong(), member.getUser().getIdLong()));
                    });
                }
            } else {
                Messages.sendMessage(channel, Type.WARNING, "Wrong Usage. Command info:\n\n" + this.info(member)).queue();
            }
        } else {
            Messages.noPermissionsMessage(channel, member);
        }
    }

    private void muteGuild(Member member) {
        var guild = member.getGuild();
        guild.getController().createTextChannel("guild-mute").queue((textChannel) -> {
            for (var m : guild.getMemberCache()) {
                for (var channel : guild.getTextChannelCache()) {
                    channel.putPermissionOverride(m).setDeny(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION).queue();
                }
                for (var channel : guild.getVoiceChannelCache()) {
                    channel.putPermissionOverride(m).setDeny(Permission.VOICE_SPEAK, Permission.VOICE_CONNECT).queue();
                }
            }
            textChannel.putPermissionOverride(member).setAllow(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION).queue();
            Messages.sendMessage((TextChannel) textChannel, Type.SUCCESS, String.format("This guild has been muted. To unmute the guild, please type `%s[guildmute|muteguild]` " +
                            "again.", Bot.getPrefix(guild.getIdLong())), true).queue();
            ((TextChannel) textChannel).sendMessage(member.getAsMention()).queue();
            guildIds.add(guild.getIdLong());
        });
    }

    @Override
    public String info(Member member) {
        // TODO
        return " ";
    }

    private class ReactionListener extends ListenerAdapter {

        private final long messageId;
        private final long userId;

        public ReactionListener(long messageId, long userId) {
            this.messageId = messageId;
            this.userId = userId;
        }

        @Override
        public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
            if (event.getMessageIdLong() == messageId) {
                if (event.getUser().isBot())
                    return;

                if (event.getUser().getIdLong() == userId) {
                    switch (event.getReactionEmote().getName()) {
                        case YES_EMOTE:
                            Messages.sendMessage(event.getChannel(), Type.WARNING, "Muting guild...").queue();
                            muteGuild(event.getMember());
                        case NO_EMOTE:
                            event.getChannel().getMessageById(event.getMessageIdLong()).queue((msg) -> msg.delete().queue());
                            event.getJDA().removeEventListener(this);
                        default:
                            event.getReaction().removeReaction(event.getUser()).queue();
                    }
                } else
                    event.getReaction().removeReaction(event.getUser()).queue();
            }
        }
    }
}
