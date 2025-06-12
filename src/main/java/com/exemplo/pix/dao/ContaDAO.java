package com.exemplo.pix.dao;

import com.exemplo.pix.model.Conta;
import com.exemplo.pix.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContaDAO {

    public void inserir(Conta conta) throws SQLException {
        String sql = "INSERT INTO contas (id_cliente, numero_conta, saldo) VALUES (?, ?, ?)";
        try (Connection conexao = DatabaseUtil.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, conta.getIdCliente());
            stmt.setString(2, conta.getNumeroConta());
            stmt.setBigDecimal(3, conta.getSaldo());
            stmt.executeUpdate();
        }
    }

    public Conta buscarPorClienteId(int idCliente) {
        String sql = "SELECT * FROM contas WHERE id_cliente = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairContaDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * MÉTODO ADICIONADO: Encontra uma conta através de uma chave PIX.
     * Essencial para as transferências.
     */
    public Conta buscarPorChavePix(String valorChave) {
        String sql = "SELECT c.* FROM contas c " +
                     "JOIN chaves_pix cp ON c.id = cp.id_conta " +
                     "WHERE cp.valor_chave = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, valorChave);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairContaDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * MÉTODO IDENTIFICADO COMO NECESSÁRIO: Busca uma conta pelo seu ID primário.
     */
    public Conta buscarPorId(int idConta) {
        String sql = "SELECT * FROM contas WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairContaDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * MÉTODO TRANSACIONAL: Atualiza o saldo, usando uma conexão externa.
     */
    public void atualizarSaldo(Connection conn, Conta conta) throws SQLException {
        String sql = "UPDATE contas SET saldo = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, conta.getSaldo());
            stmt.setInt(2, conta.getId());
            stmt.executeUpdate();
        }
    }

    private Conta extrairContaDoResultSet(ResultSet rs) throws SQLException {
        Conta conta = new Conta();
        conta.setId(rs.getInt("id"));
        conta.setIdCliente(rs.getInt("id_cliente"));
        conta.setNumeroConta(rs.getString("numero_conta"));
        conta.setSaldo(rs.getBigDecimal("saldo"));
        return conta;
    }
}