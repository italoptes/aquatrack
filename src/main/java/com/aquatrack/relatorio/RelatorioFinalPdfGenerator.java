package com.aquatrack.relatorio;

import com.aquatrack.fazenda.Fazenda;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.jetbrains.annotations.NotNull;
import projeto.grupo04.fazenda.Fazenda;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class RelatorioFinalPdfGenerator {

    //Gera o pdf
    public byte[] gerarPdf(RelatorioFinal relatorio, Fazenda fazenda) {
        try {
            // Cria documento A4 em memória
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            // Abre o documento para escrita
            document.open();

            // ---------- FONTES ----------
            Fontes fonte = getFontes();

            // ---------- LOGO ----------
            logo(document);

            // ---------- TÍTULO ----------
            titulo(relatorio, fonte, document);

            // ---------- IDENTIFICAÇÃO ----------
            identificacao("Fazenda: " + fazenda.getNome(), fonte.subtitleFont(), document, "Viveiro: " + relatorio.getViveiro().getId(), fonte);

            // ---------- TABELA 1 ----------
            tabela1(relatorio, fonte, document);

            // ---------- TABELA 2 ----------
            tabela2(relatorio, fonte, document);

            // ---------- TABELA 3 ----------
            tabela3(relatorio, fonte, document);

            // ---------- RODAPÉ ----------
            rodape("©2025 AquaTrack", fonte.subtitleFont(), document);

            // Fecha o documento
            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void rodape(String string, Font fonte, Document document) {
        Paragraph rodape = new Paragraph(string, fonte);
        rodape.setAlignment(Element.ALIGN_CENTER);
        document.add(rodape);
    }

    private void tabela3(RelatorioFinal relatorio, Fontes fonte, Document document) {
        PdfPTable table3 = new PdfPTable(3);
        table3.setWidthPercentage(100);
        addHeaderCell(table3, "Biomassa Final (kg)", fonte.headerFont());
        addHeaderCell(table3, "Biometria Final (g)", fonte.headerFont());
        addHeaderCell(table3, "Consumo Ração (kg)", fonte.headerFont());

        addValueCell(table3, String.valueOf(relatorio.getBiomassaFinal()), fonte.valueFont());
        addValueCell(table3, String.valueOf(relatorio.getBiometriaFinal()), fonte.valueFont());
        addValueCell(table3, String.valueOf(relatorio.getConsumoTotalRacao()), fonte.valueFont());

        document.add(table3);
        document.add(Chunk.NEWLINE);
    }

    private void tabela2(RelatorioFinal relatorio, Fontes fonte, Document document) {
        PdfPTable table2 = new PdfPTable(3);
        table2.setWidthPercentage(100);
        addHeaderCell(table2, "Dias de Cultivo", fonte.headerFont());
        addHeaderCell(table2, "FCA", fonte.headerFont());
        addHeaderCell(table2, "Sobrevivência (%)", fonte.headerFont());

        addValueCell(table2, String.valueOf(relatorio.getDiasDeCultivo()), fonte.valueFont());
        addValueCell(table2, String.format("%.2f", relatorio.getFca()), fonte.valueFont());
        addValueCell(table2, String.format("%.2f", relatorio.getSobrevivenciaCultivo()), fonte.valueFont());

        document.add(table2);
        document.add(Chunk.NEWLINE);
    }

    private void tabela1(RelatorioFinal relatorio, Fontes fonte, Document document) {
        PdfPTable table1 = new PdfPTable(3);
        table1.setWidthPercentage(100);
        addHeaderCell(table1, "Data Povoamento", fonte.headerFont());
        addHeaderCell(table1, "Quantidade Povoada", fonte.headerFont());
        addHeaderCell(table1, "Laboratório", fonte.headerFont());

        addValueCell(table1, relatorio.getDataPovoamento() != null ?
                relatorio.getDataPovoamento().toString() : "-", fonte.valueFont());
        addValueCell(table1, String.valueOf(relatorio.getQuantidadePovoada()), fonte.valueFont());
        addValueCell(table1, relatorio.getLaboratorio(), fonte.valueFont());

        document.add(table1);
        document.add(Chunk.NEWLINE);
    }

    private static void identificacao(String fazenda, Font fonte, Document document, String relatorio, Fontes fonte1) {
        rodape(fazenda, fonte, document);

        rodape(relatorio, fonte1.subtitleFont(), document);

        document.add(Chunk.NEWLINE);
    }

    private static void logo(Document document) throws IOException {
        Image logo = Image.getInstance("src/main/resources/public/images/logo.png");
        logo.scaleToFit(120, 60); // redimensiona logo
        logo.setAlignment(Element.ALIGN_CENTER); // centraliza
        document.add(logo);
        document.add(Chunk.NEWLINE);
    }

    private static void titulo(RelatorioFinal relatorio, Fontes fonte, Document document) {
        identificacao("Relatório Final Ciclo Viveiro", fonte.titleFont(), document, "Data da Venda: " + relatorio.getDataDaVenda(), fonte);
    }

    @NotNull
    private static Fontes getFontes() {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Fontes fonte = new Fontes(titleFont, subtitleFont, headerFont, valueFont);
        return fonte;
    }

    private record Fontes(Font titleFont, Font subtitleFont, Font headerFont, Font valueFont) {
    }

    //Cria uma célula de cabeçalho centralizada para a tabela.
    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    //Cria uma célula de valor centralizada para a tabela.
    private void addValueCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "-", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
