package com.aquatrack;
import com.aquatrack.biometria.BiometriaController;
import com.aquatrack.cicloViveiro.CicloViveiroController;
import com.aquatrack.cicloViveiro.CicloViveiroService;
import com.aquatrack.fazenda.FazendaController;
import com.aquatrack.fazenda.FazendaService;
import com.aquatrack.funcionario.FuncionarioFazendaController;
import com.aquatrack.instrucoes.InstrucaoController;
import com.aquatrack.qualidadeDeAgua.QualidadeAguaController;
import com.aquatrack.racao.RacaoController;
import com.aquatrack.relatorio.RelatorioFinalController;
import com.aquatrack.usuario.TipoUsuario;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioController;
import com.aquatrack.usuario.UsuarioService;
import com.aquatrack.viveiro.ViveiroController;
import com.aquatrack.viveiro.ViveiroService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Consumer;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    private static final int PORTA_PADRAO = 8000;

    private static final String PROP_PORTA_SERVIDOR = "porta.servidor";

    private final Properties propriedades;

    public App() {
        this.propriedades = carregarPropriedades();
    }

    private Properties carregarPropriedades() {
        Properties prop = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("application.properties")) {
            if(input == null){
                logger.error("Arquivo de propriedades /src/main/resources/application.properties não encontrado");
                logger.error("Use o arquivo application.properties.examplo como base para criar o arquivo application.properties");
                System.exit(1);
            }
            prop.load(input);
        } catch (IOException ex) {
            logger.error("Erro ao carregar o arquivo de propriedades /src/main/resources/application.properties", ex);
            System.exit(1);
        }
        return prop;
    }

    private int obterPortaServidor() {
        if (propriedades.containsKey(PROP_PORTA_SERVIDOR)) {
            try {
                return Integer.parseInt(propriedades.getProperty(PROP_PORTA_SERVIDOR));
            } catch (NumberFormatException e) {
                logger.error("Porta definida no arquivo de propriedades não é um número válido: '{}'", propriedades.getProperty(PROP_PORTA_SERVIDOR));
                System.exit(1);
            }
        } else {
            logger.info("Porta não definida no arquivo de propriedades, utilizando porta padrão {}", PORTA_PADRAO);
        }
        return PORTA_PADRAO;
    }

    private TemplateEngine configurarThymeleaf() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

    private void registrarServicos(JavalinConfig config) {
        config.appData(Keys.USUARIO_SERVICE.key(), new UsuarioService());
    }

    private void configureJavalin(JavalinConfig config) {
        TemplateEngine templateEngine = configurarThymeleaf();

        // cria a pasta uploads se não existir
        Path pastaUploads = Paths.get("uploads");
        try {
            if (!Files.exists(pastaUploads)) {
                Files.createDirectories(pastaUploads);
            }
        } catch (IOException e) {
            logger.error("Erro ao criar diretório de uploads", e);
        }

        // arquivos internos (resources/public)
        config.staticFiles.add(staticFileConfig -> {
            staticFileConfig.directory = "/public";
            staticFileConfig.location = Location.CLASSPATH;
        });

        // arquivos externos (uploads/)
        config.staticFiles.add(staticFiles -> {
            staticFiles.hostedPath = "/uploads";
            staticFiles.directory = "uploads";
            staticFiles.location = Location.EXTERNAL;
        });

        config.fileRenderer(new JavalinThymeleaf(templateEngine));
    }



    private Javalin inicializarJavalin() {
        int porta = obterPortaServidor();
        logger.info("Iniciando aplicação na porta {}", porta);
        Consumer<JavalinConfig> configConsumer = this::configureJavalin;
        return Javalin.create(configConsumer).start(porta);
    }


    private void configurarPaginasDeErro(Javalin app) {
        app.error(404, ctx -> ctx.render("erro_404.html"));
        app.error(500, ctx -> ctx.render("erro_500.html"));
    }

    public void configurarRotas(Javalin app) {
        // ====== Repositório ======
        UsuarioRepository usuarioRepository = new UsuarioRepository();

        // ====== Services ======
        UsuarioService usuarioService = new UsuarioService();
        FazendaService fazendaService = new FazendaService(usuarioRepository);
        ViveiroService viveiroService = new ViveiroService(usuarioRepository);
        CicloViveiroService cicloViveiroService = new CicloViveiroService(usuarioRepository);


        // ====== Controllers ======
        LoginController loginController = new LoginController(usuarioService);
        MasterController masterController = new MasterController(usuarioService);
        UsuarioController usuarioController = new UsuarioController(usuarioService);
        FazendaController fazendaController = new FazendaController(usuarioService, fazendaService);
        FuncionarioFazendaController funcionarioFazendaController = new FuncionarioFazendaController(usuarioService, fazendaService);
        ViveiroController viveiroController = new ViveiroController(usuarioService, fazendaService, viveiroService);
        InstrucaoController instrucaoController = new InstrucaoController(usuarioService, fazendaService, viveiroService);
        CicloViveiroController cicloViveiroController = new CicloViveiroController(usuarioService, fazendaService, viveiroService);
        RacaoController racaoController = new RacaoController(usuarioService, fazendaService, cicloViveiroService);
        BiometriaController biometriaController = new BiometriaController(fazendaService, usuarioService, cicloViveiroService);
        QualidadeAguaController qualidadeAguaController = new QualidadeAguaController(fazendaService, usuarioService, cicloViveiroService);
        RelatorioFinalController relatorioFinalController = new RelatorioFinalController(fazendaService, cicloViveiroService, usuarioService, viveiroService);

        // ===== Cria Usuário Master ====
         usuarioService.criaUsuarioMaster();

        // ====== Rotas ======

        //Middleware que vai verificar quais rotas não precisam de loggin
        app.before(ctx -> {
            String path = ctx.path();
            //Aqui é definido quais rotas são públicas e não precisam de login
            boolean rotaPublica =
                    path.equals("/") || path.equals("/login")  || path.equals("/usuarios/novo") || path.equals("/logout") ||
                            path.equals("/usuarios/cadastrar") || path.equals("/usuarios/signup") ||
                            path.equals("/contato")|| path.startsWith("/images") || path.startsWith("/uploads");
            if (!rotaPublica && ctx.sessionAttribute("usuario") == null) {
                ctx.redirect("/login");
            }
        });

        //Middleware específico para master
        app.before("/master/*", ctx ->{
            Usuario usuario = ctx.sessionAttribute("usuario");
            if (usuario == null || usuario.getTipoUsuario() != TipoUsuario.MASTER) {
                ctx.status(403).result("Acesso negado - apenas usuário master");
            }
        });

        // Login
        app.get("/", ctx -> ctx.redirect("/login"));
        app.get("/login", loginController::mostrarPaginaLogin);
        app.post("/login", loginController::processarLogin);
        app.get("/logout", loginController::logout);
        app.get("/primeiro-login", loginController::mostrarPaginaPrimeiroLogin);
        app.post("primeiro-login", loginController::setPrimeiraSenha);

        // Usuário
        app.get("/usuario", usuarioController::paginaUsuario);
        app.post("/usuario/editar", usuarioController::editarUsuario);
        app.post("/usuario/alterarSenha", usuarioController::editarSenha);
        app.post("/usuario/editarFoto", usuarioController::editarFoto);
        app.post("/usuario/removerFoto", usuarioController::removerFoto);


        // Master
        app.get("/master", masterController::mostrarPaginaMaster);
        app.get("/master/cadastrar", masterController::mostrarFormulario_signup);
        app.post("/master/cadastrar", masterController::cadastrarUsuario);
        app.post("master/removerUsuario", masterController::removerUsuario);

        // Fazenda
        app.get("/fazendas", fazendaController::listarFazendas);
        app.get("/fazendas/nova", fazendaController::mostrarFormularioFazenda);
        app.post("/fazendas/criar", fazendaController::cadastrarFazenda);
        app.post("/fazenda/{id}/remover", fazendaController::removerFazenda);
        app.get("/fazenda/{id}", fazendaController::abrirFazenda);

        //Funcionario
        app.get("/fazenda/{id}/cadastrar-funcionario", funcionarioFazendaController::mostrarFormularioFuncionario);
        app.post("/fazenda/{id}/cadastrar-funcionario", funcionarioFazendaController::cadastrarFuncionario);

        // Viveiro
        app.get("/fazenda/{id}/cadastrar-viveiro", viveiroController::mostrarFormularioViveiro);
        app.post("/fazenda/{id}/cadastrar-viveiro", viveiroController::cadastrarViveiro);
        app.post("/fazenda/{id}/viveiro/{idViveiro}/remover", viveiroController::removerViveiro);
        app.get("/fazenda/{id}/viveiro/{idViveiro}/abrirViveiro", viveiroController::abrirViveiro);

        // Instruções
        app.get("/fazendas/{id}/viveiros/{idViveiro}/instrucoes", instrucaoController::listarInstrucoes);
        app.get("/fazendas/{id}/viveiros/{idViveiro}/instrucoes/nova", instrucaoController::abrirFormularioNovaInstrucao);
        app.post("/fazendas/{id}/viveiros/{idViveiro}/instrucoes/nova", instrucaoController::criarInstrucao);
        app.get("/fazendas/{id}/viveiros/{idViveiro}/instrucoes/{idInstrucao}", instrucaoController::visualizarInstrucao);
        app.get("/fazendas/{id}/viveiros/{idViveiro}/instrucoes/{idInstrucao}/editar", instrucaoController::abrirFormularioEditarInstrucao);
        app.post("/fazendas/{id}/viveiros/{idViveiro}/instrucoes/{idInstrucao}/editar", instrucaoController::editarInstrucao);
        app.post("/fazendas/{id}/viveiros/{idViveiro}/instrucoes/{idInstrucao}/remover", instrucaoController::removerInstrucao);

        // Ração
        app.get("/fazenda/{id}/abastecer-racao", racaoController::mostrarFormularioAdicionarRacao);
        app.post("/fazenda/{id}/abastecer-racao", racaoController::adicionarRacao);
        app.get("/fazenda/{id}/viveiro/{idViveiro}/consumir-racao", racaoController::mostrarFormularioConsumirRacao);
        app.post("/fazenda/{id}/viveiro/{idViveiro}/consumir-racao", racaoController::consumirRacao);

        // Ciclo do Viveiro
        app.get("/fazendas/{id}/viveiro/{idViveiro}/formulario_ciclo_viveiro", cicloViveiroController::mostrarFormularioCicloViveiro);
        app.post("/fazendas/{id}/viveiro/{idViveiro}/formulario_ciclo_viveiro", cicloViveiroController::iniciarCiclo);
        app.get("/fazendas/{id}/viveiro/{idViveiro}/ciclo/finalizar", cicloViveiroController::finalizarCiclo);

        // Relatório
        app.post("/fazenda/{id}/viveiro/{idViveiro}/ciclo/finalizar", relatorioFinalController::fecharRelatorio);
        app.get("/fazenda/{id}/viveiro/{idViveiro}/relatorio_viveiro", relatorioFinalController::listarRelatorios);
        app.get("/fazenda/{id}/viveiro/{idViveiro}/relatorios/{dataDaVenda}/pdf", relatorioFinalController::downloadPdf);

        // Biometria
        app.get("/fazendas/{id}/viveiro/{idViveiro}/formulario_biometria", biometriaController::mostrarFormularioBiometria);
        app.post("/fazendas/{id}/viveiro/{idViveiro}/formulario_biometria", biometriaController::atualizaBiometria);
        app.get("/fazendas/{id}/viveiro/{idViveiro}/historico_biometria", biometriaController::historicoBiometria);

        // Qualidade de Água
        app.get("/fazenda/{id}/viveiro/{idViveiro}/formulario_qualidade_de_agua", qualidadeAguaController::mostrarFormularioQualidadeAgua);
        app.post("/fazenda/{id}/viveiro/{idViveiro}/formulario_qualidade_de_agua", qualidadeAguaController::atualizaQualidadeAgua);
        app.get("/fazenda/{id}/viveiro/{idViveiro}/historico_agua", qualidadeAguaController::historicoQualidadeAgua);
    }

    public void iniciar() {
        Javalin app = inicializarJavalin();
        configurarPaginasDeErro(app);
        configurarRotas(app);
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Erro não tratado", e);
            ctx.status(500);
        });
    }

    public static void main(String[] args) {
        try {
            new App().iniciar();
        } catch (Exception e) {
            logger.error("Erro ao iniciar a aplicação", e);
            System.exit(1);
        }
    }
}
