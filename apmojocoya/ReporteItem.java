package com.example.apmojocoya;

public class ReporteItem {
    private int numeroLista; // El Nº 1, 2, 3...
    private String nombreSocio;
    private double lecturaAnterior;
    private double lecturaActual;
    private double consumo;
    private double importeMinimo;  // La columna "Importe Consumo Mínimo"
    private double importeExceso;  // La columna "Importe por Exceso"
    private double totalPagar;     // La columna "TOTAL A PAGAR"
    private String observacion;    // La columna "OBS"

    public ReporteItem(int numeroLista, String nombreSocio, double lecturaAnterior, double lecturaActual, double consumo, double importeMinimo, double importeExceso, double totalPagar, String observacion) {
        this.numeroLista = numeroLista;
        this.nombreSocio = nombreSocio;
        this.lecturaAnterior = lecturaAnterior;
        this.lecturaActual = lecturaActual;
        this.consumo = consumo;
        this.importeMinimo = importeMinimo;
        this.importeExceso = importeExceso;
        this.totalPagar = totalPagar;
        this.observacion = observacion;
    }

    // Getters
    public int getNumeroLista() { return numeroLista; }
    public String getNombreSocio() { return nombreSocio; }
    public double getLecturaAnterior() { return lecturaAnterior; }
    public double getLecturaActual() { return lecturaActual; }
    public double getConsumo() { return consumo; }
    public double getImporteMinimo() { return importeMinimo; }
    public double getImporteExceso() { return importeExceso; }
    public double getTotalPagar() { return totalPagar; }
    public String getObservacion() { return observacion; }
}