package com.example.apmojocoya;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private Spinner spAnio, spMes;
    private Button btnGenerar;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private Tarifa tarifaRef; // Para saber cuánto es el fijo (10 Bs)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = FirebaseFirestore.getInstance();
        spAnio = findViewById(R.id.sp_reporte_anio);
        spMes = findViewById(R.id.sp_reporte_mes);
        btnGenerar = findViewById(R.id.btn_generar_excel);
        progressBar = findViewById(R.id.progress_reporte);

        configurarSpinners();

        // Cargar tarifa al inicio para saber cómo dividir los montos
        db.collection("configuracion").document("tarifa_actual").get()
                .addOnSuccessListener(doc -> {
                    if(doc.exists()) tarifaRef = doc.toObject(Tarifa.class);
                });

        btnGenerar.setOnClickListener(v -> generarReporte());
    }

    private void configurarSpinners() {
        // AÑOS: Cambiamos 2024 por 2020 para que salgan los anteriores
        List<String> anios = new ArrayList<>();
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);

        // Si estamos en 2026, esto mostrará: 2020, 2021, ... 2026
        for(int i = 2020; i <= anioActual; i++) {
            anios.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapterAnio = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, anios);
        adapterAnio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAnio.setAdapter(adapterAnio);

        // Seleccionar por defecto el año actual (el último de la lista)
        spAnio.setSelection(anios.size() - 1);

        // MESES
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        ArrayAdapter<String> adapterMes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, meses);
        adapterMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMes.setAdapter(adapterMes);

        // Seleccionar mes actual
        int mesActual = Calendar.getInstance().get(Calendar.MONTH);
        spMes.setSelection(mesActual);
    }

    private void generarReporte() {
        if (tarifaRef == null) {
            Toast.makeText(this, "Cargando tarifas, espera un segundo...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGenerar.setEnabled(false);

        int anio = Integer.parseInt(spAnio.getSelectedItem().toString());
        int mes = spMes.getSelectedItemPosition() + 1;

        // 1. Descargar Usuarios (Para los nombres)
        db.collection("users").get().addOnSuccessListener(userSnaps -> {
            Map<String, String> mapNombres = new HashMap<>();
            for(DocumentSnapshot doc : userSnaps) {
                mapNombres.put(doc.getId(), doc.getString("nombre") + " " + doc.getString("apellidos"));
            }

            // 2. Descargar Lecturas
            db.collection("lecturas")
                    .whereEqualTo("anio", anio)
                    .whereEqualTo("mes", mes)
                    .get()
                    .addOnSuccessListener(lecturasSnaps -> {
                        if (lecturasSnaps.isEmpty()) {
                            terminar(false, "No hay datos en esa fecha");
                            return;
                        }

                        List<ReporteItem> listaFinal = new ArrayList<>();
                        int contador = 1;

                        for(DocumentSnapshot doc : lecturasSnaps) {
                            Lectura lec = doc.toObject(Lectura.class);
                            if (lec == null) continue;

                            String nombre = mapNombres.get(lec.getUsuarioId());
                            if (nombre == null) nombre = "Usuario Eliminado";

                            double consumo = lec.getLecturaActual() - lec.getLecturaAnterior();
                            if(consumo < 0) consumo = 0;

                            // --- LÓGICA DE DIVISIÓN DE MONTOS ---
                            // Replicamos tu Excel: Separar Fijo vs Exceso
                            double montoFijo = tarifaRef.getCargoFijo(); // Ej: 10
                            double montoTotal = lec.getMontoTotal();
                            double montoExceso = 0;

                            if (montoTotal <= montoFijo) {
                                montoFijo = montoTotal; // Si pagó menos del fijo (raro), todo es fijo
                                montoExceso = 0;
                            } else {
                                montoExceso = montoTotal - montoFijo;
                            }

                            // OBS
                            String obs = lec.getEstado();
                            if("Normal".equals(obs)) obs = "1"; // En tu excel pones '1' o vacio

                            listaFinal.add(new ReporteItem(
                                    contador++,
                                    nombre,
                                    lec.getLecturaAnterior(),
                                    lec.getLecturaActual(),
                                    consumo,
                                    montoFijo,
                                    montoExceso,
                                    montoTotal,
                                    obs
                            ));
                        }

                        // Ordenar por nombre (Opcional, pero se ve mejor)
                        Collections.sort(listaFinal, (o1, o2) -> o1.getNombreSocio().compareToIgnoreCase(o2.getNombreSocio()));

                        // 3. Generar Excel
                        String path = ExcelReportGenerator.generarReporteMensual(this, listaFinal, anio, mes);

                        if (path != null) terminar(true, "Guardado en: " + path);
                        else terminar(false, "Error al crear archivo");
                    });
        });
    }

    private void terminar(boolean exito, String msg) {
        progressBar.setVisibility(View.GONE);
        btnGenerar.setEnabled(true);
        Toast.makeText(this, msg, exito ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }
}