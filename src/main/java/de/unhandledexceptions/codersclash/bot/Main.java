package de.unhandledexceptions.codersclash.bot;

import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.BotBuilder;
import de.unhandledexceptions.codersclash.bot.core.BotSettings;
import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.listener.ready;

public class Main {
    public static de.unhandledexceptions.codersclash.bot.core.Bot bot;
    public static Config config;

    public static void main(String[] args) {
        config=new Config("config.json");
        BotBuilder botBuilder = new BotBuilder(new BotSettings(config));
        botBuilder.addEventListener(new ready());
        bot = botBuilder.build();
    }

    public Config getConfig() {
        return config;
    }
    public Bot getBot() {
        return bot;
    }
}
