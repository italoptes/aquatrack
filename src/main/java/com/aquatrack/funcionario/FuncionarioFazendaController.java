package com.aquatrack.funcionario;

import com.aquatrack.Funcao;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.fazenda.FazendaService;
import com.aquatrack.usuario.TipoUsuario;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class FuncionarioFazendaController {
    private static final Logger logger = LogManager.getLogger(FuncionarioFazendaController.class);

    private final UsuarioService usuarioService;
    private final FazendaService fazendaService;

    public FuncionarioFazendaController(UsuarioService usuarioService, FazendaService fazendaService) {
        this.usuarioService = usuarioService;
        this.fazendaService = fazendaService;
    }

    public void mostrarFormularioFuncionario(Context ctx){
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        String idFazenda = ctx.pathParam("id");
        ctx.attribute("idFazenda", idFazenda);
        logger.debug("Abrindo formulário de cadastro de funcionário para fazenda={}", idFazenda);
        ctx.render("/funcionario/formulario_funcionario.html");
    }

    public void cadastrarFuncionario(Context ctx) {
        Usuario usuarioDono = ctx.sessionAttribute("usuario");
        if (usuarioDono == null) {
            throw new IllegalArgumentException("Usuário inválido ou não autenticado.");
        }

        String idFazenda = ctx.pathParam("id");
        Fazenda fazenda = usuarioDono.getFazendaPorId(idFazenda);

        if (fazenda == null) {
            ctx.attribute("erro", "Fazenda não encontrada.");
            ctx.render("/funcionario/formulario_funcionario.html");
            return;
        }

        // Campos do formulário
        String nome = ctx.formParam("nome");
        String email = ctx.formParam("login");
        String senha = ctx.formParam("senha");
        String confirmarSenha = ctx.formParam("confirmarSenha");
        List<String> funcoesSelecionadas = ctx.formParams("funcoes");

        try {
            logger.info("Tentativa de cadastro de funcionário '{}' para a fazenda '{}'", email, fazenda.getNome());

            // === Validações básicas ===
            if (!senha.equals(confirmarSenha)) {
                logger.warn("Falha: senhas não coincidem para o funcionário '{}'", email);
                ctx.attribute("erro", "As senhas não coincidem.");
                ctx.render("/funcionario/formulario_funcionario.html");
                return;
            }

            if (usuarioService.buscarUsuarioPorLogin(email) != null) {
                logger.warn("Falha: já existe um usuário com o email '{}'", email);
                ctx.attribute("erro", "Já existe um usuário com o email cadastrado: " + email);
                ctx.render("/funcionario/formulario_funcionario.html");
                return;
            }

            // === Criação do novo usuário e funcionário ===
            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
            Usuario novoUsuario = new Usuario(email, nome, senhaHash, TipoUsuario.FUNCIONARIO);
            FuncionarioFazenda funcionario = new FuncionarioFazenda(novoUsuario, fazenda);

            // === Atribui as funções selecionadas ===
            if (funcoesSelecionadas != null && !funcoesSelecionadas.isEmpty()) {
                funcoesSelecionadas.forEach(f -> {
                    try {
                        funcionario.addFuncao(Funcao.valueOf(f)); // converte String para Enum
                    } catch (IllegalArgumentException e) {
                        logger.warn("Função inválida ignorada: {}", f);
                    }
                });
            }

            // === Persiste os dados ===
            fazendaService.addFuncionario(usuarioDono, fazenda, funcionario);
            logger.info("Funcionário '{}' cadastrado com sucesso na fazenda '{}'", email, fazenda.getNome());

            ctx.sessionAttribute("info", "Funcionário cadastrado com sucesso!");
            ctx.redirect("/fazenda/" + idFazenda);

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar funcionário '{}': {}", email, e.getMessage(), e);
            ctx.attribute("erro", "Ocorreu um erro ao cadastrar o funcionário. Tente novamente.");
            ctx.render("/funcionario/formulario_funcionario.html");
        }
    }

}
