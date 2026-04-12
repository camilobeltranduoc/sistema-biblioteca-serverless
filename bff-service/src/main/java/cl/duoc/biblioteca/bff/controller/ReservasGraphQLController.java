package cl.duoc.biblioteca.bff.controller;

import cl.duoc.biblioteca.bff.service.PrestamosGraphQLService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservas/graphql")
public class ReservasGraphQLController {

    private final PrestamosGraphQLService service;

    public ReservasGraphQLController(PrestamosGraphQLService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map> graphql(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.ejecutar(body));
    }
}
