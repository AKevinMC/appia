package com.example.apmojocoya;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;

public class YearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_simple);

        TextView titulo = findViewById(R.id.tv_titulo_lista);
        titulo.setText("Seleccione Gestión (Año)");

        ListView listView = findViewById(R.id.listView);
        ArrayList<String> years = new ArrayList<>();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        // Generar lista desde 2020 hasta el año actual
        for (int y = 2020; y <= currentYear; y++) {
            years.add("Gestión " + y);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Extraer el número del texto "Gestión 2024"
            String texto = years.get(position);
            int selectedYear = Integer.parseInt(texto.replace("Gestión ", ""));

            Intent intent = new Intent(YearActivity.this, MonthActivity.class);
            intent.putExtra("YEAR", selectedYear);
            startActivity(intent);
        });
    }
}