package de.unhandledexceptions.codersclash.bot.core;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;

public class Config {

    static String Token;
    static String[] BotOwner;
    static Collection<Integer> Shards;
    static String ShardsMax;
    static String Presence;

    public Config(String token, String[] botOwner, Collection<Integer> shards, String shardsMax, String presence) {
        this.Token = token;
        this.BotOwner = botOwner;
        this.Shards = shards;
        this.ShardsMax = shardsMax;
        this.Presence = presence;
        System.out.println(this.Presence);
        System.out.println(getPresence());
        System.out.println(presence);
    }

    public Config(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            JSONObject jsonObject = new JSONObject(sb.toString());
            String[] Botowners = jsonObject.get("botowner").toString().replace("[", "").replace("]", "").replaceAll("\"", "").split(",");
            String[] shards = jsonObject.get("shards").toString().replace("[", "").replace("]", "").replaceAll("\"", "").split(",");
            ArrayList<Integer> arrayList = new ArrayList<>();
            for (String shard:shards) {
                arrayList.add(Integer.parseInt(shard));
            }
            new Config(jsonObject.getString("token"), Botowners, arrayList, jsonObject.getString("shardsmax"), jsonObject.getString("presence"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Config(String file) {
        new Config(new File(file));
    }

    public String getToken() {
        return Token;
    }

    public String[] getBotOwner() {
        return BotOwner;
    }

    public Collection<Integer> getShards() {
        return Shards;
    }

    public String getPresence() {
        return Presence;
    }

    public String getShardsMax() {
        return ShardsMax;
    }
}
