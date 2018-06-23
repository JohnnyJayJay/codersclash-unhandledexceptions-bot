package de.unhandledexceptions.codersclash.bot.core;

public class Main {

    public static void main(String[] args) {
        Config config = new Config("./config.json");
        if (!config.exists()) {
            config.create();
            System.out.println("Config " + config.getName() + " has been created. Please enter the values and restart the bot.");
            System.exit(1);
        }
    }

}
