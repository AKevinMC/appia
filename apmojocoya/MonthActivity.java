package com.example.apmojocoya;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class MonthActivity extends AppCompatActivity {

    private final String[] months = {
            "1. Enero", "2. Febrero", "3. Marzo", "4. Abril", "5. Mayo", "6. Junio",
            "7. Julio", "8. Agosto", "9. Septiembre", "10. Octubre", "11. Noviembre", "12. Diciembre"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_simple);

        int selectedYear = getIntent().getIntExtra("YEAR", 2025);

        TextView titulo = findViewById(R.id.tv_titulo_lista);
        titulo.setText("Meses del " + selectedYear);

        ListView listView = findViewById(R.id.listView);

        // Adaptador personalizado para pintar de gris los meses bloqueados
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, months) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                // Verificar si este mes debe estar habilitado o no
                if (esMesHabilitado(selectedYear, position + 1)) {
                    textView.setTextColor(Color.BLACK); // Habilitado
                } else {
                    textView.setTextColor(Color.GRAY);  // Bloqueado
                }
                return view;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            int selectedMonth = position + 1; // Enero = 1

            if (esMesHabilitado(selectedYear, selectedMonth)) {
                // ¡Paso exitoso! Vamos a tomar lecturas
                Intent intent = new Intent(MonthActivity.this, ReadingActivity.class);
                intent.putExtra("YEAR", selectedYear);
                intent.putExtra("MONTH", selectedMonth);
                startActivity(intent);
            } else {
                mostrarMensajeBloqueo(selectedYear, selectedMonth);
            }
        });
    }

    private boolean esMesHabilitado(int year, int month) {
        Calendar hoy = Calendar.getInstance();
        int currentYear = hoy.get(Calendar.YEAR);
        int currentMonth = hoy.get(Calendar.MONTH) + 1;
        int currentDay = hoy.get(Calendar.DAY_OF_MONTH);

        // 1. Si es año pasado -> SIEMPRE HABILITADO
        if (year < currentYear) return true;

        // 2. Si es año futuro -> BLOQUEADO
        if (year > currentYear) return false;

        // 3. (Mismo año) Si es mes pasado -> HABILITADO
        if (month < currentMonth) return true;

        // 4. (Mismo año) Si es mes futuro -> BLOQUEADO
        if (month > currentMonth) return false;

        // 5. (Mismo año, Mismo mes) -> Solo habilitar si es día 28 o más
        if (month == currentMonth) {
            return currentDay >= 28;
        }

        return false;
    }

    private void mostrarMensajeBloqueo(int year, int month) {
        Calendar hoy = Calendar.getInstance();
        int currentMonth = hoy.get(Calendar.MONTH) + 1;

        if (year > hoy.get(Calendar.YEAR) || month > currentMonth) {
            Toast.makeText(this, "⚠️ No puedes registrar meses futuros.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "🔒 El mes actual se habilita a partir del día 28.", Toast.LENGTH_LONG).show();
        }
    }
}