package com.exemplo.pix.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HistoricoOperacoes {
    private int id;
    private int idContaOrigem;
    private Integer idContaDestino; // <-- Resgatado da versão antiga para permitir Saques
    private String tipoOperacao;
    private BigDecimal valor;
    private LocalDateTime dataOperacao; // <-- Modernizado

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdContaOrigem() { return idContaOrigem; }
    public void setIdContaOrigem(int idContaOrigem) { this.idContaOrigem = idContaOrigem; }
    public Integer getIdContaDestino() { return idContaDestino; }
    public void setIdContaDestino(Integer idContaDestino) { this.idContaDestino = idContaDestino; }
    public String getTipoOperacao() { return tipoOperacao; }
    public void setTipoOperacao(String tipoOperacao) { this.tipoOperacao = tipoOperacao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDateTime getDataOperacao() { return dataOperacao; }
    public void setDataOperacao(LocalDateTime dataOperacao) { this.dataOperacao = dataOperacao; }
}