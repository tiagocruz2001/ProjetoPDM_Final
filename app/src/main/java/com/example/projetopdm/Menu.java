package com.example.projetopdm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Menu extends AppCompatActivity {
    private static final String BASE_URL = "https://personal-dboeqix9.outsystemscloud.com/Estacionamento/rest/api/";

    private ApiService apiService;

    private String getUserIdFromPreferences() {
        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        return preferences.getString("userName", "");  // 0 é o valor padrão caso não seja encontrado
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Recupera o nome do usuário das preferências compartilhadas
        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String userName = preferences.getString("userName", ""); // Usando a chave correta
        int userId = preferences.getInt("userId", -1); // Usando a chave correta
        Log.d("Login", "Nome salvo: " + userName);
        // Verifica se o nome do usuário está presente nas preferências
        if (!userName.isEmpty()) {
            // Exibe o nome do usuário no TextView
            userName = getUserIdFromPreferences();
            TextView nomeUtilizadorTextView = findViewById(R.id.NomeUtilizador);
            nomeUtilizadorTextView.setText("Bem-vindo, " + userName + " ao Estacionamentos Cidal");
        }

        CardView estacionamentoCard = findViewById(R.id.estacionamentoCard);
        CardView historicoEstacionamentoCard = findViewById(R.id.historicoEstacionamentoCard);
        CardView historicoTicketCard = findViewById(R.id.historicoTicketCard);
        ImageView logoutButton = findViewById(R.id.logoutButton);
        ImageView EditarUserButton = findViewById(R.id.EditarUserButton);

        estacionamentoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToDesiredPage(Estacionamento.class);
            }
        });



        historicoEstacionamentoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToDesiredPage(EstacionamentoHistorico.class);
            }
        });

        historicoTicketCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToDesiredPage(TicketHistorico.class);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

        // Configurar clique no endereço para abrir no Google Maps
        TextView enderecoTextView = findViewById(R.id.textEndereco);
        enderecoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGoogleMaps();
            }
        });

        EditarUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToDesiredPage(EditarUtilizador.class);
            }
        });

    }



    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Tem a certeza de que deseja sair?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performLogout();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void performLogout() {
        // Limpar as preferências do usuário ao fazer logout
        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Limpa todas as preferências
        editor.apply();

        // Redirecionar para a tela de login
        Intent intent = new Intent(Menu.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa a pilha de atividades
        startActivity(intent);
        finish(); // Finaliza a atividade atual para evitar voltar para o menu após o logout
    }

    private void redirectToDesiredPage(Class<?> destinationClass) {
        Intent intent = new Intent(Menu.this, destinationClass);

        // Adicione o userId ao Intent
        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        int userId = preferences.getInt("userId", -1);
        intent.putExtra("userId", userId);

        startActivity(intent);
    }

    private void openGoogleMaps() {
        String endereco = "Rua 21 de Agosto, 3510-119, Viseu";

        endereco = endereco.replace(" ", "+");

        String url = "https://www.google.com/maps/search/?api=1&query=" + endereco;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
