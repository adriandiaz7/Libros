package com.aluracursos.libros.repository;

import com.aluracursos.libros.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    // ¿Existe un autor con ese nombre?
    boolean existsByNombreIgnoreCase(String nombre);

    // Búsqueda por nombre (opcional, si lo necesitas)
    List<Autor> findByNombreContainingIgnoreCase(String nombre);

    // Autores que estaban vivos en un año dado
    @Query("""
           SELECT a FROM Autor a
           WHERE (a.fechaNacimiento IS NULL OR a.fechaNacimiento <= :ano)
             AND (a.fechaFallecimiento IS NULL OR a.fechaFallecimiento >= :ano)
           """)
    List<Autor> findAutoresVivosEn(int ano);
}