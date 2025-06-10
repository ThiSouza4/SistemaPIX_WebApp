package com.exemplo.pix.model;

// Este é o código corrigido e padronizado para a classe ChavePix.
// As variáveis e métodos agora usam o padrão camelCase.
public class ChavePix {
    private int idChave;
    private int idConta;
    private String tipoChave;
    private String chave;

    public ChavePix() {
    }

    public int getIdChave() {
        return idChave;
    }

    public void setIdChave(int idChave) {
        this.idChave = idChave;
    }

    public int getIdConta() {
        return idConta;
    }

    public void setIdConta(int idConta) {
        this.idConta = idConta;
    }

    public String getTipoChave() {
        return tipoChave;
    }

    public void setTipoChave(String tipoChave) {
        this.tipoChave = tipoChave;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }
}