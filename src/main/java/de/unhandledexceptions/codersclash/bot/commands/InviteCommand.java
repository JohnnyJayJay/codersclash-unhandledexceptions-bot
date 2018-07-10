package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Map;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class InviteCommand implements ICommand {

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (args.length > 0 || !event.getGuild().getSelfMember().hasPermission(channel, Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 1) {
            Messages.sendMessage(channel, Messages.Type.QUESTION, "What invite do you wish to get?\n\n\uD83E\uDD16 Give me the bot's invite!\n\uD83D\uDCE1 The invite for this guild!").queue((msg) -> {
                Reactions.newMenu(msg, member.getUser(), Map.of(
                        "\uD83E\uDD16", (v) -> {
                            msg.delete().queue();
                            channel.sendMessage(String.format("Invite try-catch to your guild as well:\nhttps://discordapp.com/api/oauth2/authorize?client_id=%d&permissions=8&scope=bot", event.getJDA().getSelfUser().getIdLong())).queue();
                        }, "\uD83D\uDCE1", (v) -> {
                            msg.delete().queue();
                            channel.createInvite().setTemporary(false).queue((invite) -> channel.sendMessage("Invite for this guild:\n" + invite.getURL()).queue());
                        }));
            });
        } else {
            Messages.noPermissionsMessage(channel, member);
        }
    }
}
