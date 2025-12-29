package com.aquatrack.fazenda;

import com.aquatrack.UsuarioRepository;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.viveiro.Viveiro;
import com.aquatrack.racao.TipoRacao;

import java.util.Comparator;
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
        verificaId(usuario, fazenda.getId(), viveiro.getId());
        fazenda.addViveiro(viveiro);
        usuarioRepository.salvarUsuario(usuario);
    }

    public void removerViveiro(Usuario usuario, Fazenda fazenda, String viveiroId) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        Viveiro viveiro = fazenda.getViveiros().get(viveiroId);
        if (viveiro == null) {
            throw new IllegalArgumentException("Viveiro inválido: " + viveiroId);
        }
        fazenda.removerViveiro(viveiro);
        usuarioRepository.salvarUsuario(usuario);
    }

    public List<Viveiro> listarViveiros(Fazenda fazenda) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");

        return fazenda.listarViveiros()
                .stream()
                .sorted(Comparator.comparingInt(v ->
                        Integer.parseInt(v.getId().replace("V-", ""))
                ))
                .toList();
    }

    public Viveiro getViveiro(Fazenda fazenda, String viveiroId) {
        return fazenda.getViveiros().get(viveiroId);
    }

    private void verificaId(Usuario usuario, String idFazenda, String idViveiro) {
        Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
        if (fazendaUser.getViveiros().containsKey(idViveiro) ) {
            if (!fazendaUser.getViveiros().get(idViveiro).isDeletado()){
                throw new IllegalArgumentException("Já existe uma fazenda com o id "+idViveiro.trim());
            }
        }
    }

    // ===== Estoque de Ração =====
    public void adicionarRacao(Usuario usuario, Fazenda fazenda, TipoRacao tipo, double quantidadeKg) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        Fazenda fazendaUser = usuario.getFazendaPorId(fazenda.getId());
        fazendaUser.adicionaQuantidade(tipo, quantidadeKg);
        usuarioRepository.salvarUsuario(usuario);
    }

    public double consultarEstoquePorTipo(Fazenda fazenda, TipoRacao tipo) {
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        return fazenda.listarQuantidadePorTipo(tipo);
    }

    public void removerRacao(Usuario usuario, Fazenda fazenda, TipoRacao tipo, double quantidadeKg){
        if (fazenda == null) throw new IllegalArgumentException("Fazenda inválida.");
        Fazenda fazendaUser = usuario.getFazendaPorId(fazenda.getId());
        fazendaUser.removerQuantidade(tipo, quantidadeKg);
        usuarioRepository.salvarUsuario(usuario);
    }

    public boolean isFazendaDeletada(Fazenda fazenda) {
        return fazenda != null && fazenda.isDeletado();
    }
}
