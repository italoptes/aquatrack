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

    public void mostrarPaginaPrimeiroLogin(Context ctx) {
        String teste = ctx.queryParam("teste");
        if (teste != null) {
            throw new RuntimeException("Erro de teste a partir do /login?teste=1");
        }
        ctx.render("/login/pagina_primeiro_login.html");
    }

    public void processarLogin(Context ctx) {
        String login = ctx.formParam("login");
        String senha = ctx.formParam("senha");

        Usuario usuario = usuarioService.buscarUsuarioPorLogin(login);

        if (usuario != null && BCrypt.checkpw(senha, usuario.getSenha())) {
            ctx.sessionAttribute("usuario", usuario);
            logger.info("Usuário '{}' autenticado com sucesso.", login);

            if (usuario.isPrimeiroLogin()) {
                ctx.redirect("/primeiro-login");
            } else {
                if (usuario.getTipoUsuario() == TipoUsuario.MASTER) {
                    ctx.redirect("/master");
                } else {
                    ctx.redirect("/fazendas");
                }
            }
        } else {
            logger.warn("Tentativa de login falhou para o usuário: {}", login);
            ctx.attribute("erro", "Usuário ou senha inválidos");
            ctx.render("/login/login.html");
        }
    }

    public void setPrimeiraSenha (Context ctx){
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) throw new IllegalArgumentException("Usuário inválido.");

        String nova = ctx.formParam("senha");
        String confirmar = ctx.formParam("confirmarSenha");

        try {
            if (!usuario.isPrimeiroLogin()) {
                ctx.redirect("/login");
                return;
            }

            usuarioService.setSenhaPrimeiroLogin(usuario, nova, confirmar);

            // reaproveitar o método do LoginController (ou duplicar aqui)
            ctx.sessionAttribute("usuario", usuario);

            logger.info("Primeiro login finalizado e senha alterada por '{}'", usuario.getLogin());

            logger.info("Usuário '{}' deslogado para fazer login com a nova senha", usuario.getLogin());
            ctx.sessionAttribute("usuario", null);

            ctx.attribute("info", "Senha definitiva definida com sucesso! Faça login para confirmar");
            ctx.render("/login/login.html");

        } catch (IllegalArgumentException e) {
            ctx.attribute("erro", e.getMessage());
            ctx.render("/login/primeiro_login.html");
        } catch (Exception e) {
            logger.error("Erro ao definir senha no primeiro login", e);
            ctx.attribute("erro", "Não foi possível definir sua senha. Tente novamente.");
            ctx.render("/login/primeiro_login.html");
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
