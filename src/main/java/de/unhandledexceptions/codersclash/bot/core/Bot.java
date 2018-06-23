package de.unhandledexceptions.codersclash.bot.core;

import javax.security.auth.login.LoginException;

public class Bot {

    private final Config config;

    public Bot(Config config) {
        this.config = config;
    }

    public void start() throws LoginException {
        // Sharding einstellen, Commands und Listener hinzufügen...
    }

}
