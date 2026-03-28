package cl.duoc.biblioteca.bff.controller;

import cl.duoc.biblioteca.bff.service.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService service;

    public PrestamoController(PrestamoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Map>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map> obtener(@PathVariable Long id) {
        Map prestamo = service.obtenerPorId(id);
        if (prestamo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(prestamo);
    }

    @PostMapping
    public ResponseEntity<Map> crear(@RequestBody Map<String, Object> body) {
        Map creado = service.crear(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map actualizado = service.actualizar(id, body);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
