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

    @Value("${usuarios.graphql.url}")
    private String usuariosGraphqlUrl;

    @Value("${prestamos.graphql.url}")
    private String prestamosGraphqlUrl;

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

    @Bean("usuariosGraphqlClient")
    public WebClient usuariosGraphqlClient() {
        return WebClient.builder()
                .baseUrl(usuariosGraphqlUrl)
                .build();
    }

    @Bean("prestamosGraphqlClient")
    public WebClient prestamosGraphqlClient() {
        return WebClient.builder()
                .baseUrl(prestamosGraphqlUrl)
                .build();
    }
}
