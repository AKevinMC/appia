package com.example.apmojocoya;

public class Usuario {
    private String id;
    private String name;
    private int medidoresCount;

    public Usuario(String id, String name, int medidoresCount) {
        this.id = id;
        this.name = name;
        this.medidoresCount = medidoresCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMedidoresCount() {
        return medidoresCount;
    }
}
