package com.aquatrack.fazenda;

import java.util.List;

public class ResumoFazendaDTO { //classe criada para facilitar a coleta desses dados em específico
                                // pode ser útil caso queiremos adicionar mais informações no futuo
    private String idFazenda;
    private String nomeFazenda;
    private int totalViveiros;
    private int ciclosAtivos;
    private List<String> viveirosAtivos; // lista dos IDs dos viveiros ativos

    public String getIdFazenda() {
        return idFazenda;
    }

    public void setIdFazenda(String idFazenda) {
        this.idFazenda = idFazenda;
    }

    public String getNome() {
        return nomeFazenda;
    }

    public void setNome(String nomeFazenda) {
        this.nomeFazenda = nomeFazenda;
    }

    public int getTotalViveiros() {
        return totalViveiros;
    }

    public void setTotalViveiros(int totalViveiros) {
        this.totalViveiros = totalViveiros;
    }

    public int getCiclosAtivos() {
        return ciclosAtivos;
    }

    public void setCiclosAtivos(int ciclosAtivos) {
        this.ciclosAtivos = ciclosAtivos;
    }

    public List<String> getViveirosAtivos() {
        return viveirosAtivos;
    }

    public void setViveirosAtivos(List<String> viveirosAtivos) {
        this.viveirosAtivos = viveirosAtivos;
    }
}