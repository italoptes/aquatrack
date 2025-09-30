package com.aquatrack.usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Usuario {
    private String id;
    private String login;
    private String nome;
    private String senha;
    private boolean deletado;

    private TipoUsuario tipoUsuario;
    private List<String> fazendasUsuarios; //IDs de fazendas atreladas ao usuário

    public Usuario(String login, String nome, String senha, TipoUsuario tipoUsuario) {
        this.id = java.util.UUID.randomUUID().toString();
        this.login = login;
        this.nome = nome;
        this.senha = senha;
        this.deletado = false;
        this.tipoUsuario = tipoUsuario;
        this.fazendasUsuarios = new ArrayList<>();
    }

    public Usuario() {
        this.fazendasUsuarios = new ArrayList<>();
    }


    public void addFazenda(String idFazenda) {
        fazendasUsuarios.add(idFazenda);
    }

    public boolean contemFazenda(String idFazenda) {
        return fazendasUsuarios.contains(idFazenda);
    }

    public int contaFazendas() {
        return fazendasUsuarios.size(); // Tá retornando os excluídos também
    }

    public List<String> listarIdFazendas() {
        return fazendasUsuarios;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {

        this.nome = nome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDeletado() {
        return deletado;
    }

    public void deleta() {
        this.deletado = true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", login='" + login + '\'' +
                ", nome='" + nome + '\'' +
                '}';
    }
}

