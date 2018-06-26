package de.unhandledexceptions.codersclash.bot.core;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.*;

public class Database {

    private boolean connected;
    private ComboPooledDataSource cpds;
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private String url, username, password, dbname;

    public Database(String url, String dbname, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.dbname = dbname;
    }

    public void connect()
    {
        if (!connected) {
                cpds = new ComboPooledDataSource();
            try
            {
                cpds.setDriverClass("com.mysql.cj.jdbc.Driver");
            } catch (PropertyVetoException e)
            {
                e.printStackTrace();
            }
            cpds.setJdbcUrl("jdbc:mysql://" + url + "/" + dbname);
                cpds.setUser(username);
                cpds.setPassword(password);


            try
            {
                this.connection = cpds.getConnection();
            } catch (SQLException e)
            {
                System.err.println("[ERROR] Connection could not be established due to an SQLException. Please check if the connection information is correct.");
                e.printStackTrace();
            }
            connected = true;
        }
    }

    public void disconnect() {
        if (connected) {
                cpds.close();
                connected = false;
        }
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
        try
        {
            return cpds.getConnection();
        } catch (SQLException e)
        {
            System.err.println("[ERROR] Database connection could not be closed due to an SQLException.");
            e.printStackTrace();
        }
        return null;
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