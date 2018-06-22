package de.unhandledexceptions.codersclash.bot.core;

import java.util.Collection;

public class BotSettings {

    static String Token;
    static String[] BotOwner;
    static Collection<Integer> Shards;
    static Integer ShardsMax;

    public BotSettings(String Token, String[] BotOwner, Collection<Integer> Shards, Integer ShardsMax) {
        BotSettings.Token = Token;
        BotSettings.BotOwner = BotOwner;
        BotSettings.Shards = Shards;
        BotSettings.ShardsMax = ShardsMax;
    }

    public BotSettings(Config config) {
        String[] presencekeys = config.getPresence().split("=:=");
        new BotSettings(config.getToken(), config.getBotOwner(), config.getShards(), Integer.parseInt(config.getShardsMax()));
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String[] getBotOwner() {
        return BotOwner;
    }

    public void setBotOwner(String[] botOwner) {
        BotOwner = botOwner;
    }

    public Collection<Integer> getShards() {
        return Shards;
    }

    public static void setShards(Collection<Integer> shards) {
        Shards = shards;
    }

    public Integer getShardsMax() {
        return ShardsMax;
    }

    public void setShardsMax(Integer shardsMax) {
        ShardsMax = shardsMax;
    }
}
