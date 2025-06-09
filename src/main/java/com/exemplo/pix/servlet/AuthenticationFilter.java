package com.exemplo.pix.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

// Este filtro intercepta TODAS as requisições para a aplicação
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    // Uma lista de "caminhos públicos" que não precisam de login
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/login.html", 
        "/cadastro.html", 
        "/index.html",
        "/api/auth/login", 
        "/api/auth/register",
        "/css/", 
        "/js/"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // Pega a sessão ATUAL, mas não cria uma nova se não existir
        HttpSession session = httpRequest.getSession(false); 

        // Pega o caminho da requisição (ex: /login.html, /api/data/dashboard)
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Verifica se o caminho é público
        boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        
        // Verifica se o usuário está logado (se a sessão existe E tem o atributo 'idCliente')
        boolean isLoggedIn = (session != null && session.getAttribute("idCliente") != null);

        // REGRA DE ACESSO:
        // Se o caminho for público, OU se o usuário estiver logado, permita o acesso.
        if (isPublicPath || isLoggedIn) {
            chain.doFilter(request, response); 
        } else {
            // Se o caminho for protegido e o usuário NÃO estiver logado, redirecione para o login.
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
        }
    }
}
