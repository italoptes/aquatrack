package com.aquatrack.fazenda;

import com.aquatrack.Funcao;
import com.aquatrack.UsuarioRepository;
import com.aquatrack.funcionario.FuncionarioFazenda;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.viveiro.Viveiro;
import com.aquatrack.racao.TipoRacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        return fazenda.listarViveiros();
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

    // ===== Funcionários =====
    public void addFuncionario(Usuario usuario, Fazenda fazenda, FuncionarioFazenda funcionario) {
        if (usuario == null || fazenda == null || funcionario == null)
            throw new IllegalArgumentException("Usuário, Fazenda ou Funcionário inválidos.");

        if (funcionario.isDeletado())
            throw new IllegalStateException("Não é possível adicionar um funcionário deletado.");

        Fazenda fazendaUser = usuario.getFazendaPorId(fazenda.getId());

        // Verifica se já existe funcionário com mesmo login
        boolean funcionarioJaExiste = fazendaUser.listarFuncionariosAtivos().stream()
                .anyMatch(f -> f.getUsuario().getLogin().equals(funcionario.getUsuario().getLogin()));

        if (funcionarioJaExiste)
            throw new IllegalArgumentException("Já existe um funcionário ativo com esse login.");

        fazendaUser.addFuncionario(funcionario);
        usuarioRepository.salvarUsuario(usuario);
    }

    public void removerFuncionario(Usuario usuario, Fazenda fazenda, FuncionarioFazenda funcionario) {
        if (usuario == null || fazenda == null || funcionario == null)
            throw new IllegalArgumentException("Usuário, Fazenda ou Funcionário inválidos.");

        Fazenda fazendaUser = usuario.getFazendaPorId(fazenda.getId());
        Optional<FuncionarioFazenda> existente = fazendaUser.listarFuncionariosAtivos().stream()
                .filter(f -> f.getUsuario().getLogin().equals(funcionario.getUsuario().getLogin()))
                .findFirst();

        if (existente.isEmpty())
            throw new IllegalArgumentException("Funcionário não encontrado ou já deletado.");

        existente.get().deleta();
        usuarioRepository.salvarUsuario(usuario);
    }

    public List<FuncionarioFazenda> listarFuncionarios(Usuario usuario, Fazenda fazenda) {
        if (usuario == null || fazenda == null)
            return new ArrayList<>();

        Fazenda fazendaUser = usuario.getFazendaPorId(fazenda.getId());
        return fazendaUser.listarFuncionariosAtivos();
    }

    public FuncionarioFazenda getFuncionario(Usuario usuario, Fazenda fazenda, String loginFuncionario) {
        if (usuario == null || fazenda == null)
            throw new IllegalArgumentException("Usuário ou Fazenda inválidos.");

        Fazenda fazendaUser = usuario.getFazendaPorId(fazenda.getId());
        return fazendaUser.listarFuncionariosAtivos().stream()
                .filter(f -> f.getUsuario().getLogin().equals(loginFuncionario))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado ou inativo."));
    }

    public void addFuncaoFuncionario(Usuario usuario, Fazenda fazenda, FuncionarioFazenda funcionario, Funcao funcao) {
        if (usuario == null || fazenda == null || funcionario == null || funcao == null)
            throw new IllegalArgumentException("Parâmetros inválidos.");

        Fazenda fazendaUser = usuario.getFazendaPorId(fazenda.getId());
        FuncionarioFazenda funcionarioEncontrado = getFuncionario(usuario, fazenda, funcionario.getUsuario().getLogin());

        if (funcionarioEncontrado.isDeletado())
            throw new IllegalStateException("Não é possível adicionar função a um funcionário deletado.");

        if (funcionarioEncontrado.getFuncoes().contains(funcao))
            throw new IllegalArgumentException("O funcionário já possui essa função.");

        funcionarioEncontrado.addFuncao(funcao);
        usuarioRepository.salvarUsuario(usuario);
    }

    public void removerFuncaoFuncionario(Usuario usuario, Fazenda fazenda, FuncionarioFazenda funcionario, Funcao funcao) {
        if (usuario == null || fazenda == null || funcionario == null || funcao == null)
            throw new IllegalArgumentException("Parâmetros inválidos.");

        Fazenda fazendaUser = usuario.getFazendaPorId(fazenda.getId());
        FuncionarioFazenda funcionarioEncontrado = getFuncionario(usuario, fazenda, funcionario.getUsuario().getLogin());

        if (!funcionarioEncontrado.getFuncoes().contains(funcao))
            throw new IllegalArgumentException("O funcionário não possui essa função.");

        funcionarioEncontrado.removeFuncao(funcao);
        usuarioRepository.salvarUsuario(usuario);
    }
}
