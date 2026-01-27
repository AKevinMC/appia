package com.example.apmojocoya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewUserActivity extends AppCompatActivity implements DialogAddMedidor.AddMedidorListener {
    Button btn_add_medidor, btnaceptar;
    private EditText ET_nombre, ET_apellidos, ET_ci, ET_direccion, ET_celular;
    private RadioGroup rgTipoSocio; // NUEVO
    private TextView TV_nro_medidores;

    private FirebaseFirestore db;
    private List<Map<String, Object>> medidoresList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        db = FirebaseFirestore.getInstance();
        medidoresList = new ArrayList<>();

        ET_nombre = findViewById(R.id.ETnombre);
        ET_apellidos = findViewById(R.id.ETapellidos);
        ET_ci = findViewById(R.id.ETci);
        ET_direccion = findViewById(R.id.ETdireccion);
        ET_celular = findViewById(R.id.ETcelular);
        rgTipoSocio = findViewById(R.id.rg_tipo_socio); // NUEVO
        TV_nro_medidores = findViewById(R.id.TVnro_medidores);

        btn_add_medidor = findViewById(R.id.btnadd_medidor);
        btn_add_medidor.setOnClickListener(v -> {
            FragmentManager fm = getSupportFragmentManager();
            DialogAddMedidor dialogFragment = new DialogAddMedidor();
            dialogFragment.setAddMedidorListener(this);
            dialogFragment.show(fm, "fragment_add_medidor");
        });

        btnaceptar = findViewById(R.id.btnaceptar);
        btnaceptar.setOnClickListener(v -> {
            if (ET_ci.getText().toString().isEmpty() || ET_nombre.getText().toString().isEmpty()) {
                Toast.makeText(this, "Nombre y CI son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }
            if (medidoresList.isEmpty()) {
                Toast.makeText(this, "Debe agregar al menos un medidor", Toast.LENGTH_SHORT).show();
                return;
            }
            checkIfUserExists();
        });
    }

    private void checkIfUserExists() {
        String ci = ET_ci.getText().toString().trim();

        db.collection("users").document(ci).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Toast.makeText(NewUserActivity.this, "El usuario con este CI ya existe", Toast.LENGTH_SHORT).show();
                    } else {
                        saveUserToFirestore();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(NewUserActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show()
                );
    }

    private void saveUserToFirestore() {
        String nombre = ET_nombre.getText().toString().trim();
        String apellidos = ET_apellidos.getText().toString().trim();
        String ci = ET_ci.getText().toString().trim();
        String direccion = ET_direccion.getText().toString().trim();
        String celular = ET_celular.getText().toString().trim();

        // LÓGICA PARA EL TIPO
        String tipo = "Normal";
        if (rgTipoSocio.getCheckedRadioButtonId() == R.id.rb_institucion) {
            tipo = "Institucion";
        }

        // 1. Datos del Usuario
        Map<String, Object> user = new HashMap<>();
        user.put("nombre", nombre);
        user.put("apellidos", apellidos);
        user.put("celular", celular);
        user.put("direccion", direccion);
        user.put("tipo", tipo); // GUARDAR TIPO
        user.put("estado", "activo");
        user.put("fecha_registro", com.google.firebase.firestore.FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();

        DocumentReference userRef = db.collection("users").document(ci);
        batch.set(userRef, user);

        // 2. Datos de los Medidores
        for (Map<String, Object> medidorData : medidoresList) {
            String nroMedidor = (String) medidorData.get("nro_medidor");
            DocumentReference medidorRef = userRef.collection("medidores").document(nroMedidor);
            medidorData.put("estado", "activo");
            batch.set(medidorRef, medidorData);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(NewUserActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(NewUserActivity.this, UserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }).addOnFailureListener(e -> {
            Toast.makeText(NewUserActivity.this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onAddMedidor(String nroMedidor, String ubicacion, String fechaInicio, double lecturaIni) {
        Map<String, Object> medidor = new HashMap<>();
        medidor.put("nro_medidor", nroMedidor);
        medidor.put("ubicacion", ubicacion);
        medidor.put("fecha_instalacion", fechaInicio);
        medidor.put("lectura_inicial", lecturaIni);

        medidoresList.add(medidor);

        TV_nro_medidores.setText("Nro Medidores: " + medidoresList.size());
    }
}