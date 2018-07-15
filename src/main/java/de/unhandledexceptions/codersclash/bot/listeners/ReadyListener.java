package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.SelfUser;
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
    private URL iconURL;

    private static Logger logger = Logging.getLogger();

    public ReadyListener(Config config) {
        this.config = config;
        try {
            this.iconURL = new URL(config.getIconURL());
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException occurred:", e);
        }
    }

    @Override
    public void onReady(ReadyEvent event) {

        SelfUser selfUser = event.getJDA().getSelfUser();
        try {
            Icon icon = Icon.from((iconURL).openStream());
            if (selfUser.getAvatarUrl() == null) {
                selfUser.getManager().setAvatar(icon).queue();
            }
        } catch (IOException e) {
            logger.error("An IOException occurred while creating Icon/changing avatar", e);
        }

        if (!selfUser.getName().equals(config.getBotName())) {
            selfUser.getManager().setName(config.getBotName()).queue();
        }
    }
}
