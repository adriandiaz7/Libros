package com.aluracursos.libros.service;

import com.aluracursos.libros.model.Autor;
import com.aluracursos.libros.repository.AutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AutorService {

    private final AutorRepository autorRepo;

    public AutorService(AutorRepository autorRepo) {
        this.autorRepo = autorRepo;
    }

    /* Guarda y devuelve el autor; si ya existe, lo reutiliza */
    public Autor guardarSiNoExiste(String nombre,
                                   Integer nacimiento,
                                   Integer fallecimiento) {

        return autorRepo.findByNombreContainingIgnoreCase(nombre)
                        .stream()
                        .filter(a -> a.getNombre().equalsIgnoreCase(nombre))
                        .findFirst()
                        .orElseGet(() -> {
                            Autor nuevo = new Autor();
                            nuevo.setNombre(nombre);
                            nuevo.setFechaNacimiento(nacimiento);
                            nuevo.setFechaFallecimiento(fallecimiento);
                            return autorRepo.save(nuevo);
                        });
    }

    public List<Autor> listarTodos() {
        return autorRepo.findAll();
    }

    public List<Autor> listarVivosEn(int anio) {
        return autorRepo.findAutoresVivosEn(anio);
    }
}