package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Main;
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
import static java.lang.String.format;

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
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION))
            return;
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL))
            return;

        if (Permissions.getPermissionLevel(member) >= 5) {
            if (args.length == 0) {
                var muteState = manager.getMuteState(event.getGuild());
                if (muteState.isGuildMuted()) {
                    Main.otherThread(() -> {
                        muteState.unMuteGuild();
                        channel.delete().queue();
                    });
                } else {
                    sendMessage(channel, Type.WARNING, "Are you sure? This will result in a completely muted guild.").queue((msg) -> {
                        Reactions.newYesNoMenu(member.getUser(), msg, (yes) -> {
                            Main.otherThread(() -> {
                                msg.clearReactions().queue();
                                msg.editMessage(MUTING_GUILD).queue();
                                muteState.muteGuild(member);
                            });
                        });
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
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        return permLevel < 3
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `5`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Mutes the whole guild so no one can write.\n\n" +
                "**Usage**: `%s[muteguild|guildmute|lockdown]`\n\n**Permission level**: `5`", prefix);
    }
}
