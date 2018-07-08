package de.unhandledexceptions.codersclash.bot.commands;

import com.github.johnnyjayjay.discord.commandapi.CommandEvent;
import com.github.johnnyjayjay.discord.commandapi.ICommand;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;

import java.util.stream.Collectors;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class ReportCommand implements ICommand {

    private Logger logger = Logging.getLogger();
    private Database database;

    public ReportCommand(Database database) {
        this.database = database;
    }

    @Override
    public void onCommand(CommandEvent event, Member member, TextChannel channel, String[] args) {
        var reports = database.getReports(member);
        channel.sendMessage("Your reports: " + reports.toString()).queue();
        logger.info(reports.toString());
    }

}
