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

            config = new HikariConfig("db.properties");
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

            System.out.println("Created Table");

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

    private String writeResultSet(ResultSet resultSet) throws SQLException {

        var stringBuilder = new StringBuilder();

        while (resultSet.next()) {
            String user = resultSet.getString("myuser");
            String website = resultSet.getString("webpage");
            String summary = resultSet.getString("summary");
            Date date = resultSet.getDate("datum");
            String comment = resultSet.getString("comments");
            stringBuilder.append("User: ").append(user);
            stringBuilder.append("Website: ").append(website);
            stringBuilder.append("summary: ").append(summary);
            stringBuilder.append("Date: ").append(date);
            stringBuilder.append("Comment: ").append(comment);
        }

        return stringBuilder.toString();
    }
}