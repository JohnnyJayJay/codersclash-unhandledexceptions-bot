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

    public static Logger getLogger() {
        return LoggerFactory.getLogger(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass());
    }

}
