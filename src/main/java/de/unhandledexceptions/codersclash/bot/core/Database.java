package de.unhandledexceptions.codersclash.bot.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private Connection connection;

    public Database(String url, String username, String password) {
        try {
            this.connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.err.println("Database connection could not be established due to an SQLException.");
            e.printStackTrace();
        }
    }

}
