package com.exemplo.pix.dto;

// Molde para os dados que vêm do formulário de cadastro de chave.
public class KeyRegistrationRequest {
    private String tipo_chave;
    private String chave;

    public String getTipo_chave() {
        return tipo_chave;
    }

    public void setTipo_chave(String tipo_chave) {
        this.tipo_chave = tipo_chave;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }
}
