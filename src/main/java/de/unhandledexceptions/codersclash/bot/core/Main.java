package de.unhandledexceptions.codersclash.bot.core;

import static de.unhandledexceptions.codersclash.bot.core.Logging.*;

public class Main {

    public static void main(String[] args) {

        final var config = new Config("./config.json");

        if (!config.fileExists())
        {
            config.create();
            System.out.println("[INFO] config.json has been created. Please enter database connection info, the bot token and the default command prefix. Restart the bot " +
                    "afterwards. Every value that is currenty NULL has to be a String!");
        } else if (!config.load())
        {
            System.err.println("[ERROR] config.json could not be loaded. Make sure all the values have been set correctly (not null) and restart the bot.");
        } else 
        {
            var database = new Database(config.getDBIp(), config.getDBPort(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
            database.connect();
<<<<<<< HEAD
            String[] creationStatements = {
                    "CREATE TABLE IF NOT EXISTS discord_guild (prefix VARCHAR(30),guild_id BIGINT NOT NULL,mail_channel BIGINT, PRIMARY KEY (guild_id));",
                    "CREATE TABLE IF NOT EXISTS discord_user (user_id BIGINT NOT NULL,user_xp BIGINT,PRIMARY KEY (user_id));",
                    "CREATE TABLE IF NOT EXISTS discord_member (guild_id BIGINT NOT NULL,user_id BIGINT NOT NULL,member_id BIGINT NOT NULL,reports TEXT,member_xp BIGINT,permission_lvl SMALLINT,INDEX (member_id),PRIMARY KEY (member_id),FOREIGN KEY (guild_id) REFERENCES discord_guild (guild_id) ON DELETE CASCADE,FOREIGN KEY (user_id) REFERENCES discord_user (user_id) ON DELETE CASCADE);"
            };
            database.createTablesIfNotExist(creationStatements);
=======
>>>>>>> 38ce2e4482eec1e04138954ddf5bf6316458549c
            new Bot(config, database).start();
        }

        // Nur zum Test bzw als Beispiel gedacht
        generalLogger.error("testing");
        commandLogger.info("success");
        configLogger.info("failed");

    }
}
