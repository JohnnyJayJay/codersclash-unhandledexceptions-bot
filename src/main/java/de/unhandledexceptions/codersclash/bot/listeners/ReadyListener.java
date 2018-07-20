package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author TheRealYann
 */

public class ReadyListener extends ListenerAdapter {

    private Database database;
    private Config config;
    private URL iconURL;

    private static Logger logger = Logging.getLogger();

    public ReadyListener(Config config) {
        this.database = database;
        this.config = config;
        try {
            this.iconURL = new URL(config.getIconURL());
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException occurred:", e);
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        List<Guild> guilds = event.getJDA().getGuilds().stream().collect(Collectors.toList());
        //TODO List<VoiceChannel> voiceChannels = database.getAutoChannel();
        SelfUser selfUser = event.getJDA().getSelfUser();
        try {
            if (selfUser.getAvatarUrl() == null) {
                Icon icon = Icon.from((iconURL).openStream());
                selfUser.getManager().setAvatar(icon).queue();
            }
        } catch (IOException e) {
            logger.error("An IOException occurred while creating icon/changing avatar", e);
        }

        if (!selfUser.getName().equals(config.getBotName())) {
            selfUser.getManager().setName(config.getBotName()).queue();
        }


    }
}
