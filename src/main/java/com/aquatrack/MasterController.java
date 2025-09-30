package com.aquatrack;

import com.aquatrack.usuario.TipoUsuario;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class MasterController {
    private static final Logger logger = LogManager.getLogger(MasterController.class);
    private UsuarioService usuarioService;

    public MasterController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void mostrarPaginaMaster(Context ctx) {
        ctx.attribute("usuarios", usuarioService.listarUsuarios());
        ctx.render("master/pagina_master.html");
    }

    public void mostrarFormulario_signup(Context ctx) {
        logger.debug("Abrindo formulário de cadastrar usuário");
        ctx.render("master/formulario_usuario_novo.html");
    }

    public void cadastrarUsuario(Context ctx) {
        String nome = ctx.formParam("nome");
        String email = ctx.formParam("login");
        String senha = ctx.formParam("senha");
        String confirmarSenha = ctx.formParam("confirmarSenha");
        TipoUsuario tipoUsuario = TipoUsuario.valueOf(ctx.formParam("tipoUsuario"));

        try {
            logger.info("Tentativa de cadastro de usuário: {}", email);

            if (!senha.equals(confirmarSenha)) {
                logger.warn("Cadastro falhou: senhas não coincidem para o email {}", email);
                ctx.attribute("erro", "As senhas não coincidem.");
                ctx.render("master/formulario_usuario_novo.html");
                return;
            }

            if (usuarioService.buscarUsuarioPorLogin(email) != null) {
                logger.warn("Cadastro falhou: já existe usuário com o email {}", email);
                ctx.attribute("erro", "Já existe um usuário com o email cadastrado: " + email);
                ctx.render("master/formulario_usuario_novo.html");
                return;
            }

            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
            usuarioService.cadastrarUsuario(new Usuario(email, nome, senhaHash, tipoUsuario));
            logger.info("Usuário cadastrado {} com sucesso: {}", tipoUsuario, email);

            ctx.sessionAttribute("info", "Usuário cadastrado com sucesso!");
            ctx.redirect("/master");

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar usuário: {}", email, e);
            ctx.attribute("erro", "Ocorreu um erro ao cadastrar o usuário. Tente novamente.");
            ctx.render("master/formulario_usuario_novo.html");
        }
    }

    public void removerUsuario(Context ctx) {
        String id = ctx.formParam("id");
        try {
            usuarioService.removerUsuario(id);
            logger.info("Usuário removido com sucesso. ID: {}", id);
            ctx.sessionAttribute("info", "Usuário removido com sucesso!");
            ctx.redirect("/master");
        } catch (Exception e) {
            logger.error("Erro ao remover usuário ID {}: {}", id, e.getMessage(), e);
            ctx.sessionAttribute("erro", "Erro ao remover usuário: " + e.getMessage());
            ctx.redirect("/master");
        }
    }
}
