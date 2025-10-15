package com.aquatrack.fazenda;

import com.aquatrack.Funcao;
import com.aquatrack.funcionario.FuncionarioFazenda;
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
    private List<FuncionarioFazenda> funcionarios;
    private boolean deletado;

    public Fazenda() {
        this.id = "";
        this.nome = "";
        this.localizacao = "";
        this.viveiros = new HashMap<>();
        this.racaoEstoque = new RacaoEstoque();
        this.funcionarios = new ArrayList<>();
        this.deletado = false;
    }

    public Fazenda(String nome, String localizacao) {
        this.id = "";
        this.nome = nome;
        this.localizacao = localizacao;
        this.viveiros = new HashMap<>();
        this.racaoEstoque = new RacaoEstoque();
        this.funcionarios = new ArrayList<>();
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

    public boolean isDeletado() {
        return deletado;
    }
    public void deletar() {
        this.deletado = true;
    }

    public void adicionaQuantidade(TipoRacao tipo, double quantidadeKg) {
        if (quantidadeKg <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        racaoEstoque.adicionar(tipo,  quantidadeKg);
    }
    public void removerQuantidade(TipoRacao tipo, double quantidadeKg) {
        if (quantidadeKg > listarQuantidadePorTipo(tipo)) {
            throw new IllegalArgumentException(
                    String.format("Quantidade solicitada (%.2f Kg) é maior do que o estoque disponível (%.2f Kg).",
                            quantidadeKg, listarQuantidadePorTipo(tipo))
            );
        }        racaoEstoque.consumir(tipo,  quantidadeKg);
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
    public int viveirosAtivos() { //Usado no front para exibir o Nº de viveiros da fazenda
        int viveirosAtivos = 0;
        for (Viveiro v : viveiros.values()) {
            if (!v.isDeletado()) {viveirosAtivos++;}
        }
        return viveirosAtivos;
    }
    public Map<String, Viveiro> getViveiros() {
        return viveiros;
    }

    public void setViveiros(Map<String, Viveiro> viveiros) {
        this.viveiros = viveiros;
    }

    public void addFuncionario(FuncionarioFazenda funcionario) {
        funcionarios.add(funcionario);
    }

    public void removerFuncionario(FuncionarioFazenda funcionario) {
        funcionario.deleta();
    }

    public List<FuncionarioFazenda> listarFuncionariosAtivos() {
        return funcionarios.stream().
                filter(f -> !f.isDeletado()). //pega apenas os ativos
                toList(); //cria a lista
    }

    public FuncionarioFazenda getFuncionario(String login) {
        return funcionarios.stream()
                .filter(f -> !f.isDeletado()) // filtra apenas os ativos
                .filter(f -> f.getUsuario().getLogin().equals(login)) // filtra pelo login
                .findFirst() // pega o primeiro (ou único)
                .orElse(null); // retorna null se não encontrar
    }

    public void addFuncaoFuncionario(FuncionarioFazenda funcionario, Funcao funcao) {
        funcionario.addFuncao(funcao);
    }
    public void removerFuncaoFuncionario(FuncionarioFazenda funcionario, Funcao funcao) {
        funcionario.removeFuncao(funcao);
    }
}
