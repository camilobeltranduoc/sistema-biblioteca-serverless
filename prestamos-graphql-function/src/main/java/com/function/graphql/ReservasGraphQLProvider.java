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

public class ReservasGraphQLProvider {

    private static final String SDL =
        "type Reserva {" +
        "    id: ID!" +
        "    usuarioId: Int!" +
        "    tituloLibro: String!" +
        "    fechaReserva: String!" +
        "    fechaVencimiento: String!" +
        "    estado: String!" +
        "}" +
        "input ReservaInput {" +
        "    usuarioId: Int!" +
        "    tituloLibro: String!" +
        "    fechaReserva: String!" +
        "    fechaVencimiento: String!" +
        "    estado: String!" +
        "}" +
        "type Query {" +
        "    reservas: [Reserva!]!" +
        "    reserva(id: ID!): Reserva" +
        "    reservasPorEstado(estado: String!): [Reserva!]!" +
        "}" +
        "type Mutation {" +
        "    crearReserva(input: ReservaInput!): Reserva!" +
        "    actualizarReserva(id: ID!, input: ReservaInput!): Reserva" +
        "    eliminarReserva(id: ID!): Boolean!" +
        "}";

    private static final GraphQL GRAPHQL;

    static {
        SchemaParser parser = new SchemaParser();
        TypeDefinitionRegistry registry = parser.parse(SDL);

        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
            .type("Query", builder -> builder
                .dataFetcher("reservas", env -> fetchAllReservas())
                .dataFetcher("reserva", env -> {
                    long id = Long.parseLong(env.getArgument("id").toString());
                    return fetchReservaById(id);
                })
                .dataFetcher("reservasPorEstado", env ->
                    fetchReservasByEstado(env.getArgument("estado")))
            )
            .type("Mutation", builder -> builder
                .dataFetcher("crearReserva", env -> createReserva(env.getArgument("input")))
                .dataFetcher("actualizarReserva", env -> updateReserva(
                        Long.parseLong(env.getArgument("id").toString()),
                        env.getArgument("input")))
                .dataFetcher("eliminarReserva", env -> deleteReserva(
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

    private static List<Map<String, Object>> fetchAllReservas() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM reservas")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private static Map<String, Object> fetchReservaById(long id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM reservas WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        }
    }

    private static List<Map<String, Object>> fetchReservasByEstado(String estado) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM reservas WHERE estado = ?")) {
            stmt.setString(1, estado);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private static Map<String, Object> createReserva(Map<String, Object> input) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO reservas (usuario_id, titulo_libro, fecha_reserva, fecha_vencimiento, estado) VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, Long.parseLong(input.get("usuarioId").toString()));
            stmt.setString(2, (String) input.get("tituloLibro"));
            stmt.setDate(3, java.sql.Date.valueOf((String) input.get("fechaReserva")));
            stmt.setDate(4, java.sql.Date.valueOf((String) input.get("fechaVencimiento")));
            stmt.setString(5, (String) input.get("estado"));
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            long newId = keys.next() ? keys.getLong(1) : -1;
            return fetchReservaById(newId);
        }
    }

    private static Map<String, Object> updateReserva(long id, Map<String, Object> input) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE reservas SET usuario_id=?, titulo_libro=?, fecha_reserva=?, fecha_vencimiento=?, estado=? WHERE id=?")) {
            stmt.setLong(1, Long.parseLong(input.get("usuarioId").toString()));
            stmt.setString(2, (String) input.get("tituloLibro"));
            stmt.setDate(3, java.sql.Date.valueOf((String) input.get("fechaReserva")));
            stmt.setDate(4, java.sql.Date.valueOf((String) input.get("fechaVencimiento")));
            stmt.setString(5, (String) input.get("estado"));
            stmt.setLong(6, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? fetchReservaById(id) : null;
        }
    }

    private static boolean deleteReserva(long id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM reservas WHERE id=?")) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private static Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               rs.getLong("id"));
        m.put("usuarioId",        rs.getLong("usuario_id"));
        m.put("tituloLibro",      rs.getString("titulo_libro"));
        m.put("fechaReserva",     rs.getString("fecha_reserva"));
        m.put("fechaVencimiento", rs.getString("fecha_vencimiento"));
        m.put("estado",           rs.getString("estado"));
        return m;
    }
}
