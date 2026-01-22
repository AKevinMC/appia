package com.example.apmojocoya;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GastosActivity extends AppCompatActivity {

    private Spinner spConcepto, spMes;
    private EditText etMonto;
    private Button btnGuardar, btnBalance;
    private ListView listView;
    private FirebaseFirestore db;
    private ArrayAdapter<String> listAdapter;
    private List<String> stringListGastos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gastos);

        db = FirebaseFirestore.getInstance();

        spConcepto = findViewById(R.id.sp_concepto_gasto);
        spMes = findViewById(R.id.sp_filtro_mes);
        etMonto = findViewById(R.id.et_monto_gasto);
        btnGuardar = findViewById(R.id.btn_guardar_gasto);
        btnBalance = findViewById(R.id.btn_descargar_balance);
        listView = findViewById(R.id.list_gastos);

        configurarUI();
        cargarUltimosGastos();

        btnGuardar.setOnClickListener(v -> registrarGasto());
        btnBalance.setOnClickListener(v -> generarBalanceExcel());
    }

    private void configurarUI() {
        // 1. Conceptos de Gasto (Basado en tu Excel)
        String[] conceptos = {"Pago CESSA", "Material de Escritorio", "Gastos de Operación", "Jornales", "Mantenimiento", "Compra de Llaves/Tubos", "Otros"};
        ArrayAdapter<String> adapterConceptos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, conceptos);
        spConcepto.setAdapter(adapterConceptos);

        // 2. Meses (Para el reporte)
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        ArrayAdapter<String> adapterMes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, meses);
        spMes.setAdapter(adapterMes);
        spMes.setSelection(Calendar.getInstance().get(Calendar.MONTH)); // Seleccionar mes actual

        // 3. Lista visual
        stringListGastos = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringListGastos);
        listView.setAdapter(listAdapter);
    }

    private void registrarGasto() {
        String montoStr = etMonto.getText().toString();
        if (montoStr.isEmpty()) {
            etMonto.setError("Ingrese monto");
            return;
        }

        double monto = Double.parseDouble(montoStr);
        String concepto = spConcepto.getSelectedItem().toString();

        Calendar cal = Calendar.getInstance();
        int mes = cal.get(Calendar.MONTH) + 1;
        int anio = cal.get(Calendar.YEAR);
        Date fecha = cal.getTime();

        String id = db.collection("movimientos").document().getId();

        Movimiento mov = new Movimiento(id, "EGRESO", concepto, monto, fecha, mes, anio);

        db.collection("movimientos").document(id).set(mov)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Gasto registrado", Toast.LENGTH_SHORT).show();
                    etMonto.setText("");
                    cargarUltimosGastos();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show());
    }

    private void cargarUltimosGastos() {
        db.collection("movimientos")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snaps -> {
                    stringListGastos.clear();
                    for (DocumentSnapshot doc : snaps) {
                        Movimiento m = doc.toObject(Movimiento.class);
                        if (m != null) {
                            stringListGastos.add(m.getConcepto() + ": " + m.getMonto() + " Bs");
                        }
                    }
                    listAdapter.notifyDataSetChanged();
                });
    }

    // --- AQUÍ LA MAGIA DEL BALANCE ---
    private void generarBalanceExcel() {
        Toast.makeText(this, "Calculando Balance...", Toast.LENGTH_SHORT).show();

        int mesSeleccionado = spMes.getSelectedItemPosition() + 1;
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);

        // 1. Consultar INGRESOS (Lecturas de ese mes)
        com.google.android.gms.tasks.Task<QuerySnapshot> tareaIngresos = db.collection("lecturas")
                .whereEqualTo("anio", anioActual)
                .whereEqualTo("mes", mesSeleccionado)
                .get();

        // 2. Consultar EGRESOS (Movimientos de ese mes)
        com.google.android.gms.tasks.Task<QuerySnapshot> tareaEgresos = db.collection("movimientos")
                .whereEqualTo("anio", anioActual)
                .whereEqualTo("mes", mesSeleccionado)
                .get();

        Tasks.whenAllSuccess(tareaIngresos, tareaEgresos).addOnSuccessListener(results -> {
            QuerySnapshot snapLecturas = (QuerySnapshot) results.get(0);
            QuerySnapshot snapMovimientos = (QuerySnapshot) results.get(1);

            // Calcular Total Ingresos (Agua)
            double totalIngresosAgua = 0;
            for(DocumentSnapshot doc : snapLecturas) {
                Double monto = doc.getDouble("montoTotal");
                if(monto != null) totalIngresosAgua += monto;
            }

            // Calcular Lista de Egresos
            List<Movimiento> listaEgresos = new ArrayList<>();
            double totalGastos = 0;
            for(DocumentSnapshot doc : snapMovimientos) {
                Movimiento m = doc.toObject(Movimiento.class);
                if(m != null && "EGRESO".equals(m.getTipo())) {
                    listaEgresos.add(m);
                    totalGastos += m.getMonto();
                }
            }

            // Generar el Excel
            String ruta = ExcelReportGenerator.generarBalance(this, mesSeleccionado, anioActual, totalIngresosAgua, listaEgresos);

            if(ruta != null) Toast.makeText(this, "✅ Balance guardado: " + ruta, Toast.LENGTH_LONG).show();
            else Toast.makeText(this, "Error creando archivo", Toast.LENGTH_SHORT).show();
        });
    }
}