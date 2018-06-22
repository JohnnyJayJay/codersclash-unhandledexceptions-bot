package de.unhandledexceptions.codersclash.bot;

import de.johnnyjayjay.discord.api.command.Command;
import de.johnnyjayjay.discord.api.command.CommandSettings;
import de.unhandledexceptions.codersclash.bot.core.Bot;
import de.unhandledexceptions.codersclash.bot.core.BotBuilder;
import de.unhandledexceptions.codersclash.bot.core.BotSettings;
import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.listener.ready;

public class trycatchbot {
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
