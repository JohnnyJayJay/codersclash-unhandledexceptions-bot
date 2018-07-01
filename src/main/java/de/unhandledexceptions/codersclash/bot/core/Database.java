package de.unhandledexceptions.codersclash.bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private boolean connected;

    private HikariConfig config;
    private HikariDataSource dataSource;

    private String ip, username, password, dbname, port;

    public Database(String ip, String port, String dbname, String username, String password)
    {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.dbname = dbname;
    }

    public void connect()
    {
        if (!connected)
        {
            try (var connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", ip, port),
                    username, password)) {
                System.out.println("[INFO] Creating database (if not exists)...");
                String sql = "CREATE DATABASE IF NOT EXISTS " + dbname + ";";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.execute();
                System.out.println("[INFO] Database created (or it already existed).");
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            config = new HikariConfig();

            System.out.printf("[INFO] Connecting to %s...\n", ip);

            config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", ip, port, dbname));
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

    public void createTablesIfNotExist(String[] creationStatements) {
        try (var connection = dataSource.getConnection()) {
            for (String statement : creationStatements) {
                var preparedStatement = connection.prepareStatement(statement);
                preparedStatement.execute();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
