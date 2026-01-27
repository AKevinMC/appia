package com.example.apmojocoya;

import java.util.HashMap;
import java.util.Map;

public class InstitucionRow {
    private String nombre;
    private Map<Integer, Lectura> lecturasPorMes; // Clave: 1=Enero, 2=Febrero...
    private double totalAnual;

    public InstitucionRow(String nombre) {
        this.nombre = nombre;
        this.lecturasPorMes = new HashMap<>();
        this.totalAnual = 0;
    }

    public void addLectura(int mes, Lectura lectura) {
        lecturasPorMes.put(mes, lectura);
        if (lectura != null) {
            // CORRECCIÓN AQUÍ: getMontoTotal()
            totalAnual += lectura.getMontoTotal();
        }
    }

    public String getNombre() { return nombre; }
    public Lectura getLectura(int mes) { return lecturasPorMes.get(mes); }
    public double getTotalAnual() { return totalAnual; }
}