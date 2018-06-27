package de.unhandledexceptions.codersclash.bot.core;

import de.unhandledexceptions.codersclash.bot.commandapi.CommandSettings;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.utils.SessionController;
import net.dv8tion.jda.core.utils.SessionControllerAdapter;

import javax.security.auth.login.LoginException;

public class Bot {

    private final Config config;

    private final Database database;
    private DefaultShardManagerBuilder builder;
    private SessionController sessionController;
    private ShardManager shardManager;
    private CommandSettings commandSettings;

    public Bot(Config config, Database database) {
        this.config = config;
        this.database = database;
    }

    public void start() throws LoginException {
        builder.setAutoReconnect(true);
        builder.setShardsTotal(config.getMaxShards());
        builder.setToken(config.getToken());
        builder.setSessionController(sessionController);
        this.shardManager = builder.build();
        this.commandSettings = new CommandSettings(config.getPrefix(), this.shardManager, true, true);
        // command settings einstellen
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public CommandSettings getCommandSettings() {
        return commandSettings;
    }
}
