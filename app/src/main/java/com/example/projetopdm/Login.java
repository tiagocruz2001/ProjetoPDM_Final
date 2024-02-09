package com.example.projetopdm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private EditText editTextEmailLogin, editTextPasswordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setEnabled(true);
        editTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        editTextPasswordLogin = findViewById(R.id.editTextPasswordLogin);

        TextView textViewLogin = findViewById(R.id.textViewRegister);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegister();
            }
        });
    }

    public void goToRegister() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onLoginButtonClick(View view) {
        String email = editTextEmailLogin.getText().toString();
        String password = editTextPasswordLogin.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(Login.this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        UserLogin userLogin = new UserLogin(email, password);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://personal-dboeqix9.outsystemscloud.com/Estacionamento/rest/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);


        LoginTask loginTask = new LoginTask(userLogin, apiService, new LoginCallback() {
            @Override
            public void onLoginResult(UserLogin userLogin) {
                if (userLogin != null) {
                    Toast.makeText(Login.this, "Login bem-sucedido", Toast.LENGTH_SHORT).show();
                    Button buttonLogin = findViewById(R.id.buttonLogin);
                    buttonLogin.setEnabled(false);
                    saveUserDataInPreferences(userLogin);

                    // Modificado: Passar o ID do utilizador para a atividade Estacionamento
                    Intent intent = new Intent(Login.this, Menu.class);
                    intent.putExtra("userId", userLogin.getUserId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Login.this, "Credenciais inválidas. Por favor, tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }

        });

        loginTask.execute();
    }

    private void saveUserDataInPreferences(UserLogin userLogin) {
        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("userId", userLogin.getUserId());
        editor.putString("userName", userLogin.getUserName());
        editor.putString("userEmail", userLogin.getEmail());
        editor.putLong("userNif", userLogin.getUserNif());
        editor.putLong("userContact", userLogin.getUserContact());

        editor.apply();

        // Adicione logs para verificar os valores
        Log.d("Login", "ID salvo: " + userLogin.getUserId());
        Log.d("Login", "Nome salvo: " + userLogin.getUserName());
        Log.d("Login", "E-mail salvo: " + userLogin.getEmail());
        Log.d("Login", "NIF salvo: " + userLogin.getUserNif());
        Log.d("Login", "Contato salvo: " + userLogin.getUserContact());
    }

    private static class LoginTask extends AsyncTask<Void, Void, UserLogin> {

        private UserLogin userLogin;
        private ApiService apiService;
        private LoginCallback callback;

        public LoginTask(UserLogin userLogin, ApiService apiService, LoginCallback callback) {
            this.userLogin = userLogin;
            this.apiService = apiService;
            this.callback = callback;
        }

        @Override
        protected UserLogin doInBackground(Void... voids) {
            try {
                Response<UserLogin> response = apiService.login(userLogin).execute();

                if (response.isSuccessful()) {
                    return response.body();
                } else {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(UserLogin userLogin) {
            callback.onLoginResult(userLogin);
        }
    }
}