package com.exemplo.pix.dao;

import com.exemplo.pix.model.Cliente;
import com.exemplo.pix.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClienteDAO {
    public Cliente buscarPorEmail(String email) {
        String sql = "SELECT * FROM clientes WHERE email = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractClienteFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cliente buscarPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id_cliente = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractClienteFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int inserir(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes (nome_completo, cpf, telefone, email, senha_hash) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, cliente.getNome()); 
            stmt.setString(2, cliente.getCpf());
            stmt.setString(3, cliente.getTelefone());
            stmt.setString(4, cliente.getEmail());
            stmt.setString(5, cliente.getSenhaHash());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return 0;
    }
    
    // --- NOVO MÉTODO ADICIONADO AQUI ---
    /**
     * Verifica se um dado único (como CPF, e-mail ou telefone) já está em uso na tabela de clientes.
     * @param nomeDaColuna O nome da coluna a ser verificada (ex: "cpf", "email").
     * @param valor O valor a ser procurado.
     * @return true se o dado já estiver em uso, false caso contrário.
     */
    public boolean isDadoUnicoEmUso(String nomeDaColuna, String valor) {
        // Validação para evitar SQL Injection, permitindo apenas colunas conhecidas.
        if (!nomeDaColuna.equals("cpf") && !nomeDaColuna.equals("email") && !nomeDaColuna.equals("telefone")) {
            // Se a coluna não for uma das esperadas, retorna false para não travar o sistema.
            return false;
        }

        String sql = "SELECT COUNT(*) FROM clientes WHERE " + nomeDaColuna + " = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, valor);
            
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


    private Cliente extractClienteFromResultSet(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNome(rs.getString("nome_completo")); 
        cliente.setCpf(rs.getString("cpf"));
        cliente.setTelefone(rs.getString("telefone"));  
        cliente.setEmail(rs.getString("email"));
        cliente.setSenhaHash(rs.getString("senha_hash"));
        return cliente;
    }
}