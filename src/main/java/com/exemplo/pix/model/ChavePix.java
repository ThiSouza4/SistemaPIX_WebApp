package com.exemplo.pix.model;

public class ChavePix {
    private int id;
    private int idConta;
    private String tipoChave;
    private String valorChave;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdConta() { return idConta; }
    public void setIdConta(int idConta) { this.idConta = idConta; }
    public String getTipoChave() { return tipoChave; }
    public void setTipoChave(String tipoChave) { this.tipoChave = tipoChave; }
    public String getValorChave() { return valorChave; }
    public void setValorChave(String valorChave) { this.valorChave = valorChave; }
}