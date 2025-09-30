package com.aquatrack;
import com.aquatrack.cicloViveiro.CicloViveiroService;
import com.aquatrack.fazenda.FazendaController;
import com.aquatrack.fazenda.FazendaService;
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
import java.util.Properties;
import java.util.function.Consumer;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    private static final int PORTA_PADRAO = 8000;

    //Propriedades do application.properties.exemplo:
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

        config.events(event -> {
            event.serverStarting(() -> {
                logger.info("Servidor Javalin está iniciando...");
                registrarServicos(config);
            });
            event.serverStopping(() -> {
            });
        });
        config.staticFiles.add(staticFileConfig -> {
            staticFileConfig.directory = "/public";
            staticFileConfig.location = Location.CLASSPATH;
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
        // ====== Repositórios ======
        UsuarioRepository usuarioRepository = new UsuarioRepository();

        // ====== Repositórios ======
        UsuarioService usuarioService = new UsuarioService();
        FazendaService fazendaService = new FazendaService();
        ViveiroService viveiroService = new ViveiroService();
        CicloViveiroService cicloViveiroService = new CicloViveiroService();

        // ====== Controllers ======
        LoginController loginController = new LoginController(usuarioService);
        MasterController masterController = new MasterController(usuarioService);
        UsuarioController usuarioController = new UsuarioController(usuarioService);
        FazendaController fazendaController = new FazendaController(usuarioService, fazendaService);
        ViveiroController viveiroController = new ViveiroController(usuarioService, viveiroService, fazendaService);

    }
}