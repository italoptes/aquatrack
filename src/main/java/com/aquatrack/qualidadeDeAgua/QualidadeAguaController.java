package com.aquatrack.qualidadeDeAgua;

import com.aquatrack.biometria.Biometria;
import com.aquatrack.cicloViveiro.CicloViveiro;
import com.aquatrack.cicloViveiro.CicloViveiroService;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.fazenda.FazendaController;
import com.aquatrack.fazenda.FazendaService;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import com.aquatrack.viveiro.Viveiro;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class QualidadeAguaController {
    private static final Logger logger = LogManager.getLogger(QualidadeAguaController.class);

    private final FazendaService fazendaService;
    private UsuarioService usuarioService;
    private CicloViveiroService cicloViveiroService;

    public QualidadeAguaController(FazendaService fazendaService, UsuarioService usuarioService, CicloViveiroService cicloViveiroService) {
        this.fazendaService = fazendaService;
        this.usuarioService = usuarioService;
        this.cicloViveiroService = cicloViveiroService;
    }

    public void mostrarFormularioQualidadeAgua(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);
        logger.debug("Abrindo formulário de qualidade de água: fazenda={}, viveiro={}", idFazenda, idViveiro);
        ctx.render("qualidadeDeAgua/formulario_qualidade_agua.html");
    }

    public void atualizaQualidadeAgua(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
        Viveiro viveiro = fazendaService.getViveiro(fazendaUser, idViveiro);
        CicloViveiro cicloViveiro = viveiro.ultimoCiclo();
        try {
            double amonia       = Double.parseDouble(ctx.formParam("amonia"));
            double nitrito      = Double.parseDouble(ctx.formParam("nitrito"));
            double ph           = Double.parseDouble(ctx.formParam("ph"));
            double alcalinidade = Double.parseDouble(ctx.formParam("alcalinidade"));
            double salinidade   = Double.parseDouble(ctx.formParam("salinidade"));
            double oxigenio     = Double.parseDouble(ctx.formParam("oxigenio"));

            String diaVenda = ctx.formParam("dia");
            String mesVenda = ctx.formParam("mes");
            String anoVenda = ctx.formParam("ano");
            String dataColetaStr = diaVenda + "/" + mesVenda + "/" + anoVenda;


            if (dataColetaStr == null || dataColetaStr.isBlank()) {
                ctx.attribute("erro", "A data da venda deve ser informada.");
                ctx.render("relatorio/formulario_relatorio.html");
                return;
            }
            // Usa o parseData para aceitar vários formatos
            LocalDate dataColeta = parseData(dataColetaStr);
            QualidadeDeAgua qualidadeAgua = new QualidadeDeAgua(amonia, nitrito, ph, alcalinidade, salinidade, oxigenio, dataColeta);
            cicloViveiroService.registrarQualidadeAgua(usuario, cicloViveiro, qualidadeAgua);
            logger.info("Qualidade de água registrada com sucesso: fazenda={}, viveiro={}", idFazenda, idViveiro);
            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/abrirViveiro");
        } catch (NumberFormatException e) {
            logger.warn("Entrada inválida em formulário de qualidade de água: fazenda={}, viveiro={}, erro={}", idFazenda, idViveiro, e.getMessage());
            ctx.attribute("erro", "Todos os parâmetros devem ser numéricos.");
            ctx.render("qualidadeDeAgua/formulario_qualidade_agua.html");
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação em qualidade de água: fazenda={}, viveiro={}, erro={}", idFazenda, idViveiro, e.getMessage());
            ctx.attribute("erro", e.getMessage());
            ctx.render("qualidadeDeAgua/formulario_qualidade_agua.html");
        } catch (Exception e) {
            logger.error("Erro inesperado ao registrar qualidade de água: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.attribute("erro", "Ocorreu um erro ao salvar a qualidade da água. Tente novamente.");
            ctx.render("qualidadeDeAgua/formulario_qualidade_agua.html");
        }
    }

    public void historicoQualidadeAgua(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);
        Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);
        CicloViveiro cicloViveiro = viveiro.ultimoCiclo();
        if (viveiro == null) {
            logger.warn("Tentativa de acessar histórico de qualidade de água em viveiro inexistente: fazenda={}, viveiro={}", idFazenda, idViveiro);
            ctx.status(404).result("Viveiro não encontrado");
            return;
        }

        logger.info("Histórico de qualidade de água carregado: fazenda={}, viveiro={}, totalRegistros={}", idFazenda, idViveiro, cicloViveiro.getHistoricoQualidade().size());
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);
        ctx.attribute("viveiro", viveiro);
        ctx.attribute("historicoQualidade", cicloViveiro.getHistoricoQualidade());
        ctx.render("qualidadeDeAgua/historico_qualidadeAgua.html");
    }

    //Faz o parse de datas em múltiplos formatos.
    private LocalDate parseData(String input) {
        DateTimeFormatter[] formatos = {
                DateTimeFormatter.ISO_LOCAL_DATE,              // yyyy-MM-dd (input type="date")
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),     // 08/11/2025
                DateTimeFormatter.ofPattern("d/M/yyyy")        // 8/11/2025
        };

        for (DateTimeFormatter f : formatos) {
            try {
                return LocalDate.parse(input, f);
            } catch (Exception ignore) {}
        }
        throw new IllegalArgumentException("Formato de data inválido: " + input);
    }
}