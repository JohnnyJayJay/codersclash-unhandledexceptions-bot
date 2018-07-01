package de.unhandledexceptions.codersclash.bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private boolean connected;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private HikariConfig config;
    private HikariDataSource dataSource;

    private String url, username, password, dbname, port;

    public Database(String url, String port, String dbname, String username, String password)
    {
        this.url = url;
        this.port = port;
        this.username = username;
        this.password = password;
        this.dbname = dbname;
    }

    public void connect()
    {
        if (!connected)
        {
            config = new HikariConfig();

            System.out.println("[INFO] Connecting to " + url);

            config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", url, port, dbname));
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            try
            {
                dataSource = new HikariDataSource(config);
                connected = true;
                // TODO Logger
                System.out.println("[INFO] Database connection successfully opened.");
            } catch (HikariPool.PoolInitializationException e)
            {
                e.printStackTrace();
                System.err.println("[ERROR] Error while connecting to database. Check your config.");
                System.exit(1);
            }
        }
    }


    public void disconnect()
    {
        if (connected) {
            dataSource.close();
            connected = false;
        }
    }

    public void createDataBaseIfNotExists(String dbname) {
        try (var connection = dataSource.getConnection()) {
            var preparedStatement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + dbname + ";");
            preparedStatement.execute();
            // TODO Logger
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTablesIfNotExist() {
        try (var connection = dataSource.getConnection()) {
            // TODO tables erstellen
            String sql = "CREATE TABLE IF NOT EXISTS discord_guild (\n" +
                    "  prefix VARCHAR(30),\n" +
                    "  guild_id BIGINT NOT NULL,\n" +
                    "  mail_channel BIGINT,\n" +
                    "  PRIMARY KEY (guild_id)\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS discord_user (\n" +
                    "  user_id BIGINT NOT NULL,\n" +
                    "  user_xp BIGINT,\n" +
                    "  PRIMARY KEY (user_id)\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS discord_member (\n" +
                    "  guild_id BIGINT NOT NULL REFERENCES guild(guild_id) ON DELETE CASCADE,\n" +
                    "  user_id BIGINT NOT NULL REFERENCES user(user_id) ON DELETE CASCADE,\n" +
                    "  member_id BIGINT,\n" +
                    "  reports TEXT,\n" +
                    "  member_xp BIGINT,\n" +
                    "  permission_lvl SMALLINT,\n" +
                    "  INDEX (member_id),\n" +
                    "  PRIMARY KEY (member_id),\n" +
                    "  FOREIGN KEY (guild_id) REFERENCES discord_guild (guild_id),\n" +
                    "  FOREIGN KEY (user_id) REFERENCES discord_user (user_id)\n" +
                    ");";
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
            // TODO Logger
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HikariConfig getConfig()
    {
        return config;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public ResultSet get(String table, String where, String wherevalue) {
        try (var connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `" + table + "` WHERE `" + where + "`=?");
            ps.setString(1, wherevalue);
            ResultSet rs = ps.executeQuery();
            ps.close();
            if (rs.next())
                return rs;
            else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ResultSet> getAll(String table) {
        try (var connection = dataSource.getConnection()) {
            List<ResultSet> resultSets = new ArrayList<>();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `"+table+"`");
            ResultSet rs = ps.executeQuery();
            ps.close();
            while (rs.next()) {
                resultSets.add(rs);
            }
            return resultSets;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void update(String table, String what, String whatvalue, String where, String wherevalue) {
        try (var connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("UPDATE `"+table+"` SET `"+what+"`=? WHERE `"+where+"`=?");
            ps.setString(1, whatvalue);
            ps.setString(2, wherevalue);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert(String table, String what, String whatvalue) {
        try (var connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO `"+table+"`(`"+what+"`) VALUES ('"+whatvalue+"')");
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(String table, String where, String wherevalue) {
        try (var connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM `"+table+"` WHERE `"+where+"`=?");
            ps.setString(1, wherevalue);
            ps.execute();
            // TODO Logger
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
