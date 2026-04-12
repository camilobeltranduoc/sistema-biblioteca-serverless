package com.function.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:h2:mem:librosgraphqldb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS libros (" +
                    "  id         BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  titulo     VARCHAR(200) NOT NULL," +
                    "  autor      VARCHAR(150) NOT NULL," +
                    "  isbn       VARCHAR(20)  NOT NULL UNIQUE," +
                    "  genero     VARCHAR(80)  NOT NULL," +
                    "  disponible BOOLEAN      NOT NULL DEFAULT TRUE" +
                    ")"
                );

                try { stmt.execute("INSERT INTO libros (titulo, autor, isbn, genero, disponible) VALUES ('Clean Code', 'Robert C. Martin', '978-0132350884', 'Programacion', false)"); } catch (Exception ignored) {}
                try { stmt.execute("INSERT INTO libros (titulo, autor, isbn, genero, disponible) VALUES ('The Pragmatic Programmer', 'David Thomas', '978-0135957059', 'Programacion', true)"); } catch (Exception ignored) {}
                try { stmt.execute("INSERT INTO libros (titulo, autor, isbn, genero, disponible) VALUES ('Design Patterns', 'Gang of Four', '978-0201633610', 'Arquitectura', false)"); } catch (Exception ignored) {}
                try { stmt.execute("INSERT INTO libros (titulo, autor, isbn, genero, disponible) VALUES ('Clean Architecture', 'Robert C. Martin', '978-0134494166', 'Arquitectura', true)"); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando base de datos de libros", e);
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
