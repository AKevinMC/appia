package com.example.apmojocoya.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apmojocoya.R;
import com.example.apmojocoya.models.Tarifa;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConfigTarifaActivity extends AppCompatActivity {

    private EditText etFijo, etLimite, etExtra;
    private Button btnGuardar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_tarifa);

        db = FirebaseFirestore.getInstance();

        etFijo = findViewById(R.id.et_cargo_fijo);
        etLimite = findViewById(R.id.et_limite_basico); // Aquí pondrás 6
        etExtra = findViewById(R.id.et_costo_extra);     // Aquí pondrás 4
        btnGuardar = findViewById(R.id.btn_guardar_tarifa);

        cargarTarifaActual();

        btnGuardar.setOnClickListener(v -> guardarTarifa());
    }

    private void cargarTarifaActual() {
        db.collection("configuracion").document("tarifa_actual")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Tarifa tarifa = documentSnapshot.toObject(Tarifa.class);
                        if (tarifa != null) {
                            etFijo.setText(String.valueOf(tarifa.getCargoFijo()));
                            etLimite.setText(String.valueOf(tarifa.getLimiteBasico()));
                            etExtra.setText(String.valueOf(tarifa.getCostoExceso()));
                        }
                    }
                });
    }

    private void guardarTarifa() {
        try {
            double fijo = Double.parseDouble(etFijo.getText().toString());
            int limite = Integer.parseInt(etLimite.getText().toString());
            double extra = Double.parseDouble(etExtra.getText().toString());

            // Guardamos: ID, Fijo, Limite (6), Extra (4)
            Tarifa nuevaTarifa = new Tarifa("tarifa_actual", fijo, limite, extra);

            db.collection("configuracion").document("tarifa_actual")
                    .set(nuevaTarifa)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "¡Tarifa Guardada!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Revisa que todos los números sean correctos", Toast.LENGTH_SHORT).show();
        }
    }
}