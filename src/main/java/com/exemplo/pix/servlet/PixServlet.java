package com.exemplo.pix.servlet;

import com.exemplo.pix.config.DatabaseManager;
import com.exemplo.pix.dto.ApiResponse;
import com.exemplo.pix.dto.PixTransferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

@WebServlet("/api/pix/*")
public class PixServlet extends HttpServlet {
    private final ObjectMapper objectMapper;
    private static final long serialVersionUID = 1L;

    public PixServlet() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/transfer".equals(pathInfo)) {
            handlePixTransfer(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handlePixTransfer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("id_cliente") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getWriter(), new ApiResponse("ERROR", "Usuário não autenticado."));
            return;
        }

        try {
            PixTransferRequest transferRequest = objectMapper.readValue(req.getReader(), PixTransferRequest.class);
            Integer idClienteLogado = (Integer) session.getAttribute("id_cliente");

            if (transferRequest.getValor() == null || transferRequest.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                 resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 objectMapper.writeValue(resp.getWriter(), new ApiResponse("ERROR", "O valor da transferência é inválido."));
                 return;
            }

            // A MÁGICA ACONTECE AQUI
            String statusFinal = realizarTransferenciaNoBanco(transferRequest, idClienteLogado);

            if ("SALDO_INSUFICIENTE".equals(statusFinal)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                objectMapper.writeValue(resp.getWriter(), new ApiResponse("ERROR", "Saldo insuficiente para realizar a operação."));
            } else if ("CHAVE_NAO_ENCONTRADA".equals(statusFinal)) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
                objectMapper.writeValue(resp.getWriter(), new ApiResponse("ERROR", "Chave PIX de destino não encontrada."));
            } else if (statusFinal.startsWith("ERRO")) {
                 resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
                 objectMapper.writeValue(resp.getWriter(), new ApiResponse("ERROR", "Erro no banco de dados."));
            } else {
                ApiResponse responsePayload = new ApiResponse("SUCCESS", "Transferência realizada com sucesso!");
                objectMapper.writeValue(resp.getWriter(), responsePayload);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ApiResponse("ERROR", "Erro interno no servidor: " + e.getMessage()));
        }
    }
    
    private String realizarTransferenciaNoBanco(PixTransferRequest request, Integer idClienteOrigem) throws SQLException {
        // SQLs para a transação
        String sqlFindContaOrigem = "SELECT id_conta FROM Contas WHERE id_cliente = ?";
        String sqlFindContaDestino = "SELECT c.id_conta FROM Contas c JOIN ChavesPix cp ON c.id_cliente = cp.id_cliente WHERE cp.chave = ?";
        String sqlUpdateSaldoOrigem = "UPDATE Contas SET saldo = saldo - ? WHERE id_conta = ? AND saldo >= ?";
        String sqlUpdateSaldoDestino = "UPDATE Contas SET saldo = saldo + ? WHERE id_conta = ?";
        String sqlInsertHistorico = "INSERT INTO HistoricoOperacoes (id_conta_origem, id_conta_destino, chave_pix_destino, valor, data_operacao, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Obter ID da conta de ORIGEM
            Integer idContaOrigem = null;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlFindContaOrigem)) {
                pstmt.setInt(1, idClienteOrigem);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) idContaOrigem = rs.getInt("id_conta");
                }
            }

            // 2. Obter ID da conta de DESTINO
            Integer idContaDestino = null;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlFindContaDestino)) {
                pstmt.setString(1, request.getChavePixDestino());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        idContaDestino = rs.getInt("id_conta");
                    } else {
                        return "CHAVE_NAO_ENCONTRADA";
                    }
                }
            }
            
            // Não permitir transferir para si mesmo
            if(idContaOrigem.equals(idContaDestino)) {
                 return "ERRO: Não é possível transferir para si mesmo.";
            }

            // 3. Debitar da conta de origem (verificando saldo)
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateSaldoOrigem)) {
                pstmt.setBigDecimal(1, request.getValor());
                pstmt.setInt(2, idContaOrigem);
                pstmt.setBigDecimal(3, request.getValor());
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return "SALDO_INSUFICIENTE";
                }
            }

            // 4. Creditar na conta de destino
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateSaldoDestino)) {
                pstmt.setBigDecimal(1, request.getValor());
                pstmt.setInt(2, idContaDestino);
                pstmt.executeUpdate();
            }

            // 5. Inserir a operação no histórico
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertHistorico)) {
                pstmt.setInt(1, idContaOrigem);
                pstmt.setInt(2, idContaDestino);
                pstmt.setString(3, request.getChavePixDestino());
                pstmt.setBigDecimal(4, request.getValor());
                pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(6, "CONCLUIDA");
                pstmt.executeUpdate();
            }

            conn.commit(); // Finaliza a transação com sucesso
            return "CONCLUIDA";

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            e.printStackTrace();
            return "ERRO_BANCO";
        } finally {
            if (conn != null) conn.close();
        }
    }
}
