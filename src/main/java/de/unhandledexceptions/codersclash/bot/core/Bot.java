package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import de.unhandledexceptions.codersclash.bot.commands.*;
import de.unhandledexceptions.codersclash.bot.commands.connection.LinkListener;
import de.unhandledexceptions.codersclash.bot.commands.connection.LinkManager;
import de.unhandledexceptions.codersclash.bot.listeners.DatabaseListener;
import de.unhandledexceptions.codersclash.bot.listeners.Management;
import de.unhandledexceptions.codersclash.bot.listeners.MentionListener;
import de.unhandledexceptions.codersclash.bot.listeners.ReadyListener;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.requests.RestAction;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Bot {

    private int failCount;

    private static Config config;
    private final Database database;

    private List<Object> listeners;
    private DefaultShardManagerBuilder builder;
    private ShardManager shardManager;
    private static CommandSettings commandSettings;
    private static ReportCommand reportCommand;

    private static Logger logger = Logging.getLogger();

    /*private final Map<String, String> emotes = new HashMap<>() {{
        put("full1", "http://www.baggerstation.de/testseite/bots/full1.png");
        put("full2", "http://www.baggerstation.de/testseite/bots/full2.png");
        put("full3", "http://www.baggerstation.de/testseite/bots/full3.png");
        put("empty1", "http://www.baggerstation.de/testseite/bots/empty1.png");
        put("empty2", "http://www.baggerstation.de/testseite/bots/empty2.png");
        put("empty3", "http://www.baggerstation.de/testseite/bots/empty3.png");
    }};*/

    public Bot(Config config, Database database) {
        this.failCount = 0;
        this.config = config;
        this.database = database;
        this.builder = new DefaultShardManagerBuilder();
        this.listeners = new ArrayList<>();
    }

    public void start() {
        builder.setAutoReconnect(true)
                .setShardsTotal(config.getMaxShards())
                .setGame(Game.listening("@" + config.getBotName() + " | Ping me!"))
                .setToken(config.getToken());
        try {
            this.shardManager = builder.build();
            logger.info("ShardManager has been built.");
        } catch (LoginException e) {
            if (++failCount < 3) {
                logger.error("Login failed, reloading... (Check your token in config.json)");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
                this.start();
            } else {
                logger.error("Login failed after 3 times. Exiting the program");
                Runtime.getRuntime().exit(1);
            }

        }
        commandSettings = new CommandSettings(config.getPrefix(), this.shardManager, true);
        logger.info("CommandSettings are being configured");

        // command settings einstellen
        database.getPrefixes().forEach((id, prefix) -> commandSettings.setCustomPrefix(id, prefix));

        var xpCommand = new XPCommand(commandSettings, database);
        var linkListener = new LinkListener(shardManager);

        var voteCommand = new VoteCommand();
        var searchCommand = new SearchCommand();
        var mailCommand = new MailCommand(database, searchCommand);
        var linkCommand = new LinkCommand(new LinkManager(shardManager), linkListener, searchCommand, mailCommand, database);

        commandSettings.addHelpLabels("help", "helpme", "commands")
                .setHelpCommandColor(Color.CYAN)
                .setCooldown(3000)
                .put(linkCommand, "link")
                .put(new ClearCommand(), "clear", "clean", "delete")
                .put(new GuildMuteCommand(commandSettings), "muteguild", "guildmute", "lockdown")
                .put(new Permissions(commandSettings, database), "permission", "perms", "perm")
                .put(xpCommand, "xp", "level", "lvl")
                .put(new ReportCommand(database), "report", "rep", "reports")
                .put(voteCommand, "vote", "v")
                .put(new BlockCommand(), "block", "deny")
                .put(new MuteCommand(), "mute", "silence")
                .put(new SettingsCommand(database, commandSettings), "settings", "control")
                .put(mailCommand, "mail", "contact")
                //.put(new ConnectionCommand(searchCommand, mailCommand), "connect")
                .put(new RoleCommand(), "role")
                .put(new InviteCommand(config), "invite")
                .put(searchCommand, "search", "lookfor", "browse")
                .put(new ScoreBoardCommand(database), "scoreboard", "sb")
                .put(new ProfileCommand(reportCommand), "profile")
                .put(new InfoCommand(), "info")
                .activate();

        RestAction.setPassContext(false);
        listeners.addAll(List.of(voteCommand, xpCommand, new DatabaseListener(database, shardManager), new MentionListener(config),
                new ReadyListener(config), new Management(this), linkListener));
        listeners.forEach(shardManager::addEventListener);

    }

    // FIXME geht noch nicht
    /*private void checkAndCreateEmotes() {
        if (emotes.keySet().stream().anyMatch((name) -> {
            var emoteList = shardManager.getEmotesByName(name, false);
            return emoteList.isEmpty() || !emoteList.get(0).getImageUrl().equals(emotes.get(name));
        })) {
            long random = ThreadLocalRandom.current().nextLong(2137673435212321312L);
            JDA shard = shardManager.getShardById(0);
            shard.createGuild(Long.toString(random)).queue((v) -> {
                var controller = shard.getGuildsByName(Long.toString(random), false).get(0).getController();
                emotes.keySet().forEach((name) -> {
                    try {
                        controller.createEmote(name, Icon.from(new URL(emotes.get(name)).openStream())).queue();
                    } catch (IOException e) {
                        logger.error("IOException while creating emotes.", e);
                    }
                });
            });
        }
    }*/

    public void restart(int shard) {
        var jda = shardManager.getShardById(shard);
        logger.warn("Restarting shard " + shard + "...");
        listeners.forEach(jda::removeEventListener);
        shardManager.restart(shard);
        listeners.forEach(jda::addEventListener);
    }

    public void restart() {
        listeners.forEach(shardManager::removeEventListener);
        commandSettings.deactivate();
        logger.warn("Restarting all shards...");
        shardManager.restart();
        listeners.forEach(shardManager::addEventListener);
        commandSettings.activate();
    }

    public void shutdown(int shard) {
        var jda = shardManager.getShardById(shard);
        listeners.forEach(jda::removeEventListener);
        logger.warn("Shard " + shard + " is shutting down...");
        shardManager.shutdown(shard);
    }

    public void shutdown() {
        logger.warn("Bot is shutting down...");
        listeners.forEach(shardManager::removeEventListener);
        commandSettings.deactivate();
        shardManager.shutdown();
    }

    public void addListener(Object listener) {
        listeners.add(listener);
        shardManager.addEventListener(listener);
    }

    public ShardManager getAPI() {
        return shardManager;
    }

    public CommandSettings getCommandSettings() {
        return commandSettings;
    }

    public static String getPrefix(long guildId) {
        return commandSettings.getPrefix(guildId);
    }

    public static List getBotOwners() {
        return config.getBotOwners();
    }
}
