package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.core.Permissions;
import de.unhandledexceptions.codersclash.bot.util.Reactions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import static de.unhandledexceptions.codersclash.bot.util.Messages.*;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class SettingsCommand implements ICommand {

    private Database database;
    private CommandSettings settings;

    public SettingsCommand(Database database, CommandSettings settings) {
        this.database = database;
        this.settings = settings;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        var guild = event.getGuild();
        if (!guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION))
            return;
        if (Permissions.getPermissionLevel(member) < 5) {
            noPermissionsMessage(channel, member);
            return;
        }

        // TODO evtl mit mehr menüs
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "prefix":
                    if (args.length == 1) {
                        sendMessage(channel, Type.INFO, String.format("This guild's prefix is: `%s`", settings.getPrefix(guild.getIdLong()))).queue();
                    } else if (args.length == 2) {
                        if (args[1].matches(CommandSettings.VALID_PREFIX) && args[1].length() <= 40) {
                            sendMessage(channel, Type.WARNING, String.format("Do you want to set your prefix from `%s` to `%s`?", settings.getPrefix(guild.getIdLong()), args[1]))
                                    .queue((msg) -> Reactions.newYesNoMenu(msg, member.getUser(),
                                            (v) -> {
                                                msg.delete().queue();
                                                settings.setCustomPrefix(guild.getIdLong(), args[1]);
                                                database.setPrefix(guild.getIdLong(), args[1]);
                                                sendMessage(channel, Type.SUCCESS, "Successfully set `" + args[1] + "` as the new prefix!").queue();
                                            }, Reactions.NOTHING));
                        } else {
                            sendMessage(channel, Type.WARNING, String.format("The prefix `%s` is not valid! Remember, prefixes **cannot** contain these symbols: `? * + \\ ^ " +
                                            "| $`\nAlso, the prefix cannot be longer than 40 characters.", args[1])).queue();
                        }
                    } else {
                        sendMessage(channel, Type.INFO, "Wrong usage. Command info:\n\n" + info(member)).queue();
                    }
            }
        }
    }

    private void nothing(Void voit){}

    // TODO
    @Override
    public String info(Member member) {
        return " ";
    }
}
