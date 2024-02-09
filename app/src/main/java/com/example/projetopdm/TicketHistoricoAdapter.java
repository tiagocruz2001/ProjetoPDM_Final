package com.example.projetopdm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketHistoricoAdapter extends RecyclerView.Adapter<TicketHistoricoAdapter.TicketViewHolder> {

    private List<TicketHistoricoItem> items;
    private List<TicketHistoricoItem> itemsFull; // Lista original sem filtro
    private Context context;
    private OnItemClickListener onItemClickListener;

    public TicketHistoricoAdapter(Context context, List<TicketHistoricoItem> items) {
        this.context = context;
        this.items = items;
        this.itemsFull = new java.util.ArrayList<>(items);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void add(TicketHistoricoItem item) {
        items.add(item);
        itemsFull.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_historico_custom_list_item, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketHistoricoItem currentItem = items.get(position);

        if (currentItem != null) {
            holder.ticketNumberTextView.setText("Ticket " + (position + 1));
            holder.titleTextView.setText(currentItem.getTitle());
            holder.estadoTextView.setText("Estado: " + currentItem.getEstado());

            // Definir a cor com base no estado
            int textColor;
            if ("Aberto".equals(currentItem.getEstado()) || "Concluido".equals(currentItem.getEstado())) {
                textColor = context.getResources().getColor(R.color.orange);
            } else if ("Concluido".equals(currentItem.getEstado())) {
                textColor = context.getResources().getColor(R.color.green);
            } else {
                textColor = context.getResources().getColor(android.R.color.black); // Cor padrão, ajuste conforme necessário
            }

            holder.estadoTextView.setTextColor(textColor);

            if (currentItem.getImage() != null) {
                try {
                    Glide.with(context)
                            .load(currentItem.getImage())
                            .placeholder(R.drawable.car)
                            .into(holder.ticketImageView);

                    // Adicione um OnClickListener para a imagem
                    holder.ticketImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            expandImage(view);
                        }
                    });
                } catch (Exception e) {
                    Log.e("GlideError", "Error loading image with Glide: " + e.getMessage());
                }
            } else {
                Log.e("TicketHistoricoAdapter", "ticketImageView or currentItem.getImage() is null");
            }

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                Date date = inputFormat.parse(currentItem.getData());

                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedDate = outputFormat.format(date);

                holder.dateTextView.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                holder.dateTextView.setText(currentItem.getData());
            }
        } else {
            Log.e("TicketHistoricoAdapter", "currentItem is null");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class TicketViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ticketImageView;
        TextView ticketNumberTextView;
        TextView titleTextView;
        TextView dateTextView;
        TextView estadoTextView;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketImageView = itemView.findViewById(R.id.ticketImageView);
            ticketNumberTextView = itemView.findViewById(R.id.ticketNumberTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            estadoTextView = itemView.findViewById(R.id.estadoTextView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(items.get(position));
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(TicketHistoricoItem item);
    }

    public void filterByTitle(String query, TextView noResultText) {
        items.clear();

        if (query.isEmpty()) {
            items.addAll(itemsFull);
        } else {
            query = query.toLowerCase();
            for (TicketHistoricoItem item : itemsFull) {
                if (item.getTitle().toLowerCase().contains(query)) {
                    items.add(item);
                }
            }
        }

        // Atualizar a visibilidade do TextView "Sem Tickets Registados"
        updateNoResultTextViewVisibility(noResultText);

        notifyDataSetChanged();
    }

    private void updateNoResultTextViewVisibility(TextView noResultText) {
        if (items.isEmpty()) {
            noResultText.setVisibility(View.VISIBLE);
        } else {
            noResultText.setVisibility(View.GONE);
        }
    }

    // Método para expandir a imagem
    private void expandImage(View view) {
        // Lógica para expandir a imagem
        Toast.makeText(context, "Imagem Expandida", Toast.LENGTH_SHORT).show();
    }
}
