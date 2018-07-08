package de.unhandledexceptions.bot.core;

import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.core.Database;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class StartTest {

    private static Config config;
    private static Database database;
    private static Bot bot;

    @BeforeClass
    public static void before() {
        config = new Config("./config.json");
        if (!config.fileExists())
            config.create();
        assertTrue("Config doesn't exist after creating", config.fileExists());
        assertTrue("Config contains null values", config.load());
        database = new Database(config.getDBIp(), config.getDBPort(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
        database.connect();
        assertTrue("Database is not connected", database.isConnected());
        database.disconnect();
        assertFalse("Database doesn't disconnect", database.isConnected());
        bot = new Bot(config, database);
        bot.start();
    }

    @Test
    public void testCommandSettings() {
        var settings = bot.getCommandSettings();
        assertTrue("CommandSettings weren't activated", true /*getter für activated*/);
        assertEquals(config.getPrefix(), settings.getPrefix());
        settings.setDefaultPrefix("something");
        assertEquals("something", settings.getPrefix());
    }

    @AfterClass
    public static void teardown() {
        bot.shutdown();
    }

}
