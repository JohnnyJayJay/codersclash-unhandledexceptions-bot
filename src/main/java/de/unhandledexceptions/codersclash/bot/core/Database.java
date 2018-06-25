package de.unhandledexceptions.codersclash.bot.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private boolean connected;
    private Connection connection;

    private String url, username, password;

    public Database(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.connected = false;
    }

    public boolean isSetUp() {
       return false;
    }

    public void create() {

    }

    public void connect() {
        if (!connected) {
            try {
                connection = DriverManager.getConnection(url, username, password);
                connected = true;
            } catch (SQLException e) {
                System.err.println("[ERROR] Connection could not be established due to an SQLException. Please check if the connection information is correct.");
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        if (connected) {
            try {
                connection.close();
                connected = false;
            } catch (SQLException e) {
                System.err.println("[ERROR] Database connection could not be closed due to an SQLException.");
                e.printStackTrace();
            }
        }
    }
}
