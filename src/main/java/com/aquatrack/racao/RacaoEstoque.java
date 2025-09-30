package com.aquatrack.racao;

import java.util.Collection;
import java.util.EnumMap;

public class RacaoEstoque {
    private final EnumMap<TipoRacao, Racao> racoes = new EnumMap<>(TipoRacao.class);

    public RacaoEstoque() {
        racoes.put(TipoRacao.ENGORDA, new Racao(TipoRacao.ENGORDA));
        racoes.put(TipoRacao.CRESCIMENTO, new Racao(TipoRacao.CRESCIMENTO));
    }

    public void adicionar(TipoRacao tipo, double quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        if (!racoes.containsKey(tipo)) throw new IllegalArgumentException("Não há ração tipo " + tipo + " na fazenda");
        racoes.get(tipo).adicionaQuantidade(quantidade);
    }

    public void consumir(TipoRacao tipo, double quantidade) {
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        if (!racoes.containsKey(tipo)) throw new IllegalArgumentException("Não há ração tipo " + tipo + " na fazenda");

        double disponivel = racoes.get(tipo).getQuantidadeKg();
        if (quantidade > disponivel) {
            throw new IllegalArgumentException(
                    "Quantidade solicitada (" + quantidade + " Kg) é maior do que o estoque disponível (" + disponivel + " Kg)."
            );
        }

        racoes.get(tipo).removeQuantidade(quantidade);
    }


    public double getQuantidadeRacaoPorTipo(TipoRacao tipo) {
        return racoes.get(tipo).getQuantidadeKg();
    }

    public Racao get(TipoRacao tipo) { return racoes.get(tipo); }
    public Collection<Racao> listar() { return racoes.values(); }
}
