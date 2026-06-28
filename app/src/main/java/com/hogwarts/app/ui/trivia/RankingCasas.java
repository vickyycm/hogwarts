package com.hogwarts.app.ui.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hogwarts.app.R;
import com.hogwarts.app.ui.home.Home;
import java.util.concurrent.Executors;

public class RankingCasas extends AppCompatActivity {

    private TextView tvPuntosGryffindor, tvPuntosSlytherin, tvPuntosRavenclaw, tvPuntosHufflepuff;
    private Button btnVolverHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_casas);

        inicializarComponentes();
        obtenerPuntajesGlobales();

        btnVolverHome.setOnClickListener(v -> {
            Intent intent = new Intent(RankingCasas.this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void inicializarComponentes() {
        tvPuntosGryffindor = findViewById(R.id.tvPuntosGryffindor);
        tvPuntosSlytherin = findViewById(R.id.tvPuntosSlytherin);
        tvPuntosRavenclaw = findViewById(R.id.tvPuntosRavenclaw);
        tvPuntosHufflepuff = findViewById(R.id.tvPuntosHufflepuff);
        btnVolverHome = findViewById(R.id.btnVolverHome);
    }

    private void obtenerPuntajesGlobales() {
        //hilo secundario
        Executors.newSingleThreadExecutor().execute(() -> {
            FirebaseFirestore.getInstance().collection("copa_casas")
                    .document("totales")
                    .get()
                    .addOnCompleteListener(task -> {
                        runOnUiThread(() -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot doc = task.getResult();

                                Long gryffindor = doc.getLong("gryffindor");
                                Long slytherin = doc.getLong("slytherin");
                                Long ravenclaw = doc.getLong("ravenclaw");
                                Long hufflepuff = doc.getLong("hufflepuff");

                                tvPuntosGryffindor.setText((gryffindor != null ? gryffindor : 0) + " pts");
                                tvPuntosSlytherin.setText((slytherin != null ? slytherin : 0) + " pts");
                                tvPuntosRavenclaw.setText((ravenclaw != null ? ravenclaw : 0) + " pts");
                                tvPuntosHufflepuff.setText((hufflepuff != null ? hufflepuff : 0) + " pts");

                            } else {
                                Toast.makeText(RankingCasas.this, "Error al cargar los puntajes de la copa", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }
}