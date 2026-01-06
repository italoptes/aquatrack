package com.aquatrack.viveiro;


import com.aquatrack.cicloViveiro.CicloViveiro;
import com.aquatrack.instrucoes.Instrucao;
import com.aquatrack.instrucoes.Status;
import com.aquatrack.relatorio.RelatorioFinal;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Viveiro {
    private static List<String> idExistentesInstrucoes = new ArrayList<>();
    private String id;
    private double area;
    private List<Instrucao> instrucoes;
    private Map<String, CicloViveiro> ciclos; //Id é chave para o ciclo
    private boolean deletado;

    public Viveiro() {
        this.instrucoes = new ArrayList<>();
        this.ciclos = new HashMap<>();
        this.deletado = false;
    }

    public Viveiro(String id, double area) {
        this.id = id;
        this.area = area;
        this.instrucoes = new ArrayList<>();
        this.deletado = false;
        this.ciclos = new HashMap<>();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public double getArea() {
        return area;
    }
    public void setArea(double area) {
        this.area = area;
    }

    public boolean isDeletado() {
        return deletado;
    }
    public void deleta() {
        this.deletado = true;
    }

    // Ciclos
    public void addCiclo(CicloViveiro cicloViveiro){
        ciclos.put(cicloViveiro.getIdCiclo(), cicloViveiro);
    }
    public void encerrarCiclo(CicloViveiro cicloViveiro){
        ciclos.get(cicloViveiro.getIdCiclo()).desativar(); //apenas desativa o ciclo
    }
    public List<CicloViveiro> getCiclos() { //exibe apenas os que não estao deletados
        List<CicloViveiro> listaCiclos = new ArrayList<>();
        for (CicloViveiro ciclo : ciclos.values()) {
            if (!ciclo.isDeletado()) {
                listaCiclos.add(ciclo);
            }
        }
        return listaCiclos;
    }
    public CicloViveiro getCiclo(String cicloId){
        return ciclos.get(cicloId);
    }

    public CicloViveiro ultimoCiclo() {
        return ciclos.values().stream()
                .filter(c -> c.isAtivo() && !c.isDeletado())
                .findFirst()
                .orElse(
                        ciclos.values().stream()
                                .filter(c -> !c.isDeletado())
                                .max(Comparator.comparing(CicloViveiro::getDataPovoamento))
                                .orElse(null)
                );
    }

    public List<RelatorioFinal> relatoriosFinais() {
        List<RelatorioFinal> listaRelatorios = new ArrayList<>();
        for (CicloViveiro ciclo : ciclos.values()) {
            RelatorioFinal relatorioFinal = ciclo.getRelatorioFinal();
            if (relatorioFinal != null) {
                listaRelatorios.add(relatorioFinal);
            }
        }
        return listaRelatorios; //É para listar somente os relatorios fechados
    }

    public boolean isCicloAtivo() { //Usado no front
        CicloViveiro ultimo = ultimoCiclo();
        if (ultimo == null) {
            return false; // nenhum ciclo cadastrado
        }
        return ultimo.isAtivo(); // só retorna true se o último ciclo ainda estiver em andamento
    }

    //Instruções
    public void adicionarInstrucao(Instrucao instrucao) {
        gerarId(instrucao);
        if (instrucao == null || isBlank(instrucao.getId())) {
            throw new IllegalArgumentException("Instrução inválida.");
        }
        instrucoes.add(instrucao);
    }

    public void removerInstrucao(String idInstrucao) {
        Instrucao instrucao = buscarInstrucao(idInstrucao);
        if (instrucao != null) {
            instrucao.deletar();
        }
    }

    public Instrucao buscarInstrucao(String idInstrucao) {
        return instrucoes.stream()
                .filter(instrucao -> instrucao.getId().equals(idInstrucao) && !instrucao.isDeletado())
                .findFirst()
                .orElse(null);
    }

    public List<Instrucao> getInstrucoes() {
        return instrucoes.stream()
                .filter(instrucao -> !instrucao.isDeletado())
                .toList();
    }

    public List<Instrucao> getInstrucoesRecentes() { //Retorna as ultimas 3
        return instrucoes.stream()
                .filter(instrucao -> !instrucao.isDeletado())
                .sorted(Comparator.comparing(Instrucao::getDataCriacao).reversed())
                .limit(3)
                .toList();
    }

    public void atualizarInstrucao(String idInstrucao, String novoTitulo, String novaDescricao, Status novoStatus) {
        Instrucao instrucao = buscarInstrucao(idInstrucao);
        if (instrucao != null) {
            instrucao.atualizar(novoTitulo, novaDescricao, novoStatus);
        }
    }

    // ================= HELPERS =================

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void gerarId(Instrucao instrucao) {
        while (true) {
            int numero = ThreadLocalRandom.current().nextInt(100, 1000);
            String id = String.format("I-%04d", numero);
            if (!idExistentesInstrucoes.contains(id)) {
                idExistentesInstrucoes.add(id);
                instrucao.setId(id);
                return;
            }
        }
    }
}
