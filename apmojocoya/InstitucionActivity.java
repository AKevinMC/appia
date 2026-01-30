package com.example.apmojocoya;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstitucionActivity extends AppCompatActivity {

    private Spinner spAnio;
    private EditText etAlcalde, etPresiNombre, etPresiCI, etTesoNombre, etTesoCI;
    private Button btnGenerar;
    private TextView tvStatus;
    private FirebaseFirestore db;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_institucion_report);

        db = FirebaseFirestore.getInstance();
        spAnio = findViewById(R.id.sp_anio_institucion);

        etAlcalde = findViewById(R.id.et_alcalde_nombre);
        etPresiNombre = findViewById(R.id.et_presi_nombre);
        etPresiCI = findViewById(R.id.et_presi_ci);
        etTesoNombre = findViewById(R.id.et_tesorero_nombre);
        etTesoCI = findViewById(R.id.et_tesorero_ci);

        btnGenerar = findViewById(R.id.btn_generar_carta);
        tvStatus = findViewById(R.id.tv_status);

        configurarSpinner();
        cargarDatosAutoridades(); // Cargar datos guardados previamente

        btnGenerar.setOnClickListener(v -> verificarPermisosYGenerar());
    }

    private void configurarSpinner() {
        List<String> anios = new ArrayList<>();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2020; i <= year + 1; i++) {
            anios.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, anios);
        spAnio.setAdapter(adapter);
        spAnio.setSelection(anios.size() - 2);
    }

    private void cargarDatosAutoridades() {
        db.collection("configuracion").document("autoridades")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        etAlcalde.setText(document.getString("alcalde_nombre"));
                        etPresiNombre.setText(document.getString("presidente_nombre"));
                        etPresiCI.setText(document.getString("presidente_ci"));
                        etTesoNombre.setText(document.getString("tesorero_nombre"));
                        etTesoCI.setText(document.getString("tesorero_ci"));
                    }
                });
    }

    private void guardarDatosAutoridades() {
        Map<String, Object> data = new HashMap<>();
        data.put("alcalde_nombre", etAlcalde.getText().toString());
        data.put("presidente_nombre", etPresiNombre.getText().toString());
        data.put("presidente_ci", etPresiCI.getText().toString());
        data.put("tesorero_nombre", etTesoNombre.getText().toString());
        data.put("tesorero_ci", etTesoCI.getText().toString());

        db.collection("configuracion").document("autoridades")
                .set(data, SetOptions.merge());
    }

    private void verificarPermisosYGenerar() {
        // --- 1. VALIDACIÓN DE CAMPOS ---
        if (etAlcalde.getText().toString().trim().isEmpty()) {
            etAlcalde.setError("El nombre del Alcalde es obligatorio");
            etAlcalde.requestFocus();
            return;
        }
        if (etPresiNombre.getText().toString().trim().isEmpty()) {
            etPresiNombre.setError("Nombre del Presidente/a requerido");
            etPresiNombre.requestFocus();
            return;
        }
        if (etPresiCI.getText().toString().trim().isEmpty()) {
            etPresiCI.setError("C.I. del Presidente/a requerido");
            etPresiCI.requestFocus();
            return;
        }
        if (etTesoNombre.getText().toString().trim().isEmpty()) {
            etTesoNombre.setError("Nombre del Tesorero/a requerido");
            etTesoNombre.requestFocus();
            return;
        }
        if (etTesoCI.getText().toString().trim().isEmpty()) {
            etTesoCI.setError("C.I. del Tesorero/a requerido");
            etTesoCI.requestFocus();
            return;
        }

        // --- 2. SI TODO ESTÁ BIEN, PROCEDEMOS ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            procesarCobroAnual();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                procesarCobroAnual();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                procesarCobroAnual();
            } else {
                Toast.makeText(this, "Permiso denegado. No se puede guardar el Excel.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void procesarCobroAnual() {
        // Guardamos los datos antes de generar para la próxima vez
        guardarDatosAutoridades();

        btnGenerar.setEnabled(false);
        tvStatus.setText("Obteniendo datos...");
        int anioSel = Integer.parseInt(spAnio.getSelectedItem().toString());

        // Recolectar datos de los EditTexts
        String alcalde = etAlcalde.getText().toString();
        String presiN = etPresiNombre.getText().toString();
        String presiCI = etPresiCI.getText().toString();
        String tesoN = etTesoNombre.getText().toString();
        String tesoCI = etTesoCI.getText().toString();

        db.collection("users")
                .whereEqualTo("tipo", "Institucion")
                .get()
                .addOnSuccessListener(userSnaps -> {
                    if (userSnaps.isEmpty()) {
                        Toast.makeText(this, "No hay instituciones registradas", Toast.LENGTH_SHORT).show();
                        btnGenerar.setEnabled(true);
                        return;
                    }

                    Map<String, InstitucionRow> mapaFilas = new HashMap<>();

                    for (DocumentSnapshot doc : userSnaps) {
                        String uid = doc.getId();
                        String nombre = doc.getString("apellidos");
                        if (nombre == null || nombre.isEmpty()) {
                            nombre = doc.getString("nombre");
                        }
                        if (nombre == null) nombre = "Institución S/N";

                        mapaFilas.put(uid, new InstitucionRow(nombre));
                    }

                    tvStatus.setText("Consultando lecturas...");

                    db.collection("lecturas")
                            .whereEqualTo("anio", anioSel)
                            .get()
                            .addOnSuccessListener(lecturaSnaps -> {

                                for (DocumentSnapshot doc : lecturaSnaps) {
                                    String uid = doc.getString("usuarioId");
                                    if (mapaFilas.containsKey(uid)) {
                                        Lectura lectura = doc.toObject(Lectura.class);
                                        if (lectura != null) {
                                            mapaFilas.get(uid).addLectura(lectura.getMes(), lectura);
                                        }
                                    }
                                }

                                List<InstitucionRow> listaFinal = new ArrayList<>(mapaFilas.values());

                                if (listaFinal.isEmpty()) {
                                    Toast.makeText(this, "No se encontraron datos", Toast.LENGTH_SHORT).show();
                                    tvStatus.setText("Sin datos para generar.");
                                } else {
                                    tvStatus.setText("Generando Excel...");

                                    // Pasamos los nuevos datos al generador
                                    String resultado = ExcelInstitucionGenerator.generarReporte(
                                            this, anioSel, listaFinal,
                                            alcalde, presiN, presiCI, tesoN, tesoCI
                                    );

                                    if (resultado != null) {
                                        tvStatus.setText("¡Guardado en Documentos!");
                                        Toast.makeText(this, "Guardado en: " + resultado, Toast.LENGTH_LONG).show();

                                        // --- NUEVO: GUARDAR EN HISTORIAL DE FIREBASE ---
                                        registrarHistorialEnFirebase(anioSel, resultado);
                                        // ----------------------------------------------

                                    } else {
                                        tvStatus.setText("Error al guardar archivo");
                                    }
                                }
                                btnGenerar.setEnabled(true);

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error lecturas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                btnGenerar.setEnabled(true);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnGenerar.setEnabled(true);
                });
    }

    // Método para guardar el rastro en Firebase (Historial)
    private void registrarHistorialEnFirebase(int anio, String rutaArchivo) {
        Map<String, Object> tramite = new HashMap<>();
        tramite.put("tipo_tramite", "Carta Cobro Institucional");
        tramite.put("gestion_cobrada", anio);
        tramite.put("fecha_creacion", Timestamp.now());

        // Guardamos quiénes firmaron por si cambian en el futuro, saber quién hizo esta carta
        Map<String, String> datosCarta = new HashMap<>();
        datosCarta.put("destinatario_alcalde", etAlcalde.getText().toString());
        datosCarta.put("presidente_firma", etPresiNombre.getText().toString());
        datosCarta.put("tesorero_firma", etTesoNombre.getText().toString());
        tramite.put("datos_carta", datosCarta);

        tramite.put("ruta_archivo_local", rutaArchivo);

        // Guardamos en la colección "historial_tramites"
        db.collection("historial_tramites")
                .add(tramite)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Historial", "Trámite registrado con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Historial", "Error al registrar historial", e);
                });
    }
}