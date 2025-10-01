package com.aquatrack.viveiro;

import com.aquatrack.UsuarioRepository;
import com.aquatrack.cicloViveiro.CicloViveiro;
import com.aquatrack.usuario.Usuario;

import java.time.LocalDate;
import java.util.List;

public class ViveiroService {

    private UsuarioRepository usuarioRepository;

    public ViveiroService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public CicloViveiro abrirCiclo(Usuario usuario, Viveiro viveiro, LocalDate dataPovoamento, int quantidade, String laboratorio) {
        if (viveiro == null || viveiro.isDeletado()) {
            throw new IllegalArgumentException("Viveiro inválido.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade povoada deve ser > 0.");
        }
        if (viveiro.getCiclos().stream().anyMatch(CicloViveiro::isAtivo)) {
            throw new IllegalStateException("Já existe um ciclo ativo neste viveiro.");
        }

        CicloViveiro ciclo = new CicloViveiro(dataPovoamento, quantidade, laboratorio);
        viveiro.addCiclo(ciclo);
        usuarioRepository.salvarUsuario(usuario);
        return ciclo;
    }

    public void encerrarCiclo(Usuario usuario,Viveiro viveiro, String cicloId) {
        if (viveiro == null) throw new IllegalArgumentException("Viveiro inválido.");
        CicloViveiro cicloViveiro = viveiro.getCiclo(cicloId);
        viveiro.encerrarCiclo(cicloViveiro);
        usuarioRepository.salvarUsuario(usuario);
    }

    public List<CicloViveiro> listarCiclos(Viveiro viveiro) {
        if (viveiro == null) throw new IllegalArgumentException("Viveiro inválido.");
        return viveiro.getCiclos(); // já ignora deletados
    }
}
