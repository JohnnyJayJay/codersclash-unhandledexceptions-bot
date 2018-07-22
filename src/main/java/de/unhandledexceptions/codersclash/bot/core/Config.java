package de.unhandledexceptions.codersclash.bot.core;


import de.unhandledexceptions.codersclash.bot.util.Logging;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Config {

    private static Logger logger = Logging.getLogger();

    private final long[] BOT_OWNERS = {261083609148948488L,234343108773412864L,226011931935375360L,138607604506165248L};
    private final String BOT_NAME = "try-catch";
    private final String VERSION = "1.0";
    private final String ICON_URL = "https://i.imgur.com/DRKwhqj.png";
    private final String DEFAULT_PREFIX = "tc!";
    private final long COMMAND_COOLDOWN = 1000;

    private Path file; // config.json Datei
    private JSONObject config; // Inhalt von config.json

    public Config(String path) {
        this.file = Paths.get(path);
    }

    public boolean fileExists() {
        return Files.exists(file);
    }

    public boolean load() {
        // Die Config laden, also dem Attribut config das JSONObject aus der Datei zuweisen
        boolean success = true;
        try {
            config = new JSONObject(new String(Files.readAllBytes(file))); // config file auslesen und das in ein JSONObject packen
            // Wenn ein Key irgendwo in der config keinen Wert hat
            success = !hasAnyNullValue(config); // dann kann nicht garantiert werden, dass alle values da sind (muss nicht unbedingt relevant sein, nur als "info")
        } catch (IOException e) {
            logger.error("Config could not be loaded due to an IOException. Check the application's reading permissions.", e);
            success = false;
        }
        logger.info("Config successfully loaded!");
        return success; // war das laden der Config erfolgreich?
    }

    public void create() {
        try {
            Path dir = file.getParent(); // Ordner, in dem die config ist
            if (dir != null && Files.notExists(dir)) {// Wenn die config einen ordner hat und dieser noch nicht erstellt wurde
                Files.createDirectories(dir);
                logger.warn("Config Folder is getting created");
            }
            if (Files.notExists(file)) { // wenn die datei selbst noch nicht existiert
                file = Files.createFile(file);
                logger.warn("Config File is getting created");
            }
            Files.write(file, defaultConfigContent().getBytes()); // Den default Content der Config als byte-array in die config.json schreiben
            logger.info("Default config.json content created");
        } catch (IOException e) {
            logger.error("Config couldn't be created. Please check if this application has permission to write files.", e);
        }
    }

    // Das, was am Anfang in der config stehen soll (default)
    private String defaultConfigContent() {
        String emoteGuildName = Long.toString(ThreadLocalRandom.current().nextLong());
        return new JSONStringer().object()
                .key("BOTINFO").object()
                .key("OWNER").value(BOT_OWNERS)
                .key("VERSION").value(VERSION)
                .key("NAME").value(BOT_NAME)
                .key("ICON").value(ICON_URL).endObject()
                .key("EMOTE_GUILD_NAME").value(emoteGuildName)
                .key("TOKEN").value(null)
                .key("DEFAULT_PREFIX").value(DEFAULT_PREFIX)
                .key("COMMAND_COOLDOWN").value(COMMAND_COOLDOWN)
                .key("DATABASE").object()
                .key("IP").value(null)
                .key("PORT").value(null)
                .key("DB_NAME").value(null)
                .key("USERNAME").value(null)
                .key("PASSWORD").value(null).endObject()
                .endObject().toString();
    }

    private boolean hasAnyNullValue(JSONObject objectToCheck) {
        return objectToCheck.keySet().stream().anyMatch( // wenn der value null ist ODER der value auch ein JSONObject ist und dort irgendein value null ist
                (key) -> objectToCheck.isNull(key) || (objectToCheck.get(key) instanceof JSONObject && hasAnyNullValue((JSONObject) objectToCheck.get(key)))
        );
    }

    public String getVersion() {
        return config.getJSONObject("BOTINFO").getString("VERSION");
    }

    public String getBotName() {
        return config.getJSONObject("BOTINFO").getString("NAME");
    }

    public String getIconURL() {
        return config.getJSONObject("BOTINFO").getString("ICON");
    }

    public List<Object> getBotOwners() {
        return config.getJSONObject("BOTINFO").getJSONArray("OWNER").toList();
    }

    public String getToken() {
        return config.getString("TOKEN");
    }

    public String getPrefix() {
        return config.getString("DEFAULT_PREFIX");
    }

    public String getDBIp() {
        return config.getJSONObject("DATABASE").getString("IP");
    }

    public String getDBPort(){
        return config.getJSONObject("DATABASE").getString("PORT");
    }

    public String getDBName(){
        return config.getJSONObject("DATABASE").getString("DB_NAME");
    }

    public String getDBUsername() {
        return config.getJSONObject("DATABASE").getString("USERNAME");
    }

    public String getDBPassword() {
        return config.getJSONObject("DATABASE").getString("PASSWORD");
    }

    public int getCommandCooldown() {
        return config.getInt("COMMAND_COOLDOWN");
    }

    public String getEmoteGuildName() {
        return config.getString("EMOTE_GUILD_NAME");
    }

}