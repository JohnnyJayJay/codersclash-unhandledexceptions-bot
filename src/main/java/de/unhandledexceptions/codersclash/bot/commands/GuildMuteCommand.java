package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.mute.MuteManager;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Messages.*;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class GuildMuteCommand implements ICommand {

    private final MessageEmbed MUTING_GUILD = new EmbedBuilder().setDescription("Muting guild...").setColor(Type.WARNING.getColor()).setFooter(Type.WARNING.getFooter(), Type.WARNING.getFooterUrl()).build();
    private MuteManager manager;

    public GuildMuteCommand(MuteManager manager) {
        this.manager = manager;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_WRITE, Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL))
            return;

        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length == 0) {
                var muteState = manager.getMuteState(event.getGuild());
                if (muteState.isGuildMuted()) {
                    muteState.unMuteGuild();
                    channel.delete().queue();
                } else {
                    sendMessage(channel, Type.WARNING, "Are you sure? This will result in a completely muted guild.").queue((msg) -> {
                        Reactions.newYesNoMenu(member.getUser(), msg, (yes) -> {
                            msg.clearReactions().queue();
                            msg.editMessage(MUTING_GUILD).queue();
                            muteState.muteGuild(member);
                        }, (no) -> msg.delete().queue());
                    });
                }
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    @Override
    public String info(Member member) {
        // TODO info
        return " ";
    }
}
