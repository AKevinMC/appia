package com.example.apmojocoya.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import com.example.apmojocoya.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseFirestore db;
    private CardView cardLecturas, cardCobranza, cardSocios, cardGastos;
    private Button btnRegistrarRapido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar Drawer (Menú Lateral)
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Vincular Tarjetas del Dashboard
        cardLecturas = findViewById(R.id.card_lecturas);
        cardCobranza = findViewById(R.id.card_cobranza);
        cardSocios = findViewById(R.id.card_socios);
        cardGastos = findViewById(R.id.card_gastos);
        btnRegistrarRapido = findViewById(R.id.btn_registrar_rapido);

        // Configurar acciones del Dashboard
        cardLecturas.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, YearActivity.class)));
        cardCobranza.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CobranzaActivity.class)));
        cardSocios.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, UserActivity.class)));
        cardGastos.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GastosActivity.class)));
        btnRegistrarRapido.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NewUserActivity.class)));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_reportes) {
            startActivity(new Intent(this, ReportActivity.class));
        } else if (id == R.id.nav_carta_alcaldia) {
            startActivity(new Intent(this, InstitucionActivity.class));
        } else if (id == R.id.nav_tarifa) {
            startActivity(new Intent(this, ConfigTarifaActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}