package com.aquatrack.racao;

import java.util.Objects;

public class Racao {
    private double quantidadeKg;
    private TipoRacao tipo;

    public Racao(TipoRacao tipo) {
        this.quantidadeKg = 0;
        this.tipo = tipo;
    }

    public Racao(double quantidadeKg, TipoRacao tipo) {
        this.quantidadeKg = quantidadeKg;
        this.tipo = tipo;
    }

    public double getQuantidadeKg() {
        return quantidadeKg;
    }

    public void adicionaQuantidade(double quantidadeKg) {
        this.quantidadeKg += quantidadeKg;
    }

    public void removeQuantidade(double quantidadeKg) {
        if (quantidadeKg < 0 || quantidadeKg > this.quantidadeKg)
            throw new IllegalArgumentException("Quantidade inválida para remoção");
        this.quantidadeKg -= quantidadeKg;
    }

    public TipoRacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoRacao tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Racao racao = (Racao) o;
        return tipo == racao.tipo;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tipo);
    }
}
