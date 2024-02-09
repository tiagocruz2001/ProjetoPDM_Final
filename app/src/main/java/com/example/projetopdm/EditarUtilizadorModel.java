package com.example.projetopdm;

import com.google.gson.annotations.SerializedName;

public class EditarUtilizadorModel {

    @SerializedName("Id")
    private long id;

    @SerializedName("Nome")
    private String nome;

    @SerializedName("Email")
    private String email;

    @SerializedName("Nif")
    private int nif;

    @SerializedName("Password")
    private String password;

    @SerializedName("Contacto")
    private long contacto;

    // Construtor vazio necessário para o Retrofit
    public EditarUtilizadorModel() {
    }

    public EditarUtilizadorModel(long id, String nome, String email, int nif, String password, long contacto) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.nif = nif;
        this.password = password;
        this.contacto = contacto;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public int getNif() {
        return nif;
    }

    public String getPassword() {
        return password;
    }

    public long getContacto() {
        return contacto;
    }

    // Outros getters e setters, se necessário
}
