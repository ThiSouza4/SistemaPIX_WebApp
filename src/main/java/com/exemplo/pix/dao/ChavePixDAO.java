package com.exemplo.pix.dao;

import com.exemplo.pix.model.ChavePix;
import com.exemplo.pix.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChavePixDAO {

    public List<ChavePix> listarChavesPorCliente(int idCliente) {
        List<ChavePix> chaves = new ArrayList<>();
        String sql = "SELECT cp.* FROM chavespix cp JOIN contas c ON cp.id_conta = c.id_conta WHERE c.id_cliente = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChavePix chave = new ChavePix();
                    chave.setIdChave(rs.getInt("id_chave"));
                    chave.setIdConta(rs.getInt("id_conta"));
                    chave.setTipoChave(rs.getString("tipo_chave"));
                    chave.setChave(rs.getString("chave"));
                    chaves.add(chave);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chaves;
    }

    public void adicionarChave(int idCliente, ChavePix chave) throws SQLException {
        String sqlConta = "SELECT id_conta FROM contas WHERE id_cliente = ?";
        String sqlInsert = "INSERT INTO chavespix (id_conta, tipo_chave, chave) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection()) {
            int idConta = -1;
            try (PreparedStatement stmtConta = conn.prepareStatement(sqlConta)) {
                stmtConta.setInt(1, idCliente);
                try (ResultSet rs = stmtConta.executeQuery()) {
                    if (rs.next()) {
                        idConta = rs.getInt("id_conta");
                    }
                }
            }
            if (idConta == -1) {
                throw new SQLException("Conta não encontrada para o cliente.");
            }
            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                stmtInsert.setInt(1, idConta);
                stmtInsert.setString(2, chave.getTipoChave());
                stmtInsert.setString(3, chave.getChave());
                stmtInsert.executeUpdate();
            }
        }
    }

    public void atualizarChave(int idChave, String novaChave) throws SQLException {
        String sql = "UPDATE chavespix SET chave = ? WHERE id_chave = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novaChave);
            stmt.setInt(2, idChave);
            stmt.executeUpdate();
        }
    }

    public void removerChave(int idChave) {
        String sql = "DELETE FROM chavespix WHERE id_chave = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idChave);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isChaveEmUso(String chave) {
        String sql = "SELECT COUNT(*) FROM chavespix WHERE chave = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chave);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean verificarSeExisteChavePorTipo(int idCliente, String tipoChave) {
        String sql = "SELECT COUNT(*) FROM chavespix cp JOIN contas c ON cp.id_conta = c.id_conta WHERE c.id_cliente = ? AND cp.tipo_chave = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            stmt.setString(2, tipoChave);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}