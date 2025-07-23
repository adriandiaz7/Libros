package com.aluracursos.libros.repository;

import com.aluracursos.libros.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    Long countByIdioma(String idioma);
}