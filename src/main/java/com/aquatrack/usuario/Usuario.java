package com.aquatrack.usuario;

import com.aquatrack.fazenda.Fazenda;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private String id;
    private String login;
    private String nome;
    private String senha;
    private List<Fazenda> fazendas;
    private boolean deletado;
    private TipoUsuario tipo;

    public Usuario(String login, String nome, String senha, TipoUsuario tipoUsuario) {
        this.id = java.util.UUID.randomUUID().toString();
        this.login = login;
        this.nome = nome;
        this.senha = senha;
        this.deletado = false;
        this.tipo = tipoUsuario;
        this.fazendas = new ArrayList<>();
    }

    public List<Fazenda> getFazendas() {
        return fazendas;
    }

    public void setFazendas(List<Fazenda> fazendas) {
        this.fazendas = fazendas;
    }

    public Usuario() {
        this.fazendas = new ArrayList<>();
    }

    // Getters e Setters
    public String getId() {return id;}
    public  void setId(String id) {this.id = id;}

    public String getLogin() {return login;}
    public void setLogin(String login) {this.login = login;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public String getSenha() {return senha;}

    public void setSenha(String senha) {this.senha = senha;}

    public Fazenda getFazendaPorId(String id) {
        return fazendas.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
    }
    public int contaFazendasUsuarios(Usuario usuario) {
        return usuario.listarFazendasAtivas().size();
    }
    public List<Fazenda> listarFazendasAtivas() {
        ArrayList<Fazenda> fazendasAtivas = new ArrayList<>();
        for (Fazenda fazenda : fazendas) {
            if (!fazenda.isDeletado()) {
                fazendasAtivas.add(fazenda);
            }
        }
        return fazendasAtivas;
    }
    public void addFazenda(Fazenda fazenda) {this.fazendas.add(fazenda);}
    public void removerFazenda(String idFazenda) {getFazendaPorId(idFazenda).deletar();}

    public boolean isDeletado() {return deletado;}
    public void deletar() {this.deletado = true;}

    public TipoUsuario getTipoUsuario() {return tipo;}
    public void setTipoUsuario(TipoUsuario tipo) {this.tipo = tipo;}

    public List<Fazenda> listarFazendas() {
        List<Fazenda> listaFazendas = new ArrayList<>();
        for (Fazenda fazenda : fazendas) {
            if (!fazenda.isDeletado()) {
                listaFazendas.add(fazenda);
            }
        }
        return listaFazendas;
    }

}

