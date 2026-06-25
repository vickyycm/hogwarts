package com.hogwarts.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.hogwarts.app.ui.hechizos.ListaHechizos;
import com.hogwarts.app.ui.auth.Login;
import androidx.appcompat.app.AppCompatActivity;
import com.hogwarts.app.ui.perfil.Perfil;
import com.hogwarts.app.ui.foro.ForoNovedades;
import com.google.firebase.auth.FirebaseAuth;
import com.hogwarts.app.R;

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

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, com.hogwarts.app.ui.auth.Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}