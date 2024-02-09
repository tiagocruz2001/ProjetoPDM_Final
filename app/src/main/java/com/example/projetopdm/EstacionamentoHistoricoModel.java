package com.example.projetopdm;

import com.google.gson.annotations.SerializedName;

public class EstacionamentoHistoricoModel {

    @SerializedName("Id")
    private int id;

    @SerializedName("HoraEntrada")
    private String horaEntrada;

    @SerializedName("HoraSaida")
    private String horaSaida;

    @SerializedName("Estado")
    private String estado;

    @SerializedName("UtilizadorId")
    private int utilizadorId;

    @SerializedName("Preco")
    private double preco; // Adicione o novo campo

    // Adicione os getters conforme necessário

    public int getId() {
        return id;
    }

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public String getHoraSaida() {
        return horaSaida;
    }

    public String getEstado() {
        return estado;
    }

    public int getUtilizadorId() {
        return utilizadorId;
    }

    public double getPreco() {
        return preco;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }
}
