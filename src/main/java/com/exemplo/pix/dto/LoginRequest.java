package com.exemplo.pix.dto;

// Esta classe é um "molde" para os dados que vêm do formulário de login (JSON).
// O Jackson usa isso para converter o JSON em um objeto Java automaticamente.
public class LoginRequest {
    private String email;
    private String senha;

    // Getters e Setters são necessários para o Jackson funcionar

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
