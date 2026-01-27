package com.example.apmojocoya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    private Button btn_add_user;
    private RecyclerView recyclerViewUsers;
    private Spinner spFiltro;

    private UserAdapter userAdapter;
    private List<Usuario> userList;       // Lista que se muestra (filtrada)
    private List<Usuario> listaTodos;     // Lista maestra con todos los datos

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = FirebaseFirestore.getInstance();

        // 1. Vincular Vistas
        btn_add_user = findViewById(R.id.btnadduser);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        spFiltro = findViewById(R.id.sp_filtro_tipo_usuario);

        // 2. Configurar RecyclerView
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        listaTodos = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setAdapter(userAdapter);

        // 3. Configurar Spinner de Filtro
        configurarFiltro();

        // 4. Botón Agregar
        btn_add_user.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, NewUserActivity.class);
            startActivity(intent);
        });

        // 5. Cargar Datos
        loadUsersFromFirestore();
    }

    private void configurarFiltro() {
        String[] opciones = {"Todos", "Normal", "Institucion"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones);
        spFiltro.setAdapter(adapter);

        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltro(opciones[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadUsersFromFirestore() {
        db.collection("users")
                .orderBy("apellidos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaTodos.clear(); // Limpiamos la maestra

                        for (DocumentSnapshot document : task.getResult()) {
                            String name = document.getString("apellidos") + " " + document.getString("nombre");
                            String id = document.getId();

                            // Leer Tipo
                            String tipo = document.getString("tipo");
                            if (tipo == null) tipo = "Normal";

                            final String finalTipo = tipo;

                            db.collection("users").document(id).collection("medidores")
                                    .get()
                                    .addOnCompleteListener(subTask -> {
                                        if (subTask.isSuccessful()) {
                                            int medidoresCount = subTask.getResult().size();

                                            // Agregamos a la LISTA MAESTRA
                                            listaTodos.add(new Usuario(id, name, medidoresCount, finalTipo));

                                            // Refrescamos el filtro actual (por si llegan datos tarde)
                                            String seleccionActual = spFiltro.getSelectedItem().toString();
                                            aplicarFiltro(seleccionActual);
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Error cargando usuarios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void aplicarFiltro(String criterio) {
        userList.clear(); // Limpiamos la visual

        for (Usuario u : listaTodos) {
            if (criterio.equals("Todos")) {
                userList.add(u);
            } else if (u.getTipo().equals(criterio)) {
                // Coincidencia exacta (Normal o Institucion)
                userList.add(u);
            }
        }
        userAdapter.notifyDataSetChanged();
    }
}