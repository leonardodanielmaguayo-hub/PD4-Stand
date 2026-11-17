package com.pd4stand;

import com.pd4stand.controller.ControladorStands;
import com.pd4stand.model.Editorial;
import com.pd4stand.model.EstadoStand;
import com.pd4stand.model.Stand;
import com.pd4stand.persistence.JdbcRepositorio;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Editorial filey = new Editorial("FILEY", "filey");
        Editorial arroy = new Editorial("ARROY", "arroy");
        Editorial larrous = new Editorial("LARROUS", "larrous");
        List<Editorial> editoriales = Arrays.asList(filey, arroy, larrous);

        List<Stand> stands = Arrays.asList(
                new Stand("A1", 10, 500, EstadoStand.DISPONIBLE, filey),
                new Stand("A2", 15, 500, EstadoStand.DISPONIBLE, arroy),
                new Stand("B1", 20, 450, EstadoStand.DISPONIBLE, larrous));

        String url = System.getProperty("stands.jdbc.url");
        if (url == null || url.trim().isEmpty()) {
            url = System.getenv("STANDS_JDBC_URL");
        }
        if (url == null || url.trim().isEmpty()) {
            url = "jdbc:sqlite:stands.db";
        }

        JdbcRepositorio repositorio = new JdbcRepositorio(url);
        Scanner entrada = new Scanner(System.in);
        ControladorStands controlador = new ControladorStands(repositorio, entrada);
        try {
            repositorio.inicializar(editoriales, stands);
            controlador.iniciar();
        } catch (SQLException | IOException error) {
            System.err.println("No se pudo iniciar el sistema: " + error.getMessage());
            System.exit(1);
        } finally {
            entrada.close();
        }
    }
}
