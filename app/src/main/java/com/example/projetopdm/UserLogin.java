package com.example.projetopdm;

import com.google.gson.annotations.SerializedName;

public class UserLogin {
    @SerializedName("Email")
    private String email;
    @SerializedName("Password")
    private String password;
    @SerializedName("Nome")
    private String userName;
    @SerializedName("Id")
    private int userId; // Ajuste conforme o tipo de dado retornado
    @SerializedName("Nif")
    private long userNif; // Ajuste conforme o tipo de dado retornado
    @SerializedName("Contacto")
    private long userContact; // Ajuste conforme o tipo de dado retornado

    public UserLogin(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public int getUserId() {
        return userId;
    }

    public long getUserNif() {
        return userNif;
    }

    public long getUserContact() {
        return userContact;
    }
}
