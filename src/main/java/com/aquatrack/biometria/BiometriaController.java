package com.aquatrack.biometria;

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

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BiometriaController {
    private static final Logger logger = LogManager.getLogger(BiometriaController.class);

    private final FazendaService fazendaService;
    private final UsuarioService usuarioService;
    private final CicloViveiroService cicloViveiroService;

    public BiometriaController(FazendaService fazendaService,
                               UsuarioService usuarioService, CicloViveiroService ciclo) {
        this.usuarioService = usuarioService;
        this.fazendaService = fazendaService;
        this.cicloViveiroService = ciclo;
    }

    public void mostrarFormularioBiometria(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        logger.debug("Abrindo formulário de biometria: fazenda={}, viveiro={}", idFazenda, idViveiro);
        ctx.render("biometria/formulario_biometria.html");
    }

    public void atualizaBiometria(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        try {
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

            String qtdParam = ctx.formParam("quantidade");
            String pesoParam = ctx.formParam("pesoTotal");
            if (qtdParam == null || pesoParam == null) {
                throw new IllegalArgumentException("Quantidade e peso devem ser informados.");
            }

            int quantidadeAmostra = Integer.parseInt(qtdParam);
            double pesoTotalAmostra = Double.parseDouble(pesoParam);
            if (quantidadeAmostra <= 0 || pesoTotalAmostra <= 0) {
                throw new IllegalArgumentException("Quantidade e peso devem ser valores positivos.");
            }

            Fazenda fazendaUser = usuario.getFazendaPorId(idFazenda);
            Viveiro viveiro = fazendaService.getViveiro(fazendaUser, idViveiro);
            CicloViveiro cicloViveiro = viveiro.ultimoCiclo();
            cicloViveiroService.registrarBiometria(usuario,cicloViveiro,new Biometria(quantidadeAmostra, pesoTotalAmostra, dataColeta));
            logger.info("Biometria registrada com sucesso: fazenda={}, viveiro={}, qtd={}, peso={}",
                    idFazenda, idViveiro, quantidadeAmostra, pesoTotalAmostra);
            ctx.redirect("/fazenda/" + idFazenda + "/viveiro/" + idViveiro + "/abrirViveiro");

        } catch (NumberFormatException e) {
            logger.warn("Entrada inválida para biometria: fazenda={}, viveiro={}, erro={}", idFazenda, idViveiro, e.getMessage());
            ctx.attribute("erro", "Quantidade e peso devem ser numéricos.");
            ctx.render("biometria/formulario_biometria.html");

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação em biometria: fazenda={}, viveiro={}, erro={}", idFazenda, idViveiro, e.getMessage());
            ctx.attribute("erro", e.getMessage());
            ctx.render("biometria/formulario_biometria.html");

        } catch (Exception e) {
            logger.error("Erro inesperado ao cadastrar biometria: fazenda={}, viveiro={}", idFazenda, idViveiro, e);
            ctx.attribute("erro", "Ocorreu um erro ao salvar a biometria. Tente novamente.");
            ctx.render("biometria/formulario_biometria.html");
        }
    }

    public void historicoBiometria(Context ctx) {
        String idFazenda = ctx.pathParam("id");
        String idViveiro = ctx.pathParam("idViveiro");
        Usuario usuario = ctx.sessionAttribute("usuario");
        assert usuario != null;
        ctx.attribute("usuario", usuario);
        Fazenda fazenda = usuarioService.buscarFazendaPorId(usuario.getId(), idFazenda);
        Viveiro viveiro = fazendaService.getViveiro(fazenda, idViveiro);
        if (viveiro == null) {
            logger.warn("Tentativa de acessar histórico de biometria em viveiro inexistente: fazenda={}, viveiro={}", idFazenda, idViveiro);
            ctx.status(404).result("Viveiro não encontrado");
            return;
        }
        CicloViveiro cicloViveiro = viveiro.ultimoCiclo();
        List<Biometria> historico = cicloViveiro.getHistoricoBiometria();
        DecimalFormat df = new DecimalFormat("#0.00");
        List<Map<String, Object>> historicoFormatado = new ArrayList<>();
        double pesoAnterior = 0.0;
        for (int i = 0; i < historico.size(); i++) {
            Biometria b = historico.get(i);
            double pesoMedio = b.calculaBiometria();
            double diferenca = (i == 0) ? 0.0 : pesoMedio - pesoAnterior;
            historicoFormatado.add(Map.of(
                    "data", b.getDataColeta(),
                    "pesoMedio", df.format(pesoMedio),
                    "diferenca", df.format(diferenca)
            ));
            pesoAnterior = pesoMedio;
        }
        logger.info("Histórico de biometria carregado: fazenda={}, viveiro={}, totalRegistros={}", idFazenda, idViveiro, historico.size());
        ctx.attribute("idFazenda", idFazenda);
        ctx.attribute("idViveiro", idViveiro);
        ctx.attribute("viveiro", viveiro);
        ctx.attribute("historicoBiometria", historicoFormatado);
        ctx.render("biometria/historico_biometria.html");
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

