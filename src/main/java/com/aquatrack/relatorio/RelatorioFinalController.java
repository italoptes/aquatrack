package com.aquatrack.relatorio;

import com.aquatrack.cicloViveiro.CicloViveiro;
import com.aquatrack.cicloViveiro.CicloViveiroService;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.fazenda.FazendaService;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import com.aquatrack.viveiro.Viveiro;
import com.aquatrack.viveiro.ViveiroService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RelatorioFinalController {
    private static final Logger logger = LogManager.getLogger(RelatorioFinalController.class);

    private final FazendaService fazendaService;
    private final UsuarioService usuarioService;
    private final CicloViveiroService cicloViveiroService;
    private final RelatorioFinalPdfGenerator relatorioFinalPdfGenerator;
    private final ViveiroService viveiroService;

    // Construtor com injeção de dependências


    public RelatorioFinalController(FazendaService fazendaService, CicloViveiroService cicloViveiroService, UsuarioService usuarioService, ViveiroService viveiroService) {
        this.fazendaService = fazendaService;
        this.cicloViveiroService = cicloViveiroService;
        this.usuarioService = usuarioService;
        this.relatorioFinalPdfGenerator = new RelatorioFinalPdfGenerator();
        this.viveiroService = viveiroService;
    }

    public void listarRelatorios(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);
        Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);
        try {

            if (viveiro == null) {
                throw new IllegalArgumentException("Viveiro não encontrado para o ID: " + idViveiro);
            }

            // Pega apenas os relatórios ativos
            ctx.attribute("relatorios", viveiro.relatoriosFinais());
            ctx.attribute("idFazenda", idFazenda);
            ctx.attribute("idViveiro", idViveiro);
            ctx.attribute("viveiro", viveiro);

            ctx.render("relatorio/relatorios_viveiro.html");

        } catch (Exception e) {
            logger.error("Erro ao listar relatórios: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.attribute("erro", "Não foi possível carregar os relatórios.");
            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/abrirViveiro");
        }
    }


    public void fecharRelatorio(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
        Viveiro viveiro = fazendaService.getViveiro(fazendaUser, idViveiro);
        CicloViveiro cicloViveiro = viveiro.ultimoCiclo();
        try {
            double biometriaFinal = Double.parseDouble(ctx.formParam("biometriaFinal"));
            double biomassaFinal = Double.parseDouble(ctx.formParam("biomassaFinal"));

            String diaVenda = ctx.formParam("dia");
            String mesVenda = ctx.formParam("mes");
            String anoVenda = ctx.formParam("ano");
            String dataDaVendaStr = diaVenda + "/" + mesVenda + "/" + anoVenda;


            if (dataDaVendaStr == null || dataDaVendaStr.isBlank()) {
                ctx.attribute("erro", "A data da venda deve ser informada.");
                ctx.render("relatorio/formulario_relatorio.html");
                return;
            }

            // Usa o parseData para aceitar vários formatos
            LocalDate dataDaVenda = parseData(dataDaVendaStr);

            cicloViveiroService.gerarRelatorioFinal(usuario, cicloViveiro,  biometriaFinal, biomassaFinal, dataDaVenda);
            viveiroService.encerrarCiclo(usuario,viveiro, cicloViveiro.getDataPovoamento().toString());
            logger.info("Relatório gerado e ciclo finalizado: fazenda={}, viveiro={}, data={}", idFazenda, idViveiro, dataDaVenda);
            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/abrirViveiro");

        } catch (NumberFormatException e) {
            ctx.attribute("erro", "Biometria e biomassa devem ser valores numéricos.");
            ctx.render("relatorio/formulario_relatorio.html");
        } catch (Exception e) {
            logger.error("Erro ao fechar relatório: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.attribute("erro", "Erro ao gerar relatório.");
            ctx.render("relatorio/formulario_relatorio.html");
        }
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


    public void downloadPdf(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        String dataDaVendaStr = ctx.pathParam("dataDaVenda");
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);
        try {
            Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);
            CicloViveiro cicloViveiro = viveiro.ultimoCiclo();
            if (viveiro == null) {
                throw new IllegalArgumentException("Viveiro não encontrado para o ID: " + idViveiro);
            }

            // Pega o relatorio
            RelatorioFinal relatorioFinal = cicloViveiro.getRelatorioFinal();
            if (relatorioFinal == null) {
                throw new IllegalArgumentException("Relatório não encontrado para a data: " + dataDaVendaStr);
            }

            // Gera o PDF com os dados do relatório já fechado
            byte[] pdf = relatorioFinalPdfGenerator.gerarPdf(relatorioFinal, fazenda,viveiro);

            if (pdf == null) {
                throw new IllegalStateException("Falha ao gerar PDF. Byte array retornou nulo.");
            }

            // Configura resposta HTTP
            ctx.contentType("application/pdf");
            ctx.header("Content-Disposition", "inline; filename=relatorio-" + idViveiro + "-" + dataDaVendaStr + ".pdf");
            ctx.result(new ByteArrayInputStream(pdf));

            logger.info("PDF gerado com sucesso: fazenda={}, viveiro={}, data={}", idFazenda, idViveiro, dataDaVendaStr);

        } catch (Exception e) {
            logger.error("Erro ao gerar PDF: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.attribute("erro", "Não foi possível gerar o relatório em PDF.");
            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/relatorios");
        }
    }

}
