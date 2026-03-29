package com.function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.function.db.DatabaseManager;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.*;
import java.util.*;

public class UsuariosFunction {

    private static final ObjectMapper mapper = new ObjectMapper();

    @FunctionName("listarUsuarios")
    public HttpResponseMessage listar(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.GET},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "usuarios")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Listar todos los usuarios");

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM usuarios")) {

            List<Map<String, Object>> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapRow(rs));
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(lista))
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error listar usuarios: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("obtenerUsuario")
    public HttpResponseMessage obtener(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.GET},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "usuarios/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Obtener usuario id=" + id);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM usuarios WHERE id = ?")) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return error(request, HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(mapRow(rs)))
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error obtener usuario: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("crearUsuario")
    public HttpResponseMessage crear(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.POST},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "usuarios")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Crear usuario");

        try {
            Map<String, Object> body = mapper.readValue(
                    request.getBody().orElse("{}"),
                    new TypeReference<Map<String, Object>>() {});

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO usuarios (nombre, apellido, email, rut) VALUES (?, ?, ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, (String) body.get("nombre"));
                stmt.setString(2, (String) body.get("apellido"));
                stmt.setString(3, (String) body.get("email"));
                stmt.setString(4, (String) body.get("rut"));
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
            context.getLogger().severe("Error crear usuario: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("actualizarUsuario")
    public HttpResponseMessage actualizar(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.PUT},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "usuarios/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Actualizar usuario id=" + id);

        try {
            Map<String, Object> body = mapper.readValue(
                    request.getBody().orElse("{}"),
                    new TypeReference<Map<String, Object>>() {});

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE usuarios SET nombre=?, apellido=?, email=?, rut=? WHERE id=?")) {

                stmt.setString(1, (String) body.get("nombre"));
                stmt.setString(2, (String) body.get("apellido"));
                stmt.setString(3, (String) body.get("email"));
                stmt.setString(4, (String) body.get("rut"));
                stmt.setLong(5, id);

                if (stmt.executeUpdate() == 0) {
                    return error(request, HttpStatus.NOT_FOUND, "Usuario no encontrado");
                }

                body.put("id", id);
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(mapper.writeValueAsString(body))
                        .build();
            }

        } catch (Exception e) {
            context.getLogger().severe("Error actualizar usuario: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("eliminarUsuario")
    public HttpResponseMessage eliminar(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.DELETE},
                         authLevel = AuthorizationLevel.ANONYMOUS,
                         route = "usuarios/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        context.getLogger().info("Eliminar usuario id=" + id);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM usuarios WHERE id=?")) {

            stmt.setLong(1, id);
            if (stmt.executeUpdate() == 0) {
                return error(request, HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }

            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();

        } catch (Exception e) {
            context.getLogger().severe("Error eliminar usuario: " + e.getMessage());
            return error(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",       rs.getLong("id"));
        m.put("nombre",   rs.getString("nombre"));
        m.put("apellido", rs.getString("apellido"));
        m.put("email",    rs.getString("email"));
        m.put("rut",      rs.getString("rut"));
        return m;
    }

    private HttpResponseMessage error(HttpRequestMessage<?> req, HttpStatus status, String msg) {
        return req.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"" + msg + "\"}")
                .build();
    }
}
