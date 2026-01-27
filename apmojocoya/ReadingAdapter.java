package com.example.apmojocoya;

import android.graphics.Color;
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
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ViewHolder> {

    private List<ReadingItem> items;
    private Tarifa tarifaVigente;

    public ReadingAdapter(List<ReadingItem> items) {
        this.items = items;
    }

    public void setTarifa(Tarifa tarifa) {
        this.tarifaVigente = tarifa;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reading, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReadingItem item = items.get(position);

        holder.tvNombre.setText(item.getUserName());
        holder.tvMedidor.setText("Med: " + item.getMeterNumber());
        holder.tvAnterior.setText(String.valueOf(item.getPreviousReading()));

        // 1. CONFIGURAR SPINNER
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(holder.itemView.getContext(),
                R.array.estados_medidor, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerEstado.setAdapter(spinnerAdapter);

        holder.spinnerEstado.setOnItemSelectedListener(null);
        if (item.getEstado() != null) {
            int spinnerPos = spinnerAdapter.getPosition(item.getEstado());
            holder.spinnerEstado.setSelection(spinnerPos);
        }

        // 2. CONFIGURAR CAMPO DE TEXTO (EDIT TEXT)
        if (holder.textWatcher != null) {
            holder.etActual.removeTextChangedListener(holder.textWatcher);
        }

        boolean esEditable = "Normal".equals(item.getEstado());
        holder.etActual.setEnabled(esEditable);

        if (esEditable) {
            holder.etActual.setBackgroundResource(android.R.drawable.edit_text);
            if (item.isUpdated()) {
                holder.etActual.setText(String.valueOf(item.getCurrentReading()));
            } else {
                holder.etActual.setText("");
            }
        } else {
            holder.etActual.setBackgroundColor(Color.parseColor("#E0E0E0"));
            holder.etActual.setText(String.valueOf(item.getPreviousReading()));
            item.setCurrentReading(item.getPreviousReading());
        }

        if (!esEditable) {
            holder.tvNombre.setTextColor(Color.RED);
            holder.tvNombre.setText(item.getUserName() + " (" + item.getEstado().toUpperCase() + ")");
        } else {
            holder.tvNombre.setTextColor(Color.BLACK);
            holder.tvNombre.setText(item.getUserName());
        }

        actualizarCalculos(holder, item);

        // --- MEJORA UX: LIMPIAR EL 0.0 AUTOMÁTICAMENTE ---
        holder.etActual.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && holder.etActual.isEnabled()) {
                String texto = holder.etActual.getText().toString();
                // Si es 0.0 o 0, lo borramos para escribir directo
                if (texto.equals("0.0") || texto.equals("0")) {
                    holder.etActual.setText("");
                } else {
                    // Si es otro número (ej: corrección), lo seleccionamos todo
                    holder.etActual.selectAll();
                }
            }
        });
        // ------------------------------------------------

        // 3. LISTENER DEL SPINNER
        holder.spinnerEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String nuevoEstado = parent.getItemAtPosition(pos).toString();

                if (!nuevoEstado.equals(item.getEstado())) {
                    item.setEstado(nuevoEstado);

                    if (!"Normal".equals(nuevoEstado)) {
                        item.setCurrentReading(item.getPreviousReading());
                        notifyItemChanged(holder.getAdapterPosition());
                    } else {
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 4. LISTENER DEL TEXTO
        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (holder.etActual.isEnabled()) {
                    if (s.toString().isEmpty()) {
                        item.setCurrentReading(0);
                    } else {
                        try {
                            double val = Double.parseDouble(s.toString());
                            item.setCurrentReading(val);
                        } catch (NumberFormatException e) {
                            item.setCurrentReading(0);
                        }
                    }
                    actualizarCalculos(holder, item);
                }
            }
        };
        holder.etActual.addTextChangedListener(holder.textWatcher);
    }

    private void actualizarCalculos(ViewHolder holder, ReadingItem item) {
        double consumo = item.getConsumo();
        holder.tvConsumo.setText(String.format(Locale.getDefault(), "Consumo: %.0f m³", consumo));

        if ("Normal".equals(item.getEstado()) && item.isUpdated() && item.getCurrentReading() < item.getPreviousReading()) {
            holder.etActual.setError("¡Imposible! Menor a anterior");
            holder.tvConsumo.setTextColor(Color.RED);
            if (holder.tvPrecio != null) holder.tvPrecio.setText("Error");
        } else {
            holder.etActual.setError(null);
            holder.tvConsumo.setTextColor(Color.parseColor("#388E3C"));

            if (tarifaVigente != null) {
                double aPagar = tarifaVigente.calcularMonto(consumo);
                if (holder.tvPrecio != null) {
                    holder.tvPrecio.setText(String.format(Locale.getDefault(), "Total: %.2f Bs", aPagar));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvMedidor, tvAnterior, tvConsumo, tvPrecio;
        EditText etActual;
        Spinner spinnerEstado;
        TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_socio_nombre);
            tvMedidor = itemView.findViewById(R.id.tv_nro_medidor);
            tvAnterior = itemView.findViewById(R.id.tv_lectura_anterior);
            etActual = itemView.findViewById(R.id.et_lectura_actual);
            tvConsumo = itemView.findViewById(R.id.tv_consumo_calculado);
            tvPrecio = itemView.findViewById(R.id.tv_total_pagar);
            spinnerEstado = itemView.findViewById(R.id.spinner_estado);
        }
    }
}