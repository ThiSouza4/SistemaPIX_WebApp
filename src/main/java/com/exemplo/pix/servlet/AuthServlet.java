package com.exemplo.pix.servlet;

import com.exemplo.pix.dao.ClienteDAO;
import com.exemplo.pix.dao.ContaDAO;
import com.exemplo.pix.dto.ApiResponse;
import com.exemplo.pix.dto.LoginRequest;
import com.exemplo.pix.dto.RegisterRequest;
import com.exemplo.pix.model.Cliente;
import com.exemplo.pix.model.Conta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ContaDAO contaDAO = new ContaDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            writeErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Endpoint não especificado.");
            return;
        }

        try {
            switch (pathInfo) {
                case "/login":
                    handleLogin(req, resp);
                    break;
                case "/register":
                    handleRegister(req, resp);
                    break;
                case "/logout":
                    handleLogout(req, resp);
                    break;
                default:
                    writeErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado.");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ocorreu um erro interno no servidor.");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequest loginRequest = objectMapper.readValue(req.getReader(), LoginRequest.class);
        Cliente cliente = clienteDAO.buscarPorEmail(loginRequest.getEmail());

        // Lógica de verificação de senha segura com BCrypt
        if (cliente != null && BCrypt.checkpw(loginRequest.getSenha(), cliente.getSenhaHash())) {
            HttpSession session = req.getSession(true);
            session.setAttribute("cliente", cliente);
            session.setMaxInactiveInterval(30 * 60); // 30 minutos de inatividade
            writeSuccessResponse(resp, HttpServletResponse.SC_OK, "Login bem-sucedido.", null);
        } else {
            writeErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        RegisterRequest registerRequest = objectMapper.readValue(req.getReader(), RegisterRequest.class);
        
        // Validação prévia (mais robusta que esperar o erro do banco)
        if (clienteDAO.buscarPorEmail(registerRequest.getEmail()) != null) {
             writeErrorResponse(resp, HttpServletResponse.SC_CONFLICT, "Este e-mail já está cadastrado.");
             return;
        }
        if (clienteDAO.buscarPorCpf(registerRequest.getCpf()) != null) { // Assumindo que ClienteDAO terá buscarPorCpf
            writeErrorResponse(resp, HttpServletResponse.SC_CONFLICT, "Este CPF já está cadastrado.");
            return;
        }

        Cliente novoCliente = new Cliente();
        novoCliente.setNome(registerRequest.getNomeCompleto());
        novoCliente.setCpf(registerRequest.getCpf());
        novoCliente.setTelefone(registerRequest.getTelefone());
        novoCliente.setEmail(registerRequest.getEmail());
        // Criptografando a senha antes de salvar
        String senhaComHash = BCrypt.hashpw(registerRequest.getSenha(), BCrypt.gensalt());
        novoCliente.setSenhaHash(senhaComHash);

        Cliente clienteSalvo = clienteDAO.inserir(novoCliente);

        Conta novaConta = new Conta();
        novaConta.setIdCliente(clienteSalvo.getId());
        String numeroConta = String.valueOf(100000 + ThreadLocalRandom.current().nextInt(900000));
        novaConta.setNumeroConta(numeroConta);
        novaConta.setSaldo(new BigDecimal("0.00"));

        contaDAO.inserir(novaConta);

        writeSuccessResponse(resp, HttpServletResponse.SC_CREATED, "Registro realizado com sucesso!", null);
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        writeSuccessResponse(resp, HttpServletResponse.SC_OK, "Logout bem-sucedido.", null);
    }

    // Métodos auxiliares para padronizar respostas
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