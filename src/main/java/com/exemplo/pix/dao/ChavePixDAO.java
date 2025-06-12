package com.exemplo.pix.dao;

import com.exemplo.pix.model.ChavePix;
import com.exemplo.pix.model.Conta;
import com.exemplo.pix.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChavePixDAO {

    public void inserir(ChavePix chave) throws SQLException {
        String sql = "INSERT INTO chaves_pix (id_conta, tipo_chave, valor_chave) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, chave.getIdConta());
            stmt.setString(2, chave.getTipoChave());
            stmt.setString(3, chave.getValorChave());
            stmt.executeUpdate();
        }
    }

    public void remover(int idChave) throws SQLException {
        String sql = "DELETE FROM chaves_pix WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idChave);
            stmt.executeUpdate();
        }
    }
    
    public void atualizar(ChavePix chave) throws SQLException {
        String sql = "UPDATE chaves_pix SET valor_chave = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chave.getValorChave());
            stmt.setInt(2, chave.getId());
            stmt.executeUpdate();
        }
    }

    public List<ChavePix> listarPorContaId(int idConta) {
        List<ChavePix> chaves = new ArrayList<>();
        String sql = "SELECT * FROM chaves_pix WHERE id_conta = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConta);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chaves.add(extrairChaveDoResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chaves;
    }

    public boolean verificarSeExisteChavePorTipo(int idConta, String tipoChave) {
        String sql = "SELECT COUNT(*) FROM chaves_pix WHERE id_conta = ? AND tipo_chave = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConta);
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

    // MÉTODO ADICIONADO QUE FALTAVA
    public boolean isChaveEmUso(String valorChave) {
        String sql = "SELECT COUNT(*) FROM chaves_pix WHERE valor_chave = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, valorChave);
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

    private ChavePix extrairChaveDoResultSet(ResultSet rs) throws SQLException {
        ChavePix chave = new ChavePix();
        chave.setId(rs.getInt("id"));
        chave.setIdConta(rs.getInt("id_conta"));
        chave.setTipoChave(rs.getString("tipo_chave"));
        chave.setValorChave(rs.getString("valor_chave"));
        return chave;
    }
}