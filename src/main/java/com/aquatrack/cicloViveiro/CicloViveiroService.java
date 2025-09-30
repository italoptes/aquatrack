package com.aquatrack.cicloViveiro;

import com.aquatrack.biometria.Biometria;
import com.aquatrack.qualidadeDeAgua.QualidadeDeAgua;
import com.aquatrack.racao.TipoRacao;
import com.aquatrack.relatorio.RelatorioFinal;

import java.time.LocalDate;
import java.util.List;

public class CicloViveiroService {

    // ===== Biometria =====
    public void registrarBiometria(CicloViveiro ciclo, Biometria biometria) {
        if (ciclo == null || !ciclo.isAtivo()) throw new IllegalStateException("Ciclo inativo.");
        if (biometria == null || !biometria.isValida()) {
            throw new IllegalArgumentException("Biometria inválida.");
        }
        ciclo.addBiometria(biometria);
    }

    public Biometria getUltimaBiometria(CicloViveiro ciclo) {
        return ciclo.getUltimaBiometria();
    }

    public Biometria getPenultimaBiometria(CicloViveiro ciclo) {
        return ciclo.getPenultimaBiometria();
    }

    public List<Biometria> getHistoricoBiometria(CicloViveiro ciclo) {
        return ciclo.getHistoricoBiometria();
    }

    // ===== Qualidade de Água =====
    public void registrarQualidadeAgua(CicloViveiro ciclo, QualidadeDeAgua qualidade) {
        if (ciclo == null || !ciclo.isAtivo()) throw new IllegalStateException("Ciclo inativo.");
        if (qualidade == null || !qualidade.valoresValidos()) {
            throw new IllegalArgumentException("Medição de qualidade inválida.");
        }
        ciclo.addQualidadeDeAgua(qualidade);
    }

    public QualidadeDeAgua getUltimaQualidade(CicloViveiro ciclo) {
        return ciclo.getUltimaQualidade();
    }

    public QualidadeDeAgua getPenultimaQualidade(CicloViveiro ciclo) {
        return ciclo.getPenultimaQualidade();
    }

    public List<QualidadeDeAgua> getHistoricoQualidade(CicloViveiro ciclo) {
        return ciclo.getHistoricoQualidade();
    }

    // ===== Consumo de Ração =====
    public void registrarConsumoRacao(CicloViveiro ciclo, TipoRacao tipo, double quantidade) {
        if (ciclo == null || !ciclo.isAtivo()) throw new IllegalStateException("Ciclo inativo.");
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva.");
        ciclo.addConsumoRacao(tipo, quantidade);
    }

    public double getConsumoTotal(CicloViveiro ciclo) {
        return ciclo.consumoTotalRacao();
    }

    // ===== Relatório Final =====
    public RelatorioFinal gerarRelatorioFinal(CicloViveiro ciclo, double biometriaFinal, double biomassaFinal, LocalDate dataVenda) {
        if (ciclo == null || ciclo.isDeletado()) throw new IllegalArgumentException("Ciclo inválido.");
        ciclo.gerarRelatorioFinal(biometriaFinal, biomassaFinal, dataVenda);
        return ciclo.getRelatorioFinal();
    }
}