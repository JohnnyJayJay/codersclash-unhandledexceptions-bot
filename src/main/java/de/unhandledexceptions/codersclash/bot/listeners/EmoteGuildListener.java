package de.unhandledexceptions.codersclash.bot.listeners;

import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EmoteGuildListener extends ListenerAdapter {

    private String name;
    private final Map<String, String> urls = new HashMap<>() {{
        put("full1", "http://www.baggerstation.de/testseite/bots/full1.png");
        put("full2", "http://www.baggerstation.de/testseite/bots/full2.png");
        put("full3", "http://www.baggerstation.de/testseite/bots/full3.png");
        put("empty1", "http://www.baggerstation.de/testseite/bots/empty1.png");
        put("empty2", "http://www.baggerstation.de/testseite/bots/empty2.png");
        put("empty3", "http://www.baggerstation.de/testseite/bots/empty3.png");
        put("online", "https://i.imgur.com/JZwNdVZ.png");
        put("idle", "https://i.imgur.com/z4Noqb7.png");
        put("dnd", "https://i.imgur.com/Er0johC.png");
        put("offline", "https://i.imgur.com/fPB7iQm.png");
        put("activated", "https://i.imgur.com/6K6Ng4r.png");
        put("deactivated", "https://i.imgur.com/afdM7SK.png");
    }};

    public EmoteGuildListener(String name) {
        this.name = name;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if (event.getGuild().getName().equals(name)) {
            var controller = event.getGuild().getController();
            urls.forEach((name, url) -> {
                try {
                    var realUrl = new URL(url);
                    controller.createEmote(name, Icon.from(realUrl.openStream())).queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
