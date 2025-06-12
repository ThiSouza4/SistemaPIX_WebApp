package com.exemplo.pix.servlet;

import com.exemplo.pix.dao.ChavePixDAO;
import com.exemplo.pix.dao.ClienteDAO;
import com.exemplo.pix.dao.ContaDAO;
import com.exemplo.pix.dto.ApiResponse;
import com.exemplo.pix.model.ChavePix;
import com.exemplo.pix.model.Cliente;
import com.exemplo.pix.model.Conta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@WebServlet("/api/data/*")
public class DataServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ContaDAO contaDAO = new ContaDAO();
    private final ChavePixDAO chavePixDAO = new ChavePixDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("cliente") == null) {
            writeErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Usuário não autenticado.");
            return;
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo() == null ? "/" : req.getPathInfo();
        switch (pathInfo) {
            case "/dashboard": handleDashboardData(req, resp); break;
            case "/chaves": handleListarChaves(req, resp); break;
            case "/user-info": handleUserInfo(req, resp); break;
            default: writeErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado."); break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/chaves".equals(pathInfo)) {
            handleAddChave(req, resp);
        } else {
            writeErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado.");
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/chaves".equals(pathInfo)) {
            handleUpdateChave(req, resp);
        } else {
            writeErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/chaves".equals(pathInfo)) {
            handleDeleteChave(req, resp);
        } else {
            writeErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado.");
        }
    }
    
    // --- Handlers ---

    private void handleAddChave(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpSession session = req.getSession(false);
            Cliente cliente = (Cliente) session.getAttribute("cliente");
            Conta conta = contaDAO.buscarPorClienteId(cliente.getId());
            ChavePix novaChave = objectMapper.readValue(req.getReader(), ChavePix.class);
            
            // --- LÓGICA DE VALIDAÇÃO AVANÇADA (RESGATADA) ---
            if (chavePixDAO.verificarSeExisteChavePorTipo(conta.getId(), novaChave.getTipoChave())) {
                writeErrorResponse(resp, HttpServletResponse.SC_CONFLICT, "Você já possui uma chave deste tipo.");
                return;
            }

            if ("ALEATORIA".equals(novaChave.getTipoChave())) {
                novaChave.setValorChave(UUID.randomUUID().toString());
            } else {
                if (chavePixDAO.isChaveEmUso(novaChave.getValorChave())) { // Assumindo que ChavePixDAO terá isChaveEmUso
                    writeErrorResponse(resp, HttpServletResponse.SC_CONFLICT, "Esta chave já está em uso.");
                    return;
                }
            }
            
            novaChave.setIdConta(conta.getId());
            chavePixDAO.inserir(novaChave);
            writeSuccessResponse(resp, HttpServletResponse.SC_CREATED, "Chave adicionada com sucesso!", novaChave);

        } catch (SQLException e) {
            writeErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro no banco de dados.");
        } catch (Exception e) {
            writeErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao processar requisição.");
        }
    }

    private void handleUpdateChave(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // ... (Implementação futura da atualização, similar à de adicionar, com validações) ...
        writeSuccessResponse(resp, HttpServletResponse.SC_OK, "Funcionalidade de update a ser implementada.", null);
    }
    
    private void handleDeleteChave(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int idChave = Integer.parseInt(req.getParameter("id"));
            chavePixDAO.remover(idChave);
            writeSuccessResponse(resp, HttpServletResponse.SC_OK, "Chave removida com sucesso.", null);
        } catch (Exception e) {
            writeErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao remover chave.");
        }
    }

    private void handleListarChaves(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Cliente cliente = (Cliente) req.getSession().getAttribute("cliente");
            Conta conta = contaDAO.buscarPorClienteId(cliente.getId());
            List<ChavePix> chaves = chavePixDAO.listarPorContaId(conta.getId());
            writeSuccessResponse(resp, HttpServletResponse.SC_OK, "Chaves listadas com sucesso.", chaves);
        } catch (Exception e) {
            writeErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao listar chaves.");
        }
    }

    private void handleDashboardData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // ... (código similar ao de listar chaves, buscando dados da conta e cliente) ...
    }
    
    private void handleUserInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // ... (código para retornar dados públicos do cliente logado) ...
    }

    // --- Helpers de Resposta ---
    private void writeErrorResponse(HttpServletResponse resp, int status, String message) throws IOException { /* ... */ }
    private void writeSuccessResponse(HttpServletResponse resp, int status, String message, Object data) throws IOException { /* ... */ }
}