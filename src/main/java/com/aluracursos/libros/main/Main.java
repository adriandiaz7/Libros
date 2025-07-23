package com.aluracursos.libros.main;

import com.aluracursos.libros.model.Libro;
import com.aluracursos.libros.service.AutorService;
import com.aluracursos.libros.service.LibroService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.aluracursos.libros.service.ConsumoApi;
import com.aluracursos.libros.service.ConvierteDatos;
import com.aluracursos.libros.service.iConvierteDatos;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Consola principal del sistema.
 *
 * Usa:
 *  - LibroService  para operaciones de libros / estadísticas
 *  - AutorService  para consultas de autores
 *  - ConsumoApi  para llamar a la API Gutendex (solo el primer resultado)
 */
@Component
public class Main implements CommandLineRunner {

    private static final String URL_BASE = "https://gutendex.com/books/";

    private final LibroService libroService;
    private final AutorService autorService;
    private final ConsumoApi consumoApi = new ConsumoApi();
    private final ConvierteDatos conversor = new ConvierteDatos();
    private final Scanner scanner = new Scanner(System.in);

    public Main(LibroService libroService, AutorService autorService) {
        this.libroService = libroService;
        this.autorService = autorService;
    }

    @Override
    public void run(String... args) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== Catálogo de Libros ===");
            System.out.println("1. Buscar libro por título (y guardar)");
            System.out.println("2. Listar todos los libros");
            System.out.println("3. Listar libros por idioma");
            System.out.println("4. Listar todos los autores");
            System.out.println("5. Autores vivos en un año");
            System.out.println("6. Estadísticas");
            System.out.println("0. Salir");
            System.out.print("> ");

            switch (scanner.nextLine().trim()) {
                case "1" -> buscarLibro();
                case "2" -> listarLibros();
                case "3" -> listarLibrosPorIdioma();
                case "4" -> listarAutores();
                case "5" -> autoresVivosEnAnio();
                case "6" -> menuEstadisticas();
                case "0" -> salir = true;
                default -> System.out.println("Opción no válida.");
            }
        }
        System.out.println("¡Hasta luego!");
    }

    /* ---------- Opciones del menú ---------- */

    private void buscarLibro() {
        System.out.print("\nTítulo a buscar: ");
        String titulo = scanner.nextLine().trim();
        if (titulo.isBlank()) return;

        // Llamada a Gutendex usando ConsumoApi
        String url = URL_BASE + "?search=" + titulo.replace(" ", "+");
        String json = consumoApi.obtenerDatos(url);

        // Convertir el JSON a un mapa
        Map<String, Object> respuesta = conversor.obtenerDatos(json, Map.class);

        List<Map<String, Object>> resultados = (List<Map<String, Object>>) respuesta.get("results");
        if (resultados == null || resultados.isEmpty()) {
            System.out.println("No se encontró el libro.");
            return;
        }

        Map<String, Object> primero = resultados.get(0);

        // Extraer datos relevantes (primer autor y primer idioma)
        String tituloApi = (String) primero.get("title");
        Double descargas = ((Number) primero.get("download_count")).doubleValue();

        List<Map<String, Object>> autoresApi = (List<Map<String, Object>>) primero.get("authors");
        String nombreAutor = autoresApi == null || autoresApi.isEmpty() ? "Autor desconocido"
                : (String) autoresApi.get(0).get("name");
        Integer nacimiento = (autoresApi == null || autoresApi.isEmpty()) ? null
                : (Integer) autoresApi.get(0).get("birth_year");
        Integer fallecimiento = (autoresApi == null || autoresApi.isEmpty()) ? null
                : (Integer) autoresApi.get(0).get("death_year");

        List<String> idiomasApi = (List<String>) primero.get("languages");
        String idioma = (idiomasApi == null || idiomasApi.isEmpty()) ? "desconocido" : idiomasApi.get(0);

        Libro guardado = libroService.crearLibro(
                tituloApi,
                idioma,
                descargas,
                nombreAutor,
                nacimiento,
                fallecimiento);

        System.out.println("\nLibro guardado:");
        System.out.println(guardado);
    }

    private void listarLibros() {
        System.out.println("\n=== Libros en BD ===");
        libroService.listarTodos()
                .forEach(System.out::println);
    }

    private void listarLibrosPorIdioma() {
        System.out.print("\nIdioma a filtrar (ej. 'en', 'es'): ");
        String idioma = scanner.nextLine().trim();
        if (idioma.isBlank()) return;

        List<Libro> filtrados = libroService.listarTodos()
                                           .stream()
                                           .filter(l -> idioma.equalsIgnoreCase(l.getIdioma()))
                                           .toList();

        if (filtrados.isEmpty()) {
            System.out.println("Sin resultados.");
        } else {
            filtrados.forEach(System.out::println);
        }
    }

    private void listarAutores() {
        System.out.println("\n=== Autores ===");
        autorService.listarTodos()
                    .forEach(System.out::println);
    }

    private void autoresVivosEnAnio() {
        System.out.print("\nAño a consultar: ");
        try {
            int anio = Integer.parseInt(scanner.nextLine());
            List<?> vivos = autorService.listarVivosEn(anio);
            if (vivos.isEmpty()) {
                System.out.println("Ningún autor vivo en " + anio);
            } else {
                vivos.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("Año inválido.");
        }
    }

    /* ---------- Submenú de estadísticas ---------- */

    private void menuEstadisticas() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- Estadísticas ---");
            System.out.println("1. Cantidad de libros por idioma");
            System.out.println("2. Idiomas más comunes");
            System.out.println("0. Volver");
            System.out.print("> ");
            switch (scanner.nextLine().trim()) {
                case "1" -> mostrarEstadisticasIdiomas();
                case "2" -> mostrarIdiomasMasComunes();
                case "0" -> volver = true;
                default  -> System.out.println("Opción no válida.");
            }
        }
    }

    private void mostrarEstadisticasIdiomas() {
        List<String> idiomas = libroService.listarIdiomasDisponibles();
        if (idiomas.isEmpty()) {
            System.out.println("No hay libros.");
            return;
        }
        Map<String, Long> conteo = libroService.contarLibrosPorIdiomas(idiomas);
        System.out.println("\nLibros por idioma:");
        conteo.forEach((k, v) ->
                System.out.printf(" - %s: %d libro%s%n", k, v, v == 1 ? "" : "s"));
    }

    private void mostrarIdiomasMasComunes() {
        System.out.print("¿Cuántos idiomas mostrar? (por defecto 5): ");
        int limite = 5;
        String in = scanner.nextLine().trim();
        if (!in.isBlank()) {
            try { limite = Integer.parseInt(in); } catch (NumberFormatException ignored) {}
        }
        Map<String, Long> top = libroService.obtenerIdiomasMasComunes(limite);
        System.out.println("\nIdiomas más comunes:");
        int pos = 1;
        for (var e : top.entrySet()) {
            System.out.printf("%d. %s - %d libro%s%n",
                    pos++, e.getKey(), e.getValue(), e.getValue() == 1 ? "" : "s");
        }
    }
}