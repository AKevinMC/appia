package com.example.apmojocoya;

public class Usuario {
    private String id;
    private String name;
    private int medidoresCount;
    private String tipo; // Nuevo campo

    public Usuario(String id, String name, int medidoresCount, String tipo) {
        this.id = id;
        this.name = name;
        this.medidoresCount = medidoresCount;
        this.tipo = tipo;
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

    public String getTipo() {
        return tipo;
    }
}