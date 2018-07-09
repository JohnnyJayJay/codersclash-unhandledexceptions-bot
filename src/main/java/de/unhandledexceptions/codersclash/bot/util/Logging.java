package de.unhandledexceptions.codersclash.bot.util;

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
