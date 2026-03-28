package cl.duoc.biblioteca.bff.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    private final WebClient client;

    public UsuarioService(@Qualifier("usuariosClient") WebClient client) {
        this.client = client;
    }

    public List<Map> listarTodos() {
        return client.get()
                .uri("/usuarios")
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();
    }

    public Map obtenerPorId(Long id) {
        return client.get()
                .uri("/usuarios/{id}", id)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, r -> Mono.empty())
                .bodyToMono(Map.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .block();
    }

    public Map crear(Map<String, Object> body) {
        return client.post()
                .uri("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), Map.class)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map actualizar(Long id, Map<String, Object> body) {
        return client.put()
                .uri("/usuarios/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), Map.class)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, r -> Mono.empty())
                .bodyToMono(Map.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .block();
    }

    public void eliminar(Long id) {
        client.delete()
                .uri("/usuarios/{id}", id)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, r -> Mono.empty())
                .toBodilessEntity()
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .block();
    }
}
