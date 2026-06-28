package com.hogwarts.app.model;

import java.util.List;

public class Pregunta {
    private String enunciado;
    private List<String> opciones;
    private int correcta;

    public Pregunta() {
        //constructor vacío
    }

    public Pregunta(String enunciado, List<String> opciones, int correcta) {
        this.enunciado = enunciado;
        this.opciones = opciones;
        this.correcta = correcta;
    }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public List<String> getOpciones() { return opciones; }
    public void setOpciones(List<String> opciones) { this.opciones = opciones; }

    public int getCorrecta() { return correcta; }
    public void setCorrecta(int correcta) { this.correcta = correcta; }
}
