package com.example.apmojocoya;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Button btnRegistrar, btnVerUsuarios, btnExportarPadron, btnTomarLecturas, btnConfigurarTarifa, btnGastos, btnCobranza, btnCartaAlcaldia;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        // 1. Vincular los botones con el diseño XML
        btnRegistrar = findViewById(R.id.btn_registrar_usuario);
        btnVerUsuarios = findViewById(R.id.btn_ver_usuarios);
        btnExportarPadron = findViewById(R.id.btn_exportar_padron);
        btnTomarLecturas = findViewById(R.id.btn_tomar_lecturas);
        btnConfigurarTarifa = findViewById(R.id.btn_guardar_tarifa);
        btnCobranza = findViewById(R.id.btn_cobranza);
        btnCobranza.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CobranzaActivity.class)));
        btnGastos = findViewById(R.id.btn_gastos); // Crea este botón en tu XML
        btnGastos.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GastosActivity.class)));
        btnCartaAlcaldia = findViewById(R.id.btn_carta_alcaldia);
        btnCartaAlcaldia.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, InstitucionActivity.class))
        );
        // 2. Configurar las acciones (Navegación)

        // Ir a Registrar Usuario
        btnRegistrar.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, NewUserActivity.class))
        );

        // Ir a Ver Lista de Usuarios
        btnVerUsuarios.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UserActivity.class))
        );

        // Ir a Tomar Lecturas
        btnTomarLecturas.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, YearActivity.class))
        );

        // Ir a Configurar Precios
        btnConfigurarTarifa.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ConfigTarifaActivity.class))
        );

        // --- CAMBIO IMPORTANTE AQUÍ ---
        // Ir a la nueva pantalla de Reportes (Excel)
        // Ya no llamamos a 'exportarDatos' directamente porque necesitamos elegir el mes primero.
        btnExportarPadron.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent);
        });
    }
}


//
//FASE 3: Instituciones y Documentos
//
//Poner la etiqueta "Institución" a ciertos usuarios.
//
//Generar las cartas formales PDF.