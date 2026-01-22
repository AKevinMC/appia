package com.example.apmojocoya;

public class ReadingItem {
    private String userId;
    private String userName;
    private String meterId; // ID del documento del medidor
    private String meterNumber; // El número visible
    private double previousReading;
    private double currentReading;
    private boolean isUpdated; // Para saber si ya escribimos algo
    private String estado;
    public ReadingItem(String userId, String userName, String meterId, String meterNumber, double previousReading) {

        this.userId = userId;
        this.userName = userName;
        this.meterId = meterId;
        this.meterNumber = meterNumber;
        this.previousReading = previousReading;
        this.currentReading = 0; // Por defecto
        this.isUpdated = false;
        this.estado = "Normal";
    }




    // Getters y Setters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getMeterId() { return meterId; }
    public String getMeterNumber() { return meterNumber; }
    public double getPreviousReading() { return previousReading; }

    public double getCurrentReading() { return currentReading; }
    public void setCurrentReading(double currentReading) {
        this.currentReading = currentReading;
        this.isUpdated = true;
    }
    public String getEstado() { return estado; }
    public void setEstado(String estado){
        this.estado = estado;
        this.isUpdated = true; // Cambiar estado cuenta como actualización
    }
    public boolean isUpdated() { return isUpdated; }

    // Calcula el consumo al vuelo
    public double getConsumo() {
        if (!isUpdated) return 0;
        double val = currentReading - previousReading;
        return val < 0 ? 0 : val;
    }
}