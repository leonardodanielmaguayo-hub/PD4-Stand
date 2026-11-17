package com.pd4stand.model;

public final class Editorial {
    private final String id;
    private final String nombre;

    public Editorial(String id, String nombre) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la editorial es obligatorio.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la editorial es obligatorio.");
        }
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
