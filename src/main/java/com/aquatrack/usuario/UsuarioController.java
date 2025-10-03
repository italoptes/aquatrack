package com.aquatrack.usuario;

import com.aquatrack.fazenda.ResumoFazendaDTO;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

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

    public void paginaUsuario(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
        ctx.attribute("usuario", usuario);

        List<ResumoFazendaDTO> resumoFazendaDTOS = usuarioService.gerarResumoFazendas(usuario);
        ctx.attribute("resumo", resumoFazendaDTOS);

        String msg = ctx.sessionAttribute("msg");
        if (msg != null) {
            ctx.attribute("msg", msg);
            ctx.sessionAttribute("msg", null); // limpa da sessão depois de usar
        }

        ctx.render("/usuario/pagina_usuario.html");
    }

    public void editarUsuario(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }

        String nome = ctx.formParam("nome");
        String email = ctx.formParam("email");

        try {
            if (nome != null && !nome.isBlank()) {
                usuarioService.editarNome(usuario, nome);
            }

            if (email != null && !email.isBlank()) {
                usuarioService.editarEmail(usuario, email);
            }

            ctx.sessionAttribute("usuario", usuario);
            ctx.sessionAttribute("msg", "Dados alterados com sucesso!");
            ctx.redirect("/usuario");
        } catch (Exception e) {
            logger.error("Erro ao editar usuário", e);
            ctx.attribute("erro", e.getMessage());
            ctx.render("/usuario/pagina_usuario.html");
        }
    }

    public void editarFoto(Context ctx){
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }

        UploadedFile foto = ctx.uploadedFile("foto");

        try {
            if (foto != null) {
                usuarioService.editarFoto(usuario, foto);
            }

            ctx.sessionAttribute("usuario", usuario);
            ctx.sessionAttribute("msg", "Foto alterada com sucesso!");
            ctx.redirect("/usuario");
        } catch (Exception e) {
            logger.error("Erro ao editar usuário", e);
            ctx.attribute("erro", e.getMessage());
            ctx.render("/usuario/pagina_usuario.html");
        }
    }

    public void removerFoto(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) throw new IllegalArgumentException("Usuário inválido.");

        try {
            usuarioService.removerFoto(usuario);
            // atualiza sessão para a view já cair na imagem padrão
            ctx.sessionAttribute("usuario", usuario);
            ctx.sessionAttribute("msg", "Foto removida.");
            ctx.redirect("/usuario");
        } catch (Exception e) {
            logger.error("Erro ao remover foto", e);
            ctx.attribute("erro", e.getMessage());
            ctx.render("/usuario/pagina_usuario.html");
        }
    }


    public void editarSenha(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }

        String senha = ctx.formParam("novaSenha");
        String confirmarSenha = ctx.formParam("confirmarSenha");

        try {
            ctx.attribute("usuario", usuario);
            usuarioService.editarSenha(usuario, senha, confirmarSenha);
            ctx.sessionAttribute("msg", "Senha alterada com sucesso!");
            ctx.redirect("/usuario");
        } catch (Exception e) {
            logger.error("Erro ao editar senha", e);
            ctx.attribute("erro", e.getMessage());
            ctx.render("/usuario/pagina_usuario.html");
        }
    }

}
