package de.unhandledexceptions.codersclash.bot.listeners;

import de.unhandledexceptions.codersclash.bot.core.Database;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Johnny_JayJay
 */

public class DatabaseListener extends ListenerAdapter {

    private Database database;
    private ShardManager shardManager;
    private Logger logger;

    public DatabaseListener(Database database, ShardManager shardManager) {
        this.database = database;
        this.shardManager = shardManager;
        this.logger = Logging.getLogger();
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        logger.info("Left guild \"" + event.getGuild().getName() + "\" (" + event.getGuild().getId() + ")");
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
        logger.info("Joined guild \"" + event.getGuild().getName() + "\" (" + event.getGuild().getId() + ")");
        long guildId = event.getGuild().getIdLong();
        event.getGuild().getMemberCache().forEach((member) -> database.createMemberIfNotExists(guildId, member.getUser().getIdLong()));
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        database.createMemberIfNotExists(event.getGuild().getIdLong(), event.getUser().getIdLong());
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        logger.info("Bot has been reconnected on a new session.");
        logger.warn("Database will be refreshed.");
        this.refreshDatabase();
    }

    @Override
    public void onResume(ResumedEvent event) {
        logger.info("Bot session resumed.");
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        logger.warn("Bot lost connection...");
    }

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("JDA " + event.getJDA().getShardInfo().getShardId() + " is ready.");
        this.refreshDatabase();
    }

    private synchronized void refreshDatabase() {
        logger.warn("Database is being refreshed...");
        List<Long> expectedGuilds = database.getIds("discord_guild");
        logger.debug("Expected guilds: " + expectedGuilds);
        List<Long> actualGuilds =  shardManager.getGuildCache().stream().map(Guild::getIdLong).collect(Collectors.toList());
        logger.debug("Actual guilds: " + actualGuilds);
        expectedGuilds.stream().filter((id) -> !actualGuilds.contains(id)).forEach(database::deleteGuild);
        List<Long> expectedUsers = database.getIds("discord_user");
        logger.debug("Expected users: " + expectedUsers);
        List<Long> actualUsers = shardManager.getUserCache().stream().map(User::getIdLong).collect(Collectors.toList());
        logger.debug("Actual users: " + actualUsers);
        expectedUsers.stream().filter((id) -> !actualUsers.contains(id)).forEach(database::deleteUser);
        Map<Long, Set<Long>> expectedMembers = database.getMembers();
        logger.debug("Expected members: " + expectedMembers);
        expectedMembers.forEach((guildId, userIds) -> {
                    logger.debug("GuildID: " + guildId + " Users: " + userIds);
                    if (shardManager.getGuildById(guildId) == null) {
                        logger.debug("Guild wird gelöscht");
                        database.deleteGuild(guildId);
                    } else {
                        userIds.stream().filter((userId) -> shardManager.getGuildById(guildId).getMemberById(userId) == null).forEach((userId) -> {
                            database.deleteMember(guildId, userId);
                            logger.debug("Member mit id " + userId + " wird gelöscht");
                        });
                    }
                });
        logger.debug("Inserting new Guilds and members");
        shardManager.getGuildCache().forEach((guild) ->
                guild.getMemberCache().forEach((member) ->
                        database.createMemberIfNotExists(guild.getIdLong(), member.getUser().getIdLong())));
        logger.info("Database successfully refreshed.");
    }
}
