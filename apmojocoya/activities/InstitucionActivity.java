package com.example.apmojocoya.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.apmojocoya.utils.DriveServiceHelper;
import com.example.apmojocoya.utils.ExcelInstitucionGenerator;
import com.example.apmojocoya.models.InstitucionRow;
import com.example.apmojocoya.models.Lectura;
import com.example.apmojocoya.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

    private DriveServiceHelper mDriveServiceHelper;

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
        cargarDatosAutoridades();
        initializeDriveHelper();

        btnGenerar.setOnClickListener(v -> verificarPermisosYGenerar());
    }

    private void initializeDriveHelper() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());

            Drive googleDriveService = new Drive.Builder(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("APMojocoya")
                    .build();

            mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
        }
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
        db.collection("configuracion").document("autoridades").get()
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
        db.collection("configuracion").document("autoridades").set(data, SetOptions.merge());
    }

    private void verificarPermisosYGenerar() {
        if (etAlcalde.getText().toString().trim().isEmpty()) { etAlcalde.setError("Requerido"); return; }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            procesarCobroAnual();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                procesarCobroAnual();
            }
        }
    }

    private void procesarCobroAnual() {
        guardarDatosAutoridades();
        btnGenerar.setEnabled(false);
        tvStatus.setText("Obteniendo datos...");
        int anioSel = Integer.parseInt(spAnio.getSelectedItem().toString());

        db.collection("users").whereEqualTo("tipo", "Institucion").get()
                .addOnSuccessListener(userSnaps -> {
                    if (userSnaps.isEmpty()) {
                        Toast.makeText(this, "No hay instituciones", Toast.LENGTH_SHORT).show();
                        btnGenerar.setEnabled(true);
                        return;
                    }

                    Map<String, InstitucionRow> mapaFilas = new HashMap<>();
                    for (DocumentSnapshot doc : userSnaps) {
                        String uid = doc.getId();
                        String nombre = doc.getString("apellidos");
                        if (nombre == null || nombre.isEmpty()) nombre = doc.getString("nombre");
                        mapaFilas.put(uid, new InstitucionRow(nombre));
                    }

                    db.collection("lecturas").whereEqualTo("anio", anioSel).get()
                            .addOnSuccessListener(lecturaSnaps -> {
                                for (DocumentSnapshot doc : lecturaSnaps) {
                                    String uid = doc.getString("usuarioId");
                                    if (mapaFilas.containsKey(uid)) {
                                        Lectura lectura = doc.toObject(Lectura.class);
                                        if (lectura != null) mapaFilas.get(uid).addLectura(lectura.getMes(), lectura);
                                    }
                                }

                                String pathLocal = ExcelInstitucionGenerator.generarReporte(
                                        this, anioSel, new ArrayList<>(mapaFilas.values()),
                                        etAlcalde.getText().toString(), etPresiNombre.getText().toString(),
                                        etPresiCI.getText().toString(), etTesoNombre.getText().toString(), etTesoCI.getText().toString()
                                );

                                if (pathLocal != null) {
                                    subirADrive(pathLocal, anioSel);
                                } else {
                                    tvStatus.setText("Error al crear archivo");
                                    btnGenerar.setEnabled(true);
                                }
                            });
                });
    }

    private void subirADrive(String path, int anio) {
        if (mDriveServiceHelper == null) {
            tvStatus.setText("Excel guardado localmente (Drive no configurado)");
            btnGenerar.setEnabled(true);
            return;
        }

        tvStatus.setText("Subiendo a Google Drive...");
        File fileToUpload = new File(path);

        mDriveServiceHelper.uploadFile(fileToUpload, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "AP_Mojocoya_Cartas")
                .addOnSuccessListener(fileId -> {
                    tvStatus.setText("¡Carta guardada en Documentos y Drive!");
                    registrarHistorialEnFirebase(anio, path);
                    btnGenerar.setEnabled(true);
                    Toast.makeText(this, "Subida exitosa", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    tvStatus.setText("Error Drive: " + e.getMessage());
                    btnGenerar.setEnabled(true);
                });
    }

    private void registrarHistorialEnFirebase(int anio, String rutaArchivo) {
        Map<String, Object> tramite = new HashMap<>();
        tramite.put("tipo_tramite", "Carta Cobro Institucional");
        tramite.put("gestion_cobrada", anio);
        tramite.put("fecha_creacion", Timestamp.now());
        tramite.put("ruta_archivo_local", rutaArchivo);
        db.collection("historial_tramites").add(tramite);
    }
}