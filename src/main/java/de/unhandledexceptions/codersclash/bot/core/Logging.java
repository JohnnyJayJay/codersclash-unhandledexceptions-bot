package de.unhandledexceptions.codersclash.bot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author TheRealYann
 * @version 1.0
 */

public class Logging {

    // Logger unterscheiden sich nur im Namen, alle werden hier eingetragen

    public static final Logger generalLogger =  LoggerFactory.getLogger("General");

    public static final Logger commandLogger = LoggerFactory.getLogger("Command");

    public static final Logger configLogger = LoggerFactory.getLogger("Config");

}
