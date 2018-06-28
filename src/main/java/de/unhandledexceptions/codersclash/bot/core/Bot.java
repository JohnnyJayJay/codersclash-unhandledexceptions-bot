package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class Bot {

    private final Config config;
    private final Database database;

    private DefaultShardManagerBuilder builder;
    private ShardManager shardManager;
    private CommandSettings commandSettings;

    public Bot(Config config, Database database) {
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
            System.err.println("[ERROR] Login failed, reloading... (Check your token in config.json)");
            start();
        }
        this.commandSettings = new CommandSettings(config.getPrefix(), this.shardManager, true);
        // command settings einstellen
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public CommandSettings getCommandSettings() {
        return commandSettings;
    }
}
