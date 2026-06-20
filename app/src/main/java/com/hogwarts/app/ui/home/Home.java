package com.hogwarts.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
            Toast.makeText(this, "Cuaderno de Hechizos", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnTalleres).setOnClickListener(v -> {
            Toast.makeText(this, "Talleres", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnForo).setOnClickListener(v -> {
            Toast.makeText(this, "Foro de Novedades", Toast.LENGTH_SHORT).show();
        });
    }
}