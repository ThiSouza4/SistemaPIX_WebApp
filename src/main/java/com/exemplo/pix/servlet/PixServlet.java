package com.exemplo.pix.servlet;

import com.exemplo.pix.dao.ContaDAO;
import com.exemplo.pix.dao.HistoricoOperacoesDAO;
import com.exemplo.pix.dto.ApiResponse;
import com.exemplo.pix.dto.PixTransferRequest;
import com.exemplo.pix.model.Cliente;
import com.exemplo.pix.model.Conta;
import com.exemplo.pix.model.HistoricoOperacoes;
import com.exemplo.pix.util.DatabaseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

@WebServlet("/api/pix/*")
public class PixServlet extends HttpServlet {
    private final ObjectMapper objectMapper;
    private final ContaDAO contaDAO = new ContaDAO();
    private final HistoricoOperacoesDAO historicoDAO = new HistoricoOperacoesDAO();
    private final GeminiFraudAnalysisService geminiService = new GeminiFraudAnalysisService(); // <-- O DETETIVE AI

    public PixServlet() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/transfer".equals(pathInfo)) {
            handlePixTransfer(req, resp);
        } else {
            writeErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado.");
        }
    }

    private void handlePixTransfer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("cliente") == null) {
            writeErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Usuário não autenticado.");
            return;
        }

        Connection conn = null;
        try {
            PixTransferRequest transferRequest = objectMapper.readValue(req.getReader(), PixTransferRequest.class);
            Cliente clienteLogado = (Cliente) session.getAttribute("cliente");

            if (transferRequest.getValor() == null || transferRequest.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                writeErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "O valor da transferência é inválido.");
                return;
            }

            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // Inicia a transação

            Conta contaOrigem = contaDAO.buscarPorClienteId(clienteLogado.getId());
            Conta contaDestino = contaDAO.buscarPorChavePix(transferRequest.getChavePixDestino());

            if (contaDestino == null) {
                writeErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Chave PIX de destino não encontrada.");
                conn.rollback(); return;
            }

            if (contaOrigem.getId() == contaDestino.getId()) {
                writeErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Não é possível transferir para si mesmo.");
                conn.rollback(); return;
            }

            if (contaOrigem.getSaldo().compareTo(transferRequest.getValor()) < 0) {
                writeErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Saldo insuficiente.");
                conn.rollback(); return;
            }

            // --- INTEGRAÇÃO COM A IA DE ANÁLISE DE FRAUDE ---
            String analiseIA = geminiService.analisarTransacao(
                contaOrigem.getId(), 
                transferRequest.getChavePixDestino(), 
                transferRequest.getValor(), 
                "Resumo do histórico do cliente (placeholder)" // No futuro, podemos passar dados reais aqui
            );

            System.out.println("Análise de Fraude Gemini: " + analiseIA); // Log para depuração

            if(analiseIA.contains("ALTO RISCO")) {
                writeErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, "Transação bloqueada por suspeita de fraude. " + analiseIA);
                conn.rollback(); return;
            }
            // --- FIM DA INTEGRAÇÃO ---

            // Subtrai da origem
            contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(transferRequest.getValor()));
            contaDAO.atualizarSaldo(conn, contaOrigem);

            // Adiciona ao destino
            contaDestino.setSaldo(contaDestino.getSaldo().add(transferRequest.getValor()));
            contaDAO.atualizarSaldo(conn, contaDestino);

            // Grava no histórico
            HistoricoOperacoes historico = new HistoricoOperacoes();
            historico.setIdContaOrigem(contaOrigem.getId());
            historico.setIdContaDestino(contaDestino.getId());
            historico.setTipoOperacao("PIX_ENVIADO");
            historico.setValor(transferRequest.getValor());
            historico.setDataOperacao(LocalDateTime.now());
            historicoDAO.inserir(conn, historico);

            conn.commit(); // Confirma a transação
            writeSuccessResponse(resp, HttpServletResponse.SC_OK, "Transferência realizada com sucesso!", null);

        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            e.printStackTrace();
            writeErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro interno durante a transferência.");
        } finally {
            if (conn != null) { try { conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    private void writeErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), new ApiResponse(false, message, null));
    }
    
    private void writeSuccessResponse(HttpServletResponse resp, int status, String message, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), new ApiResponse(true, message, data));
    }
}