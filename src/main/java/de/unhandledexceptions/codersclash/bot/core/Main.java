package de.unhandledexceptions.codersclash.bot.core;

public class Main {

    public static void main(String[] args) {
        Config config = new Config("./config.json");
        if (!config.fileExists()) {
            config.create();
            System.out.println("config.json has been created. Please enter database connection info, the bot token and the default command prefix. Restart the bot afterwards.");
        } else if (!config.load()) {
            System.err.println("config.json could not be loaded. Make sure all the values have been set correctly (not null) and restart the bot.");
        } else {
            new Bot(config.getToken()).start();
        }
    }

}
