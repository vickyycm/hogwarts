package com.hogwarts.app.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hogwarts.app.R;
import com.hogwarts.app.model.Usuario;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Signin extends AppCompatActivity {

    private TextInputEditText etNombre, etEmail, etPassword, etConfirmPassword;
    private Button btnRegistrarse;
    private ProgressBar progressBar;
    private TextView tvIrALogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        executorService = Executors.newSingleThreadExecutor();

        inicializarVistas();

        btnRegistrarse.setOnClickListener(v -> intentarRegistro());

        tvIrALogin.setOnClickListener(v -> finish());
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        progressBar = findViewById(R.id.progressBar);
        tvIrALogin = findViewById(R.id.tvIrALogin);
    }

    private void intentarRegistro() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        mostrarCargando(true);

        // Hilo secundario
        executorService.execute(() -> {
            String errorValidacion = validarCampos(nombre, email, password, confirmPassword);

            runOnUiThread(() -> {
                if (errorValidacion != null) {
                    mostrarCargando(false);
                    Toast.makeText(this, errorValidacion, Toast.LENGTH_SHORT).show();
                    return;
                }
                registrarEnFirebase(nombre, email, password);
            });
        });
    }

    private String validarCampos(String nombre, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            return "Completá todos los campos";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Email inválido";
        }
        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }
        if (!password.equals(confirmPassword)) {
            return "Las contraseñas no coinciden";
        }
        return null;
    }

    private void registrarEnFirebase(String nombre, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            guardarUsuarioEnFirestore(firebaseUser.getUid(), nombre, email);
                        }
                    } else {
                        mostrarCargando(false);
                        manejarErrorRegistro(task.getException());
                    }
                });
    }

    private void guardarUsuarioEnFirestore(String uid, String nombre, String email) {
        Usuario nuevoUsuario = new Usuario(uid, nombre, email, "", "");

        db.collection("usuarios")
                .document(uid)
                .set(nuevoUsuario)
                .addOnCompleteListener(task -> {
                    mostrarCargando(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "¡Cuenta creada! Bienvenido a Hogwarts", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error guardando datos del usuario", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void manejarErrorRegistro(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(this, "Ya existe una cuenta con ese email", Toast.LENGTH_LONG).show();
        } else {
            String mensaje = exception != null ? exception.getMessage() : "Error desconocido";
            Toast.makeText(this, "Error: " + mensaje, Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnRegistrarse.setEnabled(!cargando);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}