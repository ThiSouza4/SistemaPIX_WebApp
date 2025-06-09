package com.exemplo.pix.dto;

import java.math.BigDecimal;

// Data Transfer Object (DTO) para receber os dados de uma requisição de transferência PIX.
public class PixTransferRequest {
    private int idContaOrigem;    // ID da conta do cliente que está realizando a transferência.
    private String chavePixDestino; // A chave PIX para a qual o valor será enviado.
    private BigDecimal valor;       // O montante a ser transferido.
    private boolean confirmado;     // Flag para indicar se o usuário confirmou uma operação para uma chave fora da coleção.

    // Construtor padrão é necessário para a desserialização pelo Jackson (JSON para Objeto Java)
    public PixTransferRequest() {
    }

    // Getters
    public int getIdContaOrigem() {
        return idContaOrigem;
    }

    public String getChavePixDestino() {
        return chavePixDestino;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public boolean isConfirmado() { // Jackson usa 'isConfirmado' para boolean
        return confirmado;
    }

    // Setters
    public void setIdContaOrigem(int idContaOrigem) {
        this.idContaOrigem = idContaOrigem;
    }

    public void setChavePixDestino(String chavePixDestino) {
        this.chavePixDestino = chavePixDestino;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }
}
