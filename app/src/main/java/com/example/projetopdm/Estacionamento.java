package com.example.projetopdm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Estacionamento extends AppCompatActivity {

    private static final String BASE_URL = "https://personal-dboeqix9.outsystemscloud.com/Estacionamento/rest/api/";

    private ApiService apiService;
    private EstacionamentoModel estacionamentoModel;
    private int Id;
    private int utilizadorId; // Adicionado para armazenar o userId
    private String horaEntradaFormatada;
    private static final double PRECO_POR_HORA = 0.30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estacionamento);

        // Adicionando o ícone clicável no canto superior esquerdo
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redireciona para a MenuActivity
                Intent intent = new Intent(Estacionamento.this, Menu.class);
                startActivity(intent);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
        estacionamentoModel = new EstacionamentoModel();

        // Obtém o userId das preferências compartilhadas
        utilizadorId = getUserIdFromPreferences();


        ImageButton btnTicket = findViewById(R.id.ticket);
        btnTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Estacionamento.this, Ticket.class);

                startActivity(intent);
            }
        });




        Log.d("Utilizador", "ID: " + utilizadorId);

        TextView HoraEntrada = findViewById(R.id.horaEntrada);

        HoraEntrada.setVisibility(View.INVISIBLE);
        TextView HoraSaida = findViewById(R.id.horaSaida);
        HoraSaida.setVisibility(View.INVISIBLE);
        ImageButton btnEntradaEnable = findViewById(R.id.btn1);
        btnEntradaEnable.setVisibility(View.INVISIBLE);
        Button btnPagarIn = findViewById(R.id.btnPagar);
        btnPagarIn.setVisibility(View.INVISIBLE);
        TextView preco = findViewById(R.id.preco);
        preco.setVisibility(View.INVISIBLE);
        TextView QrMsg = findViewById(R.id.QrMsg);
        QrMsg.setVisibility(View.INVISIBLE);
        btnTicket.setVisibility(View.INVISIBLE);
        TextView textTicket = findViewById(R.id.textticket);
        textTicket.setVisibility(View.INVISIBLE);


        // Verifica se há estacionamento pendente ao entrar na atividade
        if (apiService != null) {
            verificarEstacionamentoPendente();
        } else {
            Log.e("Estacionamento", "apiService é nulo. Falha na inicialização.");
        }

        ImageButton btnEntrada = findViewById(R.id.btn1);
        btnEntrada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicia a atividade de leitura de QR Code
                new IntentIntegrator(Estacionamento.this).initiateScan();
            }
        });

        Button btnPagar = findViewById(R.id.btnPagar);
        btnPagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagarEstacionamento();
            }
        });
    }

    private void pagarEstacionamento() {
        // Certifique-se de que o Id seja válido
        if (Id > 0) {
            // Configurar os dados de HoraSaida e Estado no EstacionamentoModel
            estacionamentoModel.setId(Id);  // Adicione o ID ao corpo da requisição

            estacionamentoModel.setHoraSaida(obterHoraAtual());
            estacionamentoModel.setEstado("Concluído");

            // Check if horaEntradaFormatada is null, and if so, use estacionamentoModel.getHoraEntrada()
            if (horaEntradaFormatada == null) {
                horaEntradaFormatada = estacionamentoModel.getHoraEntrada();
            }

            TextView horaSaida = findViewById(R.id.horaSaida);
            horaSaida.setVisibility(View.VISIBLE);
            horaSaida.setText("Hora Saída: " + estacionamentoModel.getHoraSaida());

            double precoTotal = calcularPrecoTotal(horaEntradaFormatada, estacionamentoModel.getHoraSaida());
            estacionamentoModel.setPreco(precoTotal);

            TextView precoTextView = findViewById(R.id.preco);
            precoTextView.setVisibility(View.VISIBLE);
            ImageButton btnTicket = findViewById(R.id.ticket);
            btnTicket.setVisibility(View.INVISIBLE);
            TextView textTicket = findViewById(R.id.textticket);
            textTicket.setVisibility(View.INVISIBLE);
            precoTextView.setText("Preço: " + String.format("%.2f", precoTotal) + " €");

            // Log para mostrar o que está sendo enviado para o servidor
            Log.d("Estacionamento", "Enviando dados para o servidor - ID: " + Id +
                    ", HoraSaida: " + estacionamentoModel.getHoraSaida() +
                    ", Estado: " + estacionamentoModel.getEstado() +
                    ", Preço: " + precoTotal);

            // Aguarde 5 segundos antes de redirecionar para a MenuActivity
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            // Chame o método no ApiService para registrar a saída
                            Call<Void> call = apiService.registrarSaida(estacionamentoModel);
                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        // Pagamento efetuado com sucesso
                                        Toast.makeText(Estacionamento.this, "Pagamento efetuado com sucesso", Toast.LENGTH_SHORT).show();

                                        // Redirecione para a MenuActivity
                                        Intent intent = new Intent(Estacionamento.this, Menu.class);
                                        startActivity(intent);
                                        finish();  // Encerre a atividade atual
                                    } else {
                                        Log.e("Estacionamento", "Falha ao realizar o pagamento. Código: " + response.code());
                                        Toast.makeText(Estacionamento.this, "Falha ao realizar o pagamento. Código: " + response.code(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Log.e("Estacionamento", "Falha ao realizar o pagamento", t);
                                }
                            });
                        }
                    },
                    1
            );
        } else {
            Toast.makeText(Estacionamento.this, "ID de estacionamento inválido", Toast.LENGTH_SHORT).show();
        }
    }


    private double calcularPrecoTotal(String horaEntrada, String horaSaida) {
        try {
            SimpleDateFormat formatoData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date dataEntrada = formatoData.parse(horaEntrada);
            Date dataSaida = formatoData.parse(horaSaida);
            long diferencaEmMilissegundos = dataSaida.getTime() - dataEntrada.getTime();

            double diferencaEmHoras = diferencaEmMilissegundos / (1000 * 60 * 60.0);

            double precoTotal = diferencaEmHoras * PRECO_POR_HORA;

            return precoTotal;

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }


    private void verificarEstacionamentoPendente() {
        Call<List<EstacionamentoModel>> call = apiService.verificarEstacionamentoPendente(utilizadorId);
        call.enqueue(new Callback<List<EstacionamentoModel>>() {
            @Override
            public void onResponse(Call<List<EstacionamentoModel>> call, Response<List<EstacionamentoModel>> response) {
                if (response.isSuccessful()) {
                    List<EstacionamentoModel> estacionamentos = response.body();

                    if (estacionamentos != null && !estacionamentos.isEmpty()) {
                        // Verifica se há algum estacionamento com estado nulo
                        for (EstacionamentoModel estacionamento : estacionamentos) {

                            Log.d("Estacionamento", "ID: " + estacionamento.getId() +
                                    ", HoraEntrada: " + estacionamento.getHoraEntrada() +
                                    ", HoraSaida: " + estacionamento.getHoraSaida() +
                                    ", Estado: " + estacionamento.getEstado());

                            // Verifica se o estado é nulo, preenche a Hora de Entrada e sai do loop
                            if (estacionamento.getEstado() == null) {

                                TextView horaEntrada = findViewById(R.id.horaEntrada);
                                horaEntrada.setVisibility(View.VISIBLE);
                                TextView horaSaida = findViewById(R.id.horaSaida);
                                horaSaida.setVisibility(View.INVISIBLE);

                                Button btnPagar1 = findViewById(R.id.btnPagar);
                                btnPagar1.setVisibility(View.VISIBLE);
                                ImageButton btnEntradaEnable = findViewById(R.id.btn1);
                                btnEntradaEnable.setVisibility(View.INVISIBLE);
                                TextView QrMsg = findViewById(R.id.QrMsg);
                                QrMsg.setVisibility(View.INVISIBLE);
                                preencherHoraEntrada(estacionamento.getHoraEntrada());
                                ImageButton btnTicket = findViewById(R.id.ticket);
                                btnTicket.setVisibility(View.VISIBLE);
                                TextView textTicket = findViewById(R.id.textticket);
                                textTicket.setVisibility(View.VISIBLE);
                                // Atualize o Id aqui
                                Id = estacionamento.getId();
                                return;
                            }
                            ImageButton btnEntradaEnable = findViewById(R.id.btn1);
                            btnEntradaEnable.setVisibility(View.VISIBLE);
                            TextView QrMsg = findViewById(R.id.QrMsg);
                            QrMsg.setVisibility(View.VISIBLE);
                            // Verifica se o estacionamento está pendente
                            if ("Pendente".equals(estacionamento.getEstado())) {
                                // Preenche a Hora de Entrada

                                preencherHoraEntrada(estacionamento.getHoraEntrada());
                                // Atualize o Id aqui
                                Id = estacionamento.getId();

                                // Se HoraSaida ou Estado são nulos, exiba uma mensagem ou trate conforme necessário
                                if (estacionamento.getHoraSaida() == null || estacionamento.getEstado() == null) {
                                    Toast.makeText(Estacionamento.this, "Estacionamento com dados nulos", Toast.LENGTH_SHORT).show();
                                }
                                return; // Sair do método após encontrar um estacionamento pendente
                            }
                        }
                        Toast.makeText(Estacionamento.this, "Nenhum estacionamento pendente encontrado", Toast.LENGTH_SHORT).show();
                    } else {
                        ImageButton btnEntradaEnable = findViewById(R.id.btn1);
                        btnEntradaEnable.setVisibility(View.VISIBLE);
                        TextView QrMsg = findViewById(R.id.QrMsg);
                        QrMsg.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e("Estacionamento", "Falha ao verificar estacionamento pendente. Código: " + response.code());
                    Toast.makeText(Estacionamento.this, "Falha ao verificar estacionamento pendente. Código: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EstacionamentoModel>> call, Throwable t) {
                Log.e("Estacionamento", "Falha ao verificar estacionamento pendente", t);
            }
        });
    }

    private void preencherHoraEntrada(String horaEntrada) {
        // Adicione o código para preencher a Hora de Entrada no seu layout
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        try {
            // Parse a data original para o formato Date
            Date data = formatoEntrada.parse(horaEntrada);

            // Agora, defina o formato desejado
            SimpleDateFormat formatoSaida = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatoSaida.setTimeZone(TimeZone.getDefault());  // Use o fuso horário local do dispositivo

            // Formate a data no formato desejado
            horaEntradaFormatada = formatoSaida.format(data);

            // Adicione o código para preencher a Hora de Entrada no seu layout
            TextView horaEntradaTextView = findViewById(R.id.horaEntrada);
            horaEntradaTextView.setText("Hora Entrada: " + horaEntradaFormatada);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private String obterHoraAtual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private int getUserIdFromPreferences() {
        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        return preferences.getInt("userId", 0);  // 0 é o valor padrão caso não seja encontrado
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Obtém o resultado do scan do QR Code
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Verifica se o conteúdo do QR Code é igual a "estacionamentocidal"
                if ("estacionamentocidal".equals(result.getContents())) {
                    // Atualiza a hora de entrada
                    String horaEntrada = obterHoraAtual();
                    estacionamentoModel.setHoraEntrada(horaEntrada);

                    // Adiciona o utilizadorId ao modelo
                    estacionamentoModel.setUtilizadorId(utilizadorId);
                    TextView horaEntradaTextView = findViewById(R.id.horaEntrada);
                    horaEntradaTextView.setVisibility(View.VISIBLE);
                    TextView horaSaida = findViewById(R.id.horaSaida);
                    horaSaida.setVisibility(View.INVISIBLE);
                    horaEntradaTextView.setText("Hora Entrada: " + horaEntrada);
                    ImageButton btnEntradaEnable = findViewById(R.id.btn1);
                    btnEntradaEnable.setVisibility(View.INVISIBLE);
                    TextView QrMsg = findViewById(R.id.QrMsg);
                    QrMsg.setVisibility(View.INVISIBLE);
                    Button btnPagar1 = findViewById(R.id.btnPagar);
                    btnPagar1.setVisibility(View.VISIBLE);
                    ImageButton btnTicket = findViewById(R.id.ticket);
                    btnTicket.setVisibility(View.VISIBLE);
                    TextView textTicket = findViewById(R.id.textticket);
                    textTicket.setVisibility(View.VISIBLE);
                    // Envia dados para o servidor
                    Call<String> call = apiService.registrarEntrada(estacionamentoModel);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()) {
                                String responseBody = response.body();
                                if (responseBody != null && !responseBody.isEmpty()) {
                                    try {
                                        // Converte a string do ID para um inteiro
                                        Id = Integer.parseInt(responseBody);
                                        Toast.makeText(Estacionamento.this, "Hora de Entrada Registada com sucesso", Toast.LENGTH_SHORT).show();
                                        // Log para verificar o ID após a submissão da hora de entrada
                                        Log.d("Estacionamento", "ID após submissão da hora de entrada: " + Id);

                                        // Agora você pode prosseguir com a lógica de pagamento, se necessário
                                        // ...

                                    } catch (NumberFormatException e) {
                                        Log.e("Estacionamento", "Erro ao converter ID para inteiro", e);
                                    }
                                } else {
                                    Log.e("Estacionamento", "Resposta do servidor não contém ID");
                                }
                            } else {
                                Log.e("Estacionamento", "Falha ao registrar hora de entrada. Código: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.e("Estacionamento", "Falha ao registrar hora de entrada", t);
                        }
                    });
                } else {
                    // Se o conteúdo não for "estacionamentocidal", exibe uma mensagem de erro
                    Toast.makeText(this, "Código QR inválido", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}