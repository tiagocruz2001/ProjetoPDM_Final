package com.example.projetopdm;

import com.google.gson.annotations.SerializedName;

public class EstacionamentoModel {

    @SerializedName("Id")
    private int id;

    @SerializedName("HoraEntrada")
    private String horaEntrada;

    @SerializedName("HoraSaida")
    private String horaSaida;

    @SerializedName("Estado")
    private String estado;

    @SerializedName("UtilizadorId")  // Adicionado campo UtilizadorId
    private int utilizadorId;

    @SerializedName("Preco")  // Adicionado campo Preco
    private double preco;

    // Construtor vazio
    public EstacionamentoModel() {
    }

    // Construtor com parâmetros
    public EstacionamentoModel(int id, String horaEntrada, String horaSaida, String estado, int utilizadorId, double preco) {
        this.id = id;
        this.horaEntrada = horaEntrada;
        this.horaSaida = horaSaida;
        this.estado = estado;
        this.utilizadorId = utilizadorId;
        this.preco = preco;
    }

    // Métodos getters/setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public String getHoraSaida() {
        return horaSaida;
    }

    public void setHoraSaida(String horaSaida) {
        this.horaSaida = horaSaida;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getUtilizadorId() {
        return utilizadorId;
    }

    public void setUtilizadorId(int utilizadorId) {
        this.utilizadorId = utilizadorId;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }
}
