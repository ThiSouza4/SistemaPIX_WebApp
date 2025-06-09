package com.exemplo.pix.dao;

import com.exemplo.pix.model.Conta;
import com.exemplo.pix.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContaDAO {

    public Conta buscarPorIdCliente(int idCliente) {
        String sql = "SELECT * FROM contas WHERE id_cliente = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Conta conta = new Conta();
                    conta.setIdConta(rs.getInt("id_conta"));
                    conta.setIdCliente(rs.getInt("id_cliente"));
                    conta.setNumeroConta(rs.getString("numero_conta"));
                    conta.setSaldo(rs.getBigDecimal("saldo"));
                    return conta;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void inserir(Conta conta) throws SQLException {
        String sql = "INSERT INTO contas (id_cliente, numero_conta, saldo) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conta.getIdCliente());
            stmt.setString(2, conta.getNumeroConta());
            stmt.setBigDecimal(3, conta.getSaldo());
            stmt.executeUpdate();
        }
    }
}