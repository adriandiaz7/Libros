package com.aluracursos.libros.dto;

import java.util.List;

public record LibroDTO(
    Long id,
    String titulo,
    List<AutorDTO> autores,
    List<String> idiomas,
    Double numeroDescargas
) {
}
