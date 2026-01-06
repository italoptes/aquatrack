package com.aquatrack.custo;

import java.time.LocalDate;

public class CustoCiclo {
    private LocalDate dataCusto;
    private String id;
    private boolean isAtivo;
    private String nome;
    private Double valor;

    public CustoCiclo(String nome, double valor, LocalDate data) {
        this.dataCusto = data;
        this.id = gerarId();
        this.isAtivo = true;
        this.nome = nome;
        this.valor = valor;
    }

    private String gerarId() {
        return "C-" + java.util.UUID.randomUUID();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getValor() {
        return valor != null ? valor : 0.0;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isAtivo() {
        return isAtivo;
    }

    public void setAtivo(boolean ativo) {
        isAtivo = ativo;
    }

    public String getDataCusto() {
        return dataCusto.toString();
    }

}
