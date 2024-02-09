package com.example.projetopdm;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("utilizador")
    Call<Void> registerUser(@Body User user);


        @POST("login_estacionamento")
        Call<UserLogin> login(@Body UserLogin user);


    @POST("estacionamento")
    Call<String> registrarEntrada(@Body EstacionamentoModel estacionamentoRequest);


    @PATCH("estacionamento")
    Call<Void> registrarSaida(@Body EstacionamentoModel estacionamentoRequest);


    @PATCH("estacionamento")
    Call<Void> atualizarEstacionamento(@Body EstacionamentoModel estacionamentoRequest);


    @POST("ticket")
    Call<Void> enviarTicket(@Body TicketModel ticket);

    @GET("all_estacionamento")
    Call<List<EstacionamentoModel>> getEstacionamentosPorUsuario(@Query("UtilizadorIdIn") int utilizadorId);

    @GET("all_estacionamento")
    Call<List<EstacionamentoModel>> verificarEstacionamentoPendente(@Query("UtilizadorIdIn") int utilizadorId);

    @GET("all_estacionamento")
    Call<List<EstacionamentoHistoricoModel>> getEstacionamentoHistorico(@Query("UtilizadorIdIn") int userId);

    @GET("ticketestacionamento")
    Call<List<TicketModel>> getTicketsPorUsuario(@Query("UtilizadorIdIn") int utilizadorId);

    @GET("GetUtilizador")
    Call<EditarUtilizadorModel> getUserById(@Query("utilizadorId") int utilizadorId);



    @PATCH("editartilizador")
    Call<Void> atualizarUsuario(@Body EditarUtilizadorModel editarUtilizadorModel);







}

