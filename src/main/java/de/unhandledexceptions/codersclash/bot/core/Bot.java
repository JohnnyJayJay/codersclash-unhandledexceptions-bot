package de.unhandledexceptions.codersclash.bot.core;

import javax.security.auth.login.LoginException;

public class Bot {

    private final Config config;
    private final Database database;

    public Bot(Config config, Database database) {
        this.config = config;
        this.database = database;
    }

    public void start() throws LoginException {
        // Sharding einstellen, Commands und Listener hinzufügen...

    }
}
