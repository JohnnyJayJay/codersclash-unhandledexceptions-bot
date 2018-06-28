package de.unhandledexceptions.codersclash.bot.listener;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class ReadyListener extends ListenerAdapter {


    @Override   // Event wenn der Bot online geht (besser gesagt eine JDA)
    public void onReady(ReadyEvent event) {

        String out = "\nBot is running on following servers: \n"; // Hier werden die Server ausgegeben auf den der Bot läuft

        for (var g : event.getJDA().getGuilds() ) {
            out += g.getName() + " (" + g.getId() + ") \n";
        }

    }

    @Override   // Event wenn der Bot offline geht
    public void onDisconnect(DisconnectEvent event) {


    }

    @Override   // Event wenn der Bot die Session resumed
    public void onResume(ResumedEvent event) {


    }

    @Override   // Event wenn der Bot auf einer neuen Session reconnected
    public void onReconnect(ReconnectedEvent event) {


    }

}
