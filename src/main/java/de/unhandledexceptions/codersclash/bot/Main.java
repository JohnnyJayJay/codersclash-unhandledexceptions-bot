package de.unhandledexceptions.codersclash.bot;

import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.core.Database;

/**
 * Coded by Oskar#7402
 * At 26.06.2018
 * github.com/oskardevkappa/
 */

public class Main {



    public static void main(String args[])
    {

        final var config = new Config("config.json");

        final var database = new Database(config.getDBUrl(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
        database.connect();

    }





}
