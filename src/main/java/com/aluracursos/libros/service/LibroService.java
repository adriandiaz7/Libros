package com.aluracursos.libros.service;

import com.aluracursos.libros.model.Autor;
import com.aluracursos.libros.model.Libro;
import com.aluracursos.libros.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LibroService {

    private final LibroRepository libroRepo;
    private final AutorService autorService;

    public LibroService(LibroRepository libroRepo, AutorService autorService) {
        this.libroRepo = libroRepo;
        this.autorService = autorService;
    }

    /* ---------- Operaciones CRUD básicas ---------- */

    public Libro guardar(Libro libro) {
        return libroRepo.save(libro);
    }

    public Optional<Libro> buscarPorId(Long id) {
        return libroRepo.findById(id);
    }

    public List<Libro> listarTodos() {
        return libroRepo.findAll();
    }

    public void eliminarPorId(Long id) {
        libroRepo.deleteById(id);
    }

    /* ---------- Funciones de negocio ---------- */

    /**
     * Crea un Libro en base a datos crudos (por ejemplo, de la API)  
     * Si el autor ya existe se reutiliza, si no se crea uno nuevo.
     */
    public Libro crearLibro(String titulo,
                            String idioma,
                            Double descargas,
                            String nombreAutor,
                            Integer nacimientoAutor,
                            Integer fallecimientoAutor) {

        // Reutilizar o crear autor
        Autor autor = autorService.guardarSiNoExiste(
                nombreAutor,
                nacimientoAutor,
                fallecimientoAutor);

        // ¿Ya existe un libro con ese título?  Evitar duplicados
        Optional<Libro> existente = libroRepo.findAll()
                .stream()
                .filter(l -> l.getTitulo().equalsIgnoreCase(titulo))
                .findFirst();

        if (existente.isPresent()) {
            return existente.get(); // devolver el existente
        }

        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setIdioma(idioma);
        libro.setNumeroDescargas(descargas);
        libro.setAutor(autor);
        return libroRepo.save(libro);
    }

    /* ---------- Estadísticas ---------- */

    /** Devuelve la cantidad de libros por cada idioma solicitado. */
    public Map<String, Long> contarLibrosPorIdiomas(Collection<String> idiomas) {
        return idiomas.stream()
                .collect(Collectors.toMap(
                        i -> i,
                        libroRepo::countByIdioma));
    }

    /**
     * Devuelve un mapa ordenado (desc) con los idiomas más comunes,
     * limitado a 'limite' entradas.
     */
    public Map<String, Long> obtenerIdiomasMasComunes(int limite) {
        return libroRepo.findAll().stream()
                .collect(Collectors.groupingBy(Libro::getIdioma, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limite)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    /** Devuelve la lista de idiomas distintos registrados en la base de datos. */
    public List<String> listarIdiomasDisponibles() {
        return libroRepo.findAll().stream()
                .map(Libro::getIdioma)
                .distinct()
                .toList();
    }
}