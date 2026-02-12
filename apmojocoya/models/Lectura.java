package com.example.apmojocoya.models;

import java.util.Date; // IMPORTANTE

public class Lectura {
    private String id;
    private String idMedidor;
    private String usuarioId;
    private int anio;
    private int mes;
    private double lecturaAnterior;
    private double lecturaActual;
    private String estado;
    private double montoTotal;

    // --- NUEVOS CAMPOS ---
    private boolean pagado;     // ¿Ya pagó?
    private Date fechaPago;     // ¿Cuándo pagó? (Para saber en qué balance entra)

    public Lectura() {}

    public Lectura(String id, String idMedidor, String usuarioId, int anio, int mes, double lecturaAnterior, double lecturaActual, String estado, double montoTotal) {
        this.id = id;
        this.idMedidor = idMedidor;
        this.usuarioId = usuarioId;
        this.anio = anio;
        this.mes = mes;
        this.lecturaAnterior = lecturaAnterior;
        this.lecturaActual = lecturaActual;
        this.estado = estado;
        this.montoTotal = montoTotal;
        this.pagado = false; // Por defecto nace como deuda
        this.fechaPago = null;
    }

    // --- GETTERS Y SETTERS ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdMedidor() { return idMedidor; }
    public void setIdMedidor(String idMedidor) { this.idMedidor = idMedidor; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public double getLecturaAnterior() { return lecturaAnterior; }
    public void setLecturaAnterior(double lecturaAnterior) { this.lecturaAnterior = lecturaAnterior; }
    public double getLecturaActual() { return lecturaActual; }
    public void setLecturaActual(double lecturaActual) { this.lecturaActual = lecturaActual; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }

    // Nuevos
    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }
    public Date getFechaPago() { return fechaPago; }
    public void setFechaPago(Date fechaPago) { this.fechaPago = fechaPago; }

    public static String generarIdUnico(String nroMedidor, int anio, int mes) {
        return "LEC_" + nroMedidor + "_" + anio + "_" + mes;
    }
}