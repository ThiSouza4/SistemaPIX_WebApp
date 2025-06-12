package com.exemplo.pix.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    // Parâmetros de segurança adicionados à URL
    private static final String DEFAULT_JDBC_URL = "jdbc:mysql://localhost:3306/sistemapix?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_JDBC_USER = "root";
    private static final String DEFAULT_JDBC_PASSWORD = "Thi13579123#"; // Lembre-se de usar sua senha

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver não encontrado.", e);
        }

        String jdbcUrl = System.getenv("DB_URL");
        String jdbcUser = System.getenv("DB_USER");
        String jdbcPassword = System.getenv("DB_PASSWORD");

        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            jdbcUrl = DEFAULT_JDBC_URL;
        }
        if (jdbcUser == null || jdbcUser.isEmpty()) {
            jdbcUser = DEFAULT_JDBC_USER;
        }
        if (jdbcPassword == null) {
            jdbcPassword = DEFAULT_JDBC_PASSWORD;
        }

        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }
}