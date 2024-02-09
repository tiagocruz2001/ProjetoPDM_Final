package com.example.projetopdm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EstacionamentoHistorico extends AppCompatActivity {

    private RecyclerView recyclerView;
    private long startDate = 0;
    private long endDate = 0;
    private int userId;
    private EstacionamentoHistoricoAdapter adapter;
    private List<EstacionamentoHistoricoModel> estacionamentoEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estacionamento_historico);

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EstacionamentoHistorico.this, Menu.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);

        Log.d("EstacionamentoHistorico", "ID do Usuário Recebido: " + userId);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        estacionamentoEntries = new ArrayList<>();
        adapter = new EstacionamentoHistoricoAdapter(estacionamentoEntries);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.calendar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendar();
            }
        });

        // Botão para ordenar por preço ascendente
        ImageButton btnPrecoUp = findViewById(R.id.btnPrecoUp);
        btnPrecoUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(estacionamentoEntries, new Comparator<EstacionamentoHistoricoModel>() {
                    @Override
                    public int compare(EstacionamentoHistoricoModel o1, EstacionamentoHistoricoModel o2) {
                        return Double.compare(o1.getPreco(), o2.getPreco());
                    }
                });
                adapter.notifyDataSetChanged();
            }
        });

        // Botão para ordenar por preço descendente
        ImageButton btnPrecoDown = findViewById(R.id.btnPrecoDown);
        btnPrecoDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(estacionamentoEntries, new Comparator<EstacionamentoHistoricoModel>() {
                    @Override
                    public int compare(EstacionamentoHistoricoModel o1, EstacionamentoHistoricoModel o2) {
                        return Double.compare(o2.getPreco(), o1.getPreco());
                    }
                });
                adapter.notifyDataSetChanged();
            }
        });

        // Botão para restaurar a ordem por data
        ImageButton btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obterHistoricoEstacionamento(userId);
            }
        });

        obterHistoricoEstacionamento(userId);
    }

    private void showCalendar() {
        findViewById(R.id.calendar).setEnabled(false);
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();

        final MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                startDate = selection.first;

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection.second);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                endDate = calendar.getTimeInMillis();

                obterHistoricoEstacionamento(userId);
            }
        });

        picker.show(getSupportFragmentManager(), picker.toString());
        findViewById(R.id.calendar).setEnabled(true);
    }

    private void obterHistoricoEstacionamento(int userId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://personal-dboeqix9.outsystemscloud.com/Estacionamento/rest/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<EstacionamentoHistoricoModel>> call = apiService.getEstacionamentoHistorico(userId);

        call.enqueue(new Callback<List<EstacionamentoHistoricoModel>>() {
            @Override
            public void onResponse(Call<List<EstacionamentoHistoricoModel>> call, Response<List<EstacionamentoHistoricoModel>> response) {
                if (response.isSuccessful()) {
                    if (response.body().size() == 0) {
                        findViewById(R.id.noResultText).setVisibility(View.VISIBLE);
                    } else {
                        List<EstacionamentoHistoricoModel> allEntries = response.body();
                        List<EstacionamentoHistoricoModel> filteredEntries;
                        if (startDate != 0 && endDate != 0) {
                            filteredEntries = filtrarPorData(allEntries, startDate, endDate);
                        } else {
                            filteredEntries = allEntries;
                        }

                        estacionamentoEntries.clear();
                        estacionamentoEntries.addAll(filteredEntries);
                        adapter.notifyDataSetChanged();

                        exibirLogsDadosRecebidos(estacionamentoEntries);
                        calcularEExibirTotalDeHoras(estacionamentoEntries);
                    }
                } else {
                    Log.e("EstacionamentoHistorico", "Falha na resposta da API. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EstacionamentoHistoricoModel>> call, Throwable t) {
                Log.e("EstacionamentoHistorico", "Erro na chamada à API", t);
            }
        });
    }

    private List<EstacionamentoHistoricoModel> filtrarPorData(List<EstacionamentoHistoricoModel> entries, long startDate, long endDate) {
        List<EstacionamentoHistoricoModel> filteredEntries = new ArrayList<>();

        SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

        for (EstacionamentoHistoricoModel entry : entries) {
            try {
                Date entradaDate = serverDateFormat.parse(entry.getHoraEntrada());
                if (entradaDate != null && isDateInRange(entradaDate.getTime(), startDate, endDate)) {
                    filteredEntries.add(entry);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Log.d("EstacionamentoHistorico", "Registros após filtragem: " + filteredEntries.size());
        if (filteredEntries.size() == 0)
            findViewById(R.id.noResultText).setVisibility(View.VISIBLE);
        else findViewById(R.id.noResultText).setVisibility(View.GONE);

        return filteredEntries;
    }

    private boolean isDateInRange(long date, long startDate, long endDate) {
        return date >= startDate && date <= endDate;
    }

    private void exibirLogsDadosRecebidos(List<EstacionamentoHistoricoModel> entries) {
        for (EstacionamentoHistoricoModel entry : entries) {
            Log.d("EstacionamentoHistorico", "ID: " + entry.getId());
            Log.d("EstacionamentoHistorico", "Hora Entrada: " + entry.getHoraEntrada());
            Log.d("EstacionamentoHistorico", "Hora Saída: " + entry.getHoraSaida());
            String estado = (entry.getEstado() != null) ? entry.getEstado() : "Pendente";
            TextView estadoTextView = findViewById(R.id.textViewState);
            if (estadoTextView != null) {
                estadoTextView.setText("Estado: " + estado);
            } else {
                Log.e("EstacionamentoHistorico", "TextView do Estado é nulo");
            }
            Log.d("EstacionamentoHistorico", "Utilizador ID: " + entry.getUtilizadorId());
        }
    }

    private void calcularEExibirTotalDeHoras(List<EstacionamentoHistoricoModel> entries) {
        long totalHorasMillis = 0;

        for (EstacionamentoHistoricoModel entry : entries) {
            try {
                SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

                String horaEntradaString = entry.getHoraEntrada();
                String horaSaidaString = entry.getHoraSaida();

                if (horaEntradaString != null && horaSaidaString != null) {
                    Date entradaDate = serverDateFormat.parse(horaEntradaString);
                    Date saidaDate = serverDateFormat.parse(horaSaidaString);

                    if (entradaDate != null && saidaDate != null) {
                        long diferencaMillis = saidaDate.getTime() - entradaDate.getTime();
                        totalHorasMillis += diferencaMillis;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        long totalHoras = totalHorasMillis / (60 * 60 * 1000);
        long totalMinutos = (totalHorasMillis % (60 * 60 * 1000)) / (60 * 1000);
        long totalSegundos = ((totalHorasMillis % (60 * 60 * 1000)) % (60 * 1000)) / 1000;

        TextView totalHorasTextView = findViewById(R.id.textViewTotalHoras);
        if (totalHorasTextView != null) {
            totalHorasTextView.setText("Total de Horas: " + totalHoras + "h " + totalMinutos + "min " + totalSegundos + "s");
        } else {
            Log.e("EstacionamentoHistorico", "TextView do Total de Horas é nulo");
        }
    }
}
