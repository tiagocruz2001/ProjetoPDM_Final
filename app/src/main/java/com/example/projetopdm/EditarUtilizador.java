package com.example.projetopdm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class EditarUtilizador extends AppCompatActivity {

    private EditText editTextNome, editTextEmail, editTextNif, editTextPassword, editTextConfirmarPassword, editTextContacto;
    private Button btnEditar;
    private ImageButton btnMenu, btnMostrarSenha;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_utilizador);

        editTextNome = findViewById(R.id.editTextNome);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNif = findViewById(R.id.editTextNif);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmarPassword = findViewById(R.id.editTextConfirmarPassword);
        editTextContacto = findViewById(R.id.editTextContacto);
        btnEditar = findViewById(R.id.btnEditar);
        btnMenu = findViewById(R.id.btnMenu);
        btnMostrarSenha = findViewById(R.id.btnMostrarSenha);



        btnEditar.setEnabled(false);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditarUtilizador.this, Menu.class);
                startActivity(intent);
            }
        });

        userId = getUserIdFromPreferences();

        ApiService apiService = getRetrofitInstance().create(ApiService.class);
        Call<EditarUtilizadorModel> call = apiService.getUserById(userId);

        call.enqueue(new Callback<EditarUtilizadorModel>() {
            @Override
            public void onResponse(Call<EditarUtilizadorModel> call, Response<EditarUtilizadorModel> response) {
                if (response.isSuccessful()) {
                    EditarUtilizadorModel usuario = response.body();
                    if (usuario != null) {
                        editTextNome.setText(usuario.getNome());
                        editTextEmail.setText(usuario.getEmail());
                        editTextNif.setText(String.valueOf(usuario.getNif()));
                        editTextPassword.setText(usuario.getPassword());
                        editTextContacto.setText(String.valueOf(usuario.getContacto()));

                        editTextNome.setVisibility(View.VISIBLE);
                        editTextEmail.setVisibility(View.VISIBLE);
                        editTextNif.setVisibility(View.VISIBLE);
                        editTextPassword.setVisibility(View.VISIBLE);
                        editTextConfirmarPassword.setVisibility(View.VISIBLE);
                        btnMostrarSenha.setVisibility(View.VISIBLE);
                        editTextContacto.setVisibility(View.VISIBLE);
                        btnEditar.setVisibility(View.VISIBLE);

                        editTextEmail.setEnabled(true);
                        editTextNif.setEnabled(true);
                        editTextPassword.setEnabled(true);
                        editTextConfirmarPassword.setEnabled(true);
                        editTextContacto.setEnabled(true);

                        btnEditar.setEnabled(true);
                    }
                } else {
                    Log.e("EditarUtilizador", "Erro ao obter dados do usuário. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EditarUtilizadorModel> call, Throwable t) {
                Log.e("EditarUtilizador", "Falha na chamada à API: " + t.getMessage());
            }
        });

        btnMostrarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int inputType = editTextPassword.getInputType();
                if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                editTextPassword.setSelection(editTextPassword.getText().length());
            }
        });

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = editTextPassword.getText().toString();
                String confirmPassword = editTextConfirmarPassword.getText().toString();

                if (!password.equals(confirmPassword)) {
                    mostrarErroSenhaDiferente();
                    return;
                }

                mostrarDialogoConfirmacao();
            }
        });
    }

    private void mostrarErroSenhaDiferente() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Erro");
        builder.setMessage("As senhas não coincidem. Por favor, verifique e tente novamente.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Fecha o diálogo
            }
        });
        builder.show();
    }

    private void mostrarDialogoConfirmacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmação");
        builder.setMessage("Deseja realmente editar o utilizador?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editarUsuario();
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Fecha o diálogo sem fazer nada
            }
        });
        builder.show();
    }

    private int getUserIdFromPreferences() {
        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        return preferences.getInt("userId", -1);
    }

    private void editarUsuario() {
        String novoNome = editTextNome.getText().toString();
        String novoEmail = editTextEmail.getText().toString();
        String novoNifStr = editTextNif.getText().toString();
        String novaPassword = editTextPassword.getText().toString();
        String novoContactoStr = editTextContacto.getText().toString();

        try {
            int novoNif = Integer.parseInt(novoNifStr);
            int novoContacto = Integer.parseInt(novoContactoStr);

            EditarUtilizadorModel novoUsuario = new EditarUtilizadorModel(userId, novoNome, novoEmail, novoNif, novaPassword, novoContacto);

            ApiService apiService = getRetrofitInstance().create(ApiService.class);
            Call<Void> call = apiService.atualizarUsuario(novoUsuario);
            btnEditar.setEnabled(false);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        btnEditar.setEnabled(false);
                        Log.d("EditarUtilizador", "Utilizador editado com sucesso");
                        Toast.makeText(EditarUtilizador.this, "Utilizador Editado com Sucesso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditarUtilizador.this, Menu.class);
                        startActivity(intent);
                        finish();
                    } else {
                        btnEditar.setEnabled(true);
                        Log.e("EditarUtilizador", "Erro ao editar utilizador. Código: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("EditarUtilizador", "Falha na chamada à API: " + t.getMessage());
                    btnEditar.setEnabled(true);
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(EditarUtilizador.this, "Por favor, insira números válidos para NIF e Contato", Toast.LENGTH_SHORT).show();
            btnEditar.setEnabled(true);
        }
    }

    private Retrofit getRetrofitInstance() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://personal-dboeqix9.outsystemscloud.com/Estacionamento/rest/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}
