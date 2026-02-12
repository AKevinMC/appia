package com.example.apmojocoya.dialogs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.apmojocoya.R;

public class DialogAddMedidor extends DialogFragment {
    private EditText nroMedidor, ubicacion, fechaInicio, lecturaInicial;
    private Button btnAceptarDialog;
    private AddMedidorListener listener;

    public interface AddMedidorListener {
        // Interfaz actualizada para recibir la lectura inicial
        void onAddMedidor(String nroMedidor, String ubicacion, String fechaInicio, double lecturaIni);
    }

    public void setAddMedidorListener(AddMedidorListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dialog_add_medidor, container, false);

        nroMedidor = view.findViewById(R.id.nro_medidor);
        ubicacion = view.findViewById(R.id.ubicacion);
        fechaInicio = view.findViewById(R.id.fecha_inicio);
        // Asegúrate de agregar este ID en tu XML
        lecturaInicial = view.findViewById(R.id.et_lectura_inicial);

        btnAceptarDialog = view.findViewById(R.id.btnAceptarDialog);

        btnAceptarDialog.setOnClickListener(v -> {
            String nro = nroMedidor.getText().toString();
            String ubi = ubicacion.getText().toString();
            String fecha = fechaInicio.getText().toString();
            String lectStr = lecturaInicial != null ? lecturaInicial.getText().toString() : "0";

            if (nro.isEmpty() || ubi.isEmpty()) {
                Toast.makeText(getContext(), "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            double lectIni = 0;
            try {
                lectIni = Double.parseDouble(lectStr);
            } catch (NumberFormatException e) {
                lectIni = 0;
            }

            if (listener != null) {
                listener.onAddMedidor(nro, ubi, fecha, lectIni);
            }

            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}