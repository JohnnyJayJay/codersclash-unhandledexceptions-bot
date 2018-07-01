package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import de.unhandledexceptions.codersclash.bot.commands.ClearCommand;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class Bot {

    private int failCount;

    private final Config config;
    private final Database database;

    private DefaultShardManagerBuilder builder;
    private ShardManager shardManager;
    private CommandSettings commandSettings;

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
        try
        {
            this.shardManager = builder.build();
        } catch (LoginException e)
        {
            if (++failCount < 3) {
                System.err.println("[ERROR] Login failed, reloading... (Check your token in config.json)");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {}
                start();
            } else {
                System.err.println("[ERROR] Login failed after 3 times. Exiting the program");
                System.exit(1);
            }

        }

        this.commandSettings = new CommandSettings(config.getPrefix(), this.shardManager, true);
        // command settings einstellen
        commandSettings.setHelpLabels("help", "helpme", "commands")
                .put(new ClearCommand(commandSettings), "clear", "clean", "delete")
                .put(new Permissions(commandSettings, database), "permission", "perms", "perm")
                .activate();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public CommandSettings getCommandSettings() {
        return commandSettings;
    }
}
