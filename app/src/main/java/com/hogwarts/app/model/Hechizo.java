package com.hogwarts.app.model;

import com.google.firebase.Timestamp;

public class Hechizo {
    private String id;
    private String uid;
    private String nombre;
    private String tipo;
    private String anotaciones;
    private Timestamp fechaCreacion;

    public Hechizo() {
        // Constructor vacío requerido por Firestore
    }

    public Hechizo(String uid, String nombre, String tipo, String anotaciones) {
        this.uid = uid;
        this.nombre = nombre;
        this.tipo = tipo;
        this.anotaciones = anotaciones;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getAnotaciones() { return anotaciones; }
    public void setAnotaciones(String anotaciones) { this.anotaciones = anotaciones; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}