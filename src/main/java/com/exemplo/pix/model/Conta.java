package com.exemplo.pix.model;

import java.math.BigDecimal;

public class Conta {
    private int idConta;
    private int idCliente;
    private String numeroConta;
    private BigDecimal saldo;

    // Getters
    public int getIdConta() { return idConta; }
    public int getIdCliente() { return idCliente; }
    public String getNumeroConta() { return numeroConta; }
    public BigDecimal getSaldo() { return saldo; }

    // Setters
    public void setIdConta(int idConta) { this.idConta = idConta; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
}