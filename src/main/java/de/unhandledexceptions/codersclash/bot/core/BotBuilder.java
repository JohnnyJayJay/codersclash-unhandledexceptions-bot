package de.unhandledexceptions.codersclash.bot.core;

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;

import javax.security.auth.login.LoginException;
import java.util.Collection;

public class BotBuilder {

    static BotSettings botSettings;
    DefaultShardManagerBuilder defaultShardManagerBuilder = new DefaultShardManagerBuilder();

    public BotBuilder(BotSettings botSettings) {
        this.botSettings = botSettings;
    }

    public BotBuilder(String Token, String[] BotOwner, Collection<Integer> Shards, Integer Shardstotal) {
        botSettings.setBotOwner(BotOwner);
        botSettings.setToken(Token);
        botSettings.setShards(Shards);
        botSettings.setShardsMax(Shardstotal);
    }

    public void addEventListener(Object... listeners) {
        defaultShardManagerBuilder.addEventListeners(listeners);
    }

    public BotSettings getBotSettings() {
        return botSettings;
    }

    public Bot build() {
        try {
            System.out.println(botSettings.getShards());
            System.out.println(botSettings.getToken());
            return new Bot(defaultShardManagerBuilder.setAutoReconnect(true).setToken(botSettings.getToken()).setShardsTotal(botSettings.getShardsMax()).setShards(botSettings.getShards()).build());
        } catch (LoginException e) {
            e.printStackTrace();
        }
        return null;
    }

}
