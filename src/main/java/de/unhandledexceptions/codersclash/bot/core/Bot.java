package de.unhandledexceptions.codersclash.bot.core;

import com.github.johnnyjayjay.discord.commandapi.CommandSettings;
import de.unhandledexceptions.codersclash.bot.commands.*;
import de.unhandledexceptions.codersclash.bot.core.connection.LinkListener;
import de.unhandledexceptions.codersclash.bot.core.connection.LinkManager;
import de.unhandledexceptions.codersclash.bot.core.mute.MuteManager;
import de.unhandledexceptions.codersclash.bot.game.TicTacToe;
import de.unhandledexceptions.codersclash.bot.listeners.*;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
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

    private static Logger logger = Logging.getLogger();

    public Bot(Config config, Database database) {
        this.failCount = 0;
        Bot.config = config;
        this.database = database;
        this.builder = new DefaultShardManagerBuilder();
        this.listeners = new ArrayList<>();
    }

    public void start() {
        builder.setAutoReconnect(true)
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

        // Command settings einstellen
        database.getPrefixes().forEach((id, prefix) -> commandSettings.setCustomPrefix(id, prefix));

        var xpCommand = new XPCommand(commandSettings, database);
        var linkListener = new LinkListener();
        var linkManager = new LinkManager(shardManager);
        linkListener.setLinkManager(linkManager);
        linkManager.setLinkListener(linkListener);

        var voteCommand = new VoteCommand(shardManager);
        var ticTacToe = new TicTacToe();
        var searchCommand = new SearchCommand();
        var mailCommand = new MailCommand(database, searchCommand);
        ReportCommand reportCommand = new ReportCommand(database);
        var linkCommand = new LinkCommand(linkManager, linkListener, searchCommand, mailCommand, database);
        var muteManager = new MuteManager(shardManager, commandSettings);

        CommandSettingsHandler commandSettingsHandler = new CommandSettingsHandler(commandSettings);

        commandSettingsHandler
                .put(new BlockCommand(), "block", "deny")
                .put(new ClearCommand(), "clear", "clean", "delete")
                .put(new GuildMuteCommand(muteManager), "muteguild", "guildmute", "lockdown")
                .put(new HelpCommand(commandSettingsHandler), "help", "helpme", "commands")
                .put(new InviteCommand(config), "invite")
                .put(linkCommand, "link")
                .put(mailCommand, "mail", "contact")
                .put(new MuteCommand(muteManager), "mute", "silence")
                .put(new Permissions(commandSettings, database), "permission", "perms", "perm")
                .put(new ProfileCommand(reportCommand), "profile", "userinfo")
                .put(reportCommand, "report", "rep", "reports")
                .put(new RoleCommand(), "role")
                .put(new ScoreBoardCommand(database, commandSettings), "scoreboard", "sb")
                .put(searchCommand, "search", "lookfor", "browse")
                .put(new SettingsCommand(database, commandSettings), "settings", "control")
                .put(new TicTacToeCommand(ticTacToe), "ttt", "tictactoe")
                .put(voteCommand, "vote", "poll")
                .put(xpCommand, "xp", "level", "lvl")
                .getCommandSettings()
                .setCooldown(config.getCommandCooldown())
                .activate();

        listeners.addAll(List.of(voteCommand, xpCommand, new DatabaseListener(database, shardManager), new MentionListener(config),
                new ReadyListener(config), new Management(this), linkListener, new AutoChannelListener(database)));
        listeners.forEach(shardManager::addEventListener);
    }

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
        Runtime.getRuntime().exit(0);
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

    public static String getBotName(){return config.getBotName();}
}
