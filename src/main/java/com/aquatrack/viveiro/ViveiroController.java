package com.aquatrack.viveiro;


import com.aquatrack.cicloViveiro.CicloViveiro;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.fazenda.FazendaController;
import com.aquatrack.fazenda.FazendaService;
import com.aquatrack.instrucoes.Instrucao;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ViveiroController {

    private static final Logger logger = LogManager.getLogger(ViveiroController.class);

    private final UsuarioService usuarioService;
    private final FazendaService fazendaService;
    private final ViveiroService viveiroService;

    public ViveiroController(UsuarioService usuarioService, FazendaService fazendaService, ViveiroService viveiroService) {
        this.usuarioService = usuarioService;
        this.fazendaService = fazendaService;
        this.viveiroService = viveiroService;
    }

    public void mostrarFormularioViveiro(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        ctx.attribute("idFazenda", idFazenda);
        logger.debug("Abrindo formulário de cadastro de viveiro para fazenda={}", idFazenda);
        ctx.render("/viveiros/formulario_viveiro.html");
    }

    public void cadastrarViveiro(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        ctx.attribute("idFazenda", idFazenda);
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        try {
            String areaParam = ctx.formParam("area");
            if (areaParam == null) throw new IllegalArgumentException("A área do viveiro deve ser informada.");
            double area = Double.parseDouble(areaParam);
            if (area <= 0) throw new IllegalArgumentException("A área do viveiro deve ser maior que zero.");
            String idViveiro = ctx.formParam("idPersonalizado");
            Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = new Viveiro("V-" + idViveiro, area);
            fazendaService.adicionarViveiro(usuario,fazendaUser, viveiro);
            logger.info("Viveiro cadastrado: fazenda={}, area={}, id={}", idFazenda, area, idViveiro);
            ctx.redirect("/fazenda/" + idFazenda);

        } catch (NumberFormatException e) {
            logger.warn("Entrada inválida para área de viveiro na fazenda={}: {}", idFazenda, e.getMessage());
            ctx.attribute("erro", "A área deve ser um número válido.");
            ctx.render("/viveiros/formulario_viveiro.html");

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao cadastrar viveiro: fazenda={}, erro={}", idFazenda, e.getMessage());
            ctx.attribute("erro", e.getMessage());
            ctx.render("/viveiros/formulario_viveiro.html");

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar viveiro na fazenda={}", idFazenda, e);
            ctx.attribute("erro", "Não foi possível cadastrar o viveiro. Tente novamente.");
            ctx.render("/viveiros/formulario_viveiro.html");
        }
    }

    public void listarViveiros(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        String idFazenda = ctx.pathParam("id");
        ctx.attribute("idFazenda", idFazenda);
        Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);
        List<Viveiro> viveiros = fazendaService.listarViveiros(fazenda);
        logger.debug("Listando viveiros da fazenda={}, total={}", idFazenda, viveiros.size());
        ctx.attribute("viveiros", viveiros);
    }

    public void abrirViveiro(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        String id = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        try {
            Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), id);
            Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);

            if (viveiro != null && !viveiro.isDeletado()) {
                List<Instrucao> instrucoesRecentes = viveiroService.listarInstrucoesRecentes(viveiro);
                CicloViveiro cicloViveiro = viveiro.ultimoCiclo();

                logger.info("Abrindo viveiro: fazenda={}, viveiro={}", id, idViveiro);
                ctx.attribute("viveiro", viveiro);
                ctx.attribute("idFazenda", id);
                ctx.attribute("idViveiro", idViveiro);
                ctx.attribute("instrucoes",instrucoesRecentes);
                ctx.attribute("totalCustos", cicloViveiro != null ? cicloViveiro.getTotalCustos() : 0.0);

                if (!viveiro.isCicloAtivo()) {
                    ctx.attribute("info", "O viveiro não tem um ciclo ativo, inicie um para gerencia-lo.");
                }

                if (cicloViveiro != null) {
                    ctx.attribute("cicloViveiro", cicloViveiro);
                    ctx.attribute("ultimaBiometria", cicloViveiro.getUltimaBiometria());
                    ctx.attribute("penultimaBiometria", cicloViveiro.getPenultimaBiometria());
                    ctx.attribute("ultimaQualidade", cicloViveiro.getUltimaQualidade());
                    ctx.attribute("penultimaQualidade", cicloViveiro.getPenultimaQualidade());
                    ctx.attribute("qtdeQualidade", cicloViveiro.getHistoricoQualidade().size());
                } else {
                    ctx.attribute("cicloViveiro", null);
                }

                ctx.render("viveiros/pagina_viveiro.html");
            } else {
                logger.warn("Tentativa de acessar viveiro inexistente ou deletado: fazenda={}, viveiro={}", id, idViveiro);
                ctx.status(404).result("Viveiro não encontrado");
            }
        } catch (Exception e) {
            logger.error("Erro ao abrir viveiro fazenda={}, viveiro={}", id, idViveiro, e);
            ctx.status(500).result("Erro interno ao abrir o viveiro.");
        }
    }


    public void removerViveiro(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
        try {
            fazendaService.removerViveiro(usuario,fazendaUser, idViveiro);
            logger.info("Viveiro removido com sucesso: fazenda={}, viveiro={}", idFazenda, idViveiro);
            ctx.sessionAttribute("info", "Viveiro removido com sucesso!");
            ctx.redirect("/fazenda/" + idFazenda);
        } catch (Exception e) {
            logger.error("Erro ao remover viveiro fazenda={}, viveiro={}: {}", idFazenda, idViveiro, e.getMessage(), e);
            ctx.sessionAttribute("erro", "Erro ao remover viveiro: " + e.getMessage());
            ctx.redirect("/fazenda/" + idFazenda);
        }
    }


}