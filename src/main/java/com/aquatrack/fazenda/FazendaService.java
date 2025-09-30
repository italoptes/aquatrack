package com.aquatrack.fazenda;

import com.aquatrack.viveiro.Viveiro;
import com.aquatrack.racao.TipoRacao;

import java.util.List;

public class FazendaService {

    // ===== Viveiros =====
    public void adicionarViveiro(Fazenda fazenda, Viveiro viveiro) { //Viveiro é criado no Controller e passado para o service
        if (fazenda == null || viveiro == null) {
            throw new IllegalArgumentException("Fazenda ou Viveiro inválidos.");
        }
        fazenda.addViveiro(viveiro);
    }

    public void removerViveiro(Fazenda fazenda, String viveiroId) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        Viveiro v = fazenda.getViveiros().get(viveiroId);
        if (v != null) v.deleta();
    }

    public List<Viveiro> listarViveiros(Fazenda fazenda) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        return fazenda.listarViveiros();
    }

    // ===== Estoque de Ração =====
    public void adicionarRacao(Fazenda fazenda, TipoRacao tipo, double quantidadeKg) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        fazenda.adicionaQuantidade(tipo, quantidadeKg);
    }

    public double consultarEstoquePorTipo(Fazenda fazenda, TipoRacao tipo) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        return fazenda.listarQuantidadePorTipo(tipo);
    }

    public boolean isFazendaDeletada(Fazenda fazenda) {
        return fazenda != null && fazenda.isDeletado();
    }
}
