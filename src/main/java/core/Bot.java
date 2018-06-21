package core;

import net.dv8tion.jda.bot.sharding.ShardManager;

public class Bot {

    ShardManager shardManager;
    public Bot(ShardManager shardManager) {

    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
