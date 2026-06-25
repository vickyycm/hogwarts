package com.hogwarts.app.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hogwarts.app.R;
import com.hogwarts.app.model.Usuario;
import com.hogwarts.app.ui.home.Home;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Login extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnGoogleSignIn;
    private ProgressBar progressBar;
    private TextView tvIrARegistro;
    private GoogleSignInClient googleSignInClient;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private FirebaseAuth mAuth;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        manejarResultadoGoogle(task);
                    } else {
                        mostrarCargando(false);
                    }
                }
        );

        executorService = Executors.newSingleThreadExecutor();

        inicializarVistas();

        btnLogin.setOnClickListener(v -> intentarLogin());

        tvIrARegistro.setOnClickListener(v ->
                startActivity(new Intent(Login.this, Signin.class))
        );

        btnGoogleSignIn.setOnClickListener(v -> {
            mostrarCargando(true);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void inicializarVistas() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.progressBar);
        tvIrARegistro = findViewById(R.id.tvIrARegistro);
    }

    private void intentarLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        mostrarCargando(true);

        //hilo secundario
        executorService.execute(() -> {
            boolean esValido = validarCampos(email, password);

            runOnUiThread(() -> {
                if (!esValido) {
                    mostrarCargando(false);
                    return;
                }
                autenticarConFirebase(email, password);
            });
        });
    }

    private boolean validarCampos(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            runOnUiThread(() -> Toast.makeText(this, "Completá todos los campos", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            runOnUiThread(() -> Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show());
            return false;
        }
        if (password.length() < 6) {
            runOnUiThread(() -> Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show());
            return false;
        }
        return true;
    }

    private void autenticarConFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    mostrarCargando(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "¡Bienvenido a Hogwarts!", Toast.LENGTH_SHORT).show();
                        irAHome();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void manejarResultadoGoogle(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            autenticarConGoogleEnFirebase(account);
        } catch (ApiException e) {
            mostrarCargando(false);
            Toast.makeText(this, "Error con Google Sign-In: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void autenticarConGoogleEnFirebase(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean esNuevoUsuario = task.getResult().getAdditionalUserInfo().isNewUser();
                        String uid = mAuth.getCurrentUser().getUid();
                        String nombre = account.getDisplayName();
                        String email = account.getEmail();
                        String fotoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "";

                        if (esNuevoUsuario) {
                            guardarUsuarioGoogleEnFirestore(uid, nombre, email, fotoUrl);
                        } else {
                            mostrarCargando(false);
                            Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show();
                            irAHome();
                        }
                    } else {
                        mostrarCargando(false);
                        Toast.makeText(this, "Error de autenticación", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void guardarUsuarioGoogleEnFirestore(String uid, String nombre, String email, String fotoUrl) {
        Usuario nuevoUsuario = new Usuario(uid, nombre, email, "", fotoUrl);

        db.collection("usuarios")
                .document(uid)
                .set(nuevoUsuario)
                .addOnCompleteListener(task -> {
                    mostrarCargando(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "¡Bienvenido a Hogwarts!", Toast.LENGTH_SHORT).show();
                        irAHome();
                    } else {
                        Toast.makeText(this, "Error guardando datos del usuario", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void irAHome() {
        Intent intent = new Intent(Login.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!cargando);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}