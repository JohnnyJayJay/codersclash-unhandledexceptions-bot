package de.unhandledexceptions.codersclash.bot.core;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) {

        final var config = new Config("./config.json");

        if (!config.fileExists()) {
            config.create();
            System.out.println("[INFO] config.json has been created. Please enter database connection info, the bot token and the default command prefix. Restart the bot " +
                    "afterwards.");
            return;
        }
        if (!config.load()) {
            System.err.println("[ERROR] config.json could not be loaded. Make sure all the values have been set correctly (not null) and restart the bot.");
            return;
        }

        System.out.println("0");

        final var database = new Database(config.getDBUrl(), config.getDBPort(), config.getDBName(), config.getDBUsername(), config.getDBPassword());
        database.connect();

        System.out.println("1");

        database.executeSQL("CREATE TABLE comments (\n" +
                "        id INT NOT NULL AUTO_INCREMENT,\n" +
                "        MYUSER VARCHAR(30) NOT NULL,\n" +
                "        EMAIL VARCHAR(30),\n" +
                "        WEBPAGE VARCHAR(100) NOT NULL,\n" +
                "        DATUM DATE NOT NULL,\n" +
                "        SUMMARY VARCHAR(40) NOT NULL,\n" +
                "        COMMENTS VARCHAR(400) NOT NULL,\n" +
                "        PRIMARY KEY (ID)\n" +
                "    );");

        System.out.println("2");

        try {
                new Bot(config, database).start();

            } catch (LoginException e) {
                System.err.println("[ERROR] Login failed, reloading...");
                main(null);
            }
        }
    }
