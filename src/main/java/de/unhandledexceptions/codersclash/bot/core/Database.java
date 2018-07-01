package de.unhandledexceptions.codersclash.bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.*;
import java.util.ArrayList;

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
        System.out.println("0 " + connected);
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
                System.out.println("1 " + connected);
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
        try {
            // TODO tables erstellen
            var preparedStatement = dataSource.getConnection().prepareStatement("");
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
        try {
            PreparedStatement ps = dataSource.getConnection().prepareStatement("SELECT * FROM `" + table + "` WHERE `" + where + "`=?");
            ps.setString(1, wherevalue);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs;
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<ResultSet> getAll(String table) {
        try {
            ArrayList<ResultSet> resultSets = new ArrayList<>();
            PreparedStatement ps = dataSource.getConnection().prepareStatement("SELECT * FROM `"+table+"`");
            ResultSet rs = ps.executeQuery();
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
        try {
            PreparedStatement ps = dataSource.getConnection().prepareStatement("UPDATE `"+table+"` SET `"+what+"`=? WHERE `"+where+"`=?");
            ps.setString(1, whatvalue);
            ps.setString(2, wherevalue);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert(String table, String what, String whatvalue) {
        try {
            PreparedStatement ps = dataSource.getConnection().prepareStatement("INSERT INTO `"+table+"`(`"+what+"`) VALUES ('"+whatvalue+"')");
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(String table, String where, String wherevalue) {
        try {
            PreparedStatement ps = dataSource.getConnection().prepareStatement("DELETE FROM `"+table+"` WHERE `"+where+"`=?");
            ps.setString(1, wherevalue);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
