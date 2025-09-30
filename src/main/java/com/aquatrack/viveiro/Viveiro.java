package com.aquatrack.viveiro;


import com.aquatrack.cicloViveiro.CicloViveiro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Viveiro {
    private String id;
    private double area;
    private Map<String, CicloViveiro> ciclos;
    private boolean deletado;

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
}
