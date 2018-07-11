package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.Map;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;
import static java.lang.String.format;

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
                            String botInvite = "[Click here!](https://discordapp.com/api/oauth2/authorize?client_id="+ event.getGuild().getSelfMember().getUser().getIdLong() +"&permissions=8&scope=bot)";
                            channel.sendMessage(new EmbedBuilder()
                                            .setColor(Color.WHITE)
                                            .setThumbnail(event.getGuild().getSelfMember().getUser().getAvatarUrl())
                                            .setDescription("\uD83E\uDD16 **try-catch**")
                                            .addField("Invite it to your guild as well!\n", botInvite, true)
                                            .build()
                            ).queue();
                        }, "\uD83D\uDCE1", (v) -> {
                            msg.delete().queue();
                            channel.createInvite().setTemporary(false).queue((invite) -> channel.sendMessage(new EmbedBuilder()
                                             .setColor(Color.WHITE)
                                             .setThumbnail(event.getGuild().getIconUrl())
                                             .setDescription("\uD83D\uDCE1 **" + event.getGuild().getName() +"**")
                                            .addField("Invite for this guild:\n", invite.getURL() , true)
                                            .build()
                            ).queue());
                        }));
            });
        } else {
            Messages.noPermissionsMessage(channel, member);
        }
    }
}
