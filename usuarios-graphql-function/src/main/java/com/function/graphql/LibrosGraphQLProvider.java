package com.function.graphql;

import com.function.db.DatabaseManager;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.sql.*;
import java.util.*;

public class LibrosGraphQLProvider {

    private static final String SDL =
        "type Libro {" +
        "    id: ID!" +
        "    titulo: String!" +
        "    autor: String!" +
        "    isbn: String!" +
        "    genero: String!" +
        "    disponible: Boolean!" +
        "}" +
        "input LibroInput {" +
        "    titulo: String!" +
        "    autor: String!" +
        "    isbn: String!" +
        "    genero: String!" +
        "    disponible: Boolean!" +
        "}" +
        "type Query {" +
        "    libros: [Libro!]!" +
        "    libro(id: ID!): Libro" +
        "    librosPorGenero(genero: String!): [Libro!]!" +
        "}" +
        "type Mutation {" +
        "    crearLibro(input: LibroInput!): Libro!" +
        "    actualizarLibro(id: ID!, input: LibroInput!): Libro" +
        "    eliminarLibro(id: ID!): Boolean!" +
        "}";

    private static final GraphQL GRAPHQL;

    static {
        SchemaParser parser = new SchemaParser();
        TypeDefinitionRegistry registry = parser.parse(SDL);

        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
            .type("Query", builder -> builder
                .dataFetcher("libros", env -> fetchAllLibros())
                .dataFetcher("libro", env -> {
                    long id = Long.parseLong(env.getArgument("id").toString());
                    return fetchLibroById(id);
                })
                .dataFetcher("librosPorGenero", env ->
                    fetchLibrosByGenero(env.getArgument("genero")))
            )
            .type("Mutation", builder -> builder
                .dataFetcher("crearLibro", env -> createLibro(env.getArgument("input")))
                .dataFetcher("actualizarLibro", env -> updateLibro(
                        Long.parseLong(env.getArgument("id").toString()),
                        env.getArgument("input")))
                .dataFetcher("eliminarLibro", env -> deleteLibro(
                        Long.parseLong(env.getArgument("id").toString())))
            )
            .build();

        SchemaGenerator generator = new SchemaGenerator();
        GraphQLSchema schema = generator.makeExecutableSchema(registry, wiring);
        GRAPHQL = GraphQL.newGraphQL(schema).build();
    }

    public static GraphQL get() {
        return GRAPHQL;
    }

    private static List<Map<String, Object>> fetchAllLibros() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM libros")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private static Map<String, Object> fetchLibroById(long id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM libros WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        }
    }

    private static List<Map<String, Object>> fetchLibrosByGenero(String genero) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM libros WHERE genero = ?")) {
            stmt.setString(1, genero);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private static Map<String, Object> createLibro(Map<String, Object> input) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO libros (titulo, autor, isbn, genero, disponible) VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, (String) input.get("titulo"));
            stmt.setString(2, (String) input.get("autor"));
            stmt.setString(3, (String) input.get("isbn"));
            stmt.setString(4, (String) input.get("genero"));
            stmt.setBoolean(5, (Boolean) input.get("disponible"));
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            long newId = keys.next() ? keys.getLong(1) : -1;
            return fetchLibroById(newId);
        }
    }

    private static Map<String, Object> updateLibro(long id, Map<String, Object> input) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE libros SET titulo=?, autor=?, isbn=?, genero=?, disponible=? WHERE id=?")) {
            stmt.setString(1, (String) input.get("titulo"));
            stmt.setString(2, (String) input.get("autor"));
            stmt.setString(3, (String) input.get("isbn"));
            stmt.setString(4, (String) input.get("genero"));
            stmt.setBoolean(5, (Boolean) input.get("disponible"));
            stmt.setLong(6, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? fetchLibroById(id) : null;
        }
    }

    private static boolean deleteLibro(long id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM libros WHERE id=?")) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private static Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",         rs.getLong("id"));
        m.put("titulo",     rs.getString("titulo"));
        m.put("autor",      rs.getString("autor"));
        m.put("isbn",       rs.getString("isbn"));
        m.put("genero",     rs.getString("genero"));
        m.put("disponible", rs.getBoolean("disponible"));
        return m;
    }
}
