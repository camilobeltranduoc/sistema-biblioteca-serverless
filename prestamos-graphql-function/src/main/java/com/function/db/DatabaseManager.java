package com.function.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:h2:mem:reservasgraphqldb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS reservas (" +
                    "  id                BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  usuario_id        BIGINT       NOT NULL," +
                    "  titulo_libro      VARCHAR(200) NOT NULL," +
                    "  fecha_reserva     DATE         NOT NULL," +
                    "  fecha_vencimiento DATE         NOT NULL," +
                    "  estado            VARCHAR(20)  NOT NULL" +
                    ")"
                );

                try { stmt.execute("INSERT INTO reservas (usuario_id, titulo_libro, fecha_reserva, fecha_vencimiento, estado) VALUES (1, 'Clean Code', '2024-03-01', '2024-03-08', 'ACTIVA')"); } catch (Exception ignored) {}
                try { stmt.execute("INSERT INTO reservas (usuario_id, titulo_libro, fecha_reserva, fecha_vencimiento, estado) VALUES (2, 'Design Patterns', '2024-03-05', '2024-03-12', 'EXPIRADA')"); } catch (Exception ignored) {}
                try { stmt.execute("INSERT INTO reservas (usuario_id, titulo_libro, fecha_reserva, fecha_vencimiento, estado) VALUES (3, 'Clean Architecture', '2024-03-10', '2024-03-17', 'ACTIVA')"); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando base de datos de reservas", e);
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
