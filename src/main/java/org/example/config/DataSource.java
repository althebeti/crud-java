package org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataSource {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/app";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "password";

    private static Connection connection;

    public static void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
            createTableIfNotExists(connection);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    private static void createTableIfNotExists(Connection connection) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS persons (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255))";
        Statement statement = connection.createStatement();
        statement.execute(createTableSQL);
        statement.close();
    }
}
