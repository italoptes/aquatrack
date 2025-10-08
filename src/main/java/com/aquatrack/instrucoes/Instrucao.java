package com.aquatrack.instrucoes;

import java.time.LocalDate;

public class Instrucao {
    private String id;
    private String titulo;
    private String descricao;
    private Status status;
    private String autor;
    private LocalDate dataCriacao;
    private LocalDate ultimaAtualizacao;
    private boolean deletado;

    public Instrucao(String titulo, String descricao, String autor) {
        this.id = "";
        this.titulo = titulo;
        this.descricao = descricao;
        this.autor = autor;
        this.status = Status.PENDENTE;
        this.dataCriacao = LocalDate.now();
        this.ultimaAtualizacao = null;
        this.deletado = false;
    }

    public void atualizar(String novoTitulo, String novaDescricao, Status novoStatus) {
        this.titulo = novoTitulo;
        this.descricao = novaDescricao;
        this.status = novoStatus;
        this.ultimaAtualizacao = LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDate ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public boolean isDeletado() {
        return deletado;
    }

    public void deletar() {
        this.deletado = true;
    }

    @Override
    public String toString() {
        return "Instrucao{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", status=" + status +
                ", autor='" + autor + '\'' +
                '}';
    }
}
