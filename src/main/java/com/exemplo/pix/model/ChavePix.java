package com.exemplo.pix.model;

public class ChavePix {
    private int id_chave;
    private int id_conta;
    private String tipo_chave;
    private String chave;

    public ChavePix() {
    }

    // Getters
    public int getId_chave() {
        return id_chave;
    }

    public int getId_conta() {
        return id_conta;
    }

    public String getTipo_chave() {
        return tipo_chave;
    }

    public String getChave() {
        return chave;
    }

    // Setters
    public void setId_chave(int id_chave) {
        this.id_chave = id_chave;
    }

    public void setId_conta(int id_conta) {
        this.id_conta = id_conta;
    }

    public void setTipo_chave(String tipo_chave) {
        this.tipo_chave = tipo_chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }
}