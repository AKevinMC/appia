package com.example.apmojocoya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
    private List<ReadingItem> readingList; // Lista visible (Filtrada)
    private List<ReadingItem> masterList;  // Lista maestra (Todos los datos)

    private FirebaseFirestore db;
    private Button btnGuardar;
    private TextView tvTitulo;
    private Spinner spFiltro; // Nuevo Spinner

    private int anioSeleccionado;
    private int mesSeleccionado;
    private Tarifa tarifaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        db = FirebaseFirestore.getInstance();
        readingList = new ArrayList<>();
        masterList = new ArrayList<>(); // Inicializar maestra

        anioSeleccionado = getIntent().getIntExtra("YEAR", 2025);
        mesSeleccionado = getIntent().getIntExtra("MONTH", 1);

        tvTitulo = findViewById(R.id.tv_titulo_pantalla);
        if (tvTitulo != null) {
            tvTitulo.setText("Lecturas: " + mesSeleccionado + "/" + anioSeleccionado);
        }

        spFiltro = findViewById(R.id.sp_filtro_lectura); // Vincular Spinner

        recyclerView = findViewById(R.id.recycler_readings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReadingAdapter(readingList);
        recyclerView.setAdapter(adapter);

        btnGuardar = findViewById(R.id.btn_save_readings);
        btnGuardar.setOnClickListener(v -> guardarLecturas());

        configurarFiltro(); // Configurar lógica del spinner
        cargarTarifaYDatos();
    }

    private void configuringFiltro() { // (Pequeño typo corregido: configurarFiltro)
        // Lógica de configuración del filtro
    }

    private void configurarFiltro() {
        String[] opciones = {"Todos", "Normal", "Institucion"};
        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones);
        spFiltro.setAdapter(adapterFiltro);

        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltro(opciones[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void aplicarFiltro(String criterio) {
        readingList.clear(); // Limpiamos la vista actual

        for (ReadingItem item : masterList) {
            if (criterio.equals("Todos")) {
                readingList.add(item);
            } else if (item.getUserType().equals(criterio)) {
                // Coincidencia exacta (Normal o Institucion)
                readingList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
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
                String k = d.getString("nro_medidor");
                if(k==null) k = d.getString("idMedidor");
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

    private void procesarPadron(Map<String, DocumentSnapshot> mapActual, Map<String, DocumentSnapshot> mapAnterior) {
        db.collection("users").whereEqualTo("estado", "activo").get().addOnSuccessListener(userSnapshots -> {
            masterList.clear(); // Limpiamos la MAESTRA, no la visible

            for (DocumentSnapshot userDoc : userSnapshots) {

                // EXTRAER TIPO DE USUARIO
                String tipoUsuario = userDoc.getString("tipo");
                if (tipoUsuario == null) tipoUsuario = "Normal";

                // Variable final para usar dentro del inner-loop
                final String finalTipo = tipoUsuario;

                userDoc.getReference().collection("medidores").whereEqualTo("estado", "activo").get().addOnSuccessListener(medidorSnapshots -> {
                    for (DocumentSnapshot medDoc : medidorSnapshots) {
                        String medId = medDoc.getId();
                        String nro = medDoc.getString("nro_medidor");

                        double lecturaAnt = 0;
                        double lecturaAct = 0;
                        String estado = "Normal";
                        boolean yaRegistrado = false;

                        if (mapActual.containsKey(nro)) {
                            DocumentSnapshot d = mapActual.get(nro);
                            lecturaAnt = safeDouble(d.getDouble("lecturaAnterior"));
                            lecturaAct = safeDouble(d.getDouble("lecturaActual"));
                            estado = d.getString("estado");
                            if (estado == null) estado = "Normal";
                            yaRegistrado = true;
                        }
                        else if (mapAnterior.containsKey(nro)) {
                            DocumentSnapshot d = mapAnterior.get(nro);
                            lecturaAnt = safeDouble(d.getDouble("lecturaActual"));

                            if (lecturaAnt == 0 && medDoc.getDouble("lectura_actual") != null) {
                                double ficha = medDoc.getDouble("lectura_actual");
                                if (ficha > 0) lecturaAnt = ficha;
                            }

                            String estAnt = d.getString("estado");
                            if (estAnt != null && (estAnt.contains("Cortado") || estAnt.contains("Suspenso") || estAnt.contains("Dañado"))) {
                                estado = estAnt;
                                lecturaAct = lecturaAnt;
                                yaRegistrado = true;
                            }
                        }
                        else {
                            Double ini = medDoc.getDouble("lectura_inicial");
                            Double ficha = medDoc.getDouble("lectura_actual");
                            if (ficha != null && ficha > 0) lecturaAnt = ficha;
                            else lecturaAnt = (ini != null) ? ini : 0.0;
                        }

                        // CREAR ITEM CON EL TIPO
                        ReadingItem item = new ReadingItem(userDoc.getId(), userDoc.getString("nombre"), finalTipo, medId, nro, lecturaAnt);
                        item.setEstado(estado);

                        if (yaRegistrado) {
                            item.setCurrentReading(lecturaAct);
                        }

                        masterList.add(item); // Agregar a la maestra
                    }

                    // Al terminar de procesar un usuario (y sus medidores), refrescamos el filtro
                    // Esto se ejecutará varias veces (asíncrono), pero asegura que la lista se vaya llenando
                    String seleccionActual = spFiltro.getSelectedItem().toString();
                    aplicarFiltro(seleccionActual);
                });
            }
        });
    }

    private void guardarLecturas() {
        if (tarifaActual == null) {
            Toast.makeText(this, "Error: Tarifa no cargada", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 1. NUEVA VALIDACIÓN DE OLVIDOS (Gente oculta por el filtro) ---
        // Revisamos la lista MAESTRA para ver si alguien se quedó sin lectura
        for (ReadingItem item : masterList) {
            // Si el medidor está en estado "Normal" y NO se ha escrito nada (no actualizado)
            if ("Normal".equals(item.getEstado()) && !item.isUpdated()) {

                // A. Cambiar visualmente el filtro a "Todos" (Posición 0)
                if (spFiltro.getSelectedItemPosition() != 0) {
                    spFiltro.setSelection(0);
                    aplicarFiltro("Todos"); // Forzamos la actualización de la lista visual
                }

                // B. Avisar al usuario quién falta
                Toast.makeText(this, "⚠️ Falta la lectura de: " + item.getUserName(), Toast.LENGTH_LONG).show();

                // C. Hacer Scroll hasta ese usuario para que se vea
                int posicionVisual = readingList.indexOf(item);
                if (posicionVisual != -1) {
                    recyclerView.scrollToPosition(posicionVisual);
                }

                // D. IMPORTANTE: Cancelar el guardado.
                // No guardamos hasta que el usuario decida qué hacer con ese vacío.
                return;
            }
        }
        // ------------------------------------------------------------------

        // --- 2. VALIDACIÓN DE LECTURAS MENORES (Seguridad) ---
        for (ReadingItem item : masterList) {
            if ("Normal".equals(item.getEstado()) && item.isUpdated()) {
                if (item.getCurrentReading() < item.getPreviousReading()) {
                    // Si encontramos un error, forzamos filtro "Todos" para mostrarlo
                    if (spFiltro.getSelectedItemPosition() != 0) {
                        spFiltro.setSelection(0);
                        aplicarFiltro("Todos");
                    }

                    Toast.makeText(this, "ERROR: Lectura menor a la anterior en medidor " + item.getMeterNumber(), Toast.LENGTH_LONG).show();

                    int pos = readingList.indexOf(item);
                    if (pos != -1) recyclerView.scrollToPosition(pos);

                    return;
                }
            }
        }

        // --- 3. PROCESO DE GUARDADO (Igual que antes) ---
        int count = 0;
        WriteBatch batch = db.batch();

        for (ReadingItem item : masterList) {
            // Guardamos SI se actualizó O SI el estado NO es Normal
            if (item.isUpdated() || !item.getEstado().equals("Normal")) {

                String id = Lectura.generarIdUnico(item.getMeterNumber(), anioSeleccionado, mesSeleccionado);

                double lecturaFinal = item.getCurrentReading();

                // Si no es normal (cortado, etc), la lectura es la anterior
                if (!"Normal".equals(item.getEstado())) {
                    lecturaFinal = item.getPreviousReading();
                }
                // Si es normal pero está vacío, saltamos (aunque el bloque 1 ya debería haber evitado esto)
                else if (lecturaFinal == 0 && !item.isUpdated()) {
                    continue;
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
            Toast.makeText(this, "No hay cambios nuevos para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        final int cantidadFinal = count;
        batch.commit().addOnSuccessListener(v -> {
            Toast.makeText(this, "¡Guardado Correctamente (" + cantidadFinal + " registros)!", Toast.LENGTH_SHORT).show();
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