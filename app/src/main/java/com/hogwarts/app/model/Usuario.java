package com.hogwarts.app.model;

public class Usuario {
    private String uid;
    private String nombre;
    private String email;
    private String casa;
    private String fotoUrl;

    public Usuario() {
        // Constructor vacío requerido por Firestore
    }

    public Usuario(String uid, String nombre, String email, String casa, String fotoUrl) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.casa = casa;
        this.fotoUrl = fotoUrl;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCasa() { return casa; }
    public void setCasa(String casa) { this.casa = casa; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
