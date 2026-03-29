package com.function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.function.db.DatabaseManager;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.*;
import java.util.*;

public class PrestamosFunction {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @FunctionName("listarPrestamos")
    public HttpResponseMessage listar(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.GET},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "prestamos")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Listar todos los prestamos");

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM prestamos")) {

            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapRow(rs));
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(lista))
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error listar prestamos: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("obtenerPrestamo")
    public HttpResponseMessage obtener(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.GET},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "prestamos/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Obtener prestamo id=" + id);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM prestamos WHERE id = ?")) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return error(request, HttpStatus.NOT_FOUND, "Prestamo no encontrado");
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(mapRow(rs)))
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error obtener prestamo: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("crearPrestamo")
    public HttpResponseMessage crear(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.POST},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "prestamos")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Crear prestamo");

        try {
            Map<String, Object> body = mapper.readValue(
                    request.getBody().orElse("{}"),
                    new TypeReference<Map<String, Object>>() {});

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO prestamos (usuario_id, titulo_libro, autor, fecha_prestamo, fecha_devolucion, estado) " +
                         "VALUES (?, ?, ?, ?, ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {

                stmt.setLong(1, Long.parseLong(body.get("usuarioId").toString()));
                stmt.setString(2, (String) body.get("tituloLibro"));
                stmt.setString(3, (String) body.get("autor"));
                stmt.setDate(4, java.sql.Date.valueOf((String) body.get("fechaPrestamo")));
                stmt.setDate(5, body.get("fechaDevolucion") != null
                        ? java.sql.Date.valueOf((String) body.get("fechaDevolucion")) : null);
                stmt.setString(6, (String) body.get("estado"));
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    body.put("id", keys.getLong(1));
                }

                return request.createResponseBuilder(HttpStatus.CREATED)
                        .header("Content-Type", "application/json")
                        .body(mapper.writeValueAsString(body))
                        .build();
            }

        } catch (Exception e) {
            context.getLogger().severe("Error crear prestamo: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("actualizarPrestamo")
    public HttpResponseMessage actualizar(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.PUT},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "prestamos/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Actualizar prestamo id=" + id);

        try {
            Map<String, Object> body = mapper.readValue(
                    request.getBody().orElse("{}"),
                    new TypeReference<Map<String, Object>>() {});

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE prestamos SET usuario_id=?, titulo_libro=?, autor=?, " +
                         "fecha_prestamo=?, fecha_devolucion=?, estado=? WHERE id=?")) {

                stmt.setLong(1, Long.parseLong(body.get("usuarioId").toString()));
                stmt.setString(2, (String) body.get("tituloLibro"));
                stmt.setString(3, (String) body.get("autor"));
                stmt.setDate(4, java.sql.Date.valueOf((String) body.get("fechaPrestamo")));
                stmt.setDate(5, body.get("fechaDevolucion") != null
                        ? java.sql.Date.valueOf((String) body.get("fechaDevolucion")) : null);
                stmt.setString(6, (String) body.get("estado"));
                stmt.setLong(7, id);

                if (stmt.executeUpdate() == 0) {
                    return error(request, HttpStatus.NOT_FOUND, "Prestamo no encontrado");
                }

                body.put("id", id);
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(mapper.writeValueAsString(body))
                        .build();
            }

        } catch (Exception e) {
            context.getLogger().severe("Error actualizar prestamo: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("eliminarPrestamo")
    public HttpResponseMessage eliminar(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.DELETE},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "prestamos/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Eliminar prestamo id=" + id);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM prestamos WHERE id=?")) {

            stmt.setLong(1, id);
            if (stmt.executeUpdate() == 0) {
                return error(request, HttpStatus.NOT_FOUND, "Prestamo no encontrado");
            }

            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();

        } catch (Exception e) {
            context.getLogger().severe("Error eliminar prestamo: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              rs.getLong("id"));
        m.put("usuarioId",       rs.getLong("usuario_id"));
        m.put("tituloLibro",     rs.getString("titulo_libro"));
        m.put("autor",           rs.getString("autor"));
        m.put("fechaPrestamo",   rs.getString("fecha_prestamo"));
        m.put("fechaDevolucion", rs.getString("fecha_devolucion"));
        m.put("estado",          rs.getString("estado"));
        return m;
    }

    private HttpResponseMessage error(HttpRequestMessage<?> req, HttpStatus status, String msg) {
        return req.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"" + msg + "\"}")
                .build();
    }
}
