package de.unhandledexceptions.codersclash.bot.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class Database {

    private boolean connected;
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private HikariConfig config;
    private HikariDataSource dataSource;

    private String url, username, password, dbname, port;

    public Database(String url, String port, String dbname, String username, String password) {
        this.url = url;
        this.port = port;
        this.username = username;
        this.password = password;
        this.dbname = dbname;
    }

    public void connect()
    {
        if (!connected) {

            config = new HikariConfig();

            config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", url, port, dbname));
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            dataSource = new HikariDataSource(config);

            try
            {
                connection = dataSource.getConnection();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
            connected = true;
        }
    }

    public void disconnect() {
        if (connected) {
                dataSource.close();
                connected = false;
        }
    }

    private void setupConfig()
    {



    }

    public void executeSQL(String sql)
    {

        try
        {
            statement = connection.createStatement();

            statement.executeQuery(sql);

            System.out.println("executed");

        } catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    public Connection getConnection()
    {

        return connection;
    }

    public boolean isConnected()
    {
        return connected;
    }

}