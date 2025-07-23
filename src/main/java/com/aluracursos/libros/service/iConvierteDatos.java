package com.aluracursos.libros.service;

public interface iConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}
