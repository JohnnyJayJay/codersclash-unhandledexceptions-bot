package de.unhandledexceptions.codersclash.bot.listeners;

import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import static de.unhandledexceptions.codersclash.bot.util.Logging.readyListenerLogger;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class ReadyListener extends ListenerAdapter {


    @Override   // Event wenn der Bot online geht (besser gesagt eine JDA)
    public void onReady(ReadyEvent event) {

        readyListenerLogger.info("Bot ready to operate!");

        String out = "\nBot is running on following servers: \n"; // Hier werden die Server ausgegeben auf den der Bot läuft

        for (var g : event.getJDA().getGuilds() ) {
            out += g.getName() + " (" + g.getId() + ") \n";
        }

    }

    @Override   // Event wenn der Bot offline geht
    public void onDisconnect(DisconnectEvent event) {

        readyListenerLogger.warn("Bot has been disconnected!");

    }

    @Override   // Event wenn der Bot die Session resumed
    public void onResume(ResumedEvent event) {

        readyListenerLogger.warn("Bot has been resumed!");

    }

    @Override   // Event wenn der Bot auf einer neuen Session reconnected
    public void onReconnect(ReconnectedEvent event) {

        readyListenerLogger.warn("Bot has been reconnected on a new Session!");

    }

}
