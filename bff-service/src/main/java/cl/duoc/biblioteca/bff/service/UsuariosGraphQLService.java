package cl.duoc.biblioteca.bff.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class UsuariosGraphQLService {

    private final WebClient client;

    public UsuariosGraphQLService(@Qualifier("usuariosGraphqlClient") WebClient client) {
        this.client = client;
    }

    public Map ejecutar(Map<String, Object> body) {
        return client.post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), Map.class)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
