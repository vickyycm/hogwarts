package com.hogwarts.app.model;

import com.google.firebase.Timestamp;

public class Novedad {
    private String id;
    private String titulo;
    private String contenido;
    private Timestamp fecha;

    public Novedad() {
        //constructor vacío
    }

    public Novedad(String titulo, String contenido, Timestamp fecha) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.fecha = fecha;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
}