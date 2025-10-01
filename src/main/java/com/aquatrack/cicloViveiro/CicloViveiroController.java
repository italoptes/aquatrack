package com.aquatrack.cicloViveiro;

import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.fazenda.FazendaService;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import com.aquatrack.viveiro.Viveiro;
import com.aquatrack.viveiro.ViveiroService;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CicloViveiroController{

    private static final Logger logger = LogManager.getLogger(CicloViveiroController.class);

    private final UsuarioService usuarioService;
    private final FazendaService fazendaService;
    private final ViveiroService viveiroService;

    public CicloViveiroController(UsuarioService usuarioService,
                                  FazendaService fazendaService,
                                  ViveiroService viveiroService) {
        this.usuarioService = usuarioService;
        this.fazendaService = fazendaService;
        this.viveiroService = viveiroService;
    }

    public void mostrarFormularioCicloViveiro(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);
        logger.debug("Abrindo formulário de ciclo do viveiro: fazenda={}, viveiro={}", idFazenda, idViveiro);
        ctx.render("ciclo/formulario_ciclo_viveiro.html");
    }

    public void iniciarCiclo(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        try {
            String laboratorio = ctx.formParam("laboratorio");
            String quantidadeParam = ctx.formParam("quantidadePopulacaoPovoada");
            String diaVenda = ctx.formParam("dia");
            String mesVenda = ctx.formParam("mes");
            String anoVenda = ctx.formParam("ano");
            String dataPovoamentoStr = diaVenda + "/" + mesVenda + "/" + anoVenda;


            if (dataPovoamentoStr == null || dataPovoamentoStr.isBlank()) {
                ctx.attribute("erro", "A data da venda deve ser informada.");
                ctx.render("fazendas/formulario_ciclo_viveiro.html");
                return;
            }

            // Usa o parseData para aceitar vários formatos
            LocalDate dataPovoamento = parseData(dataPovoamentoStr);
            if (laboratorio == null) throw new NullPointerException("O laboratório não pode ser nulo");
            if (quantidadeParam == null) throw new IllegalArgumentException("Quantidade da população povoada deve ser informada.");

            int quantPopulacao = Integer.parseInt(quantidadeParam);
            if (quantPopulacao <= 0) throw new IllegalArgumentException("Quantidade deve ser um valor positivo.");
            Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazendaUser, idViveiro);
            viveiroService.abrirCiclo(usuario, viveiro, dataPovoamento, quantPopulacao, laboratorio);
            logger.info("Ciclo de viveiro iniciado com sucesso: fazenda={}, viveiro={}, laboratorio={}, quantidade={}",
                    idFazenda, idViveiro, laboratorio, quantPopulacao);
            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/abrirViveiro");
        } catch (NumberFormatException e) {
            logger.warn("Entrada inválida para ciclo do viveiro: fazenda={}, viveiro={}, erro={}", idFazenda, idViveiro, e.getMessage());
            ctx.attribute("erro", "Quantidade deve ser numérica.");
            ctx.render("ciclo/formulario_ciclo_viveiro.html");
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao iniciar ciclo: fazenda={}, viveiro={}, erro={}", idFazenda, idViveiro, e.getMessage());
            ctx.attribute("erro", e.getMessage());
            ctx.render("ciclo/formulario_ciclo_viveiro.html");
        } catch (Exception e) {
            logger.error("Erro inesperado ao iniciar ciclo: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.attribute("erro", "Ocorreu um erro ao iniciar o ciclo. Tente novamente.");
            ctx.render("ciclo/formulario_ciclo_viveiro.html");
        }
    }

    public void finalizarCiclo(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        try {
            ctx.attribute("idFazenda", idFazenda);
            ctx.attribute("idViveiro", idViveiro);
            logger.info("Preparando finalização de ciclo: fazenda={}, viveiro={}", idFazenda, idViveiro);
            ctx.render("relatorio/formulario_relatorio.html");
        } catch (Exception e) {
            logger.error("Erro ao preparar finalização do ciclo: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.attribute("erro", "Não foi possível abrir o formulário de relatório.");
            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/abrirViveiro");
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
}
