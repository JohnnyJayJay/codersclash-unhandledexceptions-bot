package de.unhandledexceptions.codersclash.bot.core;


import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Config {

    // Konstanten, die beim erstellen der Config automatisch eingetragen werden
    private final int DEFAULT_MAX_SHARDS = 3;
    private final long[] BOT_OWNERS = {226011931935375360L, 261083609148948488L, 234343108773412864L, 138607604506165248L};
    private final String BOT_NAME = "try-catch";
    private final String VERSION = "Dev. Build";

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
            // TODO Logger
            System.err.println("[ERROR] Config could not be loaded due to an IOException. Check the application's reading permissions.");
            e.printStackTrace();
            success = false;
        }
        return success; // war das laden der Config erfolgreich?
    }

    public void create() {
        try {
            Path dir = file.getParent(); // Ordner, in dem die config ist
            if (dir != null && Files.notExists(dir)) // Wenn die config einen ordner hat und dieser noch nicht erstellt wurde
                Files.createDirectories(dir);
            if (Files.notExists(file)) // wenn die datei selbst noch nicht existiert
                file = Files.createFile(file);
            Files.write(file, defaultConfigContent().getBytes()); // Den default Content der Config als byte-array in die config.json schreiben
        } catch (IOException e) {
            // TODO Logger
            System.err.println("[ERROR] Config couldn't be created. Please check if this application has permission to write files.");
            e.printStackTrace();
        }
    }

    // Das, was am Anfang in der config stehen soll (default)
    private String defaultConfigContent() {
        return new JSONStringer().object()
                .key("BOTINFO").object()
                .key("OWNER").value(BOT_OWNERS)
                .key("VERSION").value(VERSION)
                .key("NAME").value(BOT_NAME).endObject()
                .key("TOKEN").value(null)
                .key("DEFAULT_PREFIX").value(null)
                .key("MAX_SHARDS").value(DEFAULT_MAX_SHARDS)
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

    public List<Object> getBotOwners() {
        return config.getJSONObject("BOTINFO").getJSONArray("OWNER").toList();
    }

    public int getMaxShards() {
        return config.getInt("MAX_SHARDS");
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

}