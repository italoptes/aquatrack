package com.aquatrack.viveiro;


import com.aquatrack.cicloViveiro.CicloViveiro;
import com.aquatrack.relatorio.RelatorioFinal;

import java.util.*;

public class Viveiro {
    private String id;
    private double area;
    private Map<String, CicloViveiro> ciclos;
    private boolean deletado;

    public Viveiro() {
        this.ciclos = new HashMap<>();
        this.deletado = false;
    }

    public Viveiro(String id, double area) {
        this.id = id;
        this.area = area;
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

    public void addCiclo(CicloViveiro cicloViveiro){
        ciclos.put(cicloViveiro.getDataPovoamento().toString(), cicloViveiro);
    }
    public void encerrarCiclo(CicloViveiro cicloViveiro){
        ciclos.get(cicloViveiro.getDataPovoamento().toString()).desativar(); //apenas desativa o ciclo
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
                .max(Comparator.comparing(CicloViveiro::getDataPovoamento))
                .orElse(null);
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


}
