package com.example.apmojocoya;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CobranzaActivity extends AppCompatActivity {

    private EditText etBuscar;
    private Spinner spFiltro; // Nuevo Spinner
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private UsuarioAdapter adapter;

    private List<UsuarioItem> listaCompleta; // Lista Maestra

    // Variables para guardar el estado de los filtros
    private String textoBusquedaActual = "";
    private String tipoSeleccionadoActual = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranza);

        db = FirebaseFirestore.getInstance();
        listaCompleta = new ArrayList<>();

        etBuscar = findViewById(R.id.et_buscar_socio);
        spFiltro = findViewById(R.id.sp_filtro_cobranza); // Vincular
        recyclerView = findViewById(R.id.recycler_usuarios_cobranza);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        configurarFiltroSpinner();
        cargarUsuarios();

        // LISTENER DEL BUSCADOR
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusquedaActual = s.toString();
                aplicarFiltrosCruzados(); // Llamamos al filtro maestro
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void configuringFiltroSpinner() {
        // Placeholder por si acaso, la lógica real está abajo
    }

    private void configurarFiltroSpinner() {
        String[] opciones = {"Todos", "Normal", "Institucion"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones);
        spFiltro.setAdapter(adapter);

        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoSeleccionadoActual = opciones[position];
                aplicarFiltrosCruzados(); // Llamamos al filtro maestro
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarUsuarios() {
        db.collection("users").get().addOnSuccessListener(snaps -> {
            listaCompleta.clear();
            for (DocumentSnapshot doc : snaps) {
                String nombre = doc.getString("nombre");
                String apellidos = doc.getString("apellidos");
                String ci = doc.getId();

                // LEER TIPO
                String tipo = doc.getString("tipo");
                if (tipo == null) tipo = "Normal";

                if (nombre != null && apellidos != null) {
                    listaCompleta.add(new UsuarioItem(ci, nombre, apellidos, tipo));
                }
            }

            // ORDENAR ALFABÉTICAMENTE POR APELLIDO
            Collections.sort(listaCompleta, (u1, u2) -> u1.apellidos.compareToIgnoreCase(u2.apellidos));

            // Inicializar adaptador con la lista filtrada inicial (Todos + Vacío)
            adapter = new UsuarioAdapter(new ArrayList<>(listaCompleta)); // Copia inicial
            recyclerView.setAdapter(adapter);

            // Aplicar filtro inicial por si acaso
            aplicarFiltrosCruzados();
        });
    }

    // --- LÓGICA DE FILTRO DOBLE (BUSCADOR + SPINNER) ---
    private void aplicarFiltrosCruzados() {
        if (adapter == null) return;
        List<UsuarioItem> filtrada = new ArrayList<>();

        for (UsuarioItem user : listaCompleta) {
            // 1. Verificar Tipo
            boolean pasaTipo = tipoSeleccionadoActual.equals("Todos") || user.tipo.equals(tipoSeleccionadoActual);

            // 2. Verificar Texto (Nombre o Apellido)
            String nombreCompleto = user.apellidos + " " + user.nombre;
            boolean pasaTexto = textoBusquedaActual.isEmpty() || nombreCompleto.toLowerCase().contains(textoBusquedaActual.toLowerCase());

            // 3. Si cumple AMBOS, se agrega
            if (pasaTipo && pasaTexto) {
                filtrada.add(user);
            }
        }
        adapter.actualizarLista(filtrada);
    }

    // --- CLASE INTERNA PARA EL ADAPTADOR ---
    private class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.ViewHolder> {
        private List<UsuarioItem> usuarios;

        public UsuarioAdapter(List<UsuarioItem> usuarios) {
            this.usuarios = usuarios;
        }

        public void actualizarLista(List<UsuarioItem> nuevaLista) {
            this.usuarios = nuevaLista;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario_lista, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UsuarioItem item = usuarios.get(position);

            // MOSTRAR VISUALMENTE SI ES INSTITUCIÓN
            if ("Institucion".equals(item.tipo)) {
                holder.tvNombre.setText(item.apellidos.toUpperCase() + " " + item.nombre.toUpperCase() + " (INST)");
                holder.tvNombre.setTextColor(Color.parseColor("#3F51B5")); // Azul
            } else {
                holder.tvNombre.setText(item.apellidos.toUpperCase() + " " + item.nombre.toUpperCase());
                holder.tvNombre.setTextColor(Color.BLACK);
            }

            holder.tvCi.setText("CI: " + item.id);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(CobranzaActivity.this, DetalleCobroActivity.class);
                intent.putExtra("USER_ID", item.id);
                intent.putExtra("USER_NAME", item.nombre + " " + item.apellidos);
                // Pasamos el tipo también, nos servirá para el botón PDF
                intent.putExtra("USER_TYPE", item.tipo);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return usuarios.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvCi;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tv_nombre_completo);
                tvCi = itemView.findViewById(R.id.tv_ci_usuario);
            }
        }
    }

    // Modelo simple interno actualizado
    private static class UsuarioItem {
        String id, nombre, apellidos, tipo;
        public UsuarioItem(String id, String nombre, String apellidos, String tipo) {
            this.id = id;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.tipo = tipo;
        }
    }
}