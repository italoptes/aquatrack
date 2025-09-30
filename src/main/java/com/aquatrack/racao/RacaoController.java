package com.aquatrack.racao;

import com.aquatrack.cicloViveiro.CicloViveiro;
import com.aquatrack.cicloViveiro.CicloViveiroService;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.fazenda.FazendaController;
import com.aquatrack.fazenda.FazendaService;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import com.aquatrack.viveiro.Viveiro;
import com.aquatrack.viveiro.ViveiroService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RacaoController {
    private static final Logger logger = LogManager.getLogger(RacaoController.class);

    private final UsuarioService usuarioService;
    private final FazendaService fazendaService;
    private final CicloViveiroService cicloViveiroService;

    public RacaoController(UsuarioService usuarioService, FazendaService fazendaService, CicloViveiroService cicloViveiroService) {
        this.usuarioService = usuarioService;
        this.fazendaService = fazendaService;
        this.cicloViveiroService = cicloViveiroService;
    }

    public void mostrarFormularioAdicionarRacao(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        ctx.attribute("idFazenda", idFazenda);
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);

        double estoqueEngorda = fazendaService.consultarEstoquePorTipo(fazenda, TipoRacao.ENGORDA);
        double estoqueCrescimento = fazendaService.consultarEstoquePorTipo(fazenda, TipoRacao.CRESCIMENTO);

        ctx.attribute("estoqueEngorda", estoqueEngorda);
        ctx.attribute("estoqueCrescimento", estoqueCrescimento);

        logger.debug("Abrindo formulário de adicionar ração: fazenda={}", idFazenda);
        ctx.render("racao/formulario_adicionar_racao.html");
    }

    public void mostrarFormularioConsumirRacao(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);

        Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);
        double estoqueEngorda = fazendaService.consultarEstoquePorTipo(fazenda, TipoRacao.ENGORDA);
        double estoqueCrescimento = fazendaService.consultarEstoquePorTipo(fazenda, TipoRacao.CRESCIMENTO);

        ctx.attribute("estoqueEngorda", estoqueEngorda);
        ctx.attribute("estoqueCrescimento", estoqueCrescimento);

        logger.debug("Abrindo formulário para consumir ração: fazenda={}, viveiro={}", idFazenda, idViveiro);
        ctx.render("/racao/formulario_consumir_racao.html");
    }

    public void adicionarRacao(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        ctx.attribute("idFazenda", idFazenda);
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);
        try {
            TipoRacao tipoRacao = TipoRacao.valueOf(ctx.formParam("tipoRacao").toUpperCase());
            double quantidade = Double.parseDouble(ctx.formParam("quantidade"));
            if (quantidade <= 0) throw new IllegalArgumentException("A quantidade deve ser maior que zero.");

            fazendaService.adicionarRacao(fazenda, tipoRacao, quantidade);
            logger.info("Ração adicionada com sucesso: fazenda={}, tipo={}, quantidade={}", idFazenda, tipoRacao, quantidade);
            ctx.redirect("/fazenda/" + idFazenda);

        } catch (NumberFormatException e) {
            logger.warn("Entrada inválida no cadastro de ração: fazenda={}, erro={}", idFazenda, e.getMessage());
            ctx.attribute("erro", "A quantidade deve ser um número válido.");
            ctx.render("ciclo/formulario_adicionar_racao.html");

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao adicionar ração: fazenda={}, erro={}", idFazenda, e.getMessage());
            ctx.attribute("erro", e.getMessage());
            ctx.render("ciclo/formulario_adicionar_racao.html");

        } catch (Exception e) {
            logger.error("Erro inesperado ao adicionar ração: fazenda={}", idFazenda, e);
            ctx.attribute("erro", "Não foi possível adicionar a ração. Tente novamente.");
            ctx.render("ciclo/formulario_adicionar_racao.html");
        }
    }

    public void consumirRacao(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);
        Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);
        CicloViveiro cicloViveiro = viveiro.ultimoCiclo();

        double estoqueEngorda = fazendaService.consultarEstoquePorTipo(fazenda, TipoRacao.ENGORDA);
        double estoqueCrescimento = fazendaService.consultarEstoquePorTipo(fazenda, TipoRacao.CRESCIMENTO);
        ctx.attribute("estoqueEngorda", estoqueEngorda);
        ctx.attribute("estoqueCrescimento", estoqueCrescimento);
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);

        try {
            TipoRacao tipoRacao = TipoRacao.valueOf(ctx.formParam("tipoRacao").toUpperCase());
            double quantidade = Double.parseDouble(ctx.formParam("quantidade"));
            if (quantidade <= 0) throw new IllegalArgumentException("A quantidade deve ser maior que zero.");

            cicloViveiroService.registrarConsumoRacao(cicloViveiro, tipoRacao, quantidade);
            logger.info("Ração consumida com sucesso: fazenda={}, viveiro={}, tipo={}, quantidade={}", idFazenda, idViveiro, tipoRacao, quantidade);
            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/abrirViveiro");

        } catch (NumberFormatException e) {
            logger.warn("Entrada inválida ao consumir ração: fazenda={}, viveiro={}, erro={}", idFazenda, idViveiro, e.getMessage());
            ctx.attribute("erro", "A quantidade deve ser um número válido.");
            ctx.render("/racao/formulario_consumir_racao.html");

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao consumir ração: fazenda={}, viveiro={}, erro={}", idFazenda, idViveiro, e.getMessage());
            ctx.attribute("erro", e.getMessage());
            ctx.render("/racao/formulario_consumir_racao.html");

        } catch (Exception e) {
            logger.error("Erro inesperado ao consumir ração: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.attribute("erro", "Não foi possível registrar o consumo de ração.");
            ctx.render("/racao/formulario_consumir_racao.html");
        }
    }
}
