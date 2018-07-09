package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class SettingsCommand implements ICommand {

    private Database database;

    public SettingsCommand(Database database) {
        this.database = database;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {

            }
        }
    }

    // TODO
    @Override
    public String info(Member member) {
        return null;
    }
}
