package com.pd4stand.controller;

import com.pd4stand.model.Editorial;
import com.pd4stand.model.EstadoStand;
import com.pd4stand.model.Reserva;
import com.pd4stand.model.Stand;
import com.pd4stand.persistence.JdbcRepositorio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public final class ControladorStands {
    private final JdbcRepositorio repositorio;
    private final Scanner entrada;

    public ControladorStands(JdbcRepositorio repositorio, Scanner entrada) {
        this.repositorio = repositorio;
        this.entrada = entrada;
    }

    public void iniciar() {
        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            String opcion = entrada.nextLine().trim();
            try {
                switch (opcion) {
                    case "1":
                        mostrarStands();
                        break;
                    case "2":
                        cambiarEstado();
                        break;
                    case "3":
                        System.out.println("Hasta luego.");
                        continuar = false;
                        break;
                    case "4":
                        reservarStand();
                        break;
                    case "5":
                        mostrarEditoriales();
                        break;
                    case "6":
                        mostrarReservas();
                        break;
                    default:
                        System.out.println("Opción no válida. Elige una opción del menú.");
                }
            } catch (SQLException | IllegalArgumentException error) {
                System.out.println("Error: " + error.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\n--- Sistema de stands ---");
        System.out.println("1. Ver stands");
        System.out.println("2. Cambiar estado de un stand");
        System.out.println("3. Salir");
        System.out.println("4. Reservar un stand");
        System.out.println("5. Ver editoriales");
        System.out.println("6. Ver reservas");
        System.out.print("Elige una opción: ");
    }

    private void mostrarStands() throws SQLException {
        List<Stand> stands = repositorio.listarStands();
        System.out.println("\nStands registrados:");
        if (stands.isEmpty()) {
            System.out.println("No hay stands registrados.");
            return;
        }
        for (Stand stand : stands) {
            System.out.printf("%s: %.2f m², $%.2f, estado: %s, editorial: %s%n",
                    stand.getId(), stand.getMetrosCuadrados(), stand.getCostoTotal(),
                    stand.getEstado(), stand.getEditorial().getNombre());
        }
    }

    private void cambiarEstado() throws SQLException {
        mostrarStands();
        System.out.print("\nEscribe el ID del stand que quieres cambiar: ");
        String id = entrada.nextLine().trim();
        Stand stand = repositorio.buscarStand(id);
        if (stand == null) {
            throw new IllegalArgumentException("El stand " + id + " no existe en el sistema.");
        }

        EstadoStand[] estados = EstadoStand.values();
        System.out.println("\nElige el nuevo estado:");
        for (int i = 0; i < estados.length; i++) {
            System.out.printf("%d. %s%n", i + 1, estados[i]);
        }
        System.out.print("Opción: ");
        String seleccion = entrada.nextLine().trim();
        int indice;
        try {
            indice = Integer.parseInt(seleccion) - 1;
        } catch (NumberFormatException error) {
            System.out.println("Opción de estado no válida.");
            return;
        }
        if (indice < 0 || indice >= estados.length) {
            System.out.println("Opción de estado no válida.");
            return;
        }

        repositorio.cambiarEstado(id, estados[indice]);
        System.out.printf("El stand %s ahora está %s.%n", id, estados[indice]);
    }

    private void reservarStand() throws SQLException {
        mostrarStands();
        System.out.print("\nID del stand disponible: ");
        String id = entrada.nextLine().trim();
        Stand stand = repositorio.buscarStand(id);
        if (stand == null) {
            throw new IllegalArgumentException("El stand " + id + " no existe en el sistema.");
        }
        if (stand.getEstado() != EstadoStand.DISPONIBLE) {
            throw new IllegalArgumentException(
                    "El stand " + id + " ya se encuentra " + stand.getEstado() + ".");
        }

        System.out.print("Nombre del cliente: ");
        String cliente = entrada.nextLine().trim();
        System.out.print("Horas de vigencia (24 por defecto): ");
        String horasIngresadas = entrada.nextLine().trim();
        int horas = horasIngresadas.isEmpty() ? 24 : Integer.parseInt(horasIngresadas);
        if (horas <= 0) {
            throw new IllegalArgumentException("Las horas de vigencia deben ser mayores que cero.");
        }
        LocalDateTime fecha = LocalDateTime.now();
        Reserva reserva = new Reserva(0, stand, cliente, fecha, fecha.plusHours(horas));
        long idReserva = repositorio.guardarReserva(reserva);
        System.out.printf("Éxito: Stand %s reservado para %s. Vence el %s (reserva #%d).%n",
                id, cliente, reserva.getFechaLimite(), idReserva);
    }

    private void mostrarEditoriales() throws SQLException {
        List<Editorial> editoriales = repositorio.listarEditoriales();
        System.out.println("\nEditoriales:");
        for (Editorial editorial : editoriales) {
            System.out.printf("%s (%s)%n", editorial.getNombre(), editorial.getId());
        }
    }

    private void mostrarReservas() throws SQLException {
        List<Reserva> reservas = repositorio.listarReservas();
        System.out.println("\nReservas:");
        if (reservas.isEmpty()) {
            System.out.println("No hay reservas registradas.");
            return;
        }
        for (Reserva reserva : reservas) {
            System.out.printf("#%d - Stand %s, cliente: %s, reserva: %s, vence: %s%n",
                    reserva.getId(), reserva.getStand().getId(), reserva.getCliente(),
                    reserva.getFechaReserva(), reserva.getFechaLimite());
        }
    }
}
