package de.unhandledexceptions.codersclash.bot.core;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) {
        final var config = new Config("./config.json");

        if (!config.fileExists()) {
            config.create();
            System.out.println("[INFO] config.json has been created. Please enter database connection info, the bot token and the default command prefix. Restart the bot " +
                    "afterwards.");
        } else if (!config.load()) {
            System.err.println("[ERROR] config.json could not be loaded. Make sure all the values have been set correctly (not null) and restart the bot.");
        } else {
            final var database = new Database(config.getDBUrl(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
            database.connect();

            try {
                new Bot(config, database).start();
            } catch (LoginException e) {
                System.err.println("[ERROR] Login failed, reloading...");
                main(null);
            }
        }
    }
}
