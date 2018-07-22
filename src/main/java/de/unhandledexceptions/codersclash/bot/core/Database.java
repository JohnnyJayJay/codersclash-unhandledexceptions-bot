package de.unhandledexceptions.codersclash.bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import de.unhandledexceptions.codersclash.bot.commands.ScoreBoardCommand;
import de.unhandledexceptions.codersclash.bot.util.Logging;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static java.lang.String.format;

public class Database {

    private static Logger logger = Logging.getLogger();
    private boolean connected;

    private HikariConfig config;
    private HikariDataSource dataSource;

    private String selectFromGuild, selectFromMember, selectFromUser, selectAllGuilds, selectAllUsers, selectAllMembers,
            countUsers, countGuilds, countMembers,
            insertUser, insertGuild, insertMember,
            updateUserXp, updateUserLvl, updateMemberXp, updateMemberLvl, updatePermissionLvl, updatePrefix, updateMailChannel, updateMaxReports,
            selectReports, updateXpSystem, updateAutoChannel;

    private String[] creationStatements;

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
                logger.info("Creating database (if not exists)...");
                preparedStatement.executeUpdate();
                logger.info("Database created (or it already existed).");
            } catch (SQLException e) {
                logger.error("Exception caught while connecting", e);
            }
            config = new HikariConfig();
            logger.info("Connecting to " + ip + "...");

            config.setJdbcUrl(format("jdbc:mysql://%s:%s/%s?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", ip, port, dbname));
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            try {
                dataSource = new HikariDataSource(config);
                connected = true;
                logger.info("Database connection pool successfully opened.");
                this.createStatements();
            } catch (HikariPool.PoolInitializationException e) {
                logger.error(" Error while connecting to database. Check your config.", e);
                System.exit(1);
            }
        }
    }

    private void createStatements() {
        logger.info("Preparing statements...");
        this.creationStatements = new String[] {
                "CREATE TABLE IF NOT EXISTS discord_guild (reports_until_ban SMALLINT DEFAULT 3, xp_system_activated BIT(1) DEFAULT 1,prefix VARCHAR(30),guild_id BIGINT NOT " +
                        "NULL,mail_channel BIGINT,auto_channel BIGINT,PRIMARY KEY (guild_id));",
                "CREATE TABLE IF NOT EXISTS discord_user (user_id BIGINT NOT NULL,user_xp INT DEFAULT 0,user_lvl INT DEFAULT 1, PRIMARY KEY (user_id));",
                "CREATE TABLE IF NOT EXISTS discord_member (member_id BIGINT NOT NULL AUTO_INCREMENT, guild_id BIGINT NOT NULL,user_id BIGINT NOT NULL," +
                        "member_xp INT DEFAULT 0,member_lvl INT DEFAULT 1,permission_lvl SMALLINT DEFAULT 1,UNIQUE (user_id, guild_id),FOREIGN KEY (guild_id) REFERENCES discord_guild (guild_id) ON DELETE CASCADE,"
                        + "FOREIGN KEY (user_id) REFERENCES discord_user (user_id), PRIMARY KEY (member_id));",
                "CREATE TABLE IF NOT EXISTS reports (member_id BIGINT NOT NULL,report1 TEXT,report2 TEXT,report3 TEXT," +
                        "report4 TEXT,report5 TEXT,report6 TEXT,report7 TEXT,report8 TEXT,report9 TEXT,report10 TEXT,PRIMARY KEY (member_id), FOREIGN KEY (member_id) REFERENCES discord_member (member_id) ON DELETE CASCADE);"
        };
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
        this.selectReports = "SELECT * FROM reports WHERE member_id = ?;";
        this.updatePrefix = "UPDATE discord_guild SET prefix = ? WHERE guild_id = ?;";
        this.updateMailChannel = "UPDATE discord_guild SET mail_channel = ? WHERE guild_id = ?;";
        this.updateMaxReports = "UPDATE discord_guild SET reports_until_ban = ? WHERE guild_id = ?;";
        this.updateXpSystem = "UPDATE discord_guild SET xp_system_activated = ? WHERE guild_id = ?;";
        this.selectAllGuilds = "SELECT guild_id FROM discord_guild;";
        this.selectAllUsers = "SELECT user_id FROM discord_user";
        this.selectAllMembers = "SELECT guild_id, user_id FROM discord_member;";
        this.updateAutoChannel = "UPDATE discord_guild SET auto_channel = ? WHERE guild_id = ?;";

        logger.info("Statement preparation successful.");
    }

    public void disconnect() {
        if (connected) {
            dataSource.close();
            logger.warn("Database disconnected!");
            connected = false;
        }
    }

    public void createTablesIfNotExist() {
        try (var connection = dataSource.getConnection()) {
            for (String statement : this.creationStatements) {
                logger.debug(statement);
                try (var preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
            logger.info("Tables have been created (or they existed already).");
        } catch (SQLException e) {
            logger.error("Exception caught while creating tables", e);
        }
    }

    public void changePermissionLevel(Member member, int lvl) {
        this.executeUpdate(updatePermissionLvl, lvl, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public int getPermissionLevel(Member member) {
        return this.<Integer>getFirst("permission_lvl", selectFromMember, Integer.TYPE, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public void deleteMember(long guildId, long userId) {
        this.executeUpdate("DELETE FROM discord_member WHERE guild_id = ? AND user_id = ?;", guildId, userId);
    }

    public void deleteGuild(long guildId) {
        this.executeUpdate("DELETE FROM discord_guild WHERE guild_id = ?;", guildId);
    }

    public void deleteUser(long userId) {
        this.executeUpdate("DELETE FROM discord_user WHERE user_id = ?;", userId);
    }

    public void setPrefix(long guildId, String prefix) {
        this.executeUpdate(updatePrefix, prefix, guildId);
    }

    public void setMailChannel(long guildId, long channelId) {
        this.executeUpdate(updateMailChannel, channelId, guildId);
    }

    public void setAutoChannel(long guildId, long channelId) {
        this.executeUpdate(updateAutoChannel, channelId, guildId);
    }

    public void setReportsUntilBan(long guildId, int reportsUntilBan) {
        this.executeUpdate(updateMaxReports, reportsUntilBan, guildId);
    }

    public boolean addReport(Member member, String report) {
        boolean ret = false;
        var currentReports = this.getReports(member);
        if (currentReports.size() < 10) {
            int memberId = this.getMemberId(member.getGuild().getIdLong(), member.getUser().getIdLong());
            this.executeUpdate("UPDATE reports SET report" + (currentReports.size() + 1) + " = ? WHERE member_id = ?;", report, memberId);
            ret = true;
        }
        return ret;
    }

    public void removeAllReports(Member member) {
        try (var connection = dataSource.getConnection();
             var deleteStatement = connection.prepareStatement("DELETE FROM reports WHERE member_id = ?;");
             var insertStatement = connection.prepareStatement("INSERT INTO reports(member_id) VALUE (?);")) {
            int memberId = this.getMemberId(member.getGuild().getIdLong(), member.getUser().getIdLong());
            deleteStatement.setInt(1, memberId);
            insertStatement.setInt(1, memberId);
            deleteStatement.executeUpdate();
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("An SQLException occurred while removing all reports", e);
        }
    }

    public void removeReport(Member member, int number) {
        int memberId = this.getMemberId(member.getGuild().getIdLong(), member.getUser().getIdLong());
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(selectReports)) {
            statement.setInt(1, memberId);
            var resultSet = statement.executeQuery();
            resultSet.next();
            if (number < 10 && resultSet.getString("report" + (number + 1)) != null) {
                var builder = new StringBuilder().append("UPDATE reports SET ");
                for (int i = number; i < 11 && resultSet.getString("report" + i) != null; i++) {
                    String next = i < 10 ? resultSet.getString("report" + (i + 1)) : null;
                    if (next == null) {
                        builder.append("report" + i + " = NULL ");
                        break;
                    } else
                        builder.append("report" + i + " = '" + next + "', ");
                }
                builder.append("WHERE member_id = ?;");
                this.executeUpdate(builder.toString(), memberId);
            } else {
                this.executeUpdate("UPDATE reports SET report" + number + " = NULL WHERE member_id = ?;", memberId);
            }
        } catch (SQLException e) {
            logger.error("An SQLException occurred while removing report from member:", e);
        }
    }

    public Map<Long, Set<Long>> getMembers() {
        Map<Long, Set<Long>> ret = new HashMap<>();
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(selectAllMembers)) {
            var resultSet = statement.executeQuery();
            long guildId;
            while (resultSet.next()) {
                guildId = resultSet.getLong(1);
                if (ret.containsKey(guildId)) {
                    ret.get(guildId).add(resultSet.getLong(2));
                } else {
                    ret.put(guildId, new HashSet<>());
                    ret.get(guildId).add(resultSet.getLong(2));
                }
            }
        } catch (SQLException e) {
            logger.error("An SQLException occurred while getting all members from datasource: ", e);
        }
        return ret;
    }

    public List<Long> getIds(String table) {
        String statement = null;
        if (table.equals("discord_guild"))
            statement = selectAllGuilds;
        else if (table.equals("discord_user"))
            statement = selectAllUsers;

        List<Long> ret = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var prepStatement = connection.prepareStatement(statement)) {
            var resultSet = prepStatement.executeQuery();
            while (resultSet.next())
                ret.add(resultSet.getLong(1));
        } catch (SQLException e) {
            logger.error("An SQLException occurred while getting all " + table + " ids from datasource: ", e);
        }
        return ret;
    }

    public List<ScoreBoardCommand.ScoreBoardUser> getScoreBoard(String table, String order) {
        try (var connection = dataSource.getConnection();
             var preparedstatement = connection.prepareStatement("SELECT * FROM "+table+" ORDER BY "+order+" DESC;")) {
            var resultset = preparedstatement.executeQuery();
            var list = new ArrayList<ScoreBoardCommand.ScoreBoardUser>();
            String prefix = "";
            String guildid = "";
            if (table.equals("discord_user")) {
                prefix = "user";
                guildid = "user_id";
            } else {
                prefix = "member";
                guildid = "guild_id";
            }
            while (resultset.next()) {
                list.add(new ScoreBoardCommand.ScoreBoardUser(resultset.getString("user_id"), resultset.getString(guildid),
                        resultset.getLong(prefix+"_xp"), resultset.getLong(prefix+"_lvl")));
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setUserXp(User user, long xp) {
        this.executeUpdate(updateUserXp, xp, user.getIdLong());
    }

    public void setGuildXp(Member member, long xp) {
        this.executeUpdate(updateMemberXp, xp, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public void setUserLvl(User user, long lvl) {
        this.executeUpdate(updateUserLvl, lvl, user.getIdLong());
    }

    public void setGuildLvl(Member member, long lvl) {
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

    public void setUseXpSystem(long guildId, boolean use) {
        this.executeUpdate(updateXpSystem, use ? 1 : 0, guildId);
    }

    public Map<Long, String> getPrefixes() {
        Map<Long, String> ret = Collections.EMPTY_MAP;
        try (var connection = dataSource.getConnection();
             var preparedstatement = connection.prepareStatement("SELECT guild_id, prefix FROM discord_guild;")) {
            var resultSet = preparedstatement.executeQuery();
            ret = new HashMap<>();
            while (resultSet.next())
                ret.put(resultSet.getLong("guild_id"), resultSet.getString("prefix"));

        } catch (SQLException e) {
            logger.error("An Exception occurred while getting guild prefixes from database:", e);
        }
        return ret;
    }

    public boolean xpSystemActivated(long guildId) {
        return this.getFirst("xp_system_activated", selectFromGuild, Integer.TYPE, guildId) == 1;
    }

    public long getUserXp(User user) {
        return this.<Long>getFirst("user_xp", selectFromUser, Long.TYPE, user.getIdLong());
    }

    public long getGuildXp(Member member) {
        return this.<Long>getFirst("member_xp", selectFromMember, Long.TYPE, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public long getUserLvl(User user) {
        return this.<Long>getFirst("user_lvl", selectFromUser, Long.TYPE, user.getIdLong());
    }

    public long getGuildLvl(Member member) {
        return this.<Long>getFirst("member_lvl", selectFromMember, Long.TYPE, member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public int getReportsUntilBan(Guild guild) {
        return this.<Integer>getFirst("reports_until_ban", selectFromGuild, Integer.TYPE, guild.getIdLong());
    }

    public ArrayList<String> orderBy(String table, String orderby) {
        try (var connection = dataSource.getConnection();
             var preparedstatement = connection.prepareStatement("SELECT * FROM "+table)) {
            var resultSet = preparedstatement.executeQuery();
            while (resultSet.next()) {}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long getMailChannel(Guild guild) {
        return this.<Long>getFirst("mail_channel", selectFromGuild, Long.TYPE, guild.getIdLong());
    }

    public Long getAutoChannel(Guild guild) {
        return this.getFirst("auto_channel", selectFromGuild, Long.TYPE, guild.getIdLong());
    }

    public boolean isConnected() {
        return connected;
    }

    public void createMemberIfNotExists(long guildId, long userId) {
        if (this.getFirst("entries", countMembers, Integer.TYPE, guildId, userId) == 0) {
            this.createUserIfNotExists(userId);
            this.createGuildIfNotExists(guildId);
            this.executeUpdate(insertMember, guildId, userId);
            this.executeUpdate("INSERT INTO reports (member_id) VALUE (?);", this.getMemberId(guildId, userId));
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

    // Gibt den das erste Ergebnis zurück. Akzeptiert vorgefertigte SELECT statements und gibt das erste ergebnis aus der angegebenen spalte zurück
    // Für COUNT-Selects muss als Spaltenname "entries" angegeben werden
    private <T> T getFirst(String column, String sql, Class<T> type, Object... toSet) {
        T ret = null;
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)){
            logger.debug("Execute query: " + sql);
            setStatement(toSet, statement);
            var resultSet = statement.executeQuery();
            if (resultSet != null && resultSet.next())
                ret = resultSet.getObject(column, type);
        } catch (SQLException e) {
            logger.error("Exception caught while executing query or parsing the results", e);
        }
        return ret;
    }

    private int getMemberId(long guildId, long userId) {
        return this.getFirst("member_id", "SELECT member_id FROM discord_member WHERE guild_id = ? AND user_id = ?;", Integer.TYPE, guildId, userId);
    }

    public List<String> getReports(Member member) {
        List<String> ret = null;
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(selectReports)) {
            logger.debug("Execute query: " + selectReports);
            statement.setInt(1, this.getMemberId(member.getGuild().getIdLong(), member.getUser().getIdLong()));
            var resultSet = statement.executeQuery();
            resultSet.next();
            ret = new ArrayList<>();
            String report = null;
            for (int i = 1; i < 11 && (report = resultSet.getString("report" + i)) != null; i++)
                ret.add(report);
        } catch (SQLException e) {
            logger.error("An Exception occurred while parsing member reports:", e);
        }
        return ret;
    }

    private void executeUpdate(String sql, Object... toSet) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            logger.debug("Execute update: " + sql);
            setStatement(toSet, statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("An Exception occurred while trying to execute an update:", e);
        }
    }

    private void setStatement(Object[] toSet, PreparedStatement statement) {
        try {
            for (int i = 0; i < toSet.length; i++) {
                Object current = toSet[i];
                logger.debug("Setting value for statement: " + current);
                if (current instanceof Short)
                    statement.setShort(i + 1, (short) current);
                else if (current instanceof Integer)
                    statement.setInt(i + 1, (int) current);
                else if (current instanceof Long)
                    statement.setLong(i + 1, (long) current);
                else if (current instanceof String)
                    statement.setString(i + 1, (String) current);
                else if (current instanceof Boolean)
                    statement.setBoolean(i + 1, (boolean) current);
            }
        } catch (SQLException e) {
            logger.error("An exception occurred while setting values for PreparedStatement:", e);
        }
    }
}