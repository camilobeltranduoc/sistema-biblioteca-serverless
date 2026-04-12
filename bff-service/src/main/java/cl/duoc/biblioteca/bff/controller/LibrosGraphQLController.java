package cl.duoc.biblioteca.bff.controller;

import cl.duoc.biblioteca.bff.service.UsuariosGraphQLService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/libros/graphql")
public class LibrosGraphQLController {

    private final UsuariosGraphQLService service;

    public LibrosGraphQLController(UsuariosGraphQLService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map> graphql(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.ejecutar(body));
    }
}
