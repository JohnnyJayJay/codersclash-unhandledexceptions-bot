package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Config;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author TheRealYann
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

        try {
            String guildName = "§§$%§%$§%§$%§$&%$§$%§ - EMOTE GUILD - §§$%§%$§%§$%§$&%$§$%§";
            boolean createGuild = true;
            ShardManager shardManager = event.getJDA().asBot().getShardManager();
            for (Guild guild : shardManager.getGuilds()) {
                if (guild.getName().equals(guildName)) {
                    if (!createGuild) logger.warn("DETECTED DOUBLE EMOTE SERVER. This can cause errors...");
                    createGuild = false;
                }
            }
            if (createGuild) {
                if (event.getJDA().getGuilds().size()>9) {
                    logger.warn("Can't create Emoteguild... Please create one with the name \""+guildName+"\" (Without \"), \ninvite me and give me the MANAGE_EMOTES Permission (https://discordapp.com/oauth2/authorize?&client_id="+event.getJDA().getSelfUser().getId()+"&scope=bot&permissions=0)");
                    return;
                }
                event.getJDA().createGuild(guildName).queue();
                Guild guild = event.getJDA().getGuildsByName(guildName, false).get(0);
                guild.getDefaultChannel().createInvite().queue(
                        invite -> logger.warn("Created Emoteserver. Invite:" + invite.getURL())
                );

            }
            Guild guild = event.getJDA().getGuildsByName(guildName, false).get(0);
            if (guild.getSelfMember().hasPermission(Permission.MANAGE_EMOTES)) {
                for (String key : urls.keySet()) {
                    if (guild.getEmotesByName(key, false).size() == 0)
                        guild.getController().createEmote(key, Icon.from(new URL(urls.get(key)).openStream())).queue();
                }
            } else logger.warn("Please give me the MANAGE_EMOTES Permission on the Emote guild!");
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException occurred:", e);
        } catch (IOException e) {
            logger.error("An IOException occurred while creating icon/creating emotes", e);
        }
    }
}