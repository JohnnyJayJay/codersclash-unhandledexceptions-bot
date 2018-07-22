package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.core.reactions.Reactions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import de.unhandledexceptions.codersclash.bot.util.Messages.Type;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

import static de.unhandledexceptions.codersclash.bot.util.Messages.wrongUsageMessage;

/**
 * @author Johnny_JayJay
 */

public class InviteCommand implements ICommand {

    private Config config;

    public InviteCommand(Config config) {
        this.config = config;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 1) {
            if (args.length == 0) {
            Messages.sendMessage(channel, Type.QUESTION, "What invite do you wish to get?\n\n\uD83E\uDD16 Give me the bot's invite!\n\uD83D\uDCE1 The invite for this guild!").queue((msg) -> {
                List.of(Reactions.BOT, Reactions.SATTELITE, Reactions.NO_EMOTE).forEach((reaction) -> msg.addReaction(reaction).queue());
                Reactions.newMenu(member.getUser(), msg, (reaction) -> {
                    if (reaction.equals(Reactions.BOT)) {
                        msg.clearReactions().queue();
                        String botInvite = "[Click here!](" + event.getJDA().asBot().getInviteUrl(Permission.ADMINISTRATOR) + ")";
                        var builder = new EmbedBuilder().addField("Invite me to your guild as well!\n", botInvite, true).setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                                .setColor(event.getGuild().getSelfMember().getColor()).setDescription("\uD83E\uDD16  **" + config.getBotName() + "**").setTitle("Bot Invite");
                        msg.editMessage(builder.build()).queue();
                    } else if (reaction.equals(Reactions.SATTELITE)) {
                        msg.clearReactions().queue();
                        channel.createInvite().queue((invite) -> {
                            var builder = new EmbedBuilder().addField("Invite for this guild:", invite.getURL(), true).setThumbnail(event.getGuild().getIconUrl())
                                    .setColor(event.getGuild().getSelfMember().getColor()).setDescription("\uD83D\uDCE1 **" + event.getGuild().getName() + "**").setTitle("Guild Invite");
                            msg.editMessage(builder.build()).queue();
                        });
                    } else if (reaction.equals(Reactions.NO_EMOTE)) {
                        msg.delete().queue();
                    }
                }, List.of(Reactions.BOT, Reactions.SATTELITE));
            });
            } else {
                wrongUsageMessage(channel, member, this);
            }
        } else {
            Messages.noPermissionsMessage(channel, member);
        }
    }

    @Override
    public String info(Member member) {
        int permLevel = Permissions.getPermissionLevel(member);
        return permLevel < 1
                ? "Sorry, but you do not have permission to execute this command, so command help won't help you either :( \nRequired permission level: `1`\nYour permission " +
                "level: `" + permLevel + "`"
                : "**Description**: Opens the invite dialogue where you can choose between an invite for the bot or this guild.\n\n**Usage**: `" + Bot.getPrefix(member.getGuild().getIdLong())
                + "invite`\n\n**Permission level**: `1`";
    }
}
