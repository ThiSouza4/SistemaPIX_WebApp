package com.exemplo.pix.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private final Set<String> allowedPaths = new HashSet<>(Arrays.asList(
            "/login.html", "/cadastro.html", "/auth/login", "/auth/register"
    ));

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        boolean isLoggedIn = (session != null && session.getAttribute("cliente") != null);
        
        // Permite o acesso a recursos públicos (CSS, JS, imagens, etc.)
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/img/")) {
            chain.doFilter(request, response);
            return;
        }

        // Permite o acesso aos caminhos públicos definidos
        if (allowedPaths.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Se o caminho não é público, verifica se o utilizador está logado
        if (isLoggedIn) {
            chain.doFilter(request, response);
        } else {
            // Se não está logado e o caminho é privado, redireciona para o login
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
        }
    }

    // Métodos init e destroy podem ser mantidos vazios se não houver necessidade de inicialização/finalização.
    @Override public void init(FilterConfig filterConfig) throws ServletException {}
    @Override public void destroy() {}
}