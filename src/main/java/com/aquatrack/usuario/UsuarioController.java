package com.aquatrack.usuario;

import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioController {

    private static final Logger logger = LogManager.getLogger(UsuarioController.class);
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void mostrarFormularioCadastro(Context ctx) {
        logger.debug("Abrindo formulário de cadastro de usuário");
        ctx.render("/login/formulario_fazendas.html");
    }

    public void mostrarFormulario_signup(Context ctx) {
        logger.debug("Abrindo formulário de cadastrar usuário");
        ctx.render("/login/formulario_usuario_novo.html");
    }

    public void removerUsuario(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            usuarioService.removerUsuario(id);
            logger.info("Usuário com id={} removido com sucesso.", id);
            ctx.sessionAttribute("info", "Usuário removido com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao remover usuário com id={}: {}", id, e.getMessage(), e);
            ctx.sessionAttribute("erro", "Erro ao remover usuário: " + e.getMessage());
        }
    }

    public void cadastrarUsuario(Context ctx) {
        String nome = ctx.formParam("nome");
        String email = ctx.formParam("login");
        String senha = BCrypt.hashpw(ctx.formParam("senha"), BCrypt.gensalt());
        TipoUsuario tipoUsuario = TipoUsuario.valueOf(ctx.formParam("tipoUsuario"));

        boolean signup = (ctx.formParam("signup") != null);
        String formSignup = "/login/formulario_usuario_novo.html";
        String formCadastro = "/login/formulario_usuario_novo.html";

        try {
            logger.info("Tentativa de cadastro de usuário: {}", email);

            if (usuarioService.buscarUsuarioPorLogin(email) != null) {
                logger.warn("Cadastro falhou: já existe usuário com o email {}", email);
                ctx.attribute("erro", "Já existe um usuário com o email cadastrado: " + email);
                ctx.render(signup ? formSignup : formCadastro);
                return;
            }

            usuarioService.cadastrarUsuario(new Usuario(email, nome, senha, tipoUsuario));
            logger.info("Usuário cadastrado {} com sucesso: {}", tipoUsuario, email);

            ctx.attribute("info", "Usuário cadastrado com sucesso!");
            ctx.render("/login/login.html");

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar usuário: {}", email, e);
            ctx.attribute("erro", "Ocorreu um erro ao cadastrar o usuário. Tente novamente.");
            ctx.render(signup ? formSignup : formCadastro);
        }
    }

}
