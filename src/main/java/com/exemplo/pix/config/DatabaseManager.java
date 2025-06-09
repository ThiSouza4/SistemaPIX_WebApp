package com.exemplo.pix.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    // ATENÇÃO: Verifique se seu usuário e senha estão corretos aqui.
    // A URL foi atualizada para incluir a configuração de timezone.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SistemaPIX?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Thi13579123#"; // <-- COLOQUE SUA SENHA DO MYSQL AQUI

    static {
        try {
            // Carrega o driver JDBC do MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL: MySQL JDBC Driver não encontrado. Verifique o pom.xml.");
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    /**
     * Obtém uma conexão com o banco de dados.
     * @return Uma instância de Connection.
     * @throws SQLException Se ocorrer um erro ao conectar.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
