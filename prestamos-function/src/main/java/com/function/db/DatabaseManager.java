package com.function.db;

import java.sql.*;

public class DatabaseManager {

    private static final String JDBC_URL =
            "jdbc:h2:mem:prestamosdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    static {
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "");
                 Statement stmt = conn.createStatement()) {

                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS prestamos (" +
                    "  id               BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  usuario_id       BIGINT       NOT NULL," +
                    "  titulo_libro     VARCHAR(200) NOT NULL," +
                    "  autor            VARCHAR(150) NOT NULL," +
                    "  fecha_prestamo   DATE         NOT NULL," +
                    "  fecha_devolucion DATE," +
                    "  estado           VARCHAR(20)  NOT NULL" +
                    ")"
                );

                // Datos iniciales para el demo
                try { stmt.execute("INSERT INTO prestamos (usuario_id,titulo_libro,autor,fecha_prestamo,fecha_devolucion,estado) VALUES (1,'Clean Code','Robert C. Martin','2024-03-01',NULL,'ACTIVO')"); } catch (Exception ignored) {}
                try { stmt.execute("INSERT INTO prestamos (usuario_id,titulo_libro,autor,fecha_prestamo,fecha_devolucion,estado) VALUES (2,'The Pragmatic Programmer','David Thomas','2024-02-15','2024-03-01','DEVUELTO')"); } catch (Exception ignored) {}
                try { stmt.execute("INSERT INTO prestamos (usuario_id,titulo_libro,autor,fecha_prestamo,fecha_devolucion,estado) VALUES (1,'Design Patterns','Gang of Four','2024-03-10',NULL,'ACTIVO')"); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar la base de datos de prestamos", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, "sa", "");
    }
}
