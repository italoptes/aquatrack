package com.aquatrack.instrucoes;

import com.aquatrack.cicloViveiro.CicloViveiro;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.fazenda.FazendaService;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import com.aquatrack.viveiro.Viveiro;
import com.aquatrack.viveiro.ViveiroService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstrucaoController {

    private static final Logger logger = LogManager.getLogger(InstrucaoController.class);
    private final UsuarioService usuarioService;
    private final FazendaService fazendaService;
    private final ViveiroService viveiroService;

    public InstrucaoController(UsuarioService usuarioService, FazendaService fazendaService, ViveiroService viveiroService) {
        this.usuarioService = usuarioService;
        this.fazendaService = fazendaService;
        this.viveiroService = viveiroService;
    }


    public void listarInstrucoes(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");

        try {
            Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);
            CicloViveiro cicloViveiro = viveiro.ultimoCiclo();

            if (viveiro != null && !viveiro.isDeletado()) {
                List<Instrucao> instrucoes = viveiroService.listarInstrucoes(viveiro);

                ctx.attribute("usuario", usuario);
                ctx.attribute("idFazenda", idFazenda);
                ctx.attribute("idViveiro", idViveiro);
                ctx.attribute("instrucoes", instrucoes);
                ctx.attribute("cicloViveiro", cicloViveiro);

                logger.info("Listando instruções: fazenda={}, viveiro={}, total={}", idFazenda, idViveiro, instrucoes.size());
                ctx.render("instrucoes/pagina_instrucoes.html");
            } else {
                ctx.status(404).result("Viveiro não encontrado.");
            }
        } catch (Exception e) {
            logger.error("Erro ao listar instruções do viveiro: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.status(500).result("Erro ao listar instruções.");
        }
    }


    public void criarInstrucao(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        try {
            Fazenda fazenda = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);

            String titulo = ctx.formParam("titulo");
            String descricao = ctx.formParam("descricao");

            Instrucao instrucao = new Instrucao(titulo, descricao, usuario.getNome());
            viveiroService.criarInstrucao(usuario, viveiro, instrucao);
            logger.info("Nova instrução criada: fazenda={}, viveiro={}, título={}", idFazenda, idViveiro, titulo);
            ctx.redirect("/fazendas/" + idFazenda + "/viveiros/" + idViveiro + "/instrucoes");
        } catch (Exception e) {
            logger.error("Erro ao criar instrução: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.status(500).result("Erro ao criar instrução.");
        }
    }


    public void abrirFormularioNovaInstrucao(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");

        ctx.attribute("usuario", usuario);
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);
        ctx.render("instrucoes/modal_nova_instrucao.html");
    }


    public void visualizarInstrucao(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        String idInstrucao = ctx.pathParam("idInstrucao");

        try {
            Fazenda fazenda = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);
            Instrucao instrucao = viveiro.buscarInstrucao(idInstrucao);

            ctx.attribute("instrucao", instrucao);
            ctx.attribute("idFazenda", idFazenda);
            ctx.attribute("idViveiro", idViveiro);
            ctx.attribute("usuario", usuario);

            logger.info("Visualizando instrução {} do viveiro {}", idInstrucao, idViveiro);
            ctx.render("instrucoes/modal_visualizar_instrucao.html");
        } catch (Exception e) {
            logger.error("Erro ao visualizar instrução {}", idInstrucao, e);
            ctx.status(500).result("Erro ao visualizar instrução.");
        }
    }

    public void abrirFormularioEditarInstrucao(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        String idInstrucao = ctx.pathParam("idInstrucao");

        try {
            Fazenda fazenda = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);
            Instrucao instrucao = viveiro.buscarInstrucao(idInstrucao);

            Map<String, Object> model = new HashMap<>();
            model.put("usuario", usuario);
            model.put("fazenda", fazenda);
            model.put("viveiro", viveiro);
            model.put("instrucao", instrucao);
            model.put("idFazenda", idFazenda);
            model.put("idViveiro", idViveiro);

            logger.info("Abrindo formulário de edição para instrução {} no viveiro {}", idInstrucao, idViveiro);
            ctx.render("instrucoes/modal_editar_instrucao.html", model);
        } catch (Exception e) {
            logger.error("Erro ao abrir formulário de edição da instrução {}", idInstrucao, e);
            ctx.status(500).result("Erro ao abrir o formulário de edição.");
        }
    }


    public void editarInstrucao(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        String idInstrucao = ctx.pathParam("idInstrucao");

        try {
            Fazenda fazenda = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);

            String novoTitulo = ctx.formParam("titulo");
            String novaDescricao = ctx.formParam("descricao");
            Status novoStatus = Status.valueOf(ctx.formParam("status").toUpperCase());

            viveiroService.editarInstrucao(usuario, viveiro, idInstrucao, novoTitulo, novaDescricao, novoStatus);

            logger.info("Instrução {} editada com sucesso.", idInstrucao);
            ctx.redirect("/fazendas/" + idFazenda + "/viveiros/" + idViveiro+ "/instrucoes");
        } catch (Exception e) {
            logger.error("Erro ao editar instrução {}", idInstrucao, e);
            ctx.status(500).result("Erro ao editar instrução.");
        }
    }

    public void removerInstrucao(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        String idInstrucao = ctx.pathParam("idInstrucao");

        try {
            Fazenda fazenda = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);

            viveiroService.removerInstrucao(usuario, viveiro, idInstrucao);

            logger.info("Instrução {} removida com sucesso.", idInstrucao);
            ctx.redirect("/fazendas/" + idFazenda + "/viveiros/" + idViveiro + "/instrucoes");
        } catch (Exception e) {
            logger.error("Erro ao remover instrução {}", idInstrucao, e);
            ctx.status(500).result("Erro ao remover instrução.");
        }
    }
}
