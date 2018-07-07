package de.unhandledexceptions.codersclash.bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.lang.String.format;
import static de.unhandledexceptions.codersclash.bot.util.Logging.databaseLogger;

public class Database {

    private boolean connected;

    private HikariConfig config;
    private HikariDataSource dataSource;

    // TODO Precompiled statements machen
    private String selectFromGuild, selectFromMember, selectFromUser,
            countUsers, countGuilds, countMembers,
            insertUser, insertGuild, insertMember,
            updateUserXp, updateUserLvl, updateMemberXp, updateMemberLvl, updatePermissionLvl;

    private final String[] creationStatements = {
            "CREATE TABLE IF NOT EXISTS discord_guild (xp_system_activated BIT(1) DEFAULT 1,prefix VARCHAR(30),guild_id BIGINT NOT NULL,mail_channel BIGINT, PRIMARY KEY " +
                    "(guild_id));",
            "CREATE TABLE IF NOT EXISTS discord_user (user_id BIGINT NOT NULL,user_xp INT DEFAULT 0,user_lvl INT DEFAULT 1, PRIMARY KEY (user_id));",
            "CREATE TABLE IF NOT EXISTS discord_member (guild_id BIGINT NOT NULL REFERENCES discord_guild (guild_id) ON DELETE CASCADE,user_id BIGINT NOT NULL REFERENCES discord_user (user_id) ON DELETE CASCADE,reports TEXT," +
                    "member_xp INT DEFAULT 0,member_lvl INT DEFAULT 1,permission_lvl SMALLINT DEFAULT 0,PRIMARY KEY (user_id, guild_id));"
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
                this.createStatements();
            } catch (HikariPool.PoolInitializationException e) {
                databaseLogger.error(" Error while connecting to database. Check your config.", e);
                System.exit(1);
            }
        }
    }

    private void createStatements() {
        databaseLogger.info("Preparing statements...");
        this.selectFromGuild = "SELECT * FROM discord_guild WHERE guild_id = ?;";
        this.selectFromUser = "SELECT * FROM discord_user WHERE user_id = ?;";
        this.selectFromMember = "SELECT * FROM discord_member WHERE guild_id = ? AND user_id = ?;";
        this.countGuilds = "SELECT COUNT(*) AS entries FROM discord_guild WHERE guild_id = ?;";
        this.countUsers = "SELECT COUNT(*) AS entries FROM discord_user WHERE user_id = ?;";
        this.countMembers = "SELECT COUNT(*) AS entries FROM discord_member WHERE guild_id = ? AND user_id = ?;";
        this.insertGuild = "INSERT INTO discord_guild (guild_id) VALUE (?);";
        this.insertUser = "INSERT INTO discord_user (user_id, user_xp) VALUES (?, 0);";
        this.insertMember = "INSERT INTO discord_member(guild_id, user_id) VALUES (?, ?);";
        this.updateMemberLvl = "UPDATE discord_member SET member_lvl = ? WHERE guild_id = ? AND user_id = ?;";
        this.updateMemberXp = "UPDATE discord_member SET member_xp = ? WHERE guild_id = ? AND user_id = ?;";
        this.updateUserLvl = "UPDATE discord_user SET user_lvl = ? WHERE user_id = ?;";
        this.updateUserXp = "UPDATE discord_user SET user_xp = ? WHERE user_id = ?;";
        this.updatePermissionLvl = "UPDATE discord_member SET permission_lvl = ? WHERE guild_id = ? AND user_id = ?;";
        databaseLogger.info("statement preparation successful.");
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
        this.executeUpdate(updatePermissionLvl, lvl, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public int getPermissionLevel(Member member) {
        return this.<Integer>getFirst("permission_lvl", selectFromMember, Integer.TYPE, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public void setPrefix(long guildId, String prefix) {
        this.executeUpdate("UPDATE discord_guild SET prefix = " + prefix + " WHERE guild_id = " + guildId + ";");
    }

    public void setMailChannel(long guildId, long channelId) {
        this.executeUpdate("UPDATE discord_guild SET mail_channel = " + channelId + " WHERE guild_id = " + guildId + ";");
    }

    public void setUserXp(User user, long xp) {
        this.createUserIfNotExists(user.getIdLong());
        this.executeUpdate(updateUserXp, xp, user.getIdLong());
    }

    public void setGuildXp(Member member, long xp) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        this.executeUpdate(updateMemberXp, xp, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public void setUserLvl(User user, long lvl) {
        this.createUserIfNotExists(user.getIdLong());
        this.executeUpdate(updateUserLvl, lvl, user.getIdLong());
    }

    public void setGuildLvl(Member member, long lvl) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        this.executeUpdate(updateMemberLvl, lvl, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public void addUserLvl(User user) {
        this.setUserXp(user, 0);
        this.setUserLvl(user, this.getUserLvl(user)+1);
    }

    public void addGuildLvl(Member member) {
        this.setGuildXp(member, 0);
        this.setGuildLvl(member, this.getGuildLvl(member)+1);
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
        return this.getFirst("prefix", selectFromGuild, String.class, guild.getIdLong());
    }

    // TODO Automatisches erstellen von user usw. entfernen (soll von außerhalb geschehen)
    public long getUserXp(User user) {
        this.createUserIfNotExists(user.getIdLong());
        return this.<Long>getFirst("user_xp", selectFromUser, Long.TYPE, user.getIdLong());
    }

    public long getGuildXp(Member member) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return this.<Long>getFirst("member_xp", selectFromMember, Long.TYPE, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public long getUserLvl(User user) {
        this.createUserIfNotExists(user.getIdLong());
        return this.<Long>getFirst("user_lvl", selectFromUser, Long.TYPE, user.getIdLong());
    }

    public long getGuildLvl(Member member) {
        this.createMemberIfNotExists(member.getGuild().getIdLong(), member.getUser().getIdLong());
        return this.<Long>getFirst("member_lvl", selectFromMember, Long.TYPE, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public ArrayList<String> orderBy(String table, String orderby) {
        // TODO anpassen
        try (var connection = dataSource.getConnection();
             var preparedstatement = connection.prepareStatement("SELECT * FROM "+table)) {
            var resultSet = preparedstatement.executeQuery();
            while (resultSet.next()) {}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long getMailChannel(Guild guild) {
        return this.<Long>getFirst("mail_channel", selectFromGuild, Long.TYPE, guild.getIdLong());
    }

    public boolean isConnected() {
        return connected;
    }

    public void createMemberIfNotExists(long guildId, long userId) {
        if (this.getFirst("entries", countMembers, Integer.TYPE, guildId, userId) == 0) {
            this.createUserIfNotExists(userId);
            this.createGuildIfNotExists(guildId);
            this.executeUpdate(insertMember, guildId, userId);
        }
    }

    public void createGuildIfNotExists(long guildId) {
        if (this.getFirst("entries", countGuilds, Integer.TYPE, guildId) == 0) {
            this.executeUpdate(insertGuild, guildId);
        }
    }

    public void createUserIfNotExists(long userId) {
        if (this.getFirst("entries", countUsers, Integer.TYPE, userId) == 0) {
            this.executeUpdate(insertUser, userId);
        }
    }
    
    public ArrayList<String> orderBy(String table, String orderby) {
        try (var connection = dataSource.getConnection();
            var preparedstatement = connection.prepareStatement("SELECT * FROM "+table)) {
            var resultSet = preparedstatement.executeQuery();
            while (resultSet.next()) {}
        }
    }

    // Gibt den das erste Ergebnis zurück. Funktioniert nur mit einer select-column und einer tabelle. Falls Dinge von discord_member geholt werden, als erste id die guil-, als
    // zweite id die user_id angeben.
    private <T> T getFirst(String column, String sql, Class<T> type, long... ids) {
        T ret = null;
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)){
            for (int i = 0; i < ids.length; i++)
                statement.setLong((i + 1), ids[i]);
            var resultSet = statement.executeQuery();
            if (resultSet.next())
                ret = resultSet.getObject(column, type);
        } catch (SQLException e) {
            databaseLogger.error("Exception caught while executing query or parsing the results", e);
        }
        return ret;
    }

    private void executeUpdate(String sql, long... longs) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < longs.length; i++)
                statement.setLong((i + 1), longs[i]);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}