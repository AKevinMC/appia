package com.example.apmojocoya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReadingAdapter adapter;
    private List<ReadingItem> readingList;
    private FirebaseFirestore db;
    private Button btnGuardar;
    private TextView tvTitulo;

    private int anioSeleccionado;
    private int mesSeleccionado;
    private Tarifa tarifaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        db = FirebaseFirestore.getInstance();
        readingList = new ArrayList<>();

        anioSeleccionado = getIntent().getIntExtra("YEAR", 2025);
        mesSeleccionado = getIntent().getIntExtra("MONTH", 1);

        tvTitulo = findViewById(R.id.tv_titulo_pantalla);
        if (tvTitulo != null) {
            tvTitulo.setText("Lecturas: " + mesSeleccionado + "/" + anioSeleccionado);
        }

        recyclerView = findViewById(R.id.recycler_readings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReadingAdapter(readingList);
        recyclerView.setAdapter(adapter);

        btnGuardar = findViewById(R.id.btn_save_readings);
        btnGuardar.setOnClickListener(v -> guardarLecturas());

        cargarTarifaYDatos();
    }

    private void cargarTarifaYDatos() {
        Toast.makeText(this, "Cargando...", Toast.LENGTH_SHORT).show();
        db.collection("configuracion").document("tarifa_actual").get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tarifaActual = doc.toObject(Tarifa.class);
                        adapter.setTarifa(tarifaActual);
                    }
                    cargarDatosFusionados();
                })
                .addOnFailureListener(e -> cargarDatosFusionados());
    }

    private void cargarDatosFusionados() {
        int[] anterior = calcularMesAnterior(anioSeleccionado, mesSeleccionado);

        Task<QuerySnapshot> taskActual = db.collection("lecturas")
                .whereEqualTo("anio", anioSeleccionado)
                .whereEqualTo("mes", mesSeleccionado).get();
        Task<QuerySnapshot> taskAnterior = db.collection("lecturas")
                .whereEqualTo("anio", anterior[0])
                .whereEqualTo("mes", anterior[1]).get();

        Tasks.whenAllSuccess(taskActual, taskAnterior).addOnSuccessListener(results -> {
            QuerySnapshot snapActual = (QuerySnapshot) results.get(0);
            QuerySnapshot snapAnterior = (QuerySnapshot) results.get(1);

            Map<String, DocumentSnapshot> mapActual = new HashMap<>();
            for (DocumentSnapshot d : snapActual) {
                String k = d.getString("nro_medidor"); // Prioridad 1
                if(k==null) k = d.getString("idMedidor"); // Prioridad 2
                if(k!=null) mapActual.put(k, d);
            }

            Map<String, DocumentSnapshot> mapAnterior = new HashMap<>();
            for (DocumentSnapshot d : snapAnterior) {
                String k = d.getString("nro_medidor");
                if(k==null) k = d.getString("idMedidor");
                if(k!=null) mapAnterior.put(k, d);
            }
            procesarPadron(mapActual, mapAnterior);
        });
    }

    private int[] calcularMesAnterior(int anio, int mes) {
        return mes == 1 ? new int[]{anio - 1, 12} : new int[]{anio, mes - 1};
    }

    // --- AQUÍ ESTÁ LA CORRECCIÓN CLAVE ---
    private void procesarPadron(Map<String, DocumentSnapshot> mapActual, Map<String, DocumentSnapshot> mapAnterior) {
        db.collection("users").whereEqualTo("estado", "activo").get().addOnSuccessListener(userSnapshots -> {
            readingList.clear();
            for (DocumentSnapshot userDoc : userSnapshots) {
                userDoc.getReference().collection("medidores").whereEqualTo("estado", "activo").get().addOnSuccessListener(medidorSnapshots -> {
                    for (DocumentSnapshot medDoc : medidorSnapshots) {
                        String medId = medDoc.getId();
                        String nro = medDoc.getString("nro_medidor");

                        double lecturaAnt = 0;
                        double lecturaAct = 0;
                        String estado = "Normal";
                        boolean yaRegistrado = false;

                        // 1. ¿YA GUARDAMOS ESTE MES? (Edición)
                        if (mapActual.containsKey(nro)) {
                            DocumentSnapshot d = mapActual.get(nro);
                            lecturaAnt = safeDouble(d.getDouble("lecturaAnterior"));
                            lecturaAct = safeDouble(d.getDouble("lecturaActual"));
                            estado = d.getString("estado");
                            if (estado == null) estado = "Normal";
                            yaRegistrado = true;
                        }
                        // 2. ¿DATOS DEL MES ANTERIOR? (Arrastre)
                        else if (mapAnterior.containsKey(nro)) {
                            DocumentSnapshot d = mapAnterior.get(nro);
                            // IMPORTANTE: Recuperamos la lectura final del mes pasado
                            lecturaAnt = safeDouble(d.getDouble("lecturaActual"));

                            // Si por error se guardó un 0 el mes pasado en un medidor que NO es nuevo,
                            // intentamos recuperar la lectura del medidor maestro para no arrastrar el error.
                            if (lecturaAnt == 0 && medDoc.getDouble("lectura_actual") != null) {
                                double ficha = medDoc.getDouble("lectura_actual");
                                if (ficha > 0) lecturaAnt = ficha;
                            }

                            String estAnt = d.getString("estado");
                            if (estAnt != null && (estAnt.contains("Cortado") || estAnt.contains("Suspenso") || estAnt.contains("Dañado"))) {
                                estado = estAnt;
                                // CORRECCIÓN: Si sugerimos "Cortado", pre-llenamos la lectura actual
                                // para que sea igual a la anterior desde el inicio.
                                lecturaAct = lecturaAnt;
                                yaRegistrado = true; // Fingimos que ya tiene datos para que el Adapter lo pinte bien
                            }
                        }
                        // 3. NUEVO / HUECO
                        else {
                            Double ini = medDoc.getDouble("lectura_inicial");
                            Double ficha = medDoc.getDouble("lectura_actual");
                            // Si hay un hueco, preferimos la última lectura conocida (ficha)
                            if (ficha != null && ficha > 0) lecturaAnt = ficha;
                            else lecturaAnt = (ini != null) ? ini : 0.0;
                        }

                        ReadingItem item = new ReadingItem(userDoc.getId(), userDoc.getString("nombre"), medId, nro, lecturaAnt);
                        item.setEstado(estado);

                        // Si ya tenemos un dato (porque editamos O porque es Cortado automático), lo asignamos
                        if (yaRegistrado) {
                            item.setCurrentReading(lecturaAct);
                        }

                        readingList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void guardarLecturas() {
        if (tarifaActual == null) {
            Toast.makeText(this, "Error: Tarifa no cargada", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de seguridad
        for (ReadingItem item : readingList) {
            if ("Normal".equals(item.getEstado()) && item.isUpdated()) {
                if (item.getCurrentReading() < item.getPreviousReading()) {
                    Toast.makeText(this, "ERROR: Lectura menor a la anterior en medidor " + item.getMeterNumber(), Toast.LENGTH_LONG).show();
                    recyclerView.scrollToPosition(readingList.indexOf(item));
                    return;
                }
            }
        }

        int count = 0;
        WriteBatch batch = db.batch();

        for (ReadingItem item : readingList) {
            // Guardamos SI se actualizó O SI el estado NO es Normal (Cortado, Suspenso...)
            if (item.isUpdated() || !item.getEstado().equals("Normal")) {

                String id = Lectura.generarIdUnico(item.getMeterNumber(), anioSeleccionado, mesSeleccionado);

                // Lógica vital: Si no es Normal, la lectura final ES la anterior (Consumo 0)
                double lecturaFinal = item.getCurrentReading();
                if (!"Normal".equals(item.getEstado())) {
                    lecturaFinal = item.getPreviousReading();
                }
                // Si es Normal pero no escribieron nada, evitamos guardar 0
                else if (lecturaFinal == 0 && !item.isUpdated()) {
                    continue; // Saltamos este registro si está vacío y normal
                }

                double consumo = lecturaFinal - item.getPreviousReading();
                if (consumo < 0) consumo = 0;

                double monto = tarifaActual.calcularMonto(consumo);

                Lectura lec = new Lectura(id, item.getMeterNumber(), item.getUserId(),
                        anioSeleccionado, mesSeleccionado, item.getPreviousReading(),
                        lecturaFinal, item.getEstado(), monto);

                batch.set(db.collection("lecturas").document(id), lec);

                if (esUltimoMes(anioSeleccionado, mesSeleccionado)) {
                    String path = "users/" + item.getUserId() + "/medidores/" + item.getMeterId();
                    batch.update(db.document(path), "lectura_actual", lecturaFinal);
                }
                count++;
            }
        }

        if (count == 0) {
            Toast.makeText(this, "No hay datos para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        batch.commit().addOnSuccessListener(v -> {
            Toast.makeText(this, "¡Guardado Correctamente!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private boolean esUltimoMes(int anio, int mes) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int a = c.get(java.util.Calendar.YEAR);
        int m = c.get(java.util.Calendar.MONTH) + 1;
        return (anio > a) || (anio == a && mes >= m);
    }

    private double safeDouble(Double d) { return d == null ? 0.0 : d; }
}