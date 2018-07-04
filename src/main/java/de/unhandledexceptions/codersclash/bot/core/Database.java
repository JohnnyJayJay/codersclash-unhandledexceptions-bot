package de.unhandledexceptions.codersclash.bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
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
            "CREATE TABLE IF NOT EXISTS discord_user (user_id BIGINT NOT NULL,user_xp BIGINT, PRIMARY KEY (user_id));",
            "CREATE TABLE IF NOT EXISTS discord_member (guild_id BIGINT NOT NULL REFERENCES discord_guild (guild_id) ON DELETE CASCADE,user_id BIGINT NOT NULL REFERENCES discord_user (user_id) ON DELETE CASCADE,reports TEXT," +
                    "member_xp BIGINT,permission_lvl SMALLINT,PRIMARY KEY (user_id, guild_id));"
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
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        this.executeStatement(format("UPDATE discord_member SET permission_lvl = %d WHERE guild_id = %d AND user_id = %d;",
                lvl, member.getGuild().getIdLong(), member.getUser().getIdLong()));
    }

    public int getPermissionLevel(Member member) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return this.<Integer>get("permission_lvl", "discord_member", member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public void setCustomPrefix(long guildId, String prefix) {
        this.createGuildIfNotExists(guildId);
        this.executeStatement(format("UPDATE discord_guild SET prefix = %s WHERE guild_id = %d;", prefix, guildId));
    }

    public void setMailChannel(long guildId, long channelId) {
        this.createGuildIfNotExists(guildId);
        this.executeStatement(format("UPDATE discord_guild SET mail_channel = %d WHERE guild_id = %d;", channelId, guildId));
    }

    public void setXp(User user, long xp) {
        this.createUserIfNotExists(user.getIdLong());
        this.executeStatement(format("UPDATE discord_user SET user_xp = %d WHERE user_id = %d;", xp, user.getIdLong()));
    }

    public void setXp(Member member, long xp) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        this.executeStatement(format("UPDATE discord_member SET member_xp = %d WHERE guild_id = %d AND user_id = %d;",
                xp, member.getGuild().getIdLong(), member.getUser().getIdLong()));
    }

    public long getXp(User user) {
        this.createUserIfNotExists(user.getIdLong());
        return this.<Long>get("user_xp", "discord_user", user.getIdLong());
    }

    public long getXp(Member member) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return this.<Long>get("member_xp", "discord_member", member.getGuild().getIdLong(), member.getUser().getIdLong());
    }


    public void deleteGuild(long guildId) {
        this.executeStatement(format("DELETE FROM discord_guild WHERE guild_id = %d;", guildId));
    }

    public boolean isConnected() {
        return connected;
    }

    private void createMemberIfNotExists(long guildId, long userId) {
        if (this.<Long>get("COUNT(guild_id, user_id)", "discord_member", guildId, userId) == 0) {
            this.createUserIfNotExists(userId);
            this.createGuildIfNotExists(guildId);
            this.executeStatement(format("INSERT INTO discord_member(guild_id, user_id, member_xp) VALUES(%d, %d, 0);", guildId, userId));
        }
    }

    private void createGuildIfNotExists(long guildId) {
        if (this.<Long>get("COUNT(guild_id)", "discord_guild", guildId) == 0) {
            this.executeStatement(format("INSERT INTO discord_guild(guild_id) VALUES(%d);", guildId));
        }
    }

    private void createUserIfNotExists(long userId) {
        if (this.<Long>get("COUNT(user_id)", "discord_user", userId) == 0) {
            this.executeStatement(format("INSERT INTO discord_user(user_id, user_xp) VALUES(%d, 0);", userId));
        }
    }

    private <T> T get(String select, String table, long... ids) {
        String where = ids.length == 2
                ? "guild_id = " + ids[0] + " AND user_id = " + ids[1]
                : (table.equals("discord_guild") ? "guild_id = " : "user_id = ") + ids[0];
        T ret = null;
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement("SELECT ? AS entries FROM ? WHERE ?;")) {
            preparedStatement.setString(1, select);
            preparedStatement.setString(2, table);
            preparedStatement.setString(3, where);
            var resultSet = preparedStatement.executeQuery();
            ret = (T) resultSet.getObject("entries");
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