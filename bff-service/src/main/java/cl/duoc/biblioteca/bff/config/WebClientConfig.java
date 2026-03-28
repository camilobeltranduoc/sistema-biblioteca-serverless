package cl.duoc.biblioteca.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${usuarios.service.url}")
    private String usuariosUrl;

    @Value("${prestamos.service.url}")
    private String prestamosUrl;

    @Bean("usuariosClient")
    public WebClient usuariosClient() {
        return WebClient.builder()
                .baseUrl(usuariosUrl)
                .build();
    }

    @Bean("prestamosClient")
    public WebClient prestamosClient() {
        return WebClient.builder()
                .baseUrl(prestamosUrl)
                .build();
    }
}
