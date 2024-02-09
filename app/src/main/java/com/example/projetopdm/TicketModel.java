package com.example.projetopdm;

import com.google.gson.annotations.SerializedName;

public class TicketModel {

    @SerializedName("Id")
    private long id;

    @SerializedName("Descricao")
    private String descricao;

    @SerializedName("Image")
    private String image;

    @SerializedName("Data")
    private String data;

    @SerializedName("EstacionamentoId")
    private long estacionamentoId;

    @SerializedName("Estado")
    private String estado;

    @SerializedName("Horaentrada")
    private String horaEntrada;

    @SerializedName("Horasaida")
    private String horaSaida;

    // Construtor vazio necessário para o Gson
    public TicketModel() {
    }

    // Construtor com parâmetros
    public TicketModel(String descricao, String image, long estacionamentoId, String estado) {
        this.descricao = descricao;
        this.image = image;
        this.estacionamentoId = estacionamentoId;
        this.estado = estado;
    }

    public TicketModel(String descricao, String imagemBase64, int estacionamentoId) {
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getData() {
        return data;
    }

    public String getEstado() {
        return estado;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getEstacionamentoId() {
        return estacionamentoId;
    }



    public String getHoraEntrada() {
        return horaEntrada;
    }

    public String getHoraSaida() {
        return horaSaida;
    }

    public TicketHistoricoItem toTicketHistoricoItem() {
        return new TicketHistoricoItem(id, descricao, image, data, estacionamentoId, estado,horaEntrada, horaSaida);
    }
}
