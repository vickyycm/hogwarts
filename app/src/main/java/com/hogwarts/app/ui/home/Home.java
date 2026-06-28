package com.hogwarts.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.hogwarts.app.ui.hechizos.ListaHechizos;
import com.hogwarts.app.ui.auth.Login;
import androidx.appcompat.app.AppCompatActivity;
import com.hogwarts.app.ui.perfil.Perfil;
import com.hogwarts.app.ui.foro.ForoNovedades;
import com.hogwarts.app.ui.trivia.JuegoTrivia;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hogwarts.app.R;
import java.util.concurrent.Executors;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        inicializarBotones();
    }

    private void inicializarBotones() {
        findViewById(R.id.btnHechizos).setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaHechizos.class);
            startActivity(intent);
        });

        findViewById(R.id.btnPerfil).setOnClickListener(v -> {
            Intent intent = new Intent(this, Perfil.class);
            startActivity(intent);
        });

        findViewById(R.id.btnForo).setOnClickListener(v -> {
            Intent intent = new Intent(this, ForoNovedades.class);
            startActivity(intent);
        });

        findViewById(R.id.btnJugarTrivia).setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //hilo secundario
            Executors.newSingleThreadExecutor().execute(() -> {
                FirebaseFirestore.getInstance().collection("usuarios")
                        .document(uid)
                        .get()
                        .addOnCompleteListener(task -> {
                            runOnUiThread(() -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    String casaUsuario = task.getResult().getString("casa");

                                    if (casaUsuario == null || casaUsuario.isEmpty()) {
                                        Toast.makeText(Home.this, "¡Aún no has seleccionado tu casa! Completá tu perfil primero.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Intent intent = new Intent(Home.this, JuegoTrivia.class);
                                        intent.putExtra("CASA_USUARIO", casaUsuario);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(Home.this, "Error al verificar tu perfil", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
            });
        });

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}