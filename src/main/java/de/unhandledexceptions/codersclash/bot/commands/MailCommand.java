package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Messages;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class MailCommand implements ICommand {

    private Database database;

    public MailCommand(Database database) {
        this.database = database;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_WRITE))
            return;

        if (Permissions.getPermissionLevel(member) >= 4) {
            if (args.length > 1) {

            } else {
                channel.sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription(info(member)).build()).queue();
            }
        } else {
            Messages.noPermissionsMessage(channel, member);
        }
    }

    // TODO
    @Override
    public String info(Member member) {
        return null;
    }
}
