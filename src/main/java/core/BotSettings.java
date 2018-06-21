package core;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.managers.Presence;

public class BotSettings {

    String Token;
    String BotOwner;
    Integer Shards;
    Presence presence;

    public BotSettings(String Token, String BotOwner) {
        this.Token = Token;
        this.BotOwner= BotOwner;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getBotOwner() {
        return BotOwner;
    }

    public void setBotOwner(String botOwner) {
        BotOwner = botOwner;
    }

    public Integer getShards() {
        return Shards;
    }

    public void setShards(Integer shards) {
        Shards = shards;
    }

    public Presence getPresence() {
        return presence;
    }

    public void setPresence(Presence presence) {
        this.presence = presence;
    }

    public void setPresence(OnlineStatus onlineStatus, Game game) {
        this.presence.setPresence(onlineStatus, game);
    }
}
