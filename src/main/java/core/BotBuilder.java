package core;

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;

import javax.security.auth.login.LoginException;

public class BotBuilder {

    BotSettings botSettings;
    DefaultShardManagerBuilder defaultShardManagerBuilder;

    public BotBuilder(BotSettings botSettings) {
        this.botSettings = botSettings;
    }

    public BotBuilder(String Token, String BotOwner, Integer Shards) {
        this.botSettings.setBotOwner(BotOwner);
        this.botSettings.setToken(Token);
        this.botSettings.setShards(Shards);
    }

    public void addEventListener(Object... listeners) {
        defaultShardManagerBuilder.addEventListeners(listeners);
    }

    public BotSettings getBotSettings() {
        return botSettings;
    }

    public Bot build() {
        try {
            return new Bot(defaultShardManagerBuilder.setAutoReconnect(true).setToken(botSettings.getToken()).setShards(botSettings.getShards()).build());
        } catch (LoginException e) {
            e.printStackTrace();
        }
        return null;
    }

}
