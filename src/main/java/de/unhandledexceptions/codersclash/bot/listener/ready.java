package de.unhandledexceptions.codersclash.bot.listener;

import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.trycatchbot;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ready extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        Config config = new trycatchbot().getConfig();
        String[] presencekeys = config.getPresence().split("=:=");
        event.getJDA().getPresence().setPresence(OnlineStatus.valueOf(presencekeys[0]), Game.of(Game.GameType.valueOf(presencekeys[1]), presencekeys[2], presencekeys[3]));
    }
}
