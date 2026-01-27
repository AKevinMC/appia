package com.example.apmojocoya;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstitucionActivity extends AppCompatActivity {

    private Spinner spAnio;
    private Button btnGenerar;
    private TextView tvStatus;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_institucion_report);

        db = FirebaseFirestore.getInstance();
        spAnio = findViewById(R.id.sp_anio_institucion);
        btnGenerar = findViewById(R.id.btn_generar_carta);
        tvStatus = findViewById(R.id.tv_status);

        // Texto actualizado
        btnGenerar.setText("GENERAR CARTA EXCEL (.XLSX)");

        configurarSpinner();

        btnGenerar.setOnClickListener(v -> procesarCobroAnual());
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

    private void procesarCobroAnual() {
        btnGenerar.setEnabled(false);
        tvStatus.setText("Obteniendo datos...");
        int anioSel = Integer.parseInt(spAnio.getSelectedItem().toString());

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
                        // Fallback si no hay apellido
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
                                } else {
                                    tvStatus.setText("Generando Excel...");

                                    // LLAMADA AL NUEVO GENERADOR ROBUSTO
                                    String resultado = ExcelInstitucionGenerator.generarReporte(this, anioSel, listaFinal);

                                    if (resultado != null) {
                                        tvStatus.setText("¡Guardado en Documentos/AP_Mojocoya_Cartas!");
                                        Toast.makeText(this, "Excel guardado exitosamente", Toast.LENGTH_LONG).show();
                                    } else {
                                        tvStatus.setText("Error al guardar archivo");
                                        Toast.makeText(this, "Error al guardar (Ver Logcat)", Toast.LENGTH_LONG).show();
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
}