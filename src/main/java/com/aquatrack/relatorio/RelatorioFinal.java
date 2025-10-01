package com.aquatrack.relatorio;

import com.aquatrack.cicloViveiro.CicloViveiro;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RelatorioFinal {
    private transient CicloViveiro ciclo;

    private LocalDate dataDaVenda;
    private LocalDate dataPovoamento;
    private double biometriaFinal;
    private double biomassaFinal;
    private double consumoTotalRacao;
    private int quantidadePovoada;
    private Long diasDeCultivo;
    private Double sobrevivenciaCultivo;
    private Double fca;
    private String laboratorio;
    private boolean deletado;

    public RelatorioFinal(CicloViveiro ciclo, double biometriaFinal, double biomassaFinal, LocalDate dataVenda) {
        this.ciclo = ciclo;
        this.dataDaVenda = dataVenda;
        this.dataPovoamento = ciclo.getDataPovoamento();
        this.biometriaFinal = biometriaFinal;
        this.biomassaFinal = biomassaFinal;
        this.consumoTotalRacao = ciclo.consumoTotalRacao();
        this.quantidadePovoada = ciclo.getQuantidadePovoada();
        this.laboratorio = ciclo.getLaboratorio();
        this.deletado = false;

        if (ciclo.getDataPovoamento() != null && dataVenda != null) {
            this.diasDeCultivo = ChronoUnit.DAYS.between(ciclo.getDataPovoamento(), dataVenda);
        }

        if (ciclo.getQuantidadePovoada() > 0 && biometriaFinal > 0) {
            double biometriaFinalKg = biometriaFinal / 1000.0; // g → kg
            this.sobrevivenciaCultivo =
                    (biomassaFinal) / (ciclo.getQuantidadePovoada() * biometriaFinalKg) * 100;
        }

        if (biomassaFinal > 0 && consumoTotalRacao > 0) {
            this.fca = consumoTotalRacao / biomassaFinal;
        }
    }

    // getters e setters...

    public CicloViveiro getCiclo() {
        return ciclo;
    }

    public void setCiclo(CicloViveiro ciclo) {
        this.ciclo = ciclo;
    }

    public LocalDate getDataDaVenda() {
        return dataDaVenda;
    }

    public void setDataDaVenda(LocalDate dataDaVenda) {
        this.dataDaVenda = dataDaVenda;
    }

    public LocalDate getDataPovoamento() {
        return dataPovoamento;
    }

    public void setDataPovoamento(LocalDate dataPovoamento) {
        this.dataPovoamento = dataPovoamento;
    }

    public double getBiometriaFinal() {
        return biometriaFinal;
    }

    public void setBiometriaFinal(double biometriaFinal) {
        this.biometriaFinal = biometriaFinal;
    }

    public double getBiomassaFinal() {
        return biomassaFinal;
    }

    public void setBiomassaFinal(double biomassaFinal) {
        this.biomassaFinal = biomassaFinal;
    }

    public double getConsumoTotalRacao() {
        return consumoTotalRacao;
    }

    public void setConsumoTotalRacao(double consumoTotalRacao) {
        this.consumoTotalRacao = consumoTotalRacao;
    }

    public int getQuantidadePovoada() {
        return quantidadePovoada;
    }

    public void setQuantidadePovoada(int quantidadePovoada) {
        this.quantidadePovoada = quantidadePovoada;
    }

    public Long getDiasDeCultivo() {
        return diasDeCultivo;
    }

    public void setDiasDeCultivo(Long diasDeCultivo) {
        this.diasDeCultivo = diasDeCultivo;
    }

    public Double getSobrevivenciaCultivo() {
        return sobrevivenciaCultivo;
    }

    public void setSobrevivenciaCultivo(Double sobrevivenciaCultivo) {
        this.sobrevivenciaCultivo = sobrevivenciaCultivo;
    }

    public Double getFca() {
        return fca;
    }

    public void setFca(Double fca) {
        this.fca = fca;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public boolean isDeletado() {
        return deletado;
    }

    public void setDeletado(boolean deletado) {
        this.deletado = deletado;
    }
}

