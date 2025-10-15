package com.aquatrack.funcionario;

import com.aquatrack.Funcao;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.usuario.Usuario;

import java.util.HashSet;
import java.util.Set;

public class FuncionarioFazenda {
    private Usuario usuario;
    private transient Fazenda fazenda;
    private Set<Funcao> funcoes; //Set representa uma coleção que não permite elementos duplicados.
    private Boolean deletado;

    public FuncionarioFazenda(Usuario usuario, Fazenda fazenda) {
        this.usuario = usuario;
        this.fazenda = fazenda;
        this.funcoes = new HashSet<Funcao>();
        this.deletado = false;
    }

    public void addFuncao(Funcao funcao) {
        funcoes.add(funcao);
    }
    public void removeFuncao(Funcao funcao) {
        funcoes.remove(funcao);
    }

    public void deleta(){
        deletado = true;
    }
    public boolean isDeletado() {
        return deletado;
    }

    public Fazenda getFazendaUsuario() {
        return fazenda;
    }
    public Usuario getUsuario() {
        return usuario;
    }
    public Set<Funcao> getFuncoes() {
        return funcoes;
    }
}
