package com.example.demo;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstudianteService {
    private final EstudianteRepository repository;

    public EstudianteService(EstudianteRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "estudiantes", key = "'all'")
    List<Estudiante> buscarEstudiante(){
        return repository.findAll();
    }

    @Cacheable(value = "estudiantes", key = "'deleted'")
    List<Estudiante> buscarEstudiantesBorrados(){
        return repository.findByDeleted(true);
    }

    @Cacheable(value = "estudiantes", key = "'noDeleted'")
    List<Estudiante> buscarEstudiantesNoBorrados(){
        return repository.findByDeleted(false);
    }

    @CachePut(value = "estudiantes", key = "#result.id")
    Estudiante guardarEstudiante(Estudiante estudiante){
        repository.save(estudiante);
        return estudiante;
    }

    @CacheEvict(value = "estudiantes", key = "#id")
    void eliminarEstudiante(Long id){
        repository.deleteById(id);
    }

}
