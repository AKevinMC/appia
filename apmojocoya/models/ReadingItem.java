package com.example.apmojocoya.models;

public class ReadingItem {
    private String userId;
    private String userName;
    private String userType; // <--- NUEVO CAMPO
    private String meterId;
    private String meterNumber;
    private double previousReading;
    private double currentReading;
    private boolean isUpdated;
    private String estado;

    // Constructor actualizado recibe el tipo
    public ReadingItem(String userId, String userName, String userType, String meterId, String meterNumber, double previousReading) {
        this.userId = userId;
        this.userName = userName;
        this.userType = userType; // <--- GUARDARLO
        this.meterId = meterId;
        this.meterNumber = meterNumber;
        this.previousReading = previousReading;
        this.currentReading = 0;
        this.isUpdated = false;
        this.estado = "Normal";
    }

    // Getters y Setters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserType() { return userType; } // <--- NUEVO GETTER
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
        this.isUpdated = true;
    }
    public boolean isUpdated() { return isUpdated; }

    public double getConsumo() {
        if (!isUpdated) return 0;
        double val = currentReading - previousReading;
        return val < 0 ? 0 : val;
    }
}