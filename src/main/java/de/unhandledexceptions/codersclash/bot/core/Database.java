package de.unhandledexceptions.codersclash.bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.sql.DriverManager;
import java.sql.SQLException;

import static java.lang.String.*;
import static java.lang.String.format;
import static de.unhandledexceptions.codersclash.bot.util.Logging.databaseLogger;

public class Database {

    private boolean connected;

    private HikariConfig config;
    private HikariDataSource dataSource;

    private final String[] creationStatements = {
            "CREATE TABLE IF NOT EXISTS discord_guild (prefix VARCHAR(30),guild_id BIGINT NOT NULL,mail_channel BIGINT, CONSTRAINT PRIMARY KEY (guild_id));",
            "CREATE TABLE IF NOT EXISTS discord_user (user_id BIGINT NOT NULL,user_xp BIGINT, CONSTRAINT PRIMARY KEY (user_id));",
            "CREATE TABLE IF NOT EXISTS discord_member (guild_id BIGINT NOT NULL REFERENCES discord_guild (guild_id) ON DELETE CASCADE,user_id BIGINT NOT NULL REFERENCES discord_user (user_id) ON DELETE CASCADE,reports TEXT," +
                    "member_xp BIGINT,permission_lvl SMALLINT,INDEX (user_id, guild_id));"
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
        } else {
            databaseLogger.warn("Tried to connect to database although it already was connected.");
        }
    }

    public void disconnect() {
        if (connected) {
            dataSource.close();
            databaseLogger.warn("Database disconnected!");
            connected = false;
        } else {
            databaseLogger.warn("Tried to disconnect from database although it wasn't connected.");
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
        this.updateMember(member.getGuild().getIdLong(), member.getUser().getIdLong(), "permission_lvl = " + lvl);
    }

    public int getPermissionLevel(Member member) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return (int)this.get("permission_lvl", "discord_member", DataType.INT, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public String getPrefix(long guildId) {
        this.createGuildIfNotExists(guildId);
        return (String) this.get("prefix", "discord_guild", DataType.VARCHAR, guildId);
    }

    public long getXp(User user) {
        this.createUserIfNotExists(user.getIdLong());
        return (long) this.get("user_xp", "discord_user", DataType.BIGINT, user.getIdLong());
    }

    public long getXp(Member member) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return (long) this.get("member_xp", "discord_member", DataType.BIGINT, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public void setCustomPrefix(long guildId, String prefix) {
        this.createGuildIfNotExists(guildId);
        this.update(true, guildId, "prefix = '" + prefix + "'");
    }

    public void setMailChannel(long guildId, long channelId) {
        this.createGuildIfNotExists(guildId);
        this.update(true, guildId, "mail_channel = " + channelId);
    }

    public void setXp(User user, long xp) {
        this.createUserIfNotExists(user.getIdLong());
        this.update(false, user.getIdLong(), "user_xp = " + xp);
    }

    public void setXp(Member member, long xp) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        this.updateMember(member.getGuild().getIdLong(), member.getUser().getIdLong(), "member_xp = " + xp);
    }

    public void deleteGuild(long guildId) {
        this.delete("discord_guild", guildId);
    }

    public boolean isConnected() {
        return connected;
    }

    private void createMemberIfNotExists(long guildId, long userId) {
        if ((long) this.get("COUNT(guild_id, member_id)", "discord_member", DataType.BIGINT, guildId, userId) == 0) {
            this.createUserIfNotExists(userId);
            this.createGuildIfNotExists(guildId);
            this.insert("discord_member(guild_id, user_id, member_xp)", valueOf(guildId), valueOf(userId), "0");
        }
    }

    private void createGuildIfNotExists(long guildId) {
        if ((long) this.get("COUNT(guild_id)", "discord_guild", DataType.BIGINT, guildId) == 0) {
            this.insert("discord_guild(guild_id)", valueOf(guildId));
        }
    }

    private void createUserIfNotExists(long userId) {
        if ((long) this.get("COUNT(discord_id)", "discord_user", DataType.BIGINT, userId) == 0) {
            this.insert("discord_user(user_id, user_xp)", valueOf(userId), "0");
        }
    }

    private Object get(String select, String table, DataType type, long... ids) {
        String where = ids.length == 2
                ? "guild_id = " + ids[0] + " AND user_id = " + ids[1]
                : (table.equals("discord_guild") ? "guild_id = " : "user_id = ") + ids[0];
        Object ret = null;
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement("SELECT ? AS entries FROM ? WHERE ?;")) {
            preparedStatement.setString(1, select);
            preparedStatement.setString(2, table);
            preparedStatement.setString(3, where);
            var resultSet = preparedStatement.executeQuery();
            switch (type) {
                case INT:
                    ret = resultSet.getInt("entries");
                    break;
                case BIGINT:
                    ret = resultSet.getLong("entries");
                    break;
                case VARCHAR:
                    ret = resultSet.getString("entries");
                    break;
            }
        } catch (SQLException e) {
            databaseLogger.warn("SQLException caught while parsing ResultSet", e);
        }
        return ret;
    }

    private void insert(String table, String... values) {
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement("INSERT INTO ? VALUES(?);")) {
            preparedStatement.setString(1, table);
            preparedStatement.setString(2, join(", ", values));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            databaseLogger.warn("Exception caught while executing Statement", e);
        }
    }

    private void delete(String table, long id) {
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement("DELETE FROM ? WHERE ?_id = ?;")) {
            preparedStatement.setString(1, table);
            preparedStatement.setString(2, table.substring(8));
            preparedStatement.setLong(3, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            databaseLogger.warn("Exception caught while executing Statement", e);
        }
    }

    private void updateMember(long guildId, long userId, String... toSet) {
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement("UPDATE discord_member SET ? WHERE user_id = ? AND guild_id = ?;")) {
            preparedStatement.setString(1, join(", ", toSet));
            preparedStatement.setLong(2, userId);
            preparedStatement.setLong(3, guildId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            databaseLogger.warn("Exception caught while updating discord_member", e);
        }
    }

    private void update(boolean guild, long id, String... toSet) {
        String table = guild ? "guild" : "user";
        String sql = "UPDATE discord_" + table + "SET ? WHERE " + table + "_id = ?;";
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, join(", ", toSet));
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            databaseLogger.warn("Exception caught while updating discord_guild", e);
        }
    }

    private enum DataType {
        BIGINT,
        VARCHAR,
        INT;
    }
}