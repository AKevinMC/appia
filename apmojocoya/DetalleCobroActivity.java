package com.example.apmojocoya;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleCobroActivity extends AppCompatActivity {

    private TextView tvTitulo, tvTotal;
    private ListView listView;
    private Button btnCobrar;
    private Spinner spMedidores;

    private FirebaseFirestore db;
    private String userId, userName;

    // Lista MAESTRA (contiene todo lo descargado)
    private List<Lectura> todasLasDeudas;
    // Lista VISIBLE (lo que se ve en pantalla según el filtro)
    private List<Lectura> listaVisible;

    private Map<String, String> mapaUbicaciones;
    private List<String> listaIdsMedidores;
    private List<String> listaNombresSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_cobro);

        db = FirebaseFirestore.getInstance();
        todasLasDeudas = new ArrayList<>();
        listaVisible = new ArrayList<>();
        mapaUbicaciones = new HashMap<>();
        listaIdsMedidores = new ArrayList<>();
        listaNombresSpinner = new ArrayList<>();

        tvTitulo = findViewById(R.id.tv_titulo_cobro);
        tvTotal = findViewById(R.id.tv_total_detalle);
        listView = findViewById(R.id.list_detalle_deudas);
        btnCobrar = findViewById(R.id.btn_realizar_pago);
        spMedidores = findViewById(R.id.sp_filtro_medidor);

        userId = getIntent().getStringExtra("USER_ID");
        userName = getIntent().getStringExtra("USER_NAME");

        tvTitulo.setText(userName);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener((p, v, pos, id) -> calcularTotal());

        btnCobrar.setOnClickListener(v -> procesarPago());

        // 1. Cargar nombres de medidores
        cargarInfoMedidores();
    }

    private void cargarInfoMedidores() {
        db.collection("users").document(userId).collection("medidores")
                .get()
                .addOnSuccessListener(snaps -> {
                    mapaUbicaciones.clear();
                    listaIdsMedidores.clear();
                    listaNombresSpinner.clear();

                    // Opción para ver TODOS
                    listaIdsMedidores.add("TODOS");
                    listaNombresSpinner.add("VER TODOS");

                    for (DocumentSnapshot doc : snaps) {
                        String nro = doc.getString("nro_medidor");
                        String ubi = doc.getString("ubicacion");
                        if (ubi == null) ubi = "";

                        if (nro != null) {
                            mapaUbicaciones.put(nro, ubi);
                            // Llenar listas para el spinner
                            listaIdsMedidores.add(nro);
                            String texto = "Med: " + nro + (ubi.isEmpty() ? "" : " (" + ubi + ")");
                            listaNombresSpinner.add(texto);
                        }
                    }

                    // Configurar el Spinner
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaNombresSpinner);
                    spMedidores.setAdapter(adapter);

                    // Al seleccionar algo en el spinner, filtramos la lista que ya tenemos
                    spMedidores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            filtrarYMostrar(listaIdsMedidores.get(position));
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    // 2. Una vez listos los medidores, descargamos las deudas
                    descargarTodasLasDeudas();
                });
    }

    private void descargarTodasLasDeudas() {
        // Usamos la consulta que SÍ funcionaba (sin filtrar medidor en la base de datos)
        db.collection("lecturas")
                .whereEqualTo("usuarioId", userId)
                .whereEqualTo("pagado", false)
                .get()
                .addOnSuccessListener(snaps -> {
                    todasLasDeudas.clear();

                    for (DocumentSnapshot doc : snaps) {
                        Lectura lec = doc.toObject(Lectura.class);
                        if (lec != null) {
                            todasLasDeudas.add(lec);
                        }
                    }

                    // Por defecto mostramos todos o el primero
                    if (spMedidores.getSelectedItemPosition() >= 0) {
                        filtrarYMostrar(listaIdsMedidores.get(spMedidores.getSelectedItemPosition()));
                    } else {
                        filtrarYMostrar("TODOS");
                    }
                });
    }

    private void filtrarYMostrar(String idMedidorFiltro) {
        listaVisible.clear();
        List<String> displayList = new ArrayList<>();

        // FILTRADO EN MEMORIA (Seguro)
        for (Lectura lec : todasLasDeudas) {
            String idMed = lec.getIdMedidor(); // Asegúrate de usar el getter correcto
            if (idMed == null) continue;

            // Si es "TODOS" pasan todos, si no, solo el que coincida
            if (idMedidorFiltro.equals("TODOS") || idMed.equals(idMedidorFiltro)) {
                listaVisible.add(lec);
            }
        }

        // ORDENAMIENTO (Cronológico: Año -> Mes)
        Collections.sort(listaVisible, (l1, l2) -> {
            if (l1.getAnio() != l2.getAnio()) return Integer.compare(l1.getAnio(), l2.getAnio());
            return Integer.compare(l1.getMes(), l2.getMes());
        });

        // CREAR TEXTOS PARA LA LISTA
        for (Lectura lec : listaVisible) {
            String ubi = mapaUbicaciones.get(lec.getIdMedidor());
            String infoMed = "Med: " + lec.getIdMedidor() + (ubi != null && !ubi.isEmpty() ? " (" + ubi + ")" : "");

            String texto = String.format("%s %d   -------   %.2f Bs\n%s",
                    obtenerMes(lec.getMes()).toUpperCase(),
                    lec.getAnio(),
                    lec.getMontoTotal(),
                    infoMed);
            displayList.add(texto);
        }

        if (displayList.isEmpty()) {
            Toast.makeText(this, "Sin deudas en esta selección", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_deuda_custom, displayList);
        listView.setAdapter(adapter);

        // Reiniciamos el total visual al cambiar filtro
        tvTotal.setText("0.00 Bs");
        // Desmarcamos todo
        listView.clearChoices();
    }

    private void calcularTotal() {
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        double total = 0;
        for (int i = 0; i < listView.getCount(); i++) {
            if (checked.get(i)) total += listaVisible.get(i).getMontoTotal();
        }
        tvTotal.setText(String.format("%.2f Bs", total));
    }

    private void procesarPago() {
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        if (checked.size() == 0) return;

        WriteBatch batch = db.batch();
        Date hoy = new Date();

        for (int i = 0; i < listView.getCount(); i++) {
            if (checked.get(i)) {
                Lectura lec = listaVisible.get(i);
                batch.update(db.collection("lecturas").document(lec.getId()),
                        "pagado", true,
                        "fechaPago", hoy
                );
            }
        }

        batch.commit().addOnSuccessListener(v -> {
            Toast.makeText(this, "¡Pago Registrado!", Toast.LENGTH_SHORT).show();
            // Recargamos todo desde la base de datos
            descargarTodasLasDeudas();
        });
    }

    private String obtenerMes(int m) {
        String[] meses = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return (m >= 1 && m <= 12) ? meses[m] : "";
    }
}