package com.aquatrack.qualidadeDeAgua;

import java.time.LocalDate;

public class QualidadeDeAgua {
    private LocalDate dataColeta;
    private double amonia;
    private double nitrito;
    private double ph;
    private double alcalinidade;
    private double salinidade;
    private double oxigenio;

    public QualidadeDeAgua() {
        this.dataColeta = null;
    }

    public QualidadeDeAgua(double amonia, double nitrito, double ph, double alcalinidade, double salinidade, double oxigenio, LocalDate dataColeta) {
        this.dataColeta = dataColeta;
        this.amonia = amonia;
        this.nitrito = nitrito;
        this.ph = ph;
        this.alcalinidade = alcalinidade;
        this.salinidade = salinidade;
        this.oxigenio = oxigenio;
    }

    public boolean verificaValores() {
        boolean amoniaOk = this.amonia >= 0;
        boolean nitritoOk = this.nitrito >= 0;
        boolean phOk = this.ph >= 0 && this.ph <= 14; // pH só pode estar entre 0 e 14
        boolean alcalinidadeOk = this.alcalinidade >= 0;
        boolean salinidadeOk = this.salinidade >= 0;
        boolean oxigenioOk = this.oxigenio >= 0;

        return amoniaOk && nitritoOk && phOk && alcalinidadeOk && salinidadeOk && oxigenioOk;
    }

    public void atualizaQualidadeDeAgua(double amonia, double nitrito, double ph,
                                        double alcalinidade, double salinidade, double oxigenio, LocalDate dataColeta) {
        this.amonia = amonia;
        this.nitrito = nitrito;
        this.ph = ph;
        this.alcalinidade = alcalinidade;
        this.salinidade = salinidade;
        this.oxigenio = oxigenio;
        this.dataColeta = dataColeta;
        verificaValores();
    }

    public double getAmonia() {
        return amonia;
    }

    public void setAmonia(double amonia) {
        this.amonia = amonia;
    }

    public double getNitrito() {
        return nitrito;
    }

    public void setNitrito(double nitrito) {
        this.nitrito = nitrito;
    }

    public double getPh() {
        return ph;
    }

    public void setPh(double ph) {
        this.ph = ph;
    }

    public double getAlcalinidade() {
        return alcalinidade;
    }

    public void setAlcalinidade(double alcalinidade) {
        this.alcalinidade = alcalinidade;
    }

    public double getSalinidade() {
        return salinidade;
    }

    public void setSalinidade(double salinidade) {
        this.salinidade = salinidade;
    }

    public double getOxigenio() {
        return oxigenio;
    }

    public void setOxigenio(double oxigenio) {
        this.oxigenio = oxigenio;
    }

    public String getDataColeta() {
        return dataColeta.toString();
    }

    public boolean valoresValidos() {
        return amonia >= 0 && nitrito >= 0 && ph > 0 && alcalinidade >= 0 && salinidade >= 0 && oxigenio >= 0;
    }

}

