package com.example.projetopdm;

public class TicketHistoricoItem {

    private long id;
    private String descricao;
    private String image;
    private String data;
    private String estado;
    private long estacionamentoId;
    private String horaEntrada;
    private String horaSaida;

    public TicketHistoricoItem(long id, String descricao, String image, String data, long estacionamentoId, String estado, String horaEntrada, String horaSaida) {
        this.id = id;
        this.descricao = descricao;
        this.image = image;
        this.data = data;
        this.estacionamentoId = estacionamentoId;
        this.estado = estado;
        this.horaEntrada = horaEntrada;
        this.horaSaida = horaSaida;
    }
    public long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getImage() {
        return image;
    }

    public String getData() {
        return data;
    }

    public String getEstado() {
        return estado;
    }

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public String getHoraSaida() {
        return horaSaida;
    }

    public long getEstacionamentoId() {
        return estacionamentoId;
    }

    public int getTicketImage() {
        return R.drawable.car;
    }

    public String getTicketNumber() {
        return "Ticket " + getId();
    }

    public String getDescription() {
        return getDescricao();
    }

    public String getDateTime() {
        return getData();
    }

    public String getEstadoTicket() {
        return getEstado();
    }

    public String getTitle() {
        if (descricao != null && !descricao.isEmpty()) {
            String[] words = descricao.split(" ");
            if (words.length > 0) {
                return words[0];
            }
        }
        return "Sem Título";
    }
}