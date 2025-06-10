package com.exemplo.pix.servlet;

import com.exemplo.pix.dao.ChavePixDAO;
import com.exemplo.pix.dao.ClienteDAO;
import com.exemplo.pix.dao.ContaDAO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/data/*")
public class DataServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DataServlet.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChavePixDAO chavePixDAO = new ChavePixDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ContaDAO contaDAO = new ContaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("idCliente") == null) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário não autenticado.");
                return;
            }
            Integer idCliente = (Integer) session.getAttribute("idCliente");
            String path = req.getPathInfo();
            if (path == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Recurso não especificado.");
                return;
            }
            switch (path) {
                case "/dashboard":
                    handleDashboard(idCliente, resp);
                    break;
                case "/keys":
                    handleKeys(idCliente, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Recurso não encontrado.");
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro fatal em doGet no DataServlet", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ocorreu um erro interno.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handlePostKey(req, resp);
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handlePutKey(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleDeleteKey(req, resp);
    }

    private void handleDashboard(Integer idCliente, HttpServletResponse resp) throws IOException {
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        Conta conta = contaDAO.buscarPorIdCliente(idCliente);
        if (cliente != null && conta != null) {
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("nomeCliente", cliente.getNome());
            dashboardData.put("saldo", conta.getSaldo());
            objectMapper.writeValue(resp.getWriter(), dashboardData);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Dados do cliente ou conta não encontrados.");
        }
    }

    private void handleKeys(Integer idCliente, HttpServletResponse resp) throws IOException {
        List<ChavePix> chaves = chavePixDAO.listarChavesPorCliente(idCliente);
        objectMapper.writeValue(resp.getWriter(), chaves);
    }
    
    private void handlePostKey(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();
        try {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("idCliente") == null) {
                throw new SecurityException("Sessão inválida.");
            }
            Integer idCliente = (Integer) session.getAttribute("idCliente");
            ChavePix novaChave = objectMapper.readValue(req.getReader(), ChavePix.class);
            String tipoChave = novaChave.getTipoChave();
            String valorChave = novaChave.getChave();

            // --- LÓGICA DE VERIFICAÇÃO ATUALIZADA ---
            if ("ALEATORIA".equals(tipoChave)) {
                if (chavePixDAO.verificarSeExisteChavePorTipo(idCliente, "ALEATORIA")) {
                    throw new SQLException("Duplicate key type for user");
                }
                valorChave = UUID.randomUUID().toString();
                novaChave.setChave(valorChave);
            } else {
                // 1. Verifica se a chave já existe na tabela chavespix
                boolean chavePixEmUso = chavePixDAO.isChaveEmUso(valorChave);
                
                // 2. Verifica se o dado já existe na tabela clientes
                // A conversão para minúsculas é para corresponder ao nome da coluna no banco ("cpf", "email", etc.)
                boolean dadoClienteEmUso = clienteDAO.isDadoUnicoEmUso(tipoChave.toLowerCase(), valorChave);

                if (chavePixEmUso || dadoClienteEmUso) {
                    throw new SQLException("Duplicate entry");
                }

                // 3. Verifica se o usuário já tem uma chave daquele tipo
                if (chavePixDAO.verificarSeExisteChavePorTipo(idCliente, tipoChave)) {
                    throw new SQLException("Duplicate key type for user");
                }
            }
            
            chavePixDAO.adicionarChave(idCliente, novaChave);
            responseMap.put("message", "Chave cadastrada com sucesso!");
            resp.setStatus(HttpServletResponse.SC_CREATED);

        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Duplicate key type for user")) {
                responseMap.put("message", "Você já possui uma chave deste tipo.");
            } else if (msg != null && msg.contains("Duplicate entry")) {
                responseMap.put("message", "Esta chave já está em uso por outro usuário. Tente outra.");
            } else {
                responseMap.put("message", "Erro interno ao cadastrar a chave.");
            }
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            responseMap.put("message", "Ocorreu um erro inesperado.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        objectMapper.writeValue(resp.getWriter(), responseMap);
    }
    
    private void handlePutKey(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();
        try {
            ChavePix chaveParaAtualizar = objectMapper.readValue(req.getReader(), ChavePix.class);
            String novoValor = chaveParaAtualizar.getChave();
            String tipoChave = chaveParaAtualizar.getTipoChave(); // Precisamos do tipo para a verificação

            // --- LÓGICA DE VERIFICAÇÃO ATUALIZADA PARA O UPDATE ---
            boolean chavePixEmUso = chavePixDAO.isChaveEmUso(novoValor);
            boolean dadoClienteEmUso = false;
            if (tipoChave != null && !tipoChave.equals("ALEATORIA")) {
                dadoClienteEmUso = clienteDAO.isDadoUnicoEmUso(tipoChave.toLowerCase(), novoValor);
            }

            if (chavePixEmUso || dadoClienteEmUso) {
                throw new SQLException("Duplicate entry");
            }
            
            chavePixDAO.atualizarChave(chaveParaAtualizar.getIdChave(), novoValor);
            responseMap.put("message", "Chave alterada com sucesso!");

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                responseMap.put("message", "Esta chave já está em uso. Tente outra.");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                 responseMap.put("message", "Erro ao atualizar a chave.");
                 resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            responseMap.put("message", "Erro inesperado ao atualizar a chave.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        objectMapper.writeValue(resp.getWriter(), responseMap);
    }

    private void handleDeleteKey(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();
        try {
            String idChaveStr = req.getParameter("id");
            if (idChaveStr == null) throw new IllegalArgumentException("ID da chave não fornecido.");
            
            int idChave = Integer.parseInt(idChaveStr);
            chavePixDAO.removerChave(idChave);
            responseMap.put("message", "Chave excluída com sucesso!");
        } catch (Exception e) {
            responseMap.put("message", "Erro ao excluir a chave.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        objectMapper.writeValue(resp.getWriter(), responseMap);
    }
}