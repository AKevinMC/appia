package com.example.apmojocoya;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private UsuarioAdapter adapter;
    private List<UsuarioItem> listaCompleta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranza); // Asegúrate que tu XML tenga estos IDs

        db = FirebaseFirestore.getInstance();
        listaCompleta = new ArrayList<>();

        etBuscar = findViewById(R.id.et_buscar_socio); // ID del EditText en tu XML
        recyclerView = findViewById(R.id.recycler_usuarios_cobranza); // Cambia el ListView por RecyclerView en tu XML

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarUsuarios();

        // BUSCADOR DINÁMICO
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void cargarUsuarios() {
        db.collection("users").get().addOnSuccessListener(snaps -> {
            listaCompleta.clear();
            for (DocumentSnapshot doc : snaps) {
                String nombre = doc.getString("nombre");
                String apellidos = doc.getString("apellidos");
                String ci = doc.getId(); // O doc.getString("ci") según tengas

                if (nombre != null && apellidos != null) {
                    listaCompleta.add(new UsuarioItem(ci, nombre, apellidos));
                }
            }

            // ORDENAR ALFABÉTICAMENTE POR APELLIDO
            Collections.sort(listaCompleta, (u1, u2) -> u1.apellidos.compareToIgnoreCase(u2.apellidos));

            // Inicializar adaptador con todos
            adapter = new UsuarioAdapter(listaCompleta);
            recyclerView.setAdapter(adapter);
        });
    }

    private void filtrar(String texto) {
        if (adapter == null) return;
        List<UsuarioItem> filtrada = new ArrayList<>();

        for (UsuarioItem user : listaCompleta) {
            String nombreCompleto = user.apellidos + " " + user.nombre;
            if (nombreCompleto.toLowerCase().contains(texto.toLowerCase())) {
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
            // Formato: ARANCIBIA PEREZ, JUAN
            holder.tvNombre.setText(item.apellidos.toUpperCase() + " " + item.nombre.toUpperCase());
            holder.tvCi.setText("CI: " + item.id);

            holder.itemView.setOnClickListener(v -> {
                // AL CLICAR, VAMOS A LA PANTALLA DE DETALLE DE DEUDA
                Intent intent = new Intent(CobranzaActivity.this, DetalleCobroActivity.class);
                intent.putExtra("USER_ID", item.id);
                intent.putExtra("USER_NAME", item.nombre + " " + item.apellidos);
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

    // Modelo simple
    private static class UsuarioItem {
        String id, nombre, apellidos;
        public UsuarioItem(String id, String nombre, String apellidos) {
            this.id = id; this.nombre = nombre; this.apellidos = apellidos;
        }
    }
}