package de.unhandledexceptions.codersclash.bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.sql.DriverManager;
import java.sql.SQLException;

import static java.lang.String.format;
import static de.unhandledexceptions.codersclash.bot.util.Logging.databaseLogger;

public class Database {

    private boolean connected;

    private HikariConfig config;
    private HikariDataSource dataSource;

    private final String[] creationStatements = {
            "CREATE TABLE IF NOT EXISTS discord_guild (prefix VARCHAR(30),guild_id BIGINT NOT NULL,mail_channel BIGINT, PRIMARY KEY (guild_id));",
            "CREATE TABLE IF NOT EXISTS discord_user (user_id BIGINT NOT NULL,user_xp BIGINT,user_lvl BIGINT, PRIMARY KEY (user_id));",
            "CREATE TABLE IF NOT EXISTS discord_member (guild_id BIGINT NOT NULL REFERENCES discord_guild (guild_id) ON DELETE CASCADE,user_id BIGINT NOT NULL REFERENCES discord_user (user_id) ON DELETE CASCADE,reports TEXT," +
                    "member_xp BIGINT,member_lvl BIGINT,permission_lvl SMALLINT,PRIMARY KEY (user_id, guild_id));"
    };

    private String ip, username, password, dbname, port;

    public Database(String ip, String port, String dbname, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.dbname = dbname;
    }

    public void connect() {
        if (!connected) {
            String sql = "CREATE DATABASE IF NOT EXISTS " + dbname + ";";
            try (var connection = DriverManager.getConnection(format("jdbc:mysql://%s:%s/?serverTimezone=UTC", ip, port), username, password);
                 var preparedStatement = connection.prepareStatement(sql)) {
                databaseLogger.info("Creating database (if not exists)...");
                preparedStatement.executeUpdate();
                databaseLogger.info("Database created (or it already existed).");
            } catch (SQLException e) {
                databaseLogger.warn("Exception caught while connecting", e);
            }
            config = new HikariConfig();

            databaseLogger.info("Connecting to " + ip + "...");

            config.setJdbcUrl(format("jdbc:mysql://%s:%s/%s?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", ip, port, dbname));
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            try {
                dataSource = new HikariDataSource(config);
                connected = true;
                databaseLogger.info("Database connection pool successfully opened.");
            } catch (HikariPool.PoolInitializationException e) {
                databaseLogger.error(" Error while connecting to database. Check your config.", e);
                System.exit(1);
            }
        }
    }

    public void disconnect() {
        if (connected) {
            dataSource.close();
            databaseLogger.warn("Database disconnected!");
            connected = false;
        }
    }

    public void createTablesIfNotExist() {
        try (var connection = dataSource.getConnection()) {
            for (String statement : this.creationStatements) {
                try (var preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
            databaseLogger.info("Tables have been created (or they existed already).");
        } catch (SQLException e) {
            databaseLogger.warn("Exception caught while creating tables", e);
        }
    }

    public void changePermissionLevel(Member member, int lvl) {
        this.executeStatement(format("UPDATE discord_member SET permission_lvl = %d WHERE guild_id = %d AND user_id = %d;",
                lvl, member.getGuild().getIdLong(), member.getUser().getIdLong()));
    }

    public int getPermissionLevel(Member member) {
        return this.<Integer>getFirst("permission_lvl", "discord_member", Integer.TYPE, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public void setPrefix(long guildId, String prefix) {
        this.executeStatement(format("UPDATE discord_guild SET prefix = %s WHERE guild_id = %d;", prefix, guildId));
    }

    public void setMailChannel(long guildId, long channelId) {
        this.executeStatement(format("UPDATE discord_guild SET mail_channel = %d WHERE guild_id = %d;", channelId, guildId));
    }

    public void setUserXp(User user, long xp) {
        this.createUserIfNotExists(user.getIdLong());
        this.executeStatement(format("UPDATE discord_user SET user_xp = %d WHERE user_id = %d;", xp, user.getIdLong()));
    }

    public void setGuildXp(Member member, long xp) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        this.executeStatement(format("UPDATE discord_member SET member_xp = %d WHERE guild_id = %d AND user_id = %d;",
                xp, member.getGuild().getIdLong(), member.getUser().getIdLong()));
    }

    public void setUserLvl(User user, long lvl) {
        this.createUserIfNotExists(user.getIdLong());
        this.executeStatement(format("UPDATE discord_user SET user_lvl = %d WHERE user_id = %d;", lvl, user.getIdLong()));
    }

    public void setGuildLvl(Member member, long lvl) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        this.executeStatement(format("UPDATE discord_member SET member_lvl = %d WHERE guild_id = %d AND user_id = %d;",
                lvl, member.getGuild().getIdLong(), member.getUser().getIdLong()));
    }

    public void addUserLvl(User user) {
        this.setUserXp(user, 0);
        this.setUserLvl(user, this.getUserLvl(user));
    }

    public void addGuildLvl(Member member) {
        this.setGuildXp(member, 0);
        this.setGuildLvl(member, this.getGuildLvl(member));
    }

    public void addXp(Member member, long xp) {
        this.setGuildXp(member, this.getGuildXp(member)+xp);
        this.setUserXp(member.getUser(), this.getUserXp(member.getUser())+xp);
    }

    public void removeXp(Member member, long xp) {
        if (this.getGuildXp(member)>=xp) {
            this.setGuildXp(member, this.getGuildXp(member) - xp);
        } else {
            this.setGuildXp(member, (this.getGuildLvl(member)-1)*4-xp);
            this.setGuildLvl(member, this.getGuildLvl(member)-1);
        }
        if (this.getUserXp(member.getUser())>=xp) {
            this.setUserXp(member.getUser(), this.getUserXp(member.getUser()) - xp);
        } else {
            this.setUserXp(member.getUser(), (this.getUserLvl(member.getUser())-1)*4-xp);
            this.setUserLvl(member.getUser(), this.getUserLvl(member.getUser())-1);
        }
    }

    public String getPrefix(Guild guild) {
        return this.getFirst("prefix", "discord_guild", String.class, guild.getIdLong());
    }

    public long getUserXp(User user) {
        this.createUserIfNotExists(user.getIdLong());
        return this.<Long>getFirst("user_xp", "discord_user", Long.TYPE, user.getIdLong());
    }

    public long getGuildXp(Member member) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return this.<Long>getFirst("member_xp", "discord_member", Long.TYPE, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public long getUserLvl(User user) {
        this.createUserIfNotExists(user.getIdLong());
        return this.<Long>getFirst("user_lvl", "discord_user", Long.TYPE, user.getIdLong());
    }

    public long getGuildLvl(Member member) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return this.<Long>getFirst("member_lvl", "discord_member", Long.TYPE, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public void deleteGuild(long guildId) {
        this.executeStatement(format("DELETE FROM discord_guild WHERE guild_id = %d;", guildId));
    }

    public boolean isConnected() {
        return connected;
    }

    public void createMemberIfNotExists(long guildId, long userId) {
        if (this.getFirst("COUNT(user_id)", "discord_member", Long.class, guildId, userId) == 0) {
            this.createUserIfNotExists(userId);
            this.createGuildIfNotExists(guildId);
            this.executeStatement(format("INSERT INTO discord_member(guild_id, user_id, member_xp) VALUES(%d, %d, 0);", guildId, userId));
        }
    }

    public void createGuildIfNotExists(long guildId) {
        if (this.getFirst("COUNT(guild_id)", "discord_guild", Long.class, guildId) == 0) {
            this.executeStatement(format("INSERT INTO discord_guild(guild_id) VALUES(%d);", guildId));
        }
    }

    public void createUserIfNotExists(long userId) {
        if (this.getFirst("COUNT(user_id)", "discord_user", Long.class, userId) == 0) {
            this.executeStatement(format("INSERT INTO discord_user(user_id, user_xp) VALUES(%d, 0);", userId));
        }
    }

    // Gibt den das erste Ergebnis zurück. Funktioniert nur mit einer select-column und einer tabelle. Falls Dinge von discord_member geholt werden, als erste id die guil-, als
    // zweite id die user_id angeben.
    private <T> T getFirst(String select, String table, Class<T> type, long... ids) {
        String where = ids.length == 2
                ? "guild_id = " + ids[0] + " AND user_id = " + ids[1]
                : (table.equals("discord_guild") ? "guild_id = " : "user_id = ") + ids[0];
        T ret = null;
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(format("SELECT %s AS entries FROM %s WHERE %s;", select, table, where))) {
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                ret = resultSet.getObject("entries", type);
            }
        } catch (SQLException e) {
            databaseLogger.warn("SQLException caught while parsing ResultSet", e);
        }
        return ret;
    }

    private void executeStatement(String sql) {
        try (var connection = dataSource.getConnection(); var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            databaseLogger.warn("Exception caught while executing Statement", e);
        }
    }
}