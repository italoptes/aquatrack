package com.aquatrack.cicloViveiro;

import com.aquatrack.UsuarioRepository;
import com.aquatrack.biometria.Biometria;
import com.aquatrack.qualidadeDeAgua.QualidadeDeAgua;
import com.aquatrack.racao.TipoRacao;
import com.aquatrack.relatorio.RelatorioFinal;
import com.aquatrack.usuario.Usuario;

import java.time.LocalDate;
import java.util.List;

public class CicloViveiroService {

    private UsuarioRepository usuarioRepository;

    public CicloViveiroService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ===== Biometria =====
    public void registrarBiometria(Usuario usuario,CicloViveiro ciclo, Biometria biometria) {
        if (ciclo == null || !ciclo.isAtivo()) throw new IllegalStateException("Ciclo inativo.");
        if (biometria == null || !biometria.isValida()) {
            throw new IllegalArgumentException("Biometria inválida.");
        }
        ciclo.addBiometria(biometria);
        usuarioRepository.salvarUsuario(usuario);
    }

    public Biometria getUltimaBiometria(CicloViveiro ciclo) {
        return ciclo.getUltimaBiometria();
    }

    public Biometria getPenultimaBiometria(CicloViveiro ciclo) {
        return ciclo.getPenultimaBiometria();
    }

    public List<Biometria> getHistoricoBiometria(CicloViveiro ciclo) {
        return ciclo.getHistoricoBiometria();
    }

    // ===== Qualidade de Água =====
    public void registrarQualidadeAgua(Usuario usuario,CicloViveiro ciclo, QualidadeDeAgua qualidade) {
        if (ciclo == null || !ciclo.isAtivo()) throw new IllegalStateException("Ciclo inativo.");
        if (qualidade == null || !qualidade.valoresValidos()) {
            throw new IllegalArgumentException("Medição de qualidade inválida.");
        }
        ciclo.addQualidadeDeAgua(qualidade);
        usuarioRepository.salvarUsuario(usuario);
    }

    public QualidadeDeAgua getUltimaQualidade(CicloViveiro ciclo) {
        return ciclo.getUltimaQualidade();
    }

    public QualidadeDeAgua getPenultimaQualidade(CicloViveiro ciclo) {
        return ciclo.getPenultimaQualidade();
    }

    public List<QualidadeDeAgua> getHistoricoQualidade(CicloViveiro ciclo) {
        return ciclo.getHistoricoQualidade();
    }

    // ===== Consumo de Ração =====
    public void registrarConsumoRacao(Usuario usuario, CicloViveiro ciclo, TipoRacao tipo, double quantidade) {
        if (ciclo == null || !ciclo.isAtivo()) throw new IllegalStateException("Ciclo inativo.");
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva.");
        ciclo.addConsumoRacao(tipo, quantidade);
        usuarioRepository.salvarUsuario(usuario);
    }

    public double getConsumoTotal(CicloViveiro ciclo) {
        return ciclo.consumoTotalRacao();
    }

    // ===== Relatório Final =====
    public RelatorioFinal gerarRelatorioFinal(Usuario usuario, CicloViveiro ciclo, double biometriaFinal, double biomassaFinal, LocalDate dataVenda) {
        if (ciclo == null || ciclo.isDeletado()) throw new IllegalArgumentException("Ciclo inválido.");
        ciclo.gerarRelatorioFinal(biometriaFinal, biomassaFinal, dataVenda);
        usuarioRepository.salvarUsuario(usuario);
        return ciclo.getRelatorioFinal();
    }
}