package com.exemplo.pix.dto;

// Molde para receber os dados da chave a ser atualizada
public class KeyUpdateRequest {
    private int id_chave;
    private String chave;

    public int getId_chave() {
        return id_chave;
    }

    public void setId_chave(int id_chave) {
        this.id_chave = id_chave;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }
}
