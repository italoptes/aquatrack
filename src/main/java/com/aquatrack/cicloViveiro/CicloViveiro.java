package com.aquatrack.cicloViveiro;

import com.aquatrack.biometria.Biometria;
import com.aquatrack.custo.CustoCiclo;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.qualidadeDeAgua.QualidadeDeAgua;
import com.aquatrack.racao.TipoRacao;
import com.aquatrack.relatorio.RelatorioFinal;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.viveiro.ConsumoRacaoViveiro;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CicloViveiro {
    private LocalDate dataPovoamento; //Data povoamento é o id
    private int quantidadePovoada;
    private String laboratorio;
    private boolean ativo;     // true enquanto o ciclo está em andamento
    private boolean deletado;  // true se for removido do histórico

    private RelatorioFinal relatorioFinal;
    private List<Biometria> historicoBiometria;
    private List<QualidadeDeAgua> historicoQualidadeAgua;
    private ConsumoRacaoViveiro consumoRacaoCiclo;
    private List<CustoCiclo> custos;


    public CicloViveiro(LocalDate dataPovoamento, int quantidadePovoada, String laboratorio) {
        this.dataPovoamento = dataPovoamento;
        this.quantidadePovoada = quantidadePovoada;
        this.laboratorio = laboratorio;
        this.historicoBiometria = new ArrayList<Biometria>();
        this.historicoQualidadeAgua = new ArrayList<QualidadeDeAgua>();
        this.consumoRacaoCiclo = new ConsumoRacaoViveiro();
        this.custos = new ArrayList<CustoCiclo>();
        this.relatorioFinal = null;
        this.ativo = true;
        this.deletado = false;
    }

    //Relatorio
    public void gerarRelatorioFinal(double biometriaFinal, double biomassaFinal, LocalDate dataVenda, double precoVenda) {
        this.relatorioFinal = new RelatorioFinal(this, biometriaFinal, biomassaFinal, dataVenda, precoVenda);
        this.ativo = false; // quando gera relatório, ciclo automaticamente é encerrado
    }
    public RelatorioFinal getRelatorioFinal() {
        return relatorioFinal;
    }

    //Biometria
    public void addBiometria(Biometria biometria) {
        historicoBiometria.add(biometria);
    }
    public Biometria getUltimaBiometria() {
        return historicoBiometria.isEmpty() ? null :
                historicoBiometria.get(historicoBiometria.size() - 1);
    }
    public Biometria getPenultimaBiometria() {
        return historicoBiometria.size() < 2 ? null :
                historicoBiometria.get(historicoBiometria.size() - 2);
    }
    public List<Biometria> getHistoricoBiometria() {
        return new ArrayList<>(historicoBiometria); // cópia defensiva
    }


    //Qualidade de Água
    public void addQualidadeDeAgua(QualidadeDeAgua qualidadeDeAgua) {
        historicoQualidadeAgua.add(qualidadeDeAgua);
    }
    public QualidadeDeAgua getUltimaQualidade() {
        return historicoQualidadeAgua.isEmpty() ? null :
                historicoQualidadeAgua.get(historicoQualidadeAgua.size() - 1);
    }
    public QualidadeDeAgua getPenultimaQualidade() {
        return historicoQualidadeAgua.size() < 2 ? null :
                historicoQualidadeAgua.get(historicoQualidadeAgua.size() - 2);
    }
    public List<QualidadeDeAgua> getHistoricoQualidade() {
        return new ArrayList<>(historicoQualidadeAgua);
    }


    //Consumo Ração
    public void addConsumoRacao(TipoRacao tipoRacao, double quantidadeRacao) {
        switch (tipoRacao){
            case ENGORDA:
                consumoRacaoCiclo.addConsumoEngorda(quantidadeRacao);
                break;
            case CRESCIMENTO:
                consumoRacaoCiclo.addConsumoCrescimento(quantidadeRacao);
                break;
        }
    }
    public double consumoRacaoEngorda() { //São chamados no front, NÃO remover!!
        return consumoRacaoCiclo.consumoRacaoEngorda();
    }
    public double consumoRacaoCrescimento() {
        return consumoRacaoCiclo.consumoRacaoCrescimento();
    }
    public double consumoTotalRacao() {
        return consumoRacaoCrescimento()+consumoRacaoEngorda();
    }


    //Custos Produção
    public void addCusto(String nome, double valor, LocalDate data) {
        if (this.custos == null) {
            this.custos = new ArrayList<>();
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor do custo não pode ser negativo ou igual a 0.");
        }
        custos.add(new CustoCiclo(nome, valor, data));
    }


    public void removerCusto(String id){
        for (CustoCiclo custo : custos) {
            if (custo.getId().equals(id) && custo.isAtivo()) {
                custo.setAtivo(false);
            }
        }
    }

    public List<CustoCiclo> getCustos() {
        if (custos == null) {
            return new ArrayList<>();
        }

        List<CustoCiclo> ativos = new ArrayList<>();
        for (CustoCiclo custo : custos) {
            if (custo.isAtivo()) {
                ativos.add(custo);
            }
        }
        return ativos;
    }

    public double getTotalCustos() {
        if (custos == null || custos.isEmpty()) {return 0.0;}

        double totalCustos = 0;
        for (CustoCiclo custo : custos) {
            if (custo.isAtivo()) {
                totalCustos += custo.getValor();
            }
        }
        return totalCustos;
    }


    //Demais atribútos
    public boolean isAtivo() {
        return ativo;
    }
    public void desativar() {
        this.ativo = false;
    }

    public boolean isDeletado() {
        return deletado;
    }
    public void deleta() {
        this.deletado = true;
    }

    public LocalDate getDataPovoamento() {
        return dataPovoamento;
    }

    public void setDataPovoamento(LocalDate dataPovoamento) {
        this.dataPovoamento = dataPovoamento;
    }

    public int getQuantidadePovoada() {
        return quantidadePovoada;
    }

    public void setQuantidadePovoada(int quantidadePovoada) {
        this.quantidadePovoada = quantidadePovoada;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

}
