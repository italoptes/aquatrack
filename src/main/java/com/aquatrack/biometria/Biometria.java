package com.aquatrack.biometria;

import java.time.LocalDate;

public class Biometria {
    private int quantidadeAmostra; // quantidade de camarões que participam da amostragem
    private double pesoTotalAmostra; //peso total em Gramas da soma dos camarões analisados
    private LocalDate dataColeta;
    //A biometria mostra o peso médio dos camarões de acordo com uma amostra

    public Biometria(int quantidadeAmostra, double pesoTotalAmostra, LocalDate dataColeta) {
        if (quantidadeAmostra < 0 || pesoTotalAmostra < 0) throw new IllegalArgumentException("A quantidade de amostra e o peso total devem ser maiores que zero.");
        this.quantidadeAmostra = quantidadeAmostra;
        this.pesoTotalAmostra = pesoTotalAmostra;
        this.dataColeta = dataColeta;
    }

    public Biometria() {
        this.dataColeta = null;
        this.quantidadeAmostra = 0;
        this.pesoTotalAmostra = 0;
    }

    public double calculaBiometria() {
        if (quantidadeAmostra == 0) {
            return 0.0;
        }
        if (quantidadeAmostra < 0) throw new IllegalStateException("Dados de biometria não definidos ou inválidos.");
        atualizarBiometria(quantidadeAmostra, pesoTotalAmostra, dataColeta);
        double media =pesoTotalAmostra / quantidadeAmostra;
        return Math.round(media*100.0) / 100.00; // Retorna o peso médio dos camarões analisados com 2 casas decimais
    }

    public void atualizarBiometria(int quantidadeDaAmostra, double pesoDaAmostra, LocalDate dataColeta) {
        if (quantidadeDaAmostra <= 0 || pesoDaAmostra <= 0) {
            throw new IllegalArgumentException("Os valores precisam ser superiores a 0.");
        }
        this.quantidadeAmostra = quantidadeDaAmostra;
        this.pesoTotalAmostra = pesoDaAmostra;
        this.dataColeta = dataColeta;
    }

    public int getQuantidadeAmostra() {
        return quantidadeAmostra;
    }

    public void setQuantidadeAmostra(int quantidadeAmostra) {
        if (quantidadeAmostra < 0) throw new IllegalArgumentException("A quantidade de amostra deve ser maior que zero.");
        this.quantidadeAmostra = quantidadeAmostra;
    }

    public double getPesoTotalAmostra() {
        return pesoTotalAmostra;
    }

    public void setPesoTotalAmostra(double pesoTotalAmostra) {
        if (pesoTotalAmostra < 0) throw new IllegalArgumentException("O peso total da amostra deve ser maior que zero.");
        this.pesoTotalAmostra = pesoTotalAmostra;
    }

    public String getDataColeta() {
        return dataColeta.toString();
    }

    public boolean isValida() {
        return quantidadeAmostra > 0 && pesoTotalAmostra > 0 && dataColeta != null;
    }
}


