package com.exemplo.pix.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    // --- IMPORTANTE: CONFIGURE SEUS DADOS AQUI ---
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/sistemapix?useSSL=false&serverTimezone=UTC";
    private static final String DATABASE_USER = "root"; // Coloque seu usuário do MySQL aqui (geralmente "root")
    private static final String DATABASE_PASSWORD = "Thi13579123#"; // Coloque sua senha do MySQL aqui
    // ----------------------------------------------

    // Bloco estático para carregar o driver do MySQL uma única vez
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver do MySQL não encontrado! Verifique se o conector está no pom.xml.");
            e.printStackTrace();
            throw new RuntimeException("Falha ao carregar o driver do MySQL", e);
        }
    }

    /**
     * Obtém uma nova conexão com o banco de dados.
     * @return uma objeto Connection.
     * @throws SQLException se a conexão falhar.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
    }
}