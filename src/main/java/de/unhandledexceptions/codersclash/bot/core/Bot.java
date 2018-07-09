package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import de.unhandledexceptions.codersclash.bot.commands.ClearCommand;
import de.unhandledexceptions.codersclash.bot.commands.ReportCommand;
import de.unhandledexceptions.codersclash.bot.commands.XPCommand;
import de.unhandledexceptions.codersclash.bot.listeners.DatabaseListener;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.awt.Color;

public class Bot {

    private int failCount;

    private final Config config;
    private final Database database;

    private DefaultShardManagerBuilder builder;
    private ShardManager shardManager;
    private static CommandSettings commandSettings;

    private static Logger logger = Logging.getLogger();

    public Bot(Config config, Database database) {
        this.failCount = 0;
        this.config = config;
        this.database = database;
        this.builder = new DefaultShardManagerBuilder();
    }

    public void start() {
        builder.setAutoReconnect(true)
                .setShardsTotal(config.getMaxShards())
                .setToken(config.getToken());
        try {
            this.shardManager = builder.build();
            logger.info("ShardManager has been built.");
        } catch (LoginException e) {
            if (++failCount < 3) {
                logger.error("Login failed, reloading... (Check your token in config.json)");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {}
                this.start();
            } else {
                logger.error("Login failed after 3 times. Exiting the program");
                System.exit(1);
            }

        }
        this.commandSettings = new CommandSettings(config.getPrefix(), this.shardManager, true);
        logger.info("CommandSettings are being configured");
        // command settings einstellen
        database.getPrefixes().forEach((id, prefix) -> commandSettings.setCustomPrefix(id, prefix));
        commandSettings.addHelpLabels("help", "helpme", "commands")
                .setHelpCommandColor(Color.YELLOW)
                .put(new ClearCommand(commandSettings), "clear", "clean", "delete")
                .put(new Permissions(commandSettings, database), "permission", "perms", "perm")
                .put(new XPCommand(commandSettings, database), "xp", "level", "lvl")
                .put(new ReportCommand(database), "report", "rep")
                .activate();

        this.shardManager.addEventListener(new XPCommand(commandSettings, database), new DatabaseListener(database));
    }

    public void shutdown() {
        logger.warn("Bot is shutting down...");
        shardManager.shutdown();
    }

    public ShardManager getAPI() {
        return shardManager;
    }

    public CommandSettings getCommandSettings() {
        return commandSettings;
    }

    public static String getPrefix(long guildId) {
        return commandSettings.getPrefix(guildId);
    }
}
