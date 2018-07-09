package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Database;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * @author Johnny_JayJay
 * @version 0.1-SNAPSHOT
 */
public class DatabaseListener extends ListenerAdapter {

    private Database database;
    private ShardManager shardManager;

    public DatabaseListener(Database database, ShardManager shardManager) {
        this.database = database;
        this.shardManager = shardManager;
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        database.deleteGuild(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        database.deleteMember(event.getGuild().getIdLong(), event.getUser().getIdLong());
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        database.deleteMember(event.getGuild().getIdLong(), event.getUser().getIdLong());
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        event.getGuild().getMemberCache().forEach((member) -> database.createMemberIfNotExists(guildId, member.getUser().getIdLong()));
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        database.createMemberIfNotExists(event.getGuild().getIdLong(), event.getUser().getIdLong());
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        this.refreshDatabase();
    }

    @Override
    public void onReady(ReadyEvent event) {
        this.refreshDatabase();
    }

    private void refreshDatabase() {
        shardManager.getGuildCache().forEach((guild) ->
                guild.getMemberCache().forEach((member) ->
                        database.createMemberIfNotExists(guild.getIdLong(), member.getUser().getIdLong())));
    }
}
