package com.example.apmojocoya.models;

public class Tarifa {
    private String id;
    private double cargoFijo;    // El pago mínimo mensual
    private int limiteBasico;    // Tu límite de 6 m3
    private double costoExceso;  // Tus 4 Bs por m3 extra

    public Tarifa() {}

    public Tarifa(String id, double cargoFijo, int limiteBasico, double costoExceso) {
        this.id = id;
        this.cargoFijo = cargoFijo;
        this.limiteBasico = limiteBasico;
        this.costoExceso = costoExceso;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getCargoFijo() { return cargoFijo; }
    public void setCargoFijo(double cargoFijo) { this.cargoFijo = cargoFijo; }

    public int getLimiteBasico() { return limiteBasico; }
    public void setLimiteBasico(int limiteBasico) { this.limiteBasico = limiteBasico; }

    public double getCostoExceso() { return costoExceso; }
    public void setCostoExceso(double costoExceso) { this.costoExceso = costoExceso; }

    // --- CÁLCULO AUTOMÁTICO SEGÚN TUS REGLAS ---
    public double calcularMonto(double consumo) {
        // Caso 1: Gastó menos o igual a 6 (Está cubierto por el fijo)
        if (consumo <= limiteBasico) {
            return cargoFijo;
        }
        // Caso 2: Se pasó del límite
        else {
            double extra = consumo - limiteBasico;
            return cargoFijo + (extra * costoExceso);
        }
    }
}