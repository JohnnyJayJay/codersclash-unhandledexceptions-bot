package de.unhandledexceptions.codersclash.bot.listeners;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class ReadyListener extends ListenerAdapter {

    private CommandSettings settings;
    private Database database;

    private static Logger logger = Logging.getLogger();

    public ReadyListener(CommandSettings settings, Database database) {
        this.settings = settings;
        this.database = database;
    }

    @Override   // Event wenn der Bot online geht (besser gesagt eine JDA)
    public void onReady(ReadyEvent event) {

        logger.info("Bot ready to operate!");

    }

    @Override   // Event wenn der Bot offline geht
    public void onDisconnect(DisconnectEvent event) {

        logger.warn("Bot has been disconnected!");

    }

    @Override   // Event wenn der Bot die Session resumed
    public void onResume(ResumedEvent event) {

        logger.warn("Bot has been resumed!");

    }

    @Override   // Event wenn der Bot auf einer neuen Session reconnected
    public void onReconnect(ReconnectedEvent event) {

        logger.warn("Bot has been reconnected on a new Session!");

    }

}
