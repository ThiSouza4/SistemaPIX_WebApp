package com.exemplo.pix.servlet;

import com.exemplo.pix.dao.ClienteDAO;
import com.exemplo.pix.dao.ContaDAO;
import com.exemplo.pix.dto.LoginRequest;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ContaDAO contaDAO = new ContaDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/login".equals(pathInfo)) {
            handleLogin(req, resp);
        } else if ("/register".equals(pathInfo)) {
            handleRegister(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/logout".equals(pathInfo)) {
            handleLogout(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        LoginRequest loginRequest = objectMapper.readValue(req.getReader(), LoginRequest.class);
        
        Cliente cliente = clienteDAO.buscarPorEmail(loginRequest.getEmail());
        Map<String, Object> responseMap = new HashMap<>();

        if (cliente != null && cliente.getSenhaHash().equals(loginRequest.getSenha())) {
            HttpSession session = req.getSession(true);
            session.setAttribute("idCliente", cliente.getIdCliente());
            responseMap.put("success", true);
            responseMap.put("message", "Login bem-sucedido!");
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMap.put("success", false);
            responseMap.put("message", "E-mail ou senha inválidos.");
        }
        objectMapper.writeValue(resp.getWriter(), responseMap);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Cliente novoCliente = objectMapper.readValue(req.getReader(), Cliente.class);
        Map<String, Object> responseMap = new HashMap<>();

        try {
            int idNovoCliente = clienteDAO.inserir(novoCliente);

            Conta novaConta = new Conta();
            novaConta.setIdCliente(idNovoCliente);
            novaConta.setNumeroConta(generateAccountNumber());
            novaConta.setSaldo(new BigDecimal("0.00"));
            contaDAO.inserir(novaConta);
            
            responseMap.put("success", true);
            responseMap.put("message", "Cadastro realizado com sucesso!");
            resp.setStatus(HttpServletResponse.SC_CREATED);

        } catch (SQLException e) {
            // --- ATUALIZAÇÃO PARA VALIDAR TELEFONE ---
            String errorMessage = e.getMessage().toLowerCase();
            
            if (errorMessage.contains("duplicate entry") && errorMessage.contains("'clientes.cpf'")) {
                responseMap.put("message", "Este CPF já está cadastrado.");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else if (errorMessage.contains("duplicate entry") && errorMessage.contains("'clientes.email'")) {
                responseMap.put("message", "Este e-mail já está cadastrado.");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else if (errorMessage.contains("duplicate entry") && errorMessage.contains("'clientes.uc_telefone'")) {
                // Captura o erro para o telefone usando o nome da constraint que criamos
                responseMap.put("message", "Este telefone já está cadastrado.");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                responseMap.put("message", "Erro interno no servidor. Tente novamente.");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            // --- FIM DA ATUALIZAÇÃO ---
            e.printStackTrace();
        } catch (Exception e) {
            responseMap.put("message", "Ocorreu um erro inesperado.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
        objectMapper.writeValue(resp.getWriter(), responseMap);
    }
    
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", "Logout realizado com sucesso.");
        objectMapper.writeValue(resp.getWriter(), responseMap);
    }

    private String generateAccountNumber() {
        Random rnd = new Random();
        int number = 100000 + rnd.nextInt(900000);
        return String.valueOf(number);
    }
}
