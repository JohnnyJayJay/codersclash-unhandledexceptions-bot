package de.unhandledexceptions.codersclash.bot.core;

import de.unhandledexceptions.codersclash.bot.util.Logging;
import org.slf4j.Logger;


public class Main {

    private static Logger logger = Logging.getLogger();

    public static void main(String[] args) {

        final var config = new Config("./config.json");

        if (!config.fileExists()) {
            config.create();
            logger.info("config.json has been created.");
            logger.warn("Please enter database connection info, the bot token and the default command prefix. Restart the bot afterwards. Every value that is currently NULL " +
                    "has to be a String!");
        } else if (!config.load()) {
            logger.error("config.json could not be loaded. Make sure all the values have been set correctly (not null) and restart the bot.");
            logger.warn("Please enter database connection info, the bot token and the default command prefix. Restart the bot afterwards. Every value that is currently NULL " +
                    "has to be a String!");
        } else {
            logger.info("Database is being set up!");
            var database = new Database(config.getDBIp(), config.getDBPort(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
            database.connect();
            logger.info("Connected to Database. Checking tables...");
            database.createTablesIfNotExist();
            Bot bot = new Bot(config, database);
            bot.start();
            logger.info("Bot has been started!");
        }
    }
}
