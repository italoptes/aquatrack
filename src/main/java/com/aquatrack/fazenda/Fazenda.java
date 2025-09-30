package com.aquatrack.fazenda;

import com.aquatrack.racao.RacaoEstoque;
import com.aquatrack.racao.TipoRacao;
import com.aquatrack.viveiro.Viveiro;

import java.util.*;

public class Fazenda {
    private String id;
    private String nome;
    private String localizacao;
    private RacaoEstoque racaoEstoque;
    private Map<String, Viveiro> viveiros;
    private boolean deletado;

    public Fazenda() {
        this.id = "";
        this.nome = "";
        this.localizacao = "";
        this.viveiros = new HashMap<>();
        this.racaoEstoque = new RacaoEstoque();
        this.deletado = false;
    }

    public Fazenda(String nome, String localizacao) {
        this.id = "";
        this.nome = nome;
        this.localizacao = localizacao;
        this.viveiros = new HashMap<>();
        this.racaoEstoque = new RacaoEstoque();
        this.deletado = false;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLocalizacao() {
        return localizacao;
    }
    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public RacaoEstoque getRacaoEstoque() {
        return racaoEstoque;
    }
    public void setRacaoEstoque(RacaoEstoque racaoEstoque) {
        this.racaoEstoque = racaoEstoque;
    }

    public Map<String, Viveiro> getViveiros() {
        return viveiros;
    }
    public void setViveiros(Map<String, Viveiro> viveiros) {
        this.viveiros = viveiros;
    }

    public boolean isDeletado() {
        return deletado;
    }
    public void setDeletado(boolean deletado) {
        this.deletado = deletado;
    }

    public void adicionaQuantidade(TipoRacao tipo, double quantidadeKg) {
        racaoEstoque.adicionar(tipo,  quantidadeKg);
    }
    public void removerQuantidade(TipoRacao tipo, double quantidadeKg) {
        racaoEstoque.consumir(tipo,  quantidadeKg);
    }
    public double listarQuantidadePorTipo(TipoRacao tipo) {
        return racaoEstoque.getQuantidadeRacaoPorTipo(tipo);
    }

    public void addViveiro(Viveiro viveiro) {
        viveiros.put(viveiro.getId(), viveiro);
    }
    public void removerViveiro(Viveiro viveiro) {
        viveiros.get(viveiro.getId()).deleta();
    }
    public List<Viveiro> listarViveiros() {
        List<Viveiro> listaViveiros = new ArrayList<>();
        for (Viveiro viveiro : viveiros.values()) {
            if (!viveiro.isDeletado()) {
                listaViveiros.add(viveiro);
            }
        }
        return listaViveiros;
    }

}
