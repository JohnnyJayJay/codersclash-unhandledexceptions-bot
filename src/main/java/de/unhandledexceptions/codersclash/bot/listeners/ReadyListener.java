package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Config;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AccountManager;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class ReadyListener extends ListenerAdapter {

    private Config config;

    public ReadyListener(Config config) {
        this.config = config;
    }

    @Override
    public void onReady(ReadyEvent event) {

        AccountManager accountManager = new AccountManager(event.getJDA().getSelfUser());
        if (!accountManager.getSelfUser().getName().equals(config.getBotName())) {
            accountManager.setName(config.getBotName()).queue();
        }
    }
}
