package com.example.apmojocoya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewUserActivity extends AppCompatActivity implements DialogAddMedidor.AddMedidorListener {
    Button btn_add_medidor, btnaceptar;
    private EditText ET_nombre, ET_apellidos, ET_ci, ET_direccion, ET_celular;
    private TextView TV_nro_medidores;

    private FirebaseFirestore db;
    // Cambiamos a Map<String, Object> para soportar números (double)
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
            // Validación básica antes de enviar
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

        // 1. Datos del Usuario
        Map<String, Object> user = new HashMap<>();
        user.put("nombre", nombre);
        user.put("apellidos", apellidos);
        user.put("celular", celular);
        user.put("direccion", direccion);
        user.put("estado", "activo"); // Recomendado para filtrar en reportes
        user.put("fecha_registro", com.google.firebase.firestore.FieldValue.serverTimestamp());

        // Usamos un Batch para guardar todo junto (Usuario + Medidores)
        WriteBatch batch = db.batch();

        // Referencia al documento del usuario
        DocumentReference userRef = db.collection("users").document(ci);
        batch.set(userRef, user);

        // 2. Datos de los Medidores (Subcolección limpia "medidores")
        for (Map<String, Object> medidorData : medidoresList) {
            String nroMedidor = (String) medidorData.get("nro_medidor");

            // Usamos el número de medidor como ID del documento para evitar duplicados
            DocumentReference medidorRef = userRef.collection("medidores").document(nroMedidor);

            // Añadimos datos extra necesarios
            medidorData.put("estado", "activo");
            // Nota: lectura_inicial ya viene en el mapa desde onAddMedidor

            batch.set(medidorRef, medidorData);
        }

        // Ejecutar el guardado masivo
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(NewUserActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(NewUserActivity.this, UserActivity.class);
            // Limpiar pila de actividades para que no vuelva atrás con "Back"
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
        medidor.put("fecha_instalacion", fechaInicio); // Nombre más técnico
        medidor.put("lectura_inicial", lecturaIni);    // Dato clave para el primer cobro

        medidoresList.add(medidor);

        TV_nro_medidores.setText("Nro Medidores: " + medidoresList.size());
    }
}