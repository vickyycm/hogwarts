package com.hogwarts.app.ui.foro;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hogwarts.app.R;
import com.hogwarts.app.model.Novedad;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForoNovedades extends AppCompatActivity {

    private RecyclerView rvNovedades;
    private ProgressBar progressBar;
    private ForoNovedadesAdapter adapter;
    private List<Novedad> listaNovedades;

    private FirebaseFirestore db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foro_novedades);

        db = FirebaseFirestore.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        rvNovedades = findViewById(R.id.rvNovedades);
        progressBar = findViewById(R.id.progressBarForo);
        findViewById(R.id.btnVolverForo).setOnClickListener(v -> finish());

        configurarRecyclerView();
        cargarNovedadesDesdeFirebase();
    }

    private void configurarRecyclerView() {
        listaNovedades = new ArrayList<>();
        adapter = new ForoNovedadesAdapter(listaNovedades);
        rvNovedades.setAdapter(adapter);
        rvNovedades.setLayoutManager(new LinearLayoutManager(this));
    }

    private void cargarNovedadesDesdeFirebase() {
        progressBar.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            db.collection("novedades")
                    .orderBy("fecha", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful() && task.getResult() != null) {
                                List<Novedad> novedadesBase = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Novedad novedad = document.toObject(Novedad.class);
                                    novedad.setId(document.getId());
                                    novedadesBase.add(novedad);
                                }
                                adapter.actualizarLista(novedadesBase);
                                if (novedadesBase.isEmpty()) {
                                    Toast.makeText(ForoNovedades.this, "Cartelera de novedades vacía", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ForoNovedades.this, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}