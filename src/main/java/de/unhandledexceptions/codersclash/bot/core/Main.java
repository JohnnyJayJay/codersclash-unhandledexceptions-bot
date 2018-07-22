package de.unhandledexceptions.codersclash.bot.core;

import de.unhandledexceptions.codersclash.bot.util.Logging;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class Main {

    private static final Logger logger = Logging.getLogger();
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    public static void main(String[] args) {

        final var config = new Config("./config.json");


        if (!config.fileExists()) {
            config.create();
            logger.info("config.json has been created.");
            logger.warn("Please enter database connection info, the bot token and the default command prefix. Restart the bot afterwards. Every value that is currently NULL " +
                    "has to be a String!");
            logger.info("You may change the existing values as well. If you do so, please make sure that every new value has the correct type.");
            logger.info("The database will be created automatically if it doesn't exist.");
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

    public static void otherThread(Runnable task) {
        executorService.execute(task);
    }

    public static ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit timeUnit) {
        return executorService.schedule(task, delay, timeUnit);
    }
}
