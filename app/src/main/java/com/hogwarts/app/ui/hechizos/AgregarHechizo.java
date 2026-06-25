package com.hogwarts.app.ui.hechizos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hogwarts.app.R;
import com.hogwarts.app.model.Hechizo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgregarHechizo extends AppCompatActivity {

    private TextInputEditText etNombre, etAnotaciones;
    private AutoCompleteTextView etTipo;
    private Button btnGuardar, btnCancelar;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ExecutorService executorService;
    private String usuarioUID;

    private String hechizoId = null;
    private boolean esEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_hechizo);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        usuarioUID = mAuth.getCurrentUser().getUid();

        inicializarVistas();
        configurarSpinner();

        // === REVISAR SI VIENE UN ID PARA EDITAR ===
        if (getIntent().hasExtra("HECHIZO_ID")) {
            hechizoId = getIntent().getStringExtra("HECHIZO_ID");
            esEdicion = true;
            btnGuardar.setText("Actualizar"); // Cambiamos el texto del botón por UX
            cargarDatosHechizo();
        }

        btnGuardar.setOnClickListener(v -> intentarGuardar());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etTipo = findViewById(R.id.etTipo);
        etAnotaciones = findViewById(R.id.etAnotaciones);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void configurarSpinner() {
        String[] tipos = {"Hechizo", "Poción", "Encantamiento", "Maldición", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                tipos
        );
        etTipo.setAdapter(adapter);
        etTipo.setText("Hechizo", false);
    }

    private void cargarDatosHechizo() {
        mostrarCargando(true);
        executorService.execute(() -> {
            db.collection("hechizos")
                    .document(hechizoId)
                    .get()
                    .addOnCompleteListener(task -> {
                        runOnUiThread(() -> {
                            mostrarCargando(false);
                            if (task.isSuccessful() && task.getResult() != null) {
                                Hechizo hechizo = task.getResult().toObject(Hechizo.class);
                                if (hechizo != null) {
                                    etNombre.setText(hechizo.getNombre());
                                    etTipo.setText(hechizo.getTipo(), false);
                                    etAnotaciones.setText(hechizo.getAnotaciones());
                                }
                            } else {
                                Toast.makeText(this, "Error al cargar los datos del hechizo", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }

    private void intentarGuardar() {
        String nombre = etNombre.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();
        String anotaciones = etAnotaciones.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "Completá el nombre del hechizo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tipo)) {
            Toast.makeText(this, "Elegí un tipo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(anotaciones)) {
            Toast.makeText(this, "Agregá anotaciones", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarCargando(true);

        Hechizo hechizo = new Hechizo(usuarioUID, nombre, tipo, anotaciones);
        hechizo.setFechaCreacion(com.google.firebase.Timestamp.now());

        if (esEdicion) {
            executorService.execute(() -> {
                db.collection("hechizos")
                        .document(hechizoId)
                        .set(hechizo)
                        .addOnCompleteListener(task -> {
                            runOnUiThread(() -> {
                                mostrarCargando(false);
                                if (task.isSuccessful()) {
                                    Toast.makeText(AgregarHechizo.this, "¡Hechizo actualizado!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(AgregarHechizo.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
            });
        } else {
            db.collection("hechizos")
                    .add(hechizo)
                    .addOnCompleteListener(task -> {
                        mostrarCargando(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(AgregarHechizo.this, "¡Hechizo guardado!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AgregarHechizo.this, "Error al guardar", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        mostrarCargando(false);
                        Toast.makeText(AgregarHechizo.this, "Error Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void mostrarCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!cargando);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}