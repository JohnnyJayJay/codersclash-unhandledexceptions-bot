package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.mute.MuteManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

/**
 * @author TheRealYann
 */

public class MuteCommand implements ICommand {


    private MuteManager manager;

    public MuteCommand(MuteManager manager) {
        this.manager = manager;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MANAGE_ROLES))
            return;
        if (Permissions.getPermissionLevel(member) >= 3) {
            if (args.length >= 1 && event.getCommand().getJoinedArgs().matches("<@!?\\d+>( .+)?") && !event.getMessage().getMentionedMembers().isEmpty()) {
                var reason = String.join(" ", Arrays.asList(args).subList(1, args.length));
                var muteState = manager.getMuteState(event.getGuild());
                User targetUser = event.getMessage().getMentionedMembers().get(0).getUser();
                if (muteState.isUserMuted(targetUser)) {
                    muteState.unMuteUser(targetUser);
                    sendMessage(channel, Type.SUCCESS, format("Successfully unmuted `%#s`.", targetUser)).queue();
                } else {
                    muteState.mute(targetUser);
                    sendMessage(channel, Type.SUCCESS, args.length == 1
                            ? format("Successfully muted `%#s` by %s", targetUser, member.getAsMention())
                            : format("Successfully muted `%#s` for ```%n%s``` by %s", targetUser, reason, member.getAsMention()), true).queue();
                }
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            noPermissionsMessage(channel, member);
        }
    }

    @Override
    public String info (Member member){
        String prefix = Bot.getPrefix(member.getGuild().getIdLong());
        int permLevel = Permissions.getPermissionLevel(member);
        return permLevel < 3
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `3`\nYour permission " +
                "level: `" + permLevel + "`"
                : format("**Description**: Mutes a member so that he can't write in the whole guild.\n\n" +
                "**Usage**: `%s[mute|silence] @Member <reason>`\n\n**Permission level**: `3`", prefix, prefix);
    }
}