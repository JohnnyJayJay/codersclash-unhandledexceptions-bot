package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class ReadyListener extends ListenerAdapter {

    private Config config;
    private static Icon icon;
    private static URL iconURL;

    private static Logger logger = Logging.getLogger();

    {
        try {
            iconURL = new URL("https://i.imgur.com/X7je2jH.png");
            //iconURL = new URL(config.getIconUrl()); FIXME erzeugt einen NullPointer beim aufrufen von "getIconURL" TEST CHANGE
        } catch (MalformedURLException e) {
            logger.error("An Exception occurred while getting the URL.", e);
        }
    }

    public ReadyListener(Config config) {
        this.config = config;
    }

    @Override
    public void onReady(ReadyEvent event) {
        try {
            icon = Icon.from((iconURL).openStream());
        } catch (IOException e) {
            logger.error("An Exception occurred while creating icon from file", e);
        }

        event.getJDA().getSelfUser().getManager().setAvatar(icon).queue();
        if (!event.getJDA().getSelfUser().getName().equals(config.getBotName())) {
            event.getJDA().getSelfUser().getManager().setName(config.getBotName()).queue();
        }
    }
}
