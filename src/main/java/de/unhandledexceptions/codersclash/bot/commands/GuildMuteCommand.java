package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashSet;
import java.util.Set;

import static de.unhandledexceptions.codersclash.bot.util.Messages.noPermissionsMessage;
import static de.unhandledexceptions.codersclash.bot.util.Messages.sendMessage;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class GuildMuteCommand implements ICommand {

    private CommandSettings settings;
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
                    // TODO mit Rolle
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
                    sendMessage(channel, Type.WARNING, "This will have immediate effect and will result in a completely muted guild.\nAre you sure?").queue((msg) -> {
                        Reactions.newYesNoMenu(msg, member.getUser(), (v) -> {
                            msg.delete().queue();
                            sendMessage(channel, Type.WARNING, "Muting guild...").queue();
                            muteGuild(member);
                        }, Reactions.NOTHING);
                    });
                }
            } else {
                sendMessage(channel, Type.WARNING, "Wrong Usage. Command info:\n\n" + this.info(member)).queue();
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    private void muteGuild(Member member) {
        var guild = member.getGuild();
        guild.getController().createTextChannel("guild-mute").queue((textChannel) -> {
            // TODO mit Rolle
            for (var m : guild.getMemberCache()) {
                for (var channel : guild.getTextChannelCache()) {
                    channel.putPermissionOverride(m).setDeny(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION).queue();
                }
                for (var channel : guild.getVoiceChannelCache()) {
                    channel.putPermissionOverride(m).setDeny(Permission.VOICE_SPEAK, Permission.VOICE_CONNECT).queue();
                }
            }
            textChannel.putPermissionOverride(member).setAllow(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION).queue();
            sendMessage((TextChannel) textChannel, Type.SUCCESS, String.format("This guild has been muted. To unmute the guild, please type `%s[guildmute|muteguild|lockdown]` " +
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
}
