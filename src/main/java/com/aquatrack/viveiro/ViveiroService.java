package com.aquatrack.viveiro;

import com.aquatrack.cicloViveiro.CicloViveiro;

import java.time.LocalDate;
import java.util.List;

public class ViveiroService {

    public CicloViveiro abrirCiclo(Viveiro viveiro, LocalDate dataPovoamento, int quantidadePovoada, String laboratorio) {
        if (viveiro == null) throw new IllegalArgumentException("Viveiro inválido.");
        if (!viveiro.getCiclos().isEmpty()) {
            // garante apenas 1 ciclo ativo por vez
            for (CicloViveiro ciclo : viveiro.getCiclos()) {
                if (ciclo.isAtivo()) {
                    throw new IllegalStateException("Já existe um ciclo ativo neste viveiro.");
                }
            }
        }
        CicloViveiro ciclo = new CicloViveiro(dataPovoamento, quantidadePovoada, laboratorio);
        viveiro.addCiclo(ciclo);
        return ciclo;
    }

    public void encerrarCiclo(Viveiro viveiro, String cicloId) {
        if (viveiro == null) throw new IllegalArgumentException("Viveiro inválido.");
        CicloViveiro ciclo = viveiro.getCiclos().stream()
                .filter(c -> c.getIdCiclo().equals(cicloId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ciclo não encontrado."));
        ciclo.desativar();
    }

    public List<CicloViveiro> listarCiclos(Viveiro viveiro) {
        if (viveiro == null) throw new IllegalArgumentException("Viveiro inválido.");
        return viveiro.getCiclos(); // já ignora deletados
    }
}
