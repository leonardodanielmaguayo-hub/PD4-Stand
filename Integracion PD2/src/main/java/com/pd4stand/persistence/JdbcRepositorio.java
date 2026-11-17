package com.pd4stand.persistence;

import com.pd4stand.model.Editorial;
import com.pd4stand.model.EstadoStand;
import com.pd4stand.model.Reserva;
import com.pd4stand.model.Stand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class JdbcRepositorio {
    private final String url;

    public JdbcRepositorio(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL JDBC es obligatoria.");
        }
        this.url = url;
    }

    public void inicializar(List<Editorial> editoriales, List<Stand> stands)
            throws SQLException, IOException {
        ejecutarEsquema();
        try (Connection connection = abrirConexion()) {
            connection.setAutoCommit(false);
            try {
                for (Editorial editorial : editoriales) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT OR IGNORE INTO editoriales (id, nombre) VALUES (?, ?)")) {
                        statement.setString(1, editorial.getId());
                        statement.setString(2, editorial.getNombre());
                        statement.executeUpdate();
                    }
                }
                for (Stand stand : stands) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT OR IGNORE INTO stands "
                                    + "(id, metros_cuadrados, costo_por_m2, estado, editorial_id) "
                                    + "VALUES (?, ?, ?, ?, ?)")) {
                        statement.setString(1, stand.getId());
                        statement.setDouble(2, stand.getMetrosCuadrados());
                        statement.setDouble(3, stand.getCostoPorMetroCuadrado());
                        statement.setString(4, stand.getEstado().name());
                        statement.setString(5, stand.getEditorial().getId());
                        statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    public List<Stand> listarStands() throws SQLException {
        String sql = "SELECT s.id, s.metros_cuadrados, s.costo_por_m2, s.estado, "
                + "e.id AS editorial_id, e.nombre AS editorial_nombre "
                + "FROM stands s JOIN editoriales e ON e.id = s.editorial_id ORDER BY s.id";
        List<Stand> stands = new ArrayList<Stand>();
        try (Connection connection = abrirConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                stands.add(mapearStand(results));
            }
        }
        return stands;
    }

    public Stand buscarStand(String id) throws SQLException {
        String sql = "SELECT s.id, s.metros_cuadrados, s.costo_por_m2, s.estado, "
                + "e.id AS editorial_id, e.nombre AS editorial_nombre "
                + "FROM stands s JOIN editoriales e ON e.id = s.editorial_id WHERE s.id = ?";
        try (Connection connection = abrirConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? mapearStand(results) : null;
            }
        }
    }

    public List<Editorial> listarEditoriales() throws SQLException {
        List<Editorial> editoriales = new ArrayList<Editorial>();
        try (Connection connection = abrirConexion();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, nombre FROM editoriales ORDER BY nombre");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                editoriales.add(new Editorial(results.getString("id"), results.getString("nombre")));
            }
        }
        return editoriales;
    }

    public List<Reserva> listarReservas() throws SQLException {
        String sql = "SELECT r.id AS reserva_id, r.cliente, r.fecha_reserva, r.fecha_limite, "
                + "s.id, s.metros_cuadrados, s.costo_por_m2, s.estado, "
                + "e.id AS editorial_id, e.nombre AS editorial_nombre "
                + "FROM reservas r JOIN stands s ON s.id = r.stand_id "
                + "JOIN editoriales e ON e.id = s.editorial_id ORDER BY r.id";
        List<Reserva> reservas = new ArrayList<Reserva>();
        try (Connection connection = abrirConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                reservas.add(new Reserva(
                        results.getLong("reserva_id"),
                        mapearStand(results),
                        results.getString("cliente"),
                        LocalDateTime.parse(results.getString("fecha_reserva")),
                        LocalDateTime.parse(results.getString("fecha_limite"))));
            }
        }
        return reservas;
    }

    public void cambiarEstado(String id, EstadoStand estado) throws SQLException {
        try (Connection connection = abrirConexion();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE stands SET estado = ? WHERE id = ?")) {
            statement.setString(1, estado.name());
            statement.setString(2, id);
            statement.executeUpdate();
        }
    }

    public long guardarReserva(Reserva reserva) throws SQLException {
        try (Connection connection = abrirConexion()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE stands SET estado = 'RESERVADO' "
                                + "WHERE id = ? AND estado = 'DISPONIBLE'")) {
                    statement.setString(1, reserva.getStand().getId());
                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "El stand ya no está disponible; actualiza la lista e inténtalo de nuevo.");
                    }
                }

                long id;
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO reservas (stand_id, cliente, fecha_reserva, fecha_limite) "
                                + "VALUES (?, ?, ?, ?)")) {
                    statement.setString(1, reserva.getStand().getId());
                    statement.setString(2, reserva.getCliente());
                    statement.setString(3, reserva.getFechaReserva().toString());
                    statement.setString(4, reserva.getFechaLimite().toString());
                    statement.executeUpdate();
                }
                try (Statement statement = connection.createStatement();
                     ResultSet results = statement.executeQuery("SELECT last_insert_rowid()")) {
                    results.next();
                    id = results.getLong(1);
                }
                connection.commit();
                return id;
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    private void ejecutarEsquema() throws SQLException, IOException {
        InputStream resource = JdbcRepositorio.class.getResourceAsStream("/schema.sql");
        if (resource == null) {
            throw new IOException("No se encontró el recurso de base de datos schema.sql.");
        }
        StringBuilder schema = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                schema.append(line).append('\n');
            }
        }
        try (Connection connection = abrirConexion(); Statement statement = connection.createStatement()) {
            for (String sql : schema.toString().split(";")) {
                if (!sql.trim().isEmpty()) {
                    statement.execute(sql);
                }
            }
        }
    }

    private Connection abrirConexion() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException error) {
            connection.close();
            throw error;
        }
        return connection;
    }

    private Stand mapearStand(ResultSet results) throws SQLException {
        Editorial editorial = new Editorial(
                results.getString("editorial_id"),
                results.getString("editorial_nombre"));
        return new Stand(
                results.getString("id"),
                results.getDouble("metros_cuadrados"),
                results.getDouble("costo_por_m2"),
                EstadoStand.valueOf(results.getString("estado")),
                editorial);
    }
}
