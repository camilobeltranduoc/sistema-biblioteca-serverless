package com.function.db;

import java.sql.*;

public class DatabaseManager {

    private static final String JDBC_URL =
            "jdbc:h2:mem:usuariosdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    static {
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "");
                 Statement stmt = conn.createStatement()) {

                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS usuarios (" +
                    "  id       BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  nombre   VARCHAR(100) NOT NULL," +
                    "  apellido VARCHAR(100) NOT NULL," +
                    "  email    VARCHAR(150) NOT NULL UNIQUE," +
                    "  rut      VARCHAR(12)  NOT NULL UNIQUE" +
                    ")"
                );


                try { stmt.execute("INSERT INTO usuarios (nombre,apellido,email,rut) VALUES ('Juan','Gonzalez','juan.gonzalez@correo.cl','12345678-9')"); } catch (Exception ignored) {}
                try { stmt.execute("INSERT INTO usuarios (nombre,apellido,email,rut) VALUES ('Maria','Lopez','maria.lopez@correo.cl','98765432-1')"); } catch (Exception ignored) {}
                try { stmt.execute("INSERT INTO usuarios (nombre,apellido,email,rut) VALUES ('Carlos','Ramirez','carlos.ramirez@correo.cl','11223344-5')"); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar la base de datos de usuarios", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, "sa", "");
    }
}
