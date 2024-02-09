package com.example.projetopdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EstacionamentoHistoricoAdapter extends RecyclerView.Adapter<EstacionamentoHistoricoAdapter.EstacionamentoHistoricoViewHolder> {

    private List<EstacionamentoHistoricoModel> estacionamentoEntries;

    public EstacionamentoHistoricoAdapter(List<EstacionamentoHistoricoModel> estacionamentoEntries) {
        this.estacionamentoEntries = estacionamentoEntries;
    }

    @NonNull
    @Override
    public EstacionamentoHistoricoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_estacionamento_entry, parent, false);
        return new EstacionamentoHistoricoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EstacionamentoHistoricoViewHolder holder, int position) {
        EstacionamentoHistoricoModel entry = estacionamentoEntries.get(position);

        // Configurar os TextViews com os dados da entrada
        holder.entryTimeTextView.setText("Entrada: " + formatDate(entry.getHoraEntrada()));

        // Verificar e configurar a hora de saída
        if (entry.getHoraSaida() != null) {
            holder.exitTimeTextView.setText("Saída: " + formatDate(entry.getHoraSaida()));

            // Calcular o total de horas
            long entradaMillis = convertToMillis(entry.getHoraEntrada());
            long saidaMillis = convertToMillis(entry.getHoraSaida());
            long diffMillis = saidaMillis - entradaMillis;

            long totalHoras = TimeUnit.MILLISECONDS.toHours(diffMillis);
            long totalMinutos = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % TimeUnit.HOURS.toMinutes(1);
            long totalSegundos = TimeUnit.MILLISECONDS.toSeconds(diffMillis) % TimeUnit.MINUTES.toSeconds(1);

            holder.textViewTotalHoras.setText(String.format("Total de Horas: %dh %dmin %ds", totalHoras, totalMinutos, totalSegundos));
        } else {
            holder.exitTimeTextView.setText("Saída: Ainda não saiu");
            holder.textViewTotalHoras.setText("Total de Horas: Ainda não saiu");
        }

        // Verificar e configurar o estado
        if (entry.getEstado() != null) {
            holder.textViewState.setText("Estado: " + entry.getEstado());

            // Definir a cor com base no estado
            int textColor;
            if ("Concluído".equals(entry.getEstado()) || "Concluido".equals(entry.getEstado())) {
                textColor = ContextCompat.getColor(holder.textViewState.getContext(), R.color.green);
            } else if ("Pendente".equals(entry.getEstado())) {
                textColor = ContextCompat.getColor(holder.textViewState.getContext(), R.color.orange);
            } else {
                textColor = ContextCompat.getColor(holder.textViewState.getContext(), android.R.color.black);
            }

            holder.textViewState.setTextColor(textColor);
        } else {
            holder.textViewState.setText("Estado: Pendente");
            holder.textViewState.setTextColor(ContextCompat.getColor(holder.textViewState.getContext(), R.color.orange));
        }

        // Verificar e configurar o preço
        double preco = entry.getPreco();
        // Criar um DecimalFormat para formatar o preço com duas casas decimais
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String precoFormatado = decimalFormat.format(preco);
        holder.textViewPreco.setText("Preço: " + precoFormatado +"€");
    }

    @Override
    public int getItemCount() {
        return estacionamentoEntries.size();
    }

    static class EstacionamentoHistoricoViewHolder extends RecyclerView.ViewHolder {
        TextView entryTimeTextView;
        TextView exitTimeTextView;
        TextView textViewState;
        TextView textViewPreco;
        TextView textViewTotalHoras;

        EstacionamentoHistoricoViewHolder(@NonNull View itemView) {
            super(itemView);
            entryTimeTextView = itemView.findViewById(R.id.entryTimeTextView);
            exitTimeTextView = itemView.findViewById(R.id.exitTimeTextView);
            textViewState = itemView.findViewById(R.id.textViewState);
            textViewPreco = itemView.findViewById(R.id.textViewPreco);
            textViewTotalHoras = itemView.findViewById(R.id.textViewTotalHoras);
        }
    }

    // Método para formatar a data e hora
    private String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return inputDate; // Retorna a data original em caso de erro
        }
    }

    // Método para converter a data para milissegundos
    private long convertToMillis(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            Date date = inputFormat.parse(inputDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
