package de.unhandledexceptions.codersclash.bot.util;

import de.unhandledexceptions.codersclash.bot.commands.ClearCommand;
import de.unhandledexceptions.codersclash.bot.commands.SettingsCommand;
import de.unhandledexceptions.codersclash.bot.commands.VoteCommand;
import de.unhandledexceptions.codersclash.bot.core.*;
import de.unhandledexceptions.codersclash.bot.listeners.ReadyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class Logging {

    public static final Logger clearCommandLogger = LoggerFactory.getLogger(ClearCommand.class);

    public static final Logger settingsCommandLogger = LoggerFactory.getLogger(SettingsCommand.class);

    public static final Logger voteCommandLogger = LoggerFactory.getLogger(VoteCommand.class);

    public static final Logger botLogger = LoggerFactory.getLogger(Bot.class);

    public static final Logger configLogger = LoggerFactory.getLogger(Config.class);

    public static final Logger databaseLogger = LoggerFactory.getLogger(Database.class);

    public static final Logger mainLogger = LoggerFactory.getLogger(Main.class);

    public static final Logger permissionsLogger = LoggerFactory.getLogger(Permissions.class);

    public static final Logger readyListenerLogger = LoggerFactory.getLogger(ReadyListener.class);


}
