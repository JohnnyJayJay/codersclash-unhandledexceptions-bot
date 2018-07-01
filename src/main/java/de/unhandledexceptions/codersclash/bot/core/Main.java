package de.unhandledexceptions.codersclash.bot.core;

import static de.unhandledexceptions.codersclash.bot.core.Logging.*;

public class Main {

    public static void main(String[] args) {

        final var config = new Config("./config.json");

        if (!config.fileExists())
        {
            config.create();
            System.out.println("[INFO] config.json has been created. Please enter database connection info, the bot token and the default command prefix. Restart the bot " +
                    "afterwards.");
        } else if (!config.load())
        {
            System.err.println("[ERROR] config.json could not be loaded. Make sure all the values have been set correctly (not null) and restart the bot.");
        } else 
        {
            var database = new Database(config.getDBUrl(), config.getDBPort(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
            database.connect();
            new Bot(config, database).start();
        }

        // Nur zum Test bzw als Beispiel gedacht
        generalLogger.info("info");
        commandLogger.error("error");
        configLogger.debug("debug");
    }
}
