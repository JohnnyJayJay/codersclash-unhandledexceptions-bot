package de.unhandledexceptions.codersclash.bot.core;

import static de.unhandledexceptions.codersclash.bot.util.Logging.mainLogger;

public class Main {

    public static void main(String[] args) {
        final var config = new Config("./config.json");

        if (!config.fileExists()) {
            config.create();
            mainLogger.info("config.json has been created.");
            mainLogger.warn("Please enter database connection info, the bot token and the default command prefix. Restart the bot afterwards. Every value that is currently NULL " +
                    "has to be a String!");
        } else if (!config.load()) {
            mainLogger.error("config.json could not be loaded. Make sure all the values have been set correctly (not null) and restart the bot.");
            mainLogger.warn("Please enter database connection info, the bot token and the default command prefix. Restart the bot afterwards. Every value that is currently NULL " +
                    "has to be a String!");
        } else {
            mainLogger.info("Database is being set up!");
            var database = new Database(config.getDBIp(), config.getDBPort(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
            database.connect();
            mainLogger.warn("Connected to Database. Checking tables...");
            database.createTablesIfNotExist();
            new Bot(config, database).start();
            mainLogger.info("Bot has been started!");
        }
    }
}
