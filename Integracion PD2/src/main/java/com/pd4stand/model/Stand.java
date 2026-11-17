package com.pd4stand.model;

public final class Stand {
    private final String id;
    private final double metrosCuadrados;
    private final double costoPorMetroCuadrado;
    private final Editorial editorial;
    private EstadoStand estado;

    public Stand(String id, double metrosCuadrados, double costoPorMetroCuadrado,
                 EstadoStand estado, Editorial editorial) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del stand es obligatorio.");
        }
        if (metrosCuadrados <= 0 || Double.isNaN(metrosCuadrados) || Double.isInfinite(metrosCuadrados)) {
            throw new IllegalArgumentException("Los metros cuadrados deben ser mayores que cero.");
        }
        if (costoPorMetroCuadrado < 0 || Double.isNaN(costoPorMetroCuadrado)
                || Double.isInfinite(costoPorMetroCuadrado)) {
            throw new IllegalArgumentException("El costo por metro cuadrado no puede ser negativo.");
        }
        if (estado == null || editorial == null) {
            throw new IllegalArgumentException("El estado y la editorial son obligatorios.");
        }
        this.id = id;
        this.metrosCuadrados = metrosCuadrados;
        this.costoPorMetroCuadrado = costoPorMetroCuadrado;
        this.estado = estado;
        this.editorial = editorial;
    }

    public String getId() {
        return id;
    }

    public double getMetrosCuadrados() {
        return metrosCuadrados;
    }

    public double getCostoPorMetroCuadrado() {
        return costoPorMetroCuadrado;
    }

    public double getCostoTotal() {
        return metrosCuadrados * costoPorMetroCuadrado;
    }

    public EstadoStand getEstado() {
        return estado;
    }

    public void setEstado(EstadoStand estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado del stand es obligatorio.");
        }
        this.estado = estado;
    }

    public Editorial getEditorial() {
        return editorial;
    }
}
