package com.hogwarts.app.ui.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hogwarts.app.R;
import com.hogwarts.app.model.Pregunta;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class JuegoTrivia extends AppCompatActivity {

    private TextView tvContadorPreguntas, tvEnunciado;
    private RadioGroup rgOpciones;
    private RadioButton rbOpcion1, rbOpcion2, rbOpcion3, rbOpcion4;
    private Button btnSiguiente;

    private List<Pregunta> listaPreguntas = new ArrayList<>();
    private int indicePreguntaActual = 0;
    private int puntosGanados = 0;
    private String casaUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego_trivia);

        casaUsuario = getIntent().getStringExtra("CASA_USUARIO");

        inicializarComponentes();
        cargarPreguntasDesdeFirestore();

        btnSiguiente.setOnClickListener(v -> procesarRespuesta());
    }

    private void inicializarComponentes() {
        tvContadorPreguntas = findViewById(R.id.tvContadorPreguntas);
        tvEnunciado = findViewById(R.id.tvEnunciado);
        rgOpciones = findViewById(R.id.rgOpciones);
        rbOpcion1 = findViewById(R.id.rbOpcion1);
        rbOpcion2 = findViewById(R.id.rbOpcion2);
        rbOpcion3 = findViewById(R.id.rbOpcion3);
        rbOpcion4 = findViewById(R.id.rbOpcion4);
        btnSiguiente = findViewById(R.id.btnSiguiente);
    }

    private void cargarPreguntasDesdeFirestore() {
        //hilo secundario
        Executors.newSingleThreadExecutor().execute(() -> {
            FirebaseFirestore.getInstance().collection("trivia")
                    .get()
                    .addOnCompleteListener(task -> {
                        runOnUiThread(() -> {
                            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    Pregunta pregunta = doc.toObject(Pregunta.class);
                                    if (pregunta != null) {
                                        listaPreguntas.add(pregunta);
                                    }
                                }
                                mostrarPreguntaActual();
                            } else {
                                Toast.makeText(JuegoTrivia.this, "Error al cargar las preguntas", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }

    private void mostrarPreguntaActual() {
        if (listaPreguntas.isEmpty()) return;

        rgOpciones.clearCheck();

        Pregunta pregunta = listaPreguntas.get(indicePreguntaActual);

        tvContadorPreguntas.setText("Pregunta " + (indicePreguntaActual + 1) + " de " + listaPreguntas.size());
        tvEnunciado.setText(pregunta.getEnunciado());

        List<String> opciones = pregunta.getOpciones();
        rbOpcion1.setText(opciones.get(0));
        rbOpcion2.setText(opciones.get(1));
        rbOpcion3.setText(opciones.get(2));
        rbOpcion4.setText(opciones.get(3));

        if (indicePreguntaActual == listaPreguntas.size() - 1) {
            btnSiguiente.setText("Finalizar y Sumar Puntos");
        } else {
            btnSiguiente.setText("Siguiente Pregunta");
        }
    }

    private void procesarRespuesta() {
        int idSeleccionado = rgOpciones.getCheckedRadioButtonId();

        if (idSeleccionado == -1) {
            Toast.makeText(this, "Por favor, selecciona una respuesta", Toast.LENGTH_SHORT).show();
            return;
        }

        int indiceSeleccionado = -1;
        if (idSeleccionado == R.id.rbOpcion1) indiceSeleccionado = 0;
        else if (idSeleccionado == R.id.rbOpcion2) indiceSeleccionado = 1;
        else if (idSeleccionado == R.id.rbOpcion3) indiceSeleccionado = 2;
        else if (idSeleccionado == R.id.rbOpcion4) indiceSeleccionado = 3;

        Pregunta preguntaActual = listaPreguntas.get(indicePreguntaActual);
        if (indiceSeleccionado == preguntaActual.getCorrecta()) {
            puntosGanados += 10;
        }
        if (indicePreguntaActual < listaPreguntas.size() - 1) {
            indicePreguntaActual++;
            mostrarPreguntaActual();
        } else {
            finalizarJuegoYSubirPuntos();
        }
    }

    private void finalizarJuegoYSubirPuntos() {
        btnSiguiente.setEnabled(false); // Evitamos múltiples clics molestos

        String campoCasa = casaUsuario.toLowerCase().trim();

        //hilo secundario
        Executors.newSingleThreadExecutor().execute(() -> {
            FirebaseFirestore.getInstance().collection("copa_casas")
                    .document("totales")
                    .update(campoCasa, FieldValue.increment(puntosGanados))
                    .addOnCompleteListener(task -> {
                        runOnUiThread(() -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(JuegoTrivia.this, "¡Sumaste " + puntosGanados + " puntos para " + casaUsuario + "!", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(JuegoTrivia.this, RankingCasas.class);
                                startActivity(intent);

                                finish();
                            } else {
                                Toast.makeText(JuegoTrivia.this, "Error al subir los puntos a la Copa", Toast.LENGTH_SHORT).show();
                                btnSiguiente.setEnabled(true);
                            }
                        });
                    });
        });
    }
}