package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Config;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

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

        if (!event.getJDA().getSelfUser().getName().equals(config.getBotName())) {
            event.getJDA().getSelfUser().getManager().setName(config.getBotName()).queue();
        }
    }
}
