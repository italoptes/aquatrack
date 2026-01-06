package com.aquatrack.custo;

import com.aquatrack.cicloViveiro.CicloViveiro;
import com.aquatrack.cicloViveiro.CicloViveiroService;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.fazenda.FazendaService;
import com.aquatrack.usuario.Usuario;
import com.aquatrack.usuario.UsuarioService;
import com.aquatrack.viveiro.Viveiro;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustoController {

    private static final Logger logger = LogManager.getLogger(CustoController.class);

    private final FazendaService fazendaService;
    private final UsuarioService usuarioService;
    private final CicloViveiroService cicloViveiroService;

    public CustoController(
            FazendaService fazendaService,
            UsuarioService usuarioService,
            CicloViveiroService cicloViveiroService
    ) {
        this.fazendaService = fazendaService;
        this.usuarioService = usuarioService;
        this.cicloViveiroService = cicloViveiroService;
    }

    // ================= FORMULÁRIO =================

    public void mostrarFormularioCusto(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");

        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        ctx.attribute("usuario", usuario);
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);

        logger.debug("Abrindo formulário de custo: fazenda={}, viveiro={}", idFazenda, idViveiro);
        ctx.render("custo/formulario_custo.html");
    }

    // ================= ADICIONAR =================

    public void adicionarCusto(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");

        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        try {
            Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazendaUser, idViveiro);
            CicloViveiro ciclo = viveiro.ultimoCiclo();

            String nome = ctx.formParam("nome");
            double valor = Double.parseDouble(ctx.formParam("valor"));

            String dia = ctx.formParam("dia");
            String mes = ctx.formParam("mes");
            String ano = ctx.formParam("ano");
            String dataStr = dia + "/" + mes + "/" + ano;

            LocalDate data = parseData(dataStr);

            cicloViveiroService.adicionarCusto(usuario, ciclo, nome, valor, data);

            logger.info("Custo adicionado com sucesso: fazenda={}, viveiro={}, nome={}, valor={}",
                    idFazenda, idViveiro, nome, valor);

            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/abrirViveiro");

        } catch (NumberFormatException e) {
            logger.warn("Valor inválido ao adicionar custo: {}", e.getMessage());
            ctx.attribute("erro", "O valor do custo deve ser numérico.");
            ctx.render("custo/formulario_custo.html");

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao adicionar custo: {}", e.getMessage());
            ctx.attribute("erro", e.getMessage());
            ctx.render("custo/formulario_custo.html");

        } catch (Exception e) {
            logger.error("Erro inesperado ao adicionar custo", e);
            ctx.attribute("erro", "Erro ao salvar o custo. Tente novamente.");
            ctx.render("custo/formulario_custo.html");
        }
    }


    // ================= REMOVER =================

    public void removerCusto(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        String idCusto   = ctx.pathParam("idCusto");

        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        try {
            Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazendaUser, idViveiro);
            CicloViveiro ciclo = viveiro.ultimoCiclo();

            cicloViveiroService.removerCusto(usuario, ciclo, idCusto);

            logger.info("Custo removido (inativado): id={}", idCusto);

            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/abrirViveiro");

        } catch (Exception e) {
            logger.error("Erro ao remover custo", e);
            ctx.attribute("erro", "Erro ao remover custo.");
            ctx.render("custo/formulario_custo.html");
        }
    }

    // ================= HISTÓRICO =================

    public void listarCustos(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");

        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;

        Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
        Viveiro viveiro = fazendaService.getViveiro(fazendaUser, idViveiro);
        CicloViveiro ciclo = viveiro.ultimoCiclo();

        ctx.attribute("usuario", usuario);
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);
        ctx.attribute("custos", ciclo.getCustos());
        ctx.attribute("totalCustos", ciclo.getTotalCustos());

        ctx.render("custo/historico_custo.html");
    }

    // ================= DATA =================

    private LocalDate parseData(String input) {
        DateTimeFormatter[] formatos = {
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy")
        };

        for (DateTimeFormatter f : formatos) {
            try {
                return LocalDate.parse(input, f);
            } catch (Exception ignore) {}
        }
        throw new IllegalArgumentException("Formato de data inválido: " + input);
    }
}
