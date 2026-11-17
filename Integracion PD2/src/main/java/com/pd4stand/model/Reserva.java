package com.pd4stand.model;

import java.time.LocalDateTime;

public final class Reserva {
    private final long id;
    private final Stand stand;
    private final String cliente;
    private final LocalDateTime fechaReserva;
    private final LocalDateTime fechaLimite;

    public Reserva(long id, Stand stand, String cliente, LocalDateTime fechaReserva,
                   LocalDateTime fechaLimite) {
        if (stand == null || cliente == null || cliente.trim().isEmpty()
                || fechaReserva == null || fechaLimite == null) {
            throw new IllegalArgumentException("Los datos de la reserva son obligatorios.");
        }
        this.id = id;
        this.stand = stand;
        this.cliente = cliente;
        this.fechaReserva = fechaReserva;
        this.fechaLimite = fechaLimite;
    }

    public long getId() {
        return id;
    }

    public Stand getStand() {
        return stand;
    }

    public String getCliente() {
        return cliente;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }
}
