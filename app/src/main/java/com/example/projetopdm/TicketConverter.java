package com.example.projetopdm;

public class TicketConverter {

    public static TicketHistoricoItem convertFromTicketModel(TicketModel ticketModel) {
        return new TicketHistoricoItem(
                ticketModel.getId(),
                ticketModel.getDescricao(),
                ticketModel.getImage(),
                ticketModel.getData(),
                ticketModel.getEstacionamentoId(),
                ticketModel.getEstado(),
                ticketModel.getHoraEntrada(),
                ticketModel.getHoraSaida()

        );
    }
}

