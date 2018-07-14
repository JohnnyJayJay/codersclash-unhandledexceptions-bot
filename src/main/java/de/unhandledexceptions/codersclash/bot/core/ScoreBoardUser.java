package de.unhandledexceptions.codersclash.bot.core;

// Eventuell könnte man das als Memberklasse von ScoreBoard machen
public class ScoreBoardUser {

    private String userid;
    private String guildid;
    private long xp;
    private long lvl;
    public ScoreBoardUser(String userid, long user_xp, long user_lvl) {
        this.userid = userid;
        this.xp = user_xp;
        this.lvl = user_lvl;
    }

    public ScoreBoardUser(String userid, String guildid, long member_xp, long member_lvl) {
        this.userid = userid;
        this.guildid = guildid;
        this.xp = member_xp;
        this.lvl = member_lvl;
    }

    public String getUserid() {
        return userid;
    }

    public String getGuildid() {
        return guildid;
    }

    public long getXp() {
        return xp;
    }

    public long getLvl() {
        return lvl;
    }
}
