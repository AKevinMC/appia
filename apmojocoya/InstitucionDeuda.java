package com.example.apmojocoya;

public class InstitucionDeuda {
    private String nombre;
    private double montoTotal;
    private int mesesAdeudados;

    public InstitucionDeuda(String nombre, double montoTotal, int mesesAdeudados) {
        this.nombre = nombre;
        this.montoTotal = montoTotal;
        this.mesesAdeudados = mesesAdeudados;
    }

    public String getNombre() { return nombre; }
    public double getMontoTotal() { return montoTotal; }
    public int getMesesAdeudados() { return mesesAdeudados; }
}