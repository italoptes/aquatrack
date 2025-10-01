package com.aquatrack;

import com.aquatrack.usuario.TipoUsuario;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);

    private final UsuarioService usuarioService;

    public LoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void mostrarPaginaLogin(Context ctx) {
        String teste = ctx.queryParam("teste");
        if (teste != null) {
            throw new RuntimeException("Erro de teste a partir do /login?teste=1");
        }
        ctx.render("/login/login.html");
    }

    public void processarLogin(Context ctx) {
        String login = ctx.formParam("login");
        String senha = ctx.formParam("senha");

        Usuario usuario = usuarioService.buscarUsuarioPorLogin(login);

        if (usuario != null && BCrypt.checkpw(senha, usuario.getSenha())) {
            ctx.sessionAttribute("usuario", usuario);
            logger.info("Usuário '{}' autenticado com sucesso.", login);

            if (usuario.getTipoUsuario() == TipoUsuario.MASTER) {
                ctx.redirect("/master");
            } else {
                ctx.redirect("/fazendas");
            }
        } else {
            logger.warn("Tentativa de login falhou para o usuário: {}", login);
            ctx.attribute("erro", "Usuário ou senha inválidos");
            ctx.render("/login/login.html");
        }
    }

    public void logout(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario != null) {
            logger.info("Usuário '{}' realizou logout.", usuario.getLogin());
        }
        ctx.sessionAttribute("usuario", null);
        ctx.redirect("/login");
    }
}
