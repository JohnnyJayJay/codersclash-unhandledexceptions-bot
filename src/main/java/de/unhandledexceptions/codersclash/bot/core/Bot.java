package de.unhandledexceptions.codersclash.bot.core;

import net.dv8tion.jda.bot.sharding.ShardManager;

public class Bot {

    private ShardManager shardManager;

    public Bot(ShardManager shardManager) {

    }

    // TODO: getSessionController usw.
    public ShardManager getShardManager() {
        return shardManager;
    }
}
