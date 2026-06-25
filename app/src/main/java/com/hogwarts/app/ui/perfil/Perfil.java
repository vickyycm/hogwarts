package com.hogwarts.app.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hogwarts.app.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Perfil extends AppCompatActivity {

    private TextInputEditText etEmail, etNombre;
    private AutoCompleteTextView etCasa;
    private Button btnGuardar, btnEliminar, btnVolver;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ExecutorService executorService;
    private String usuarioUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        inicializarVistas();
        configurarSpinnerCasas();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            usuarioUID = user.getUid();
            etEmail.setText(user.getEmail());
            cargarDatosPerfil();
        } else {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnGuardar.setOnClickListener(v -> guardarDatosPerfil());
        btnEliminar.setOnClickListener(v -> confirmarEliminacionCuenta());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void inicializarVistas() {
        etEmail = findViewById(R.id.etPerfilEmail);
        etNombre = findViewById(R.id.etPerfilNombre);
        etCasa = findViewById(R.id.etPerfilCasa);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);
        btnEliminar = findViewById(R.id.btnEliminarCuenta);
        btnVolver = findViewById(R.id.btnVolverPerfil);
        progressBar = findViewById(R.id.progressBarPerfil);
    }

    private void configurarSpinnerCasas() {
        String[] casas = {"Gryffindor", "Slytherin", "Ravenclaw", "Hufflepuff"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                casas
        );
        etCasa.setAdapter(adapter);
    }

    private void cargarDatosPerfil() {
        mostrarCargando(true);
        executorService.execute(() -> {
            db.collection("usuarios")
                    .document(usuarioUID)
                    .get()
                    .addOnCompleteListener(task -> {
                        runOnUiThread(() -> {
                            mostrarCargando(false);
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    etNombre.setText(document.getString("nombre"));
                                    String casaGuardada = document.getString("casa");
                                    if (casaGuardada != null) {
                                        etCasa.setText(casaGuardada, false);
                                    }
                                }
                            } else {
                                Toast.makeText(Perfil.this, "Error al leer datos de Firestore", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }

    private void guardarDatosPerfil() {
        String nombre = etNombre.getText().toString().trim();
        String casa = etCasa.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(casa)) {
            Toast.makeText(this, "Seleccioná tu casa de Hogwarts", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarCargando(true);

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("nombre", nombre);
        datosUsuario.put("casa", casa);
        datosUsuario.put("email", etEmail.getText().toString());

        executorService.execute(() -> {
            db.collection("usuarios")
                    .document(usuarioUID)
                    .set(datosUsuario) // Modifica el documento existente en Firestore
                    .addOnCompleteListener(task -> {
                        runOnUiThread(() -> {
                            mostrarCargando(false);
                            if (task.isSuccessful()) {
                                Toast.makeText(Perfil.this, "¡Perfil actualizado en el servidor!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Perfil.this, "Error al guardar los cambios", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }

    private void confirmarEliminacionCuenta() {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar cuenta definitivamente?")
                .setMessage("Esta acción es irreversible. Perderás todos tus registros y serás expulsado de Hogwarts.")
                .setPositiveButton("Sí, eliminar", (d, which) -> ejecutarEliminacionCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarEliminacionCuenta() {
        mostrarCargando(true);
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            executorService.execute(() -> {
                db.collection("usuarios")
                        .document(usuarioUID)
                        .delete()
                        .addOnCompleteListener(taskFirestore -> {
                            if (taskFirestore.isSuccessful()) {
                                user.delete().addOnCompleteListener(taskAuth -> {
                                    runOnUiThread(() -> {
                                        mostrarCargando(false);
                                        if (taskAuth.isSuccessful()) {
                                            Toast.makeText(Perfil.this, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show();
                                            redireccionarAlLogin();
                                        } else {
                                            Toast.makeText(Perfil.this, "Error de seguridad. Reautenticate e intentalo de nuevo.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                });
                            } else {
                                runOnUiThread(() -> {
                                    mostrarCargando(false);
                                    Toast.makeText(Perfil.this, "Error al remover datos del servidor", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
            });
        }
    }

    private void redireccionarAlLogin() {
        Intent intent = new Intent(Perfil.this, com.hogwarts.app.ui.auth.Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!cargando);
        btnEliminar.setEnabled(!cargando);
        btnVolver.setEnabled(!cargando);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}