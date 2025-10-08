package com.aquatrack.fazenda;

import com.aquatrack.exceptions.LimiteDeFazendasException;
import com.aquatrack.racao.TipoRacao;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FazendaController {

    private static final Logger logger = LogManager.getLogger(FazendaController.class);

    private final UsuarioService usuarioService;
    private final FazendaService fazendaService;

    public FazendaController(UsuarioService usuarioService, FazendaService fazendaService) {
        this.usuarioService = usuarioService;
        this.fazendaService = fazendaService;
    }

    public void mostrarFormularioFazenda(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
        ctx.attribute("usuario", usuario);
        ctx.render("/fazendas/formulario_fazendas.html");
    }

    public void cadastrarFazenda(Context ctx) {
        try {
            Usuario usuario = ctx.sessionAttribute("usuario");
            assert usuario != null;
            ctx.attribute("usuario", usuario);
            if (usuarioService.contaFazendasUsuarios(usuario) >= 3) {
                throw new LimiteDeFazendasException("Você atingiu seu limite de 3 fazendas. Para adicionar uma nova, será necessário liberar espaço.");
            }

            String nome = ctx.formParam("nome");
            String localizacao = ctx.formParam("localizacao");

            if (nome == null || nome.trim().isEmpty()) throw new IllegalArgumentException("O nome da fazenda é obrigatório.");
            if (localizacao == null || localizacao.trim().isEmpty()) throw new IllegalArgumentException("A localização da fazenda é obrigatória.");

            Fazenda fazenda = new Fazenda(nome, localizacao);
            usuarioService.adicionarFazendaAoUsuario(usuario, fazenda);

            logger.info("Fazenda cadastrada: nome={}, localizacao={}", nome, localizacao);
            ctx.redirect("/fazendas");

        } catch (LimiteDeFazendasException e) {
            logger.warn("Cadastro de fazenda negado: {}", e.getMessage());
            ctx.attribute("erro", e.getMessage());
            ctx.render("/fazendas/formulario_fazendas.html");
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao cadastrar fazenda: {}", e.getMessage());
            ctx.attribute("erro", e.getMessage());
            ctx.render("/fazendas/formulario_fazendas.html");
        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar fazenda", e);
            ctx.attribute("erro", "Não foi possível cadastrar a fazenda. Tente novamente.");
            ctx.render("/fazendas/formulario_fazendas.html");
        }
    }

    public void removerFazenda(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        try {
            usuarioService.removerFazendaDoUsuario(usuario.getId(), idFazenda);
            logger.info("Fazenda com id={} removida com sucesso.", idFazenda);
            ctx.sessionAttribute("info", "Fazenda removida com sucesso!");
            ctx.redirect("/fazendas");
        } catch (Exception e) {
            logger.error("Erro ao remover fazenda id={}: {}", idFazenda, e.getMessage(), e);
            ctx.sessionAttribute("erro", "Erro ao remover fazenda: " + e.getMessage());
            ctx.redirect("/fazendas");
        }
    }

    public void listarFazendas(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        logger.debug("Listando fazendas, total={}", usuarioService.listarFazendasDoUsuario(usuario.getId()).size());
        ctx.attribute("fazendas", usuarioService.listarFazendasDoUsuario(usuario.getId()));
        ctx.attribute("existemFazendas", usuarioService.contaFazendasUsuarios(usuario));
        ctx.render("/fazendas/listar_fazenda.html");
    }

    public void abrirFazenda(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        String idFazenda = ctx.pathParam("id");
        ctx.attribute("usuario", usuario);
        try {
            Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(),idFazenda);
            if (fazenda != null && !fazenda.isDeletado()) {
                double estoqueEngorda = fazendaService.consultarEstoquePorTipo(fazenda, TipoRacao.ENGORDA);
                double estoqueCrescimento = fazendaService.consultarEstoquePorTipo(fazenda, TipoRacao.CRESCIMENTO);

                ctx.attribute("estoqueEngorda", estoqueEngorda);
                ctx.attribute("estoqueCrescimento", estoqueCrescimento);
                ctx.attribute("fazenda", fazenda);
                ctx.attribute("viveiros", fazendaService.listarViveiros(fazenda));

                logger.info("Abrindo fazenda: id={}, nome={}", idFazenda, fazenda.getNome());
                ctx.render("/fazendas/pagina_fazenda.html");
            } else {
                logger.warn("Tentativa de acesso a fazenda inexistente ou deletada: id={}", idFazenda);
                ctx.status(404).result("Fazenda não encontrada");
            }
        } catch (Exception e) {
            logger.error("Erro ao abrir fazenda id={}", idFazenda, e);
            ctx.status(500).result("Erro interno ao abrir a fazenda.");
        }
    }

}
