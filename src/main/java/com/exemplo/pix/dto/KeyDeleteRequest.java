package com.exemplo.pix.dto;

// Molde para receber o ID da chave a ser deletada
public class KeyDeleteRequest {
    private int id_chave;

    public int getId_chave() {
        return id_chave;
    }

    public void setId_chave(int id_chave) {
        this.id_chave = id_chave;
    }
}
