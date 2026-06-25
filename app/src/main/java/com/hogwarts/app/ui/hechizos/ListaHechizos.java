package com.hogwarts.app.ui.hechizos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hogwarts.app.R;
import com.hogwarts.app.model.Hechizo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaHechizos extends AppCompatActivity implements ListaHechizosAdapter.OnHechizoClickListener {

    private RecyclerView rvHechizos;
    private FloatingActionButton fabAgregarHechizo;
    private ProgressBar progressBar;
    private ListaHechizosAdapter adapter;
    private List<Hechizo> listaHechizos;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ExecutorService executorService;
    private String usuarioUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_hechizos);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        usuarioUID = mAuth.getCurrentUser().getUid();

        inicializarVistas();
        configurarRecyclerView();
        cargarHechizos();

        fabAgregarHechizo.setOnClickListener(v ->
                startActivity(new Intent(ListaHechizos.this, AgregarHechizo.class))
        );
    }

    private void inicializarVistas() {
        rvHechizos = findViewById(R.id.rvHechizos);
        fabAgregarHechizo = findViewById(R.id.fabAgregarHechizo);
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
    }

    private void configurarRecyclerView() {
        listaHechizos = new ArrayList<>();
        adapter = new ListaHechizosAdapter(listaHechizos, this);
        rvHechizos.setAdapter(adapter);
        rvHechizos.setLayoutManager(new LinearLayoutManager(this));
    }

    private void cargarHechizos() {
        mostrarCargando(true);

        executorService.execute(() -> {
            db.collection("hechizos")
                    .whereEqualTo("uid", usuarioUID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot resultado = task.getResult();
                            List<Hechizo> hechizos = new ArrayList<>();

                            for (int i = 0; i < resultado.size(); i++) {
                                Hechizo hechizo = resultado.getDocuments().get(i).toObject(Hechizo.class);
                                if (hechizo != null) {
                                    hechizo.setId(resultado.getDocuments().get(i).getId());
                                    hechizos.add(hechizo);
                                }
                            }

                            runOnUiThread(() -> {
                                adapter.actualizarLista(hechizos);
                                mostrarCargando(false);
                                if (hechizos.isEmpty()) {
                                    Toast.makeText(ListaHechizos.this, "Sin hechizos guardados aún", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                mostrarCargando(false);
                                Toast.makeText(ListaHechizos.this, "Error al cargar hechizos", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });
    }

    @Override
    public void onEliminarClick(int position, String hechizoId) {
        mostrarCargando(true);

        executorService.execute(() -> {
            db.collection("hechizos")
                    .document(hechizoId)
                    .delete()
                    .addOnCompleteListener(task -> {
                        runOnUiThread(() -> {
                            mostrarCargando(false);
                            if (task.isSuccessful()) {
                                Toast.makeText(ListaHechizos.this, "Hechizo eliminado", Toast.LENGTH_SHORT).show();
                                cargarHechizos();
                            } else {
                                Toast.makeText(ListaHechizos.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }

    @Override
    public void onEditarClick(Hechizo hechizo) {
        Intent intent = new Intent(ListaHechizos.this, AgregarHechizo.class);
        intent.putExtra("HECHIZO_ID", hechizo.getId());
        startActivity(intent);
    }

    @Override
    public void onItemClick(Hechizo hechizo) {
        Toast.makeText(this, hechizo.getNombre(), Toast.LENGTH_SHORT).show();
    }

    private void mostrarCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        fabAgregarHechizo.setEnabled(!cargando);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarHechizos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}