package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByTituloContainingIgnoreCase(String titulo);
    List<Libro> findByAutorContainingIgnoreCase(String autor);
    List<Libro> findByAnioPublicacion(Integer anio);
    List<Libro> findByAnioPublicacionBetween(Integer minAnio, Integer maxAnio);
    List<Libro> findByPrecioBetween(Double min, Double max);
    List<Libro> findByIsbn(String isbn);
}
