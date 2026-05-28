package com.biblioteca.service;

import com.biblioteca.model.Libro;
import com.biblioteca.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LibroService {
    @Autowired
    private LibroRepository libroRepository;

    public List<Libro> listarTodos() {
        return libroRepository.findAll();
    }

    public Optional<Libro> obtenerPorId(Long id) {
        return libroRepository.findById(id);
    }

    public Libro guardar(Libro libro) {
        return libroRepository.save(libro);
    }

    public Libro actualizar(Long id, Libro libroActualizado) {
        Optional<Libro> libroExistente = libroRepository.findById(id);
        if (libroExistente.isPresent()) {
            Libro libro = libroExistente.get();
            if (libroActualizado.getTitulo() != null) {
                libro.setTitulo(libroActualizado.getTitulo());
            }
            if (libroActualizado.getAutor() != null) {
                libro.setAutor(libroActualizado.getAutor());
            }
            if (libroActualizado.getIsbn() != null) {
                libro.setIsbn(libroActualizado.getIsbn());
            }
            if (libroActualizado.getAnioPublicacion() != null) {
                libro.setAnioPublicacion(libroActualizado.getAnioPublicacion());
            }
            if (libroActualizado.getDescripcion() != null) {
                libro.setDescripcion(libroActualizado.getDescripcion());
            }
            if (libroActualizado.getPrecio() != null) {
                libro.setPrecio(libroActualizado.getPrecio());
            }
            if (libroActualizado.getCantidad() != null) {
                libro.setCantidad(libroActualizado.getCantidad());
            }
            return libroRepository.save(libro);
        }
        return null;
    }

    public void eliminar(Long id) {
        libroRepository.deleteById(id);
    }

    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepository.findByTituloContainingIgnoreCase(titulo);
    }

    public List<Libro> buscarPorAutor(String autor) {
        return libroRepository.findByAutorContainingIgnoreCase(autor);
    }

    public List<Libro> buscarPorAnioPublicacion(Integer anio) {
        return libroRepository.findByAnioPublicacion(anio);
    }

    public List<Libro> buscarPorRangoPrecio(Double min, Double max) {
        return libroRepository.findByPrecioBetween(min, max);
    }

    public List<Libro> buscarPorIsbn(String isbn) {
        return libroRepository.findByIsbn(isbn);
    }

    public List<Libro> buscarPorRangoAnios(Integer minAnio, Integer maxAnio) {
        return libroRepository.findByAnioPublicacionBetween(minAnio, maxAnio);
    }

    public List<Libro> busquedaAvanzada(String titulo, String autor, String isbn, 
                                         Integer minAnio, Integer maxAnio, 
                                         Double minPrecio, Double maxPrecio) {
        List<Libro> resultados = libroRepository.findAll();
        
        if (titulo != null && !titulo.isEmpty()) {
            resultados.retainAll(libroRepository.findByTituloContainingIgnoreCase(titulo));
        }
        
        if (autor != null && !autor.isEmpty()) {
            resultados.retainAll(libroRepository.findByAutorContainingIgnoreCase(autor));
        }
        
        if (isbn != null && !isbn.isEmpty()) {
            resultados.retainAll(libroRepository.findByIsbn(isbn));
        }
        
        if (minAnio != null && maxAnio != null) {
            resultados.retainAll(libroRepository.findByAnioPublicacionBetween(minAnio, maxAnio));
        }
        
        if (minPrecio != null && maxPrecio != null) {
            resultados.retainAll(libroRepository.findByPrecioBetween(minPrecio, maxPrecio));
        }
        
        return resultados;
    }
}
