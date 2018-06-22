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

    public Config(String path) {
        this.file = Paths.get(path);
    }

    public boolean exists() {
        return Files.exists(file);
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
                    .key("TOKEN").value("")
                    .key("BOTOWNERS").value("")
                    .key("PREFIX").value("")
                    .key("TEST-KEY").value("")
                    .endObject();
            Files.write(file, stringer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getToken() {

    }
}
