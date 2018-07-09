package de.unhandledexceptions.bot.core;

import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.*;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class StartTest {

    private static Logger logger = Logging.getLogger();

    private static Config config;
    private static Database database;
    private static Bot bot;

    @BeforeClass
    public static void before() {
        logger.info("====INITIALIZING START TEST====");
        logger.warn("TESTING CONFIG...");
        config = new Config("./config.json");
        if (!config.fileExists())
            config.create();
        assertTrue("Config doesn't exist after creating", config.fileExists());
        assertTrue("Config contains null values", config.load());
        logger.info("====CONFIG LOADING SUCCESSFUL====");
        logger.warn("TESTING DATABASE...");
        database = new Database(config.getDBIp(), config.getDBPort(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
        database.connect();
        assertTrue("Database is not connected", database.isConnected());
        database.disconnect();
        assertFalse("Database doesn't disconnect", database.isConnected());
        logger.info("====DATABASE TEST SUCCESSFUL====");
        bot = new Bot(config, database);
        bot.start();
    }

    @Test
    public void testBotStarted() {
        logger.warn("TESTING BOT...");
        assertNotNull("API hasn't been set up", bot.getAPI());
        assertNotNull("CommandSettings weren't set up correctly", bot.getCommandSettings());
        logger.info("====BOT TEST SUCCESSFUL====");
    }

    @Test
    public void testCommandSettings() {
        logger.warn("TESTING COMMANDSETTINGS...");
        var settings = bot.getCommandSettings();
        assertTrue("CommandSettings weren't activated", settings.isActivated());
        assertEquals(config.getPrefix(), settings.getPrefix());
        settings.setDefaultPrefix("something");
        assertEquals("something", settings.getPrefix());
        logger.info("====COMMANDSETTINGS TEST SUCCESSFUL====");
    }

    @AfterClass
    public static void teardown() {
        bot.shutdown();
        logger.info("====FINISHED START TESTS====");
    }

}
