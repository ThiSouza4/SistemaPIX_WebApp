package com.exemplo.pix.dao;

import com.exemplo.pix.model.HistoricoOperacoes;
import com.exemplo.pix.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HistoricoOperacoesDAO { // <-- GARANTA QUE ESTA LINHA ESTEJA EXATA

    public List<HistoricoOperacoes> listarOperacoesPorCliente(int idCliente) {
        List<HistoricoOperacoes> operacoes = new ArrayList<>();
        // Esta query busca operações onde o cliente é a origem OU o destino
        String sql = "SELECT ho.* FROM historicooperacoes ho " +
                     "JOIN contas c ON ho.id_conta_origem = c.id_conta " +
                     "LEFT JOIN contas c_destino ON ho.id_conta_destino = c_destino.id_conta " +
                     "WHERE c.id_cliente = ? OR c_destino.id_cliente = ? " +
                     "ORDER BY ho.data_operacao DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            stmt.setInt(2, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HistoricoOperacoes op = new HistoricoOperacoes();
                    op.setIdOperacao(rs.getInt("id_operacao"));
                    op.setIdContaOrigem(rs.getInt("id_conta_origem"));
                    // Usar getObject para tratar possíveis nulos na coluna de destino
                    op.setIdContaDestino(rs.getObject("id_conta_destino", Integer.class));
                    op.setTipoOperacao(rs.getString("tipo_operacao"));
                    op.setValor(rs.getBigDecimal("valor"));
                    op.setDataOperacao(rs.getTimestamp("data_operacao"));
                    operacoes.add(op);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar operações no DAO: " + e.getMessage());
            e.printStackTrace();
        }
        return operacoes;
    }
}