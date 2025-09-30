package com.aquatrack.viveiro;

import com.aquatrack.racao.Racao;
import com.aquatrack.racao.TipoRacao;

public class ConsumoRacaoViveiro {
    private Racao consumoRacaoEngorda;
    private Racao consumoRacaoCrescimento;

    public ConsumoRacaoViveiro() {
        this.consumoRacaoEngorda = new Racao(TipoRacao.ENGORDA);
        this.consumoRacaoCrescimento = new Racao(TipoRacao.CRESCIMENTO);
    }

    public void addConsumoEngorda(double quantidade) {
        consumoRacaoEngorda.adicionaQuantidade(quantidade);
    }

    public void addConsumoCrescimento(double quantidade) {
        consumoRacaoCrescimento.adicionaQuantidade(quantidade);
    }

    public double consumoRacaoEngorda(){
        return consumoRacaoEngorda.getQuantidadeKg();
    }

    public double consumoRacaoCrescimento(){
        return consumoRacaoCrescimento.getQuantidadeKg();
    }

    public void zerar() {
        this.consumoRacaoEngorda = new Racao(TipoRacao.ENGORDA);
        this.consumoRacaoCrescimento = new Racao(TipoRacao.CRESCIMENTO);
    }
}
