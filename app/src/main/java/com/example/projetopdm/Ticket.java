package com.example.projetopdm;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class Ticket extends AppCompatActivity {
    private EditText editTextDescricao;
    private TextView textViewData;
    private TicketModel ticketModel;
    private ImageView imgCamera;
    private static final String BASE_URL = "https://personal-dboeqix9.outsystemscloud.com/Estacionamento/rest/api/";
    private ApiService apiService;
    private int utilizadorId;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private int estacionamentoIdEncontrado; // Adicione essa variável de instância
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);


        EditText editTextMessage = findViewById(R.id.editTextDescricao);
        final TextView textViewCharacterCount = findViewById(R.id.textViewCharacterCount);

        editTextMessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int remainingCharacters = 400 - editable.length();

                textViewCharacterCount.setText(remainingCharacters + " / 400");

            }

        });


        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Ticket.this, Estacionamento.class);
                startActivity(intent);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
        ticketModel = new TicketModel();
        utilizadorId = getUserIdFromPreferences();
        Log.d("Utilizador", "ID: " + utilizadorId);
        if (apiService != null) {
            verificarEstacionamentoPendente();
        } else {
            Log.e("Estacionamento", "apiService é nulo. Falha na inicialização.");
        }
        editTextDescricao = findViewById(R.id.editTextDescricao);
        textViewData = findViewById(R.id.textViewData);
        imgCamera = findViewById(R.id.imgCamera);
        exibirDataHoraAtual();
        ImageButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{CAMERA_PERMISSION}, REQUEST_CAMERA_PERMISSION);
                } else {
                    startCameraIntent();
                }
            }
        });
        Button btnSubmeterTicket = findViewById(R.id.buttonSubmeterTicket);
        btnSubmeterTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submeterTicket();
            }
        });
    }
    private void startCameraIntent() {
        Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(iCamera);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraIntent();
            } else {
                Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void handleCameraResult(Intent data) {
        if (data != null) {
            imgCamera.setImageBitmap((Bitmap) data.getExtras().get("data"));
            imgCamera.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Erro ao obter a imagem da câmera", Toast.LENGTH_SHORT).show();
        }
    }
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            handleCameraResult(data);
                        }
                    });
    private String converterImagemParaBase64() {
        Bitmap img = ((BitmapDrawable) imgCamera.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imagemBytes = baos.toByteArray();
        return Base64.encodeToString(imagemBytes, Base64.DEFAULT);
    }
    private void exibirDataHoraAtual() {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dataHoraAtual = formato.format(Calendar.getInstance().getTime());
        textViewData.setText(dataHoraAtual);
    }
    private int getUserIdFromPreferences() {
        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        return preferences.getInt("userId", 0);
    }
    private void verificarEstacionamentoPendente() {
        Call<List<EstacionamentoModel>> call = apiService.getEstacionamentosPorUsuario(utilizadorId);
        call.enqueue(new Callback<List<EstacionamentoModel>>() {
            @Override
            public void onResponse(Call<List<EstacionamentoModel>> call, Response<List<EstacionamentoModel>> response) {
                if (response.isSuccessful()) {
                    List<EstacionamentoModel> estacionamentos = response.body();
                    boolean estacionamentoEncontrado = false;
                    for (EstacionamentoModel estacionamento : estacionamentos) {
                        if (estacionamento.getEstado() == null) {
                            estacionamentoIdEncontrado = estacionamento.getId();
                            Log.d("Estacionamento", "Estacionamento com Estado nulo encontrado. ID: " + estacionamentoIdEncontrado);
                            utilizarEstacionamento(estacionamentoIdEncontrado);
                            estacionamentoEncontrado = true;
                            break;
                        }
                    }
// Se nenhum estacionamento com estado nulo for encontrado, vá para a atividade Menu.java
                    if (!estacionamentoEncontrado) {
                        Toast.makeText(Ticket.this, "Nenhum estacionamento pendente encontrado", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Ticket.this, Menu.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Log.e("API_ERROR", "Erro ao obter estacionamentos. Código: " + response.code());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API_ERROR", "Error body: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call<List<EstacionamentoModel>> call, Throwable t) {
                Log.e("API_ERROR", "Falha na requisição: " + t.getMessage());
            }
        });
    }

    private void utilizarEstacionamento(int estacionamentoId) {
// Faça o que for necessário com o estacionamentoId encontrado
// Por exemplo, atualizar a interface do usuário, iniciar outra atividade, etc.
    }
    private void submeterTicket() {
        String descricao = editTextDescricao.getText().toString();
        String imagemBase64 = converterImagemParaBase64();
        String estado = "Pendente";
        int estacionamentoId = estacionamentoIdEncontrado; // Use o estacionamentoIdEncontrado
        TicketModel ticket = new TicketModel(descricao, imagemBase64, estacionamentoId, estado);

        enviarTicketParaAPI(ticket);
    }
    private void enviarTicketParaAPI(TicketModel ticket) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://personal-dboeqix9.outsystemscloud.com/Estacionamento/rest/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<Void> call = apiService.enviarTicket(ticket);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Ticket.this, "Ticket enviado com sucesso", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Ticket.this, Menu.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API_ERROR", "Error body: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Ticket.this, "Erro ao enviar o ticket. Código: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Ticket.this, "Falha na requisição: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}