package de.unhandledexceptions.codersclash.bot.core;


import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class Config {

    // Konstanten, die beim erstellen der Config automatisch eingetragen werden
    private final int DEFAULT_MAX_SHARDS = 10;
    private final int DEFAULT_GUILDS_PER_SHARD = 1000;
    private final long[] BOT_OWNERS = {226011931935375360L, 261083609148948488L, 234343108773412864L, 138607604506165248L};
    private final String BOT_NAME = "try-catch";
    private final String VERSION = "Development";

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
        boolean success;
        try {
            config = new JSONObject(Files.readAllBytes(file)); // config file auslesen und das in ein JSONObject packen
            var database = config.getJSONObject("DATABASE"); // JSONObject, in dem die Infos zur DB drinstehen
            // wenn eines der benötigten Dinge in der Config keinen Wert hat
            if (config.isNull("TOKEN") || config.isNull("DEFAULT_PREFIX") || database.isNull("URL") || database.isNull("USERNAME") || database.isNull("PASSWORD"))
                success = false;
            else
                success = true;
        } catch (IOException e) {
            System.err.println("Config could not be loaded due to an IOException. Check the application's reading permissions.");
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
            // Das Standard JSONOBject zusammenbauen
            JSONStringer stringer = new JSONStringer();
            stringer.object()
                    .key("BOTINFO").object()
                        .key("OWNER")
                        .array().value(BOT_OWNERS).endArray()
                        .key("VERSION").value(VERSION)
                        .key("NAME").value(BOT_NAME).endObject()
                    .key("TOKEN").value(null)
                    .key("DEFAULT_PREFIX").value(null)
                    .key("GUILDS_PER_SHARD").value(DEFAULT_GUILDS_PER_SHARD)
                    .key("MAX_SHARDS").value(DEFAULT_MAX_SHARDS)
                    .key("DATABASE").object()
                        .key("URL").value(null)
                        .key("USERNAME").value(null)
                        .key("PASSWORD").value(null).endObject()
                    .endObject();
            Files.write(file, stringer.toString().getBytes()); // Das JSONObject als String in die config.json schreiben
        } catch (IOException e) {
            System.err.println("JSON-Config couldn't be created. Please check if this application has permission to write files.");
            e.printStackTrace();
        }
    }

    public String getToken() {
        return config.getString("TOKEN");
    }
}
