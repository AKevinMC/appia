package com.example.apmojocoya;

import java.util.Date;

public class Movimiento {
    private String id;
    private String tipo;        // "EGRESO" (Gasto) o "INGRESO_EXTRA" (Ej: Multa, Instalación nueva)
    private String concepto;    // Ej: "Pago CESSA", "Material Escritorio"
    private double monto;
    private Date fecha;
    private int mes;            // Para filtrar rápido
    private int anio;           // Para filtrar rápido

    public Movimiento() {} // Constructor vacío para Firebase

    public Movimiento(String id, String tipo, String concepto, double monto, Date fecha, int mes, int anio) {
        this.id = id;
        this.tipo = tipo;
        this.concepto = concepto;
        this.monto = monto;
        this.fecha = fecha;
        this.mes = mes;
        this.anio = anio;
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getTipo() { return tipo; }
    public String getConcepto() { return concepto; }
    public double getMonto() { return monto; }
    public Date getFecha() { return fecha; }
    public int getMes() { return mes; }
    public int getAnio() { return anio; }
}