package com.aquatrack.fazenda;

import com.aquatrack.UsuarioRepository;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.viveiro.Viveiro;
import com.aquatrack.racao.TipoRacao;

import java.util.List;

public class FazendaService {

    private UsuarioRepository usuarioRepository;

    public FazendaService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ===== Viveiros =====
    public void adicionarViveiro(Usuario usuario,Fazenda fazenda, Viveiro viveiro) { //Viveiro é criado no Controller e passado para o service
        if (fazenda == null || viveiro == null) {
            throw new IllegalArgumentException("Fazenda ou Viveiro inválidos.");
        }
        fazenda.addViveiro(viveiro);
        usuarioRepository.salvarUsuario(usuario);
    }

    public void removerViveiro(Usuario usuario, Fazenda fazenda, String viveiroId) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        Viveiro v = fazenda.getViveiros().get(viveiroId);
        if (v != null) v.deleta();
        usuarioRepository.salvarUsuario(usuario);
    }

    public List<Viveiro> listarViveiros(Fazenda fazenda) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");

        return fazenda.listarViveiros();
    }

    public Viveiro getViveiro(Fazenda fazenda, String viveiroId) {
        return fazenda.getViveiros().get(viveiroId);
    }

    // ===== Estoque de Ração =====
    public void adicionarRacao(Usuario usuario, Fazenda fazenda, TipoRacao tipo, double quantidadeKg) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        fazenda.adicionaQuantidade(tipo, quantidadeKg);
        usuarioRepository.salvarUsuario(usuario);
    }

    public double consultarEstoquePorTipo(Fazenda fazenda, TipoRacao tipo) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        return fazenda.listarQuantidadePorTipo(tipo);
    }

    public boolean isFazendaDeletada(Fazenda fazenda) {
        return fazenda != null && fazenda.isDeletado();
    }
}
