package com.function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.function.graphql.LibrosGraphQLProvider;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import graphql.ExecutionInput;
import graphql.ExecutionResult;

import java.util.Map;
import java.util.Optional;

public class LibrosGraphQLFunction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @FunctionName("graphqlLibros")
    public HttpResponseMessage execute(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "graphql")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("GraphQL request recibido en libros-graphql-function");

        try {
            String body = request.getBody().orElse("{}");
            Map<String, Object> gqlRequest = MAPPER.readValue(
                    body, new TypeReference<Map<String, Object>>() {});

            String query = (String) gqlRequest.getOrDefault("query", "");
            String operationName = (String) gqlRequest.get("operationName");

            @SuppressWarnings("unchecked")
            Map<String, Object> variables = gqlRequest.get("variables") instanceof Map
                    ? (Map<String, Object>) gqlRequest.get("variables")
                    : Map.of();

            ExecutionInput input = ExecutionInput.newExecutionInput()
                    .query(query)
                    .operationName(operationName)
                    .variables(variables)
                    .build();

            ExecutionResult result = LibrosGraphQLProvider.get().execute(input);

            String responseBody = MAPPER.writeValueAsString(result.toSpecification());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseBody)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error en GraphQL libros: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body("{\"errors\":[{\"message\":\"" + e.getMessage() + "\"}]}")
                    .build();
        }
    }
}
