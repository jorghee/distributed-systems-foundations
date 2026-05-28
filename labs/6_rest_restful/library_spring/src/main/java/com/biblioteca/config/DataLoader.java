package com.biblioteca.config;

import com.biblioteca.model.Libro;
import com.biblioteca.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader implements CommandLineRunner {
    @Autowired
    private LibroRepository libroRepository;

    @Override
    public void run(String... args) throws Exception {
        // Agregar libros de ejemplo
        libroRepository.save(new Libro(null, "El Quijote", "Miguel de Cervantes", 
            "978-8424142964", 1605, "Una obra maestra de la literatura universal", 34.99, 5));

        libroRepository.save(new Libro(null, "Cien años de soledad", "Gabriel García Márquez", 
            "978-8448011239", 1967, "Novela de realismo mágico", 28.50, 3));

        libroRepository.save(new Libro(null, "1984", "George Orwell", 
            "978-0451524935", 1949, "Distopía totalitaria", 15.99, 8));

        libroRepository.save(new Libro(null, "El Principito", "Antoine de Saint-Exupéry", 
            "978-8467934335", 1943, "Novela infantil clásica", 12.99, 12));

        libroRepository.save(new Libro(null, "Don Juan Tenorio", "José Zorrilla", 
            "978-8427202726", 1844, "Drama romántico español", 22.00, 2));

        libroRepository.save(new Libro(null, "Orgullo y Prejuicio", "Jane Austen", 
            "978-0141439518", 1813, "Novela romántica clásica", 16.50, 6));

        libroRepository.save(new Libro(null, "Frankenstein", "Mary Shelley", 
            "978-0486282114", 1818, "Novela de ciencia ficción gótica", 14.99, 4));

        libroRepository.save(new Libro(null, "La Metamorfosis", "Franz Kafka", 
            "978-8446009627", 1915, "Relato de transformación", 11.50, 7));

        System.out.println("Base de datos inicializada con libros de ejemplo");
    }
}
