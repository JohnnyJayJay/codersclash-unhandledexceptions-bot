package de.unhandledexceptions.codersclash.bot.core;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private boolean connected;
    private ComboPooledDataSource cpds;
    private Connection connection;

    private String url, username, password, dbname;

    public Database(String url, String username, String password, String dbname) {
        this.url = url;

        this.username = username;
        this.password = password;
        this.dbname = dbname;
    }

    public void setup() {

    }

    public void connect() throws PropertyVetoException, SQLException
    {
        if (!connected) {
                cpds = new ComboPooledDataSource();
                cpds.setDriverClass("com.mysql.jdbc.Driver");
                cpds.setJdbcUrl(url + "/" + dbname);
                cpds.setUser(username);
                cpds.setPassword(password);

                this.connection = cpds.getConnection();
                connected = true;
        }
    }

    public void disconnect() {
        if (connected) {
                cpds.close();
                connected = false;
        }
    }

    public Connection getConnection() throws SQLException
    {
        return cpds.getConnection();
    }

    public boolean isConnected()
    {
        return connected;
    }
}