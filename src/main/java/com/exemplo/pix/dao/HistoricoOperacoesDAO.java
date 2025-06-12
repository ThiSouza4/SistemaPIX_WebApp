package com.exemplo.pix.dao;

import com.exemplo.pix.model.HistoricoOperacoes;
import com.exemplo.pix.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoOperacoesDAO {

    /**
     * MÉTODO RESGATADO: Lista todas as operações de um cliente (entrada e saída).
     */
    public List<HistoricoOperacoes> listarOperacoesPorCliente(int idCliente) {
        List<HistoricoOperacoes> operacoes = new ArrayList<>();
        String sql = "SELECT ho.* FROM historico_operacoes ho " +
                     "JOIN contas c ON ho.id_conta_origem = c.id " + // Assumindo que a coluna é 'id'
                     "WHERE c.id_cliente = ? " +
                     "UNION " +
                     "SELECT ho.* FROM historico_operacoes ho " +
                     "JOIN contas c ON ho.id_conta_destino = c.id " +
                     "WHERE c.id_cliente = ? " +
                     "ORDER BY data_operacao DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            stmt.setInt(2, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HistoricoOperacoes op = new HistoricoOperacoes();
                    op.setId(rs.getInt("id"));
                    op.setIdContaOrigem(rs.getInt("id_conta_origem"));
                    op.setIdContaDestino(rs.getObject("id_conta_destino", Integer.class));
                    op.setTipoOperacao(rs.getString("tipo_operacao"));
                    op.setValor(rs.getBigDecimal("valor"));
                    op.setDataOperacao(rs.getTimestamp("data_operacao").toLocalDateTime());
                    operacoes.add(op);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operacoes;
    }

    /**
     * MÉTODO TRANSACIONAL: Insere um novo registro de operação no banco de dados.
     */
    public void inserir(Connection conn, HistoricoOperacoes operacao) throws SQLException {
        String sql = "INSERT INTO historico_operacoes (id_conta_origem, id_conta_destino, tipo_operacao, valor, data_operacao) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, operacao.getIdContaOrigem());

            // CORREÇÃO DO BUG: Trata o caso de id_conta_destino ser nulo (saque)
            if (operacao.getIdContaDestino() != null) {
                stmt.setInt(2, operacao.getIdContaDestino());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }

            stmt.setString(3, operacao.getTipoOperacao());
            stmt.setBigDecimal(4, operacao.getValor());
            stmt.setTimestamp(5, Timestamp.valueOf(operacao.getDataOperacao()));
            stmt.executeUpdate();
        }
    }
}