package com.example.projetopdm;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private EditText editTextNome, editTextEmail, editTextNif, editTextPassword, editTextContacto;
    private Button buttonRegister;
    private TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editTextNome = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNif = findViewById(R.id.editTextNIF);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextContacto = findViewById(R.id.editTextContact);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        buttonRegister.setEnabled(true);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });
    }

    private void registerUser() {
        String nome = editTextNome.getText().toString();
        String email = editTextEmail.getText().toString();
        String nif = editTextNif.getText().toString();
        String password = editTextPassword.getText().toString();
        String contacto = editTextContacto.getText().toString();

        // Verificar se os campos obrigatórios estão preenchidos
        if (nome.isEmpty() || email.isEmpty() || nif.isEmpty() || password.isEmpty() || contacto.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return; // Encerrar o método se algum campo estiver vazio
        }

        // Verificar se NIF e Contacto são números válidos
        if (!TextUtils.isDigitsOnly(nif) || !TextUtils.isDigitsOnly(contacto)) {
            Toast.makeText(MainActivity.this, "NIF e Contacto devem ser números válidos", Toast.LENGTH_SHORT).show();
            return; // Encerrar o método se algum campo não for um número válido
        }

        int nifValue = Integer.parseInt(nif);
        int contactoValue = Integer.parseInt(contacto);


        User user = new User(nome, email, nifValue, password, contactoValue);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://personal-dboeqix9.outsystemscloud.com/Estacionamento/rest/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Void> call = apiService.registerUser(user);

        // Adicionando logs para depuração
        Log.d("DEBUG", "Enviando dados para o servidor: " + user.toString());
        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setEnabled(false);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("DEBUG", "Registro bem-sucedido");
                    Toast.makeText(MainActivity.this, "Registro bem-sucedido", Toast.LENGTH_SHORT).show();

                    // Iniciar a atividade de login após um registro bem-sucedido
                    goToLogin();
                } else {
                    Log.d("DEBUG", "Erro no registro. Código: " + response.code());
                    // Exibir mensagem de erro específica do servidor, se disponível
                    try {
                        String errorBody = response.errorBody().string();
                        Log.d("DEBUG", "Detalhes do erro: " + errorBody);
                        // Mostrar mensagem de erro específica na interface do usuário
                        Toast.makeText(MainActivity.this, "Erro no registro: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Mostrar mensagem de erro genérica na interface do usuário
                        Toast.makeText(MainActivity.this, "Erro no registro", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ERROR", "Falha na comunicação com o servidor", t);
                Toast.makeText(MainActivity.this, "Falha na comunicação com o servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, Login.class); // Substitua LoginActivity pela sua classe de login
        startActivity(intent);
        finish(); // Isso encerra a atividade atual, impedindo que o usuário volte para a tela de registro ao pressionar o botão "voltar"
    }
}
