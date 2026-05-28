package com.biblioteca.controller;

import com.biblioteca.model.Libro;
import com.biblioteca.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/libros")
@CrossOrigin(origins = "*")
public class LibroController {
    @Autowired
    private LibroService libroService;

    @GetMapping
    public ResponseEntity<List<Libro>> listar() {
        return ResponseEntity.ok(libroService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerPorId(@PathVariable Long id) {
        Optional<Libro> libro = libroService.obtenerPorId(id);
        return libro.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@RequestBody Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().isEmpty() ||
            libro.getAutor() == null || libro.getAutor().isEmpty() ||
            libro.getIsbn() == null || libro.getIsbn().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(libroService.guardar(libro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Libro> actualizar(@PathVariable Long id, @RequestBody Libro libro) {
        Libro libroActualizado = libroService.actualizar(id, libro);
        if (libroActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(libroActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (libroService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar/titulo")
    public ResponseEntity<List<Libro>> buscarPorTitulo(@RequestParam String titulo) {
        return ResponseEntity.ok(libroService.buscarPorTitulo(titulo));
    }

    @GetMapping("/buscar/autor")
    public ResponseEntity<List<Libro>> buscarPorAutor(@RequestParam String autor) {
        return ResponseEntity.ok(libroService.buscarPorAutor(autor));
    }

    @GetMapping("/buscar/anio")
    public ResponseEntity<List<Libro>> buscarPorAnioPublicacion(@RequestParam Integer anio) {
        return ResponseEntity.ok(libroService.buscarPorAnioPublicacion(anio));
    }

    @GetMapping("/buscar/precio")
    public ResponseEntity<List<Libro>> buscarPorRangoPrecio(@RequestParam Double min, @RequestParam Double max) {
        return ResponseEntity.ok(libroService.buscarPorRangoPrecio(min, max));
    }

    @GetMapping("/buscar/isbn")
    public ResponseEntity<List<Libro>> buscarPorIsbn(@RequestParam String isbn) {
        return ResponseEntity.ok(libroService.buscarPorIsbn(isbn));
    }

    @GetMapping("/buscar/anios")
    public ResponseEntity<List<Libro>> buscarPorRangoAnios(@RequestParam Integer minAnio, @RequestParam Integer maxAnio) {
        return ResponseEntity.ok(libroService.buscarPorRangoAnios(minAnio, maxAnio));
    }

    @GetMapping("/buscar/avanzada")
    public ResponseEntity<List<Libro>> busquedaAvanzada(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer minAnio,
            @RequestParam(required = false) Integer maxAnio,
            @RequestParam(required = false) Double minPrecio,
            @RequestParam(required = false) Double maxPrecio) {
        return ResponseEntity.ok(libroService.busquedaAvanzada(titulo, autor, isbn, minAnio, maxAnio, minPrecio, maxPrecio));
    }
}
