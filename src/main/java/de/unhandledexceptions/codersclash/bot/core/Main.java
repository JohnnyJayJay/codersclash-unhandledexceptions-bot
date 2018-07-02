package de.unhandledexceptions.codersclash.bot.core;

import static de.unhandledexceptions.codersclash.bot.core.Logging.*;

public class Main {

    public static void main(String[] args) {

        final var config = new Config("./config.json");

        if (!config.fileExists())
        {
            config.create();
            // TODO Logger
            System.out.println("[INFO] config.json has been created. Please enter database connection info, the bot token and the default command prefix. Restart the bot " +
                    "afterwards. Every value that is currently NULL has to be a String!");
        } else if (!config.load())
        {
            // TODO Logger
            System.err.println("[ERROR] config.json could not be loaded. Make sure all the values have been set correctly (not null) and restart the bot.");
        } else
        {
            // TODO Logger
            var database = new Database(config.getDBIp(), config.getDBPort(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
            database.connect();
            // TODO Logger
            database.createTablesIfNotExist();
            new Bot(config, database).start();
            // TODO Logger
        }

        // Nur zum Test bzw als Beispiel gedacht
        generalLogger.info("info");
        generalLogger.warn("warn");
        commandLogger.error("error");
        configLogger.debug("debug");
    }
}
