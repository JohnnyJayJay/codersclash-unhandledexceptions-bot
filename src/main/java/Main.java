import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class Main {

    private static ShardManager shardManager;
    private static DefaultShardManagerBuilder shardManagerBuilder;

    public static void main(String[] args) {
        shardManagerBuilder = new DefaultShardManagerBuilder().setAudioEnabled(true).setShards(2);
        registerListener();

    }

    private static void registerListener() {
        shardManagerBuilder.addEventListeners(

        );
    }

}
