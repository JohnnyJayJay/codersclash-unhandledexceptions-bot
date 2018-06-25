package de.unhandledexceptions.codersclash.bot.core;

import de.unhandledexceptions.codersclash.bot.commandapi.CommandSettings;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.utils.SessionController;

import javax.security.auth.login.LoginException;
import java.util.Collection;

public class Bot {

    private final Config config;
    private DefaultShardManagerBuilder builder=new DefaultShardManagerBuilder();
    private SessionController sessionController;
    private ShardManager shardManager;
    private CommandSettings commandSettings;

    public Bot(Config config) {
        this.config = config;
    }

    public void addListener(Collection<Class> listener) {
        builder.addEventListeners(
                listener
        );
    }

    public void addCommands() {

    }

    public void start() throws LoginException {
        builder.setAutoReconnect(true);
        builder.setShardsTotal(config.getMaxShards());
        builder.setToken(config.getToken());
        builder.setSessionController(sessionController);
        this.shardManager = builder.build();
        this.commandSettings = new CommandSettings(config.getPrefix(), this.shardManager, true, true);
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
