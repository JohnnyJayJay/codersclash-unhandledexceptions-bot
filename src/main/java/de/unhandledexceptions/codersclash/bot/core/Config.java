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

    private Path file;
    JSONObject config;

    public Config(String path) {
        this.file = Paths.get(path);
    }

    public boolean fileExists() {
        return Files.exists(file);
    }

    public boolean load() {
        boolean success;
        try {
            config = new JSONObject(Files.readAllBytes(file));
            var database = config.getJSONObject("DATABASE");
            if (config.isNull("TOKEN") || config.isNull("DEFAULT_PREFIX") || database.isNull("URL") || database.isNull("USERNAME") || database.isNull("PASSWORD"))
                success = false;
            else
                success = true;
        } catch (IOException e) {
            System.err.println("Config could not be loaded due to an IOException. Check the application's reading permissions.");
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public void create() {
        try {
            Path dir = file.getParent();
            if (dir != null && Files.notExists(dir))
                Files.createDirectories(dir);
            if (Files.notExists(file))
                file = Files.createFile(file);
            JSONStringer stringer = new JSONStringer();
            stringer.object()
                    .key("BOTINFO").object()
                        .key("OWNER")
                        .array().value(226011931935375360L)
                        .value(261083609148948488L)
                        .value(234343108773412864L)
                        .value(138607604506165248L).endArray()
                        .key("VERSION").value("Development")
                        .key("NAME").value("try-catch").endObject()
                    .key("TOKEN").value(null)
                    .key("DEFAULT_PREFIX").value(null)
                    .key("GUILDS_PER_SHARD").value(1000)
                    .key("MAX_SHARDS").value(10)
                    .key("DATABASE").object()
                        .key("URL").value(null)
                        .key("USERNAME").value(null)
                        .key("PASSWORD").value(null).endObject()
                    .endObject();
            Files.write(file, stringer.toString().getBytes());
        } catch (IOException e) {
            System.err.println("JSON-Config couldn't be created. Please check if this application has permission to write files.");
            e.printStackTrace();
        }
    }

    public String getToken() {
        return config.getString("TOKEN");
    }
}
