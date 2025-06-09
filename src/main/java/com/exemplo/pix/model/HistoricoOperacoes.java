package com.exemplo.pix.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class HistoricoOperacoes {
    private int idOperacao;
    private int idContaOrigem;
    private Integer idContaDestino;
    private String tipoOperacao;
    private BigDecimal valor;
    private Timestamp dataOperacao;

    // Getters
    public int getIdOperacao() { return idOperacao; }
    public int getIdContaOrigem() { return idContaOrigem; }
    public Integer getIdContaDestino() { return idContaDestino; }
    public String getTipoOperacao() { return tipoOperacao; }
    public BigDecimal getValor() { return valor; }
    public Timestamp getDataOperacao() { return dataOperacao; }

    // Setters
    public void setIdOperacao(int idOperacao) { this.idOperacao = idOperacao; }
    public void setIdContaOrigem(int idContaOrigem) { this.idContaOrigem = idContaOrigem; }
    public void setIdContaDestino(Integer idContaDestino) { this.idContaDestino = idContaDestino; }
    public void setTipoOperacao(String tipoOperacao) { this.tipoOperacao = tipoOperacao; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public void setDataOperacao(Timestamp dataOperacao) { this.dataOperacao = dataOperacao; }
}