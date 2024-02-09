package com.example.projetopdm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

public class TicketHistorico extends AppCompatActivity {

    private ApiService apiService;
    private RecyclerView recyclerView;
    private int utilizadorId;
    private static final String BASE_URL = "https://personal-dboeqix9.outsystemscloud.com/Estacionamento/rest/api/";
    private TicketHistoricoAdapter ticketHistoricoAdapter;
    private SearchView searchView;
    private Spinner spinnerEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_historico);

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redireciona para a MenuActivity
                Intent intent = new Intent(TicketHistorico.this, Menu.class);
                startActivity(intent);
            }
        });
        // Inicializar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Obter ID do usuário a partir da Intent
        utilizadorId = getIntent().getIntExtra("userId", -1);
        Log.e("ID:", "utilizadorId: " + utilizadorId);

        // Inicializar e configurar a lista de tickets
        List<TicketHistoricoItem> itemList = new ArrayList<>();
        ticketHistoricoAdapter = new TicketHistoricoAdapter(this, itemList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(ticketHistoricoAdapter);

        // Inicializar SearchView
        searchView = findViewById(R.id.searchView);
        setupSearchView();



        // Adicionar listener de clique para a lista
        ticketHistoricoAdapter.setOnItemClickListener(new TicketHistoricoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TicketHistoricoItem selectedItem) {
                // Exibir detalhes do ticket em um AlertDialog
                if (selectedItem != null) {
                    showTicketDetailsDialog(selectedItem);
                }
            }
        });

        // Obter os tickets do usuário
        getTicketsPorUsuario();
    }

    private void getTicketsPorUsuario() {
        Call<List<TicketModel>> call = apiService.getTicketsPorUsuario(utilizadorId);

        call.enqueue(new Callback<List<TicketModel>>() {
            @Override
            public void onResponse(Call<List<TicketModel>> call, Response<List<TicketModel>> response) {
                if (response.body().size() == 0) {
                    findViewById(R.id.noResultTextt).setVisibility(View.VISIBLE);
                } else {
                if (response.isSuccessful()) {
                    List<TicketModel> tickets = response.body();

                    // Preencher a lista de tickets com os dados da API
                    if (tickets != null) {
                        for (TicketModel ticket : tickets) {
                            TicketHistoricoItem ticketHistoricoItem = convertToTicketHistoricoItem(ticket);
                            ticketHistoricoAdapter.add(ticketHistoricoItem);
                        }
                    }

                } else {
                    handleApiError(response);
                }
                }
            }

            @Override
            public void onFailure(Call<List<TicketModel>> call, Throwable t) {
                Log.e("API_ERROR", "Falha na chamada da API: " + t.getMessage());
                Toast.makeText(TicketHistorico.this, "Falha na chamada da API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TicketHistoricoItem convertToTicketHistoricoItem(TicketModel ticketModel) {

        long id = ticketModel.getId();
        String descricao = ticketModel.getDescricao();
        String image = ticketModel.getImage();
        String data = ticketModel.getData();
        String estado = ticketModel.getEstado();
        long estacionamentoId = ticketModel.getEstacionamentoId();

        // Verificar se as horas não são nulas antes de formatá-las
        String horaEntrada = (ticketModel.getHoraEntrada() != null) ? formatarHora(ticketModel.getHoraEntrada()) : "";
        String horaSaida = (ticketModel.getHoraSaida() != null) ? formatarHora(ticketModel.getHoraSaida()) : "Ainda não saiu";

        return new TicketHistoricoItem(id, descricao, image, data, estacionamentoId, estado, horaEntrada, horaSaida);
    }



    private String formatarHora(String horaOriginal) {
        try {
            // Parse a hora original para o formato Date
            DateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            Date hora = formatoOriginal.parse(horaOriginal);

            // Agora, defina o formato desejado
            DateFormat formatoDesejado = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // Formate a hora no formato desejado
            return formatoDesejado.format(hora);
        } catch (ParseException e) {
            e.printStackTrace();
            return horaOriginal; // Retorna a hora original em caso de erro
        }
    }

    private void handleApiError(Response<?> response) {
        try {
            String errorBody = response.errorBody().string();
            Log.e("API_ERROR", "Error body: " + errorBody);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("API_ERROR", "Erro na chamada da API. Código: " + response.code());
        Toast.makeText(TicketHistorico.this, "Erro ao obter tickets. Código: " + response.code(), Toast.LENGTH_SHORT).show();
    }

    public Bitmap stringToBitmap(String encodedString) {
        try {
            // Decodifica a string em Base64 para um array de bytes
            byte[] decodedBytes = Base64.decode(encodedString, Base64.DEFAULT);
            // Decodifica o array de bytes em Bitmap
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            // Tratamento de erro caso a string não esteja no formato correto
            e.printStackTrace();
            return null;
        }
    }

    private void showTicketDetailsDialog(TicketHistoricoItem ticketHistoricoItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.ticket_historico_custom_ticket_details_dialog, null);
        builder.setView(dialogView);

        // Inicialize os elementos da UI do AlertDialog
        ImageView ticketImageView = dialogView.findViewById(R.id.ticketImageView);
        TextView textViewDescricao = dialogView.findViewById(R.id.descriptionTextView);
        TextView textViewData = dialogView.findViewById(R.id.dateTimeTextView);
        TextView horaEntradaTextView = dialogView.findViewById(R.id.horaEntradaTextView);
        TextView horaSaidaTextView = dialogView.findViewById(R.id.horaSaidaTextView);
        // Adicione mais elementos conforme necessário

        // Preencha os elementos com os dados do ticket selecionado
        ticketImageView.setImageBitmap(stringToBitmap(ticketHistoricoItem.getImage()));
        textViewDescricao.setText("Descrição: " + ticketHistoricoItem.getDescricao());

        String dataOriginal = ticketHistoricoItem.getData();
        DateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date data;
        try {
            // Parse a data original para o formato Date
            data = formatoOriginal.parse(dataOriginal);

            // Agora, defina o formato desejado
            DateFormat formatoDesejado = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Formate a data no formato desejado
            String dataFormatada = formatoDesejado.format(data);

            // Defina o texto no textViewData
            textViewData.setText("Data: " + dataFormatada);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        horaEntradaTextView.setText("Hora Entrada: " + ticketHistoricoItem.getHoraEntrada());
        horaSaidaTextView.setText("Hora Saída: " + ticketHistoricoItem.getHoraSaida());
        // Preencha outros elementos conforme necessário

        builder.setPositiveButton("Voltar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ticketHistoricoAdapter.filterByTitle(newText, findViewById(R.id.noResultTextt));

                return true;
            }
        });
    }


}
